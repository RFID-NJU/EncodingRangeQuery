/**
 * Created by Liuhaisong, Yuxi.
 * Last update: 2022/01/03.
 * Version: 3.1
 *
 * Useful parameters and methods.
 */

public class Utils {
    // IP and port number of the reader
    public static final String ReaderIP = "192.168.1.102";
    public static final int ReaderPort = 23;

    // The specific prefix hex string of the IDs of tags for current experiments
    // (which can be used to mask the other tags in surroundings)
    public static final String tagID_prefix_str = "20 22 01 03";
    // The length of the prefix binary string
    public static final int tagID_prefix_len = 32;

    // Tags writing parameters
    public static final int write_batch_tags_num = 5;//the number of tags to be written in a batch

    // Test data parameters
    public static final String test_data_filepath = ".\\Data\\TestData\\";
    public static final int test_data_num = 100;

    /***
     * Convert a binary string to a grouped hex string.
     * e.g. 0010111100101111
     * the grouped hex string is 2f 2f
     *
     * @param bin_str: binary string
     * @return the grouped hex string
     */
    public static String binStringToHexString(String bin_str) {
        if (bin_str == "") return "";

        // Add '0' in the end of the string
        while (bin_str.length() % 8 != 0) bin_str += "0";

        // Convert to hex string
        String tmp = "";
        for (int i = 0; i < bin_str.length(); i += 4) {
            tmp += Long.toHexString(Long.valueOf(bin_str.substring(i, i + 4), 2));
        }

        // Group the hex string
        String hexstr = tmp.substring(0, 2);
        for (int i = 2; i <= tmp.length() - 2; i += 2) {
            hexstr += " " + tmp.substring(i, i + 2);
        }
        return hexstr;
    }

    /***
     * Convert a long integer to a binary string with given length.
     * If the length of binary string is less than binaryLength, we will add enough '0'.
     * e.g. value=15, binaryLength=8
     * -> the 8-bits binary string is 00001111
     *
     * @param num: target number
     * @param binaryLength: the given length
     * @return a binaryLength-bits binary string
     */
    public static String getBinaryStr(Long num, int binaryLength) {
        String binarystr = Long.toBinaryString(num);
        for (int i = binarystr.length(); i < binaryLength; i++) {
            binarystr = "0" + binarystr;
        }
        return binarystr;
    }

    /***
     * Convert a long integer to a special binary string with given length as describe in paper
     * The cardinal number used in experiments is 16
     * If the length of hex string is less than hexLength, we will add enough '0'.
     * e.g. hexLength=2
     * 1  -> 01 -> 000000000000000 000000000000001 00
     *
     * @param num: target number
     * @param encodeStringLength: the length of the encoding string
     * @return: a encoding binary string (e.g. "00000000000000000000000000000100" in above case)
     */
    public static String getSpecialID16BinaryString(long num, int encodeStringLength) {
        String hexStr = Long.toHexString(num);

        for (int i = hexStr.length(); i < encodeStringLength; i++) hexStr = "0" + hexStr;

        int index = hexStr.length() - 1;
        String specialIDBinStr = "";
        while (index >= 0) {
            // The length of data hold in tags should be 8x bits.
            // But each number will be convert to a 15-bits string when the cardinal number is 16
            // So we add another '0' in the end to make the length of the final string to be 16x bits
            specialIDBinStr = getSpecial16(hexStr.charAt(index)) + specialIDBinStr + "0";

            index--;
        }
        return specialIDBinStr;
    }

    /***
     * Convert a long integer to a special grouped hex string with given length as describe in paper
     * e.g. hexLength=2
     * 1  -> 01 -> 000000000000000 000000000000001 00 -> 00 00 00 04
     *
     * @param num: target number
     * @param encodeStringLength: the length of the encoding string
     * @return a encoding grouped hex string (e.g. "00 00 00 04" in above case)
     */
    public static String getSpecialID16HexString(long num, int encodeStringLength) {
        String specialIDBinStr = getSpecialID16BinaryString(num, encodeStringLength);

        // Convert to hex string
        String specialIDHexStr = "";
        for (int i = 0; i < specialIDBinStr.length(); i += 4) {
            specialIDHexStr += Integer.toHexString(Integer.valueOf(specialIDBinStr.substring(i, i + 4), 2));
        }

        // Group the hex string
        String grouped_specialID16 = "";
        for (int i = 0; i < specialIDHexStr.length() - 2; i += 2) {
            grouped_specialID16 += specialIDHexStr.substring(i, i + 2) + " ";
        }
        grouped_specialID16 += specialIDHexStr.substring(specialIDHexStr.length() - 2);

        return grouped_specialID16;
    }

    /***
     * Convert a char to a special string as describe in paper
     * The cardinal number used in experiments is 16
     * e.g.
     * 0  -> 000000000000000
     * 1  -> 000000000000001
     * 2  -> 000000000000010
     * 3  -> 000000000000100
     * 4  -> 000000000001000
     * 5  -> 000000000010000
     * 6  -> 000000000100000
     * 7  -> 000000001000000
     * 8  -> 000000010000000
     * 9  -> 000000100000000
     * a  -> 000001000000000
     * b  -> 000010000000000
     * c  -> 000100000000000
     * d  -> 001000000000000
     * e  -> 010000000000000
     * f  -> 100000000000000
     *
     * @param ch: target char
     * @return a encoding string
     */
    private static String getSpecial16(char ch) {
        StringBuilder str = new StringBuilder("00000" + "00000" + "00000");
        switch (ch) {
            case '1': str.setCharAt(14, '1');break;
            case '2': str.setCharAt(13, '1');break;
            case '3': str.setCharAt(12, '1');break;
            case '4': str.setCharAt(11, '1');break;
            case '5': str.setCharAt(10, '1');break;
            case '6': str.setCharAt(9, '1');break;
            case '7': str.setCharAt(8, '1');break;
            case '8': str.setCharAt(7, '1');break;
            case '9': str.setCharAt(6, '1');break;
            case 'a': str.setCharAt(5, '1');break;
            case 'b': str.setCharAt(4, '1');break;
            case 'c': str.setCharAt(3, '1');break;
            case 'd': str.setCharAt(2, '1');break;
            case 'e': str.setCharAt(1, '1');break;
            case 'f': str.setCharAt(0, '1');break;
            default:
        }
        return str.toString();
    }

}