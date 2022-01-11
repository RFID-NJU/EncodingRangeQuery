import com.alien.enterpriseRFID.reader.AlienClass1Reader;
import com.alien.enterpriseRFID.reader.AlienReaderException;

/**
 * Created by Chenxingyu, Yuxi.
 * Last update: 2021/12/26.
 * Version: 1.0
 *
 * Initialize the reader.
 */

public class AlienUtil {

    /***
     * Initialize the reader.
     * @param ip: ip address of the reader
     * @param port: port number of the reader
     * @return the initialized reader
     */
    public static AlienClass1Reader initReader(String ip, int port) throws AlienReaderException {
        AlienClass1Reader reader = new AlienClass1Reader();
        reader.setConnection(ip, port);
        reader.open();

        // Basic set
        reader.setReaderFunction("Alien");
        reader.doReaderCommand("freq=1");
        reader.setTimeOutMilliseconds(600000);//set the maximum time to wait for a reader's reply, in milliseconds
        reader.setPersistTime(-1);//the tags are stored in TagList until a 'get TagList' command is issued
        reader.setAcquireTime(0);//no inventory timing restrictions
        reader.setRFAttenuations(0, 0, 0);//set the reader's RFAttenuation values for a specific antenna, with different values for reading and writing operations

        // Mask the other tags in surroundings(the IDs of tags for experiments are all programed with the common prefix : Utils.tagID_prefix_len)
        reader.setAcquireG2Session(2);
        reader.setAcqG2SL("ALL");//SL  nSL  ALL
        reader.setAcqG2Mask(1, 32, Utils.tagID_prefix_len, Utils.tagID_prefix_str);
        reader.setAcqG2MaskAction("AB");
        reader.setAcquireG2Target("A");

        // Inventory parameters set
        reader.setAcquireG2Q(4);
        reader.doReaderCommand("AcqG2QMax = 10");
        reader.doReaderCommand("Set AcqG2AntennaCombine = OFF");
        reader.setAcquireG2Cycles(1);
        reader.setAntennaSequence("0");
        reader.setAcquireG2Selects(1);
        reader.setAcquireG2Count(1);
        reader.close();

        return reader;
    }

    public static void main(String[] args) {
        try {
            initReader(Utils.ReaderIP, Utils.ReaderPort);
        } catch (AlienReaderException e) {
            e.printStackTrace();
        }
    }
}