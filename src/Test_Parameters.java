import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/***
 * Created by Yuxi
 * Last update: 2022/01/03
 * Version: 3.1
 *
 * Test parameters in each experiment.
 */

public class Test_Parameters {
    // The max length of user data, it must be 16x, e.g., 16, 32, 48 in experiments
    private int userLength = 16;

    // The start bit of user data for RQ
    private int data_start_bit_RQ = 0;
    // The start bit of user data for EnRQ
    private int data_start_bit_EncodingRQ = 128;

    // The number of abnormal tags
    private int abnormal_num = 1;

    // The number of tags
    private int tag_num = 500;

    // The number of repetition for each experiment
    private int run_times = 20;

    // Today
    private String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

    /***
     * Make experimental data permanent, i.e., write the experimental data of multiple experiments into a file
     * @param data: each string in the array is a time cost list in milliseconds of one experiment
     * @param recordPath: the path of file to be stored
     */
    public void multi_writeToFile(String[] data, String recordPath) {
        try {
            String com_recordpath = recordPath + today + ".csv";
            Writer wrecord = new FileWriter(new File(com_recordpath), true);

            // Write the basic information of the experiment into file
            wrecord.write("Test Info,\n");
            wrecord.write("Tags Num," + tag_num + "\n");
            wrecord.write("Abnormal Tags Num," + abnormal_num + "\n");
            wrecord.write("Length of data," + userLength + "\n");
            wrecord.write("Test times," + run_times + "\n\n");

            // Write the experimental results into file
            wrecord.write("Test Result,\n");
            wrecord.write("Timings,\n");
            // Write the experimental results one by one into file
            for (int i = 0; i < data.length; i++) {
                wrecord.write(data[i] + "\n");
            }
            wrecord.write("\n");

            wrecord.flush();
            wrecord.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Getter and Setter
    public int getUserLength() {
        return userLength;
    }

    public void setUserLength(int userLength) {
        this.userLength = userLength;
    }

    public int getData_start_bit_RQ() {
        return data_start_bit_RQ;
    }

    public void setData_start_bit_RQ(int data_start_bit_RQ) {
        this.data_start_bit_RQ = data_start_bit_RQ;
    }

    public int getData_start_bit_EncodingRQ() {
        return data_start_bit_EncodingRQ;
    }

    public void setData_start_bit_EncodingRQ(int data_start_bit_EncodingRQ) {
        this.data_start_bit_EncodingRQ = data_start_bit_EncodingRQ;
    }

    public int getAbnormal_num() {
        return abnormal_num;
    }

    public void setAbnormal_num(int abnormal_num) {
        this.abnormal_num = abnormal_num;
    }

    public int getTag_num() {
        return tag_num;
    }

    public void setTag_num(int tag_num) {
        this.tag_num = tag_num;
    }

    public int getRun_times() {
        return run_times;
    }

    public void setRun_times(int run_times) {
        this.run_times = run_times;
    }
}
