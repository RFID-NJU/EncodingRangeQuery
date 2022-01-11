import com.alien.enterpriseRFID.reader.AlienClass1Reader;
import com.alien.enterpriseRFID.tags.Tag;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Yuxi.
 * Last update: 2021/12/27
 * Version: 1.0
 *
 * Run URQ of RQ protocol with the given test data array list to get the average time efficiency of RQ
 */

public class RQ {
    /***
     * Run URQ with the given test data list to get the average time efficiency of RQ
     * and repeat the experiment to get the average time
     * @param ts: the parameters of this experiment
     * @param testdata: the test data list
     * @return: a string list which is the time cost array of test data of URQ
     *          string i in the list is the time cost list of the test data i and is split by ',', the last time cost is the average time cost
     */
    public static String[] RQ_URQ(Test_Parameters ts, ArrayList<Long> testdata) {
        long[] timings;
        long timesum;
        double avetime;
        int test_num = testdata.size();
        String[] res = new String[test_num];
        double[] average_timings = new double[test_num];

        try {
            AlienClass1Reader reader = new AlienClass1Reader();
            reader.setConnection(Utils.ReaderIP, Utils.ReaderPort);

            // Open a connection to the reader
            reader.open();
            // Reset
            reader.setFactorySettings();

            // Format the tagList
            reader.setTagListCustomFormat("ID:%k,  USR:${G2DATA1}");
            reader.setAcquireG2TagData(String.format("3, %d, %d", ts.getData_start_bit_RQ() / 16, ts.getUserLength() / 16));
            reader.setTagListFormat(AlienClass1Reader.CUSTOM_FORMAT);

            for (int datai = 0; datai < test_num; datai++) {
                timings = new long[ts.getRun_times()];

                // Start testing for test data i
                System.out.printf("Starting range query test %d...\n", datai);
                for (int t = 0; t < ts.getRun_times(); t++) {
                    // Get the time cost of URQ when the boundary is test data i
                    timings[t] = RQ_Alg(reader, ts, testdata.get(datai));

                    // Print the result timing
                    if (timings[t] == -1) {
                        System.out.printf("RQ Test %d-%d failed, rerunning...\n", datai, t);
                        t--;
                    } else {
                        System.out.printf("RQ Test %d-%d, timing: %dms\n\n", datai, t, timings[t]);
                    }
                }

                // Get the average time cost for test data i
                timesum = 0;
                for (long timing : timings)
                    timesum += timing;
                avetime = (double) timesum / (double) ts.getRun_times();

                // Save the time cost and the average time cost for test data i
                String tmpstr = Arrays.toString(timings);
                // Time cost string will be formatted as: time_1,time_2,...,time_n, ,ave_time
                res[datai] = testdata.get(datai) + "," + tmpstr.substring(1, tmpstr.length() - 1) + ",," + avetime;

                average_timings[datai] = avetime;
                System.out.printf("Average timing: %fms\n\n", avetime);
            }
            // Restore and close
            reader.setFactorySettings();
            reader.close();

        } catch (Exception ex) {
            System.out.println("Error: " + ex.toString());
        }

        // Print the average result timings and the average time efficiency of URQ
        double ave_timingsum = 0;
        for (int i = 0; i < average_timings.length; i++) ave_timingsum += average_timings[i];
        System.out.println("RQ Average Timings: " + Arrays.toString(average_timings));
        System.out.println("RQ Average Timing: " + ave_timingsum / (double) average_timings.length);
        System.out.println();

        return res;
    }

    /***
     * Run URQ with a given boundary (URQ algorithm)
     * @param reader: the reader used in experiment
     * @param ts: the parameters of this experiment
     * @param abnormal_data: the given boundary
     * @return the time cost of URQ
     */
    public static long RQ_Alg(AlienClass1Reader reader, Test_Parameters ts, long abnormal_data) {
        long start = System.currentTimeMillis(), res = 0;
        try {
            // Mask the other tags in surroundings
            reader.setAcqG2Mask(1, 32, Utils.tagID_prefix_len, Utils.tagID_prefix_str);
            reader.setAcqG2MaskAction("AB");
            reader.setAcquireG2Selects(1);
            reader.setAcquireG2Count(0);
            reader.getCustomTagList();

            // Get masks and execute corresponding select command
            ArrayList<String> masks = getMasks_RQ(abnormal_data, ts.getUserLength());
            String maskAction = "";
            for (int i = 0; i < masks.size(); i++) {
                String mask = String.format("3, %d, %d, %s",
                        ts.getData_start_bit_RQ(), masks.get(i).length(), Utils.binStringToHexString(masks.get(i)));
                reader.setAcqG2Mask(mask);
                reader.setAcquireG2Selects(2);
                reader.setAcquireG2Count(0);
                reader.setRFAttenuation(0);
                maskAction = i == 0 ? "BA" : "B-";
                reader.setAcqG2MaskAction(maskAction);
                reader.getCustomTagList();
            }

            // Mask the other tags in surroundings
            reader.setAcqG2Mask(1, 32, Utils.tagID_prefix_len, Utils.tagID_prefix_str);
            reader.setAcqG2MaskAction("-B");
            reader.setAcquireG2Selects(1);
            reader.setAcquireG2Count(1);
            reader.setAcquireG2Target("A");

            // Print the query result
            reader.setAcquireTime(20);
            Tag[] tags = reader.getCustomTagList();
            if (tags == null) {
                System.out.println("No anomalous tags found.");
            } else {
                System.out.println("Warning: anomalous tag detected!");
                /*for(Tag tag:tags){
                    System.out.println(tag.getTagID()+" : "+tag.getG2Data(0));
                }*/
            }

            res = System.currentTimeMillis() - start;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
        return res;
    }

    /***
     * Return masks in binary for RQ-URQ.
     * eg. When value = 254(d) = 11111110(b), len = 8
     * RQ-URQ needs to send 8 selects (x means 0 or 1):
     * 1. 0xxxxxxx -> AB
     * 2. 10xxxxxx -> A-
     * 3. 110xxxxx -> A-
     * 4. 1110xxxx -> A-
     * 5. 11110xxx -> A-
     * 6. 111110xx -> A-
     * 7. 1111110x -> A-
     * 8. 11111110 -> A-
     * @param value: value in decimal
     * @param bin_len: binary length
     * @return a vector of masks (e.g. {"0", "10", "110", "1110", ...} in above case)
     */
    private static ArrayList<String> getMasks_RQ(long value, int bin_len) {
        String binString = Long.toBinaryString(value);
        while (binString.length() < bin_len) binString = "0" + binString;

        int last_ones = 0;
        for (int i = binString.length() - 1; i >= 0 && binString.charAt(i) == '1'; i--) {
            last_ones++;
        }

        ArrayList<String> masks = new ArrayList<>();
        for (int i = 0; i < binString.length() - last_ones; i++)
            if (binString.charAt(i) == '1') {
                masks.add(binString.substring(0, i) + "0");
            }

        if (last_ones > 0)
            masks.add(binString.substring(0, binString.length() - last_ones));
        else
            masks.add(binString);

        return masks;
    }
}