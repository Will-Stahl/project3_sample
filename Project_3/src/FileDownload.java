import java.util.zip.CRC32;
import java.util.Random;
import java.io.Serializable;

/**
 * instances of this class are passed from p2p as file downloads
 * has checksum
 * has simulation method to randomly add noise to download
 */
public class FileDownload implements Serializable {
    byte[] contents;
    long checksum;

    public FileDownload(byte[] fContent) {
        contents = fContent;
        CRC32 crc = new CRC32();
        crc.update(contents);  // computes checksum
        checksum = crc.getValue();  // store checksum value
        addNoise();
    }

    public byte[] GetContents() {
        return contents;
    }

    public long GetChecksum(){
        return checksum;
    }

    /**
     * checks computed checksum against stored checksum
     * false if they don't match
     */
    public boolean Checksum() {
        CRC32 crc = new CRC32();
        crc.update(contents);  // computes checksum
        return (crc.getValue() == checksum);
    }

    // Function for computing checksum of a file
    public long computeChecksum() {
        CRC32 crc = new CRC32();
        crc.update(contents);  // computes checksum
        return crc.getValue();
    }

    /**
     * corrupts file randomly
     * if file is 5 bytes for instance:
     * it has 5/10,000 chance to get corrupted
     */
    private void addNoise() {
        Random rand = new Random();
        int corrupt = rand.nextInt(10000);  // corrupt 1 of every 10 KB
        if (corrupt < contents.length) {
            contents[corrupt]++;
            // this should cause checksum failure
        }
    }

}