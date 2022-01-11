import org.junit.Test;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/***
 * Created by Yuxi
 * Last update: 2021/12/27
 * Version: 1.0
 *
 * Compare the time efficiency of two protocols, i.e., RQ, EnRQ
 * and study the effects of different parameters on the protocols.
 */

public class Real_Test_EncodingRQ {
    // Number of repetitions per experiment
    private int runtimes = 20;

    // Study the effect of data length
    @Test
    public void Test_Length16() {
        lengthTest(16, 112, 128, ".\\Data\\TestData\\test_EnRQ_16data.csv");
    }
    @Test
    public void Test_Length32() {
        lengthTest(32, 96, 192, ".\\Data\\TestData\\test_EnRQ_32data.csv");
    }
    @Test
    public void Test_Length48() {
        lengthTest(48, 80, 320, ".\\Data\\TestData\\test_EnRQ_48data.csv");
    }

    // Study the effect of the number of tags
    // The number of tags should be changed to 400, 300, 200, 100
    @Test
    public void Test_tagNum() {
        int tag_num = 100;
        tagNumTest(tag_num);
    }

    // Study the effect of the number of abnormal tags
    // The number of abnormal tags should be changed to 0, 1, 2, 3, 4, 5
    @Test
    public void Test_abnormalNum() {
        int abnormal_tag_num = 5;
        abnormalTagNumTest(abnormal_tag_num);
    }

    /***
     * Study the effect of data length and set the rest parameters to default
     * @param data_len: the length of data
     * @param data_start_bit_RQ: the start bit of data of RQ
     * @param data_start_bit_EnRQ: the start bit of data of EnRQ
     * @param testfilepath: the file path of the test data
     */
    private void lengthTest(int data_len, int data_start_bit_RQ, int data_start_bit_EnRQ, String testfilepath) {
        Test_Parameters ts = new Test_Parameters();
        ts.setTag_num(500);
        ts.setAbnormal_num(1);
        ts.setUserLength(data_len);
        ts.setData_start_bit_RQ(data_start_bit_RQ);
        ts.setData_start_bit_EncodingRQ(data_start_bit_EnRQ);
        ts.setRun_times(runtimes);

        ArrayList<Long> testdata = getTestDataFromFile(testfilepath);

        String[] timings_RQ = RQ.RQ_URQ(ts, testdata);
        String RQ_file_path = ".\\Data\\RQ\\Test_Length\\testResult_RQ_len_" + ts.getUserLength() + "_";
        ts.multi_writeToFile(timings_RQ, RQ_file_path);

        String[] timings_EnRQ = EncodingRQ16.EnRQ_URQ(ts, testdata);
        String EnRQ_file_path = ".\\Data\\EncodingRQ\\Test_Length\\testResult_EnRQ_len_" + ts.getUserLength() + "_";
        ts.multi_writeToFile(timings_EnRQ, EnRQ_file_path);
    }

    /***
     * Study the effect of the number of tags and set the rest parameters to default
     * @param tag_num: the number of tags
     */
    private void tagNumTest(int tag_num) {
        Test_Parameters ts = new Test_Parameters();
        ts.setTag_num(tag_num);
        ts.setAbnormal_num(1);
        ts.setUserLength(16);
        ts.setData_start_bit_RQ(112);
        ts.setData_start_bit_EncodingRQ(128);
        ts.setRun_times(runtimes);

        ArrayList<Long> testdata = getTestDataFromFile(".\\Data\\TestData\\test_EnRQ_16data.csv");

        String[] timings_RQ = RQ.RQ_URQ(ts, testdata);
        String RQ_file_path = ".\\Data\\RQ\\Test_TagNum\\testResult_RQ_tag_num_" + ts.getTag_num() + "_";
        ts.multi_writeToFile(timings_RQ, RQ_file_path);

        String[] timings_EnRQ = EncodingRQ16.EnRQ_URQ(ts, testdata);
        String EnRQ_file_path = ".\\Data\\EncodingRQ\\Test_TagNum\\testResult_EnRQ_tag_num_" + ts.getTag_num() + "_";
        ts.multi_writeToFile(timings_EnRQ, EnRQ_file_path);
    }

    /***
     * Study the effect of the number of abnormal tags and set the rest parameters to default
     * @param abnormal_tag_num: the number of abnormal tags
     */
    private void abnormalTagNumTest(int abnormal_tag_num) {
        Test_Parameters ts = new Test_Parameters();
        ts.setTag_num(500);
        ts.setAbnormal_num(abnormal_tag_num);
        ts.setUserLength(16);
        ts.setData_start_bit_RQ(112);
        ts.setData_start_bit_EncodingRQ(128);
        ts.setRun_times(runtimes);

        ArrayList<Long> testdata = getTestDataFromFile(".\\Data\\TestData\\test_EnRQ_16data.csv");

        String[] timings_RQ = RQ.RQ_URQ(ts, testdata);
        String RQ_file_path = ".\\Data\\RQ\\Test_AbnormalNum\\testResult_RQ_abnormal_num_" + ts.getAbnormal_num() + "_";
        ts.multi_writeToFile(timings_RQ, RQ_file_path);

        String[] timings_EnRQ = EncodingRQ16.EnRQ_URQ(ts, testdata);
        String EnRQ_file_path = ".\\Data\\EncodingRQ\\Test_AbnormalNum\\testResult_EnRQ_abnormal_num_" + ts.getAbnormal_num() + "_";
        ts.multi_writeToFile(timings_EnRQ, EnRQ_file_path);
    }

    /***
     * Read test data from a file into a list
     * @param filepath:the file path of test data
     * @return the test data arraylist
     */
    private ArrayList<Long> getTestDataFromFile(String filepath) {
        ArrayList<Long> testData = new ArrayList<>();
        BufferedReader testfileReader;
        File testFile;
        String tmpdata;
        try {
            testFile = new File(filepath);
            testfileReader = new BufferedReader(new FileReader(testFile));
            while ((tmpdata = testfileReader.readLine()) != null) {
                testData.add(Long.parseLong(tmpdata.substring(0, tmpdata.indexOf(','))));
            }
        } catch (Exception ex) {
            System.out.println("Error: " + ex.toString());
        }
        return testData;
    }

}
