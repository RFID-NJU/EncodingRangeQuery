import com.alien.enterpriseRFID.reader.AlienClass1Reader;
import com.alien.enterpriseRFID.reader.AlienReaderException;
import com.alien.enterpriseRFID.tags.Tag;

import java.util.Random;

/***
 * Created by Chenxingyu, Yuxi.
 * Last update: 2022/01/03
 * Version: 3.1
 *
 * Write "normal" user data to tags.
 *
 * We will program the data of MemBank-3 (USER data).
 * The data will be divided into three segments: data_a + data_b + data_c.
 * data_a (0~63 bits) :
 *      Data_a is reserved.
 * data_b (64~127 bits):
 *      Data_b is to get the time efficiency of RQ
 *      "Normal" user data are 16-bit integers ranging from 1 to Utils.tags_num.
 * data_c (128~end bits):
 *      Data_c is to get the time efficiency of EnRQ
 *      It is the special encoding string as describe in paper
 *      Data_c will be divided into three segments: data_1 + data_2 + data_3.
 *          data_1 is used when the test_parameter-user_data_length is 16
 *          data_2 is used when the test_parameter-user_data_length is 32
 *          data_3 is used when the test_parameter-user_data_length is 48
 *          the only difference between the three data is the length of it, in other words, they represent the same data with different length
 */

public class WriteNormalTags {
    //cur_batch_num should be unique during writing
    //!!! We suggest to manually add 1 after each execution !!!
    public static int cur_batch_num = 16;

    // The length of data_a (0 ~ write_data_len_a-1 bits in MemBank-3)
    public static int write_data_len_a = 64;

    // The length of data_b (write_data_len_a ~ write_data_len_a+write_data_len_b-1 bits in MemBank-3)
    public static int write_data_len_b = 64;

    private static AlienClass1Reader reader;

    /***
     * Write "normal" user data to tags.
     */
    public static void main(String[] args) {
        try {
            reader = new AlienClass1Reader();
            reader.setConnection(Utils.ReaderIP, Utils.ReaderPort);

            // Open a connection to the reader
            reader.open();
            // Reset
            reader.setFactorySettings();
            // Set the grouping of bytes when formatting tag data
            reader.doReaderCommand("TagDataFormatGroupSize = 1");
            // Require USER (Membank-3)
            reader.setAcquireG2TagData(3, 0, 32);
            // Disable auto incrementing
            reader.setProgUserDataInc("OFF");
            reader.setProgUserDataIncCount(0);
            // Set the reader's RFAttenuation values for a specific antenna, with different values for reading and writing operations
            reader.setRFAttenuations(0, 50, 0);

            // Get tag list and write user data to each of them
            Tag[] tagList = reader.getTagList();
            // There are Utils.write_batch_tags_num tags to be written in each execution
            if (tagList == null || tagList.length != Utils.write_batch_tags_num) {
                System.out.printf("Not enough tags found (%d).\n", tagList.length);
            } else {
                int success = 0;
                for (int i = 0; i < tagList.length; i++) {
                    String TID = tagList[i].getTagID();
                    if (TID == null || TID.length() == 0) continue;
                    try {
                        // Write one tag
                        writeOneTag(TID, i);

                        success++;
                        System.out.println(" Success.");
                    } catch (Exception ex) {
                        System.out.println(" " + ex.getMessage());
                    }
                }

                System.out.printf("%d of %d tags are successfully programmed.\n\n", success, tagList.length);

                // Print the program result
                System.out.println("Program result:");
                printTagList();
            }

            // Restore and close
            reader.setFactorySettings();
            reader.close();
        } catch (Exception ex) {
            System.out.println("Error: " + ex.toString());
        }
    }

    /***
     * Write the user data and TID of a tag
     * @param TID: the TID of tag to be written
     * @param index: the index of tag in this writing (0 ~ Utils.write_batch_tags_num-1)
     */
    private static void writeOneTag(String TID, long index) throws AlienReaderException {
        // Get data_a
        String data1 = Utils.binStringToHexString(Utils.getBinaryStr(0l, write_data_len_a));
        // Get data_b
        String data2 = Utils.binStringToHexString(Utils.getBinaryStr(cur_batch_num * 5 + index, write_data_len_b));
        // Get data_c
        String data3 = Utils.getSpecialID16HexString(cur_batch_num * 5 + index, 4)
                + " " + Utils.getSpecialID16HexString(cur_batch_num * 5 + index, 8)
                + " " + Utils.getSpecialID16HexString(cur_batch_num * 5 + index, 12);

        // Get user data
        String data = data1 + " " + data2 + " " + data3;

        System.out.println("Write TID: " + TID);
        System.out.println(" -> EPC: " + Utils.tagID_prefix_str + " " + data2);
        System.out.println(" -> USER: " + data);

        // Some prior set before writing
        writeOpSet();

        // Mask the current tag using TID
        // Because the default format of TID is like 'XX XX XX XX', the binary length of TID is (TID.length() + 1) / 3 * 8
        reader.setAcqG2Mask(1, 32, (TID.length() + 1) / 3 * 8, TID);
        reader.setAcqG2MaskAction("Include");
        reader.setRFAttenuation(0);

        // Program user data
        reader.programUser(data);
        // Program the ID of the tag with specific prefix(Utils.tagID_prefix_str)
        reader.programEPC(Utils.tagID_prefix_str + " " + data2);

        reader.doReaderCommand("to");
    }

    /***
     * Some prior set before writing
     */
    private static void writeOpSet() throws AlienReaderException {
        reader.setProgAntenna(0);
        reader.setProgUserDataInc("OFF");
        reader.setProgUserDataIncCount(-1);
        reader.setProgEPCDataInc("OFF");
        reader.setProgEPCDataIncCount(-1);
        reader.setProgDataUnit("Block");
        reader.setProgBlockSize(0);
        reader.setProgAttempts(1);
        reader.doReaderCommand("ProgSuccessFormat=1");
        reader.setProgSingulate(0);
    }

    /***
     * Get the tagList and print it
     */
    private static void printTagList() throws AlienReaderException {
        reader.setAcqG2Mask(1, 32, Utils.tagID_prefix_len, Utils.tagID_prefix_str);
        reader.setAcqG2MaskAction("AB");
        reader.g2Wake();
        Tag[] tagList = reader.getTagList();
        if (tagList != null) {
            for (int i = 0; i < tagList.length; i++) {
                System.out.println("Tag " + i + ": TID: " + tagList[i].getTagID() + " USER: " + tagList[i].getG2Data(0));
            }
        }
    }
}
