import com.alien.enterpriseRFID.reader.AlienClass1Reader;
import com.alien.enterpriseRFID.reader.AlienReaderException;
import com.alien.enterpriseRFID.tags.Tag;

/***
 * Created by Liuhaisong, Yuxi
 * Last update: 2021/12/27
 * Version: 3.0
 *
 * Write "abnormal" user data to tags.
 */

public class WriteAbnormalTags {
    private static String max_abnormal_data =
            "FF FF FF FF FF FF FF FF"
                    + " " + "FF FF FF FF FF FF FF FF"
                    + " " + Utils.getSpecialID16HexString(Long.parseUnsignedLong("ffff", 16), 4)
                    + " " + Utils.getSpecialID16HexString(Long.parseUnsignedLong("ffff" + "ffff", 16), 8)
                    + " " + Utils.getSpecialID16HexString(Long.parseUnsignedLong("ffff" + "ffff" + "ffff", 16), 12);

    /***
     * Write "abnormal" user data to tags.
     */
    public static void main(String[] args) {
        try {
            AlienClass1Reader reader = new AlienClass1Reader();
            reader.setConnection(Utils.ReaderIP, Utils.ReaderPort);

            // Open a connection to the reader
            reader.open();
            // Reset
            reader.setFactorySettings();
            // Set the grouping of bytes when formatting tag data
            reader.doReaderCommand("TagDataFormatGroupSize = 1");
            // Set the reader's RFAttenuation values for a specific antenna, with different values for reading and writing operations
            reader.setRFAttenuations(0, 70, 0);
            // Require USER (Membank-3)
            reader.setAcquireG2TagData(3, 0, 32);

            // Get tag list and write user data to each of them
            Tag[] tagList = reader.getTagList();
            if (tagList == null) {
                System.out.println("No Tags Found.");
            } else {
                System.out.println(tagList.length + " tags found.");

                int success = 0;
                for (Tag tag : tagList) {
                    String TID = tag.getTagID();
                    if (TID == null || TID.length() == 0)
                        continue;
                    System.out.println("TID: " + TID + " -> User: " + max_abnormal_data);

                    // Write user data
                    try {
                        // Mask the current tag using TID
                        // Because the default format of TID is like 'XX XX XX XX', the binary length of TID is (TID.length() + 1) / 3 * 8
                        reader.setAcqG2Mask(1, 32, (TID.length() + 1) / 3 * 8, TID);
                        reader.setAcqG2MaskAction("Include");
                        reader.setRFAttenuation(0);

                        // Program user data
                        reader.programUser(max_abnormal_data);
                        // Program the ID of the tag with specific prefix(Utils.tagID_prefix_str)
                        reader.programEPC(Utils.tagID_prefix_str + " " + TID);

                        reader.doReaderCommand("to");
                        success++;
                        System.out.println(" Success.");
                    } catch (AlienReaderException ex) {
                        System.out.println(" " + ex.getMessage());
                    }
                }

                System.out.printf("%d of %d tags are successfully programmed.\n\n", success, tagList.length);

                // Print the program result
                System.out.println("Program result:");
                reader.setAcqG2Mask(1, 32, Utils.tagID_prefix_len, Utils.tagID_prefix_str);
                reader.setAcqG2MaskAction("AB");
                reader.g2Wake();
                tagList = reader.getTagList();
                if (tagList != null) {
                    for (int i = 0; i < tagList.length; i++) {
                        System.out.println("Tag " + i + ": TID: " + tagList[i].getTagID() + " USER: " + tagList[i].getG2Data(0));
                    }
                }
            }
            // Restore and close
            reader.setFactorySettings();
            reader.close();
        } catch (AlienReaderException ex) {
            System.out.println("Error: " + ex.toString());
        }
    }
}
