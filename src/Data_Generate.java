import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by Yuxi.
 * Last update: 2021/12/27.
 * Version: 2.0
 *
 * Generate the test data and save the data to files.
 */

public class Data_Generate {
    private static Random rand = new Random();

    /***
     * Get the test data when the length of data is 16, 32, 48, respectively.
     * i.e., generate the test data randomly in [1024, 2^16-1], [2^16-1, 2^32-1], [2^32-1, 2^48-1] and save the data to files.
     *
     * The average number of masks for RQ/EnRQ in theory is:
     *          RQ      EnRQ    RQ-EnRQ
     * 16-bits: 8.00;   3.75;   53.125%
     * 32-bits: 16.00;  7.50;   53.125%
     */
    public static void main(String args[]) {
        // Get test data in [1024, 2^16-1]
        long low = 1024;
        long high = Long.parseUnsignedLong("ffff", 16);
        generateInRange(low, high, Utils.test_data_filepath + "test_EnRQ_16data.csv", 16);

        // Get test data in [2^16-1, 2^32-1]
        low = Long.parseUnsignedLong("ffff", 16);
        high = Long.parseUnsignedLong("ffff" + "ffff", 16);
        generateInRange(low, high, Utils.test_data_filepath + "test_EnRQ_32data.csv", 32);

        // Get test data in [2^32-1, 2^48-1]
        low = Long.parseUnsignedLong("ffff" + "ffff", 16);
        high = Long.parseUnsignedLong("ffff" + "ffff" + "ffff", 16);
        generateInRange(low, high, Utils.test_data_filepath + "test_EnRQ_48data.csv", 48);
    }

    /***
     * Get the test data with the given range and save them to the file.
     * The file will be formatted as:
     *      num1, binary string of num1, hex string of num1, the number of masks of num1 in RQ, the number of masks of num1 in EnRQ,
     *      num2, binary string of num2, hex string of num2, the number of masks of num2 in RQ, the number of masks of num2 in EnRQ,
     *      ......
     *
     * @param low: the lower boundary of the range
     * @param high: the upper boundary of the range
     * @param filepath: the path of file to save data
     * @param bin_len: the length of data
     */
    private static void generateInRange(long low, long high, String filepath, int bin_len) {
        try {
            Writer wrecord = new FileWriter(new File(filepath), false);
            HashSet<Long> numset = new HashSet<>();
            long masks_sum_RQ = 0;
            long masks_sum_EnRQ = 0;

            // Get the Utils.test_data_num s test data in range [low, high], and get the corresponding masks for RQ/ERQ/EnRQ in theory.
            for (int i = 0; i < Utils.test_data_num; i++) {
                long tmpdata = getRandomLongInRange(low, high);
                while (numset.contains(tmpdata)) {
                    tmpdata = getRandomLongInRange(low, high);
                }
                numset.add(tmpdata);
                int masks_RQ = getMaskNum_RQ(tmpdata);
                int masks_EnRQ = getMaskNum_EncodingRQ16(tmpdata);
                masks_sum_RQ += masks_RQ;
                masks_sum_EnRQ += masks_EnRQ;
                wrecord.write(tmpdata + "," + Utils.getBinaryStr(tmpdata, bin_len) + "," + Long.toHexString(tmpdata) + ","
                        + masks_RQ + "," + masks_EnRQ + "\n");
            }

            // Get the average number of masks of the test data
            System.out.println("MaskNum Average:");
            double ave_masks_RQ = (double) masks_sum_RQ / (double) Utils.test_data_num;
            double ave_masks_EnRQ = (double) masks_sum_EnRQ / (double) Utils.test_data_num;
            System.out.println("RQ : " + ave_masks_RQ);
            System.out.println("EnRQ : " + ave_masks_EnRQ);
            System.out.println("Improved :");
            System.out.println("EnRQ : " + ((ave_masks_RQ - ave_masks_EnRQ) / ave_masks_RQ));
            System.out.println();

            wrecord.flush();
            wrecord.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * Generate a long integer randomly with the given range.
     * @param low: the lower boundary of the range
     * @param high: the upper boundary of the range
     * @return a long integer
     */
    private static long getRandomLongInRange(long low, long high) {
        long tmpdata = rand.nextLong();

        // Make tmpdata >0
        if (tmpdata < 0) tmpdata = -tmpdata;

        // Make tmpdata < high
        tmpdata %= high;

        // Make tmpdata > low
        if (tmpdata < low) tmpdata += low;

        return tmpdata;
    }

    /***
     * Get the number of masks of a given data/boundary for RQ in theory
     * @param data: the target number/boundary
     * @return the number of masks of data for RQ in theory
     */
    private static int getMaskNum_RQ(long data) {
        String binstr = Long.toBinaryString(data);
        int masks_num = 0;

        // Count the number of '1's
        for (int i = 0; i < binstr.length(); i++) {
            if (binstr.charAt(i) == '1') masks_num++;
        }

        // Minus the number of consecutive rightmost '1's
        for (int i = binstr.length() - 1; i >= 0 && binstr.charAt(i) == '1'; i--) {
            masks_num--;
        }

        // Add the last mask
        masks_num += 1;

        return masks_num;
    }

    /***
     * Get the number of masks of a given data/boundary for EnRQ in theory
     * The default cardinal number is 16
     * @param data: the target number/boundary
     * @return the number of masks of data for EnRQ in theory
     */
    private static int getMaskNum_EncodingRQ16(long data) {
        String hexstr = Long.toHexString(data);
        int last_F = 0;

        // Get the number of the consecutive rightmost 'f's
        for (int i = hexstr.length() - 1; i >= 0 && hexstr.charAt(i) == 'f'; i--) {
            last_F++;
        }

        // XFF...FFF
        if (data == 0 || last_F >= hexstr.length() - 1) return 1;

        int res = 0;
        for (int i = 0; i < hexstr.length(); i++) {
            if (hexstr.charAt(i) != '0') res++;
        }

        // Minus the number of the consecutive rightmost 'f's
        res -= last_F;

        // X...Y0FF...F or X...Y0
        if ((last_F > 0 && hexstr.charAt(hexstr.length() - last_F - 1) == '0')
                || (last_F == 0 && hexstr.charAt(hexstr.length() - 1) == '0')) {
            res++;
        }

        return res;
    }

    /***
     * Get the average number of masks for RQ/ERQ/EnRQ in theory
     */
    private static void RQ_ERQ_EnRQ_Compare_Theoretical() {
        double masks_sum_RQ = 0;
        double masks_sum_EnRQ = 0;

        // Get the average number of masks in [1, 2^16-1] for RQ/ERQ/EnRQ in theory
        long bound1 = Long.parseUnsignedLong("ffff", 16);

        for (long i = 1; i <= bound1; i++) {
            masks_sum_RQ += getMaskNum_RQ(i);
            masks_sum_EnRQ += getMaskNum_EncodingRQ16(i);
        }
        System.out.println("16 bits:");
        System.out.println("RQ Ave: " + (masks_sum_RQ / (double) bound1));
        System.out.println("EnRQ Ave: " + (masks_sum_EnRQ / (double) bound1));
        System.out.println();

        // Get the average number of masks in [1, 2^32-1] for RQ/ERQ/EnRQ in theory
        long bound2 = Long.parseUnsignedLong("ffff" + "ffff", 16);
        for (long i = bound1 + 1; i <= bound2; i++) {
            masks_sum_RQ += getMaskNum_RQ(i);
            masks_sum_EnRQ += getMaskNum_EncodingRQ16(i);
        }
        System.out.println("32 bits:");
        System.out.println("RQ Ave: " + (masks_sum_RQ / (double) bound2));
        System.out.println("EnRQ Ave: " + (masks_sum_EnRQ / (double) bound2));
        System.out.println();

        // Get the average number of masks in [1, 2^48-1] for RQ/ERQ/EnRQ in theory
        long bound3 = Long.parseUnsignedLong("ffff" + "ffff" + "ffff", 16);
        for (long i = bound2 + 1; i <= bound3; i++) {
            masks_sum_RQ += getMaskNum_RQ(i);
            masks_sum_EnRQ += getMaskNum_EncodingRQ16(i);
        }
        System.out.println("48 bits:");
        System.out.println("RQ Ave: " + (masks_sum_RQ / (double) bound3));
        System.out.println("EnRQ Ave: " + (masks_sum_EnRQ / (double) bound3));
        System.out.println();
    }
}