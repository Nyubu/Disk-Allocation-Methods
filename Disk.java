/*
 *
 * @author huynh
 */
public class Disk {
    private byte[][] blocks = new byte[256][512];     
    
    public byte[] read(int blockNum) {        
        return blocks[blockNum];
    }
    
    public void write(int blockNum, byte[] block) {
        blocks[blockNum] = block;
    }        
}
