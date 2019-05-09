import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author huynh
 */
public class Handler {

    // allocation methods
    private boolean contiguous = false;
    private boolean chained = false;
    private boolean indexed = false;

    private String fileName;
    private int startBlock = 0;
    private int fileLength = 0;
    private Scanner in = new Scanner(System.in);
    private Disk disk = new Disk();

    public Handler() {
        /* store bitmap */
        BitSet bits = new BitSet(256);
        bits.set(1, true);
        bits.set(0, true);
        byte[] mapBytes = bits.toByteArray();
        disk.write(1, mapBytes);

        /* Store FAT */
        String fat = "";
        byte[] fatBytes = fat.getBytes();
        disk.write(0, fatBytes);
    }

    public void setMethod(int op) {
        switch (op) {
            case 1:
                contiguous = true;
                break;
            case 2:
                chained = true;
                break;
            default:
                indexed = true;
                break;
        }
    }
    
    public boolean getFileInfo() {
        
        // Get file name and validate
        do  {
            System.out.print("Enter file name: ");
            fileName = in.next();
            if (fileName.length() > 12)
                System.out.println("File names are allowed up to 8 characters. Try again");
        } while (fileName.length() > 12);
            
        // Get FAT
        byte[] fatBytes = disk.read(0);
        String fat = new String(fatBytes);

        // Split by entry
        String[] fileEntries = fat.split("\n");

        // Find the file by name and get information
        String[] fileInfo;
                
        for (int i = 0; i < fileEntries.length; i++) {
            
            fileInfo = fileEntries[i].split("\t");                        

            if (fileInfo[0].equals(fileName)) {                
                startBlock = Integer.parseInt(fileInfo[1]);                
                fileLength = Integer.parseInt(fileInfo[2]);
                return true;
            } 
            else if (i == fileEntries.length - 1) { // we reached the last index and file is not found
                System.out.println("File not found");
                return false;
            }
        }               
        return true;
    }

    // Choice 1
    public void displayFile() {
                        
        if (getFileInfo() == false)
            return;
        
        System.out.println("Displaying file .. \n");

        // Allocate list for blocks of the file
        List<byte[]> fileBlock = new ArrayList<byte[]>();
        if (contiguous) {        
            for (int i = 0; i < fileLength; i++) {                
                fileBlock.add(disk.read(startBlock + i));
            }       
                        
            // Print lines
            Iterator<byte[]> it = fileBlock.iterator();
            while (it.hasNext()) {
                System.out.println(new String(it.next()));
            }
        } else if (chained) {
            // Read disk for start block
            byte[] block;
            byte[] fileBytes;

            int next = 0;        
            byte[] nextBytes;  
            int cur = 0;                  
            for (int i = 0; i < fileLength; i++) {            

                // Read new block
                if (i == 0) {
                    block = disk.read(startBlock);  
                    cur = startBlock;
                }
                else
                    block = disk.read(cur);                            

                // Get file portion of current block
                fileBytes = Arrays.copyOfRange(block, 0, 508);

                // Get next block position
                nextBytes = Arrays.copyOfRange(block, 508, 512);            
                next = ByteBuffer.wrap(nextBytes).getInt();                       

                fileBlock.add(fileBytes);
                cur = next;                
            }   
        } else {            
            // Read disk for index
            String index = new String(disk.read(startBlock));        

            // Parse index
            String[] blockIndices = index.split("/");

            int blockIndex = 0;
            for (int i = 0; i < fileLength; i++) {            
                blockIndex = Integer.parseInt(blockIndices[i]);                  
                fileBlock.add(disk.read(blockIndex));
            }                                           
        }
        Iterator<byte[]> it = fileBlock.iterator();
        while (it.hasNext()) {
            System.out.println(new String(it.next()));
        }
    }

    // Choice 2
    public void displayFileTable() {
        byte[] fatBytes = disk.read(0);

        String fat = new String(fatBytes);

        System.out.println(fat);
    }
    
    // Choice 3
    public void displayBitmap() {
        
        // Read bitmap data from disk and convert to bitmap
        byte[] bitMapBytes = disk.read(1);        
        BitSet bits = BitSet.valueOf(bitMapBytes);

        for (int i = 0; i < 256; i++) {
            if (bits.get(i))
                System.out.print(1);
            else
                System.out.print(0);            

            if ((i + 1) % 32 == 0)
                System.out.println();            
        }
    }

    // Choice 4
    public void displayDiskBlock() {
        System.out.print("Enter block number: ");
        int blockNum = Integer.parseInt(in.next());

        byte[] block = disk.read(blockNum);
        
        for (int i = 0; i < block.length; i++) {
            System.out.print(block[i]);
            if (i + 1 % 50 == 0)
                System.out.println();
        }
    }

    // Choice 5
    public void copyFileFromSim() throws FileNotFoundException, IOException {
        
        // Get file starting block and length
        if (getFileInfo() == false)
            return;                
        
        System.out.print("Enter a name for copy: ");
        fileName = in.next();
        
        // Allocate list for blocks for file
        List<byte[]> fileBlock = new ArrayList<byte[]>();
        
        if (contiguous) {            
            for (int i = 0; i < fileLength; i++) {                
                fileBlock.add(disk.read(startBlock + i));
            }                               
        } else if (chained) {
            // Read disk for start block
            byte[] block;
            byte[] fileBytes;

            int next = 0;        
            byte[] nextBytes;  
            int cur = 0;
            for (int i = 0; i < fileLength; i++) {            

                // Read new block
                if (i == 0) {
                    block = disk.read(startBlock);  
                    cur = startBlock;                    
                }
                else
                    block = disk.read(cur);                            

                // Get file portion of current block
                fileBytes = Arrays.copyOfRange(block, 0, 508);

                // Get next block position
                nextBytes = Arrays.copyOfRange(block, 508, 512);            
                next = ByteBuffer.wrap(nextBytes).getInt();                       

                fileBlock.add(fileBytes);
                cur = next;
            }                             
        } else {            
            // Read disk for index
            String index = new String(disk.read(startBlock));        

            // Parse index
            String[] blockIndices = index.split("/");

            int blockIndex = 0;
            for (int i = 0; i < fileLength; i++) {            
                blockIndex = Integer.parseInt(blockIndices[i]);
                System.out.println("Reading from block " + blockIndex + " . . . ");
                fileBlock.add(disk.read(blockIndex));
            }                  
        }
        // Write bytes to new file                             
        FileOutputStream fos = new FileOutputStream(fileName);
        Iterator<byte[]> it = fileBlock.iterator();
        while (it.hasNext()) {
            fos.write(it.next());
        }
        
        System.out.println("File copied successfully");
    }

    // Choice 6
    public void copyFileFromSys() throws IOException {
        
        do  {
            System.out.print("Enter file name: ");
            fileName = in.next();
            if (fileName.length() > 12)
                System.out.println("File names are allowed up to 8 characters. Try again");
        } while (fileName.length() > 12);
                
        File file = new File(fileName);        
        
        // Calculate length
        int fileLength = 0;
        if (chained) {
            int extraBytes = (int) ((file.length() / 512) + 1) * 4;
            fileLength = (int) ((file.length() + extraBytes) / 512) + 1;
        }
        else
            fileLength = (int) (file.length() / 512) + 1;        

        // Read disk for bitmap
        byte[] bitMapBytes = disk.read(1);
        BitSet bitMap = BitSet.valueOf(bitMapBytes);                
                               
        // Split file and store it
        byte[] fileBytes = Files.readAllBytes(file.toPath());                     

        byte[] block = new byte[512];
        String fatEntry = "";
        
        if (contiguous) {
            boolean open = true;            
            for (int i = 0; i < 256; i++) {
                // Find an open block and check the next few blocks according to file length
                if (bitMap.get(i) == false) {                    
                    for (int j = 0; j < fileLength; j++) {
                        if (bitMap.get(i + j) == true) {
                            open = false;
                            break;
                        }
                    }
                    // If its open, save startBlock at index i
                    if (open) {                        
                        startBlock = i;
                        break;
                    } else {
                        open = true; // reset flag
                    }
                }
            }

            // If no available space at all
            if (!open) {
                System.out.println("Error: disk space unavailable");
            } else // Close spaces on bitmap
            {
                for (int i = 0; i < fileLength; i++) {
                    bitMap.set(startBlock + i, true);
                }
            }
            for (int i = 0; i < fileLength; i++) {
                block = Arrays.copyOfRange(fileBytes, (i * 512), (i * 512 + 512));                
                disk.write(startBlock + i, block);
            }

            // Update FAT
            fatEntry = String.format("%s\t%d\t%d\n", file.getName(), startBlock, fileLength);

        } else if (chained) {
            /* Find free random blocks in the bitmap and allocate file to them  */
            Random rand = new Random();
            int randNum = rand.nextInt(255) + 2; 
            
            // File allocation
            int next = 0;            
            byte[] nextBytes;        
            for (int i = 0; i < fileLength; i++) {

                // Partition file into blocks of 508 bytes each, reserving 4 bytes
                block = Arrays.copyOfRange(fileBytes, (i * 512), (i * 512 + 508));   
                block = Arrays.copyOfRange(block, 0, 512); // force 0's and increase size                                               

                // Save start block with first iteration
                if (i == 0) {
                    while (bitMap.get(randNum) == true)
                        randNum = rand.nextInt(255) + 2; 
                    startBlock = randNum;
                }                                        

                // Find next random free block
                while (bitMap.get(next) == true)
                    next = rand.nextInt(255) + 2;              

                // Get bytes for next position                         
                nextBytes = ByteBuffer.allocate(4).putInt(next).array();

                // Add the 4 bytes to end of block
                for (int j = 0; j < nextBytes.length; j++) {
                    block[508 + j] = nextBytes[j];
                }                        

                // Write block to the random position and update bitmap           
                disk.write(randNum, block);  
                bitMap.set(randNum, true);  

                if (i != fileLength - 1)
                    bitMap.set(next, true);

                // Set next index        
                randNum = next;                                
            }       
            // Update FAT
            fatEntry = String.format("%s\t%d\t%d\n", file.getName(), startBlock, fileLength);
        } else {            
            /* Find free random blocks in the bitmap and allocate file to them  */
            Random rand = new Random();
            int randNum = rand.nextInt(255);
            String index = "";
            for (int i = 0; i < fileLength; i++) {

                // keep updating randNum until a free block is found
                while (bitMap.get(randNum) == true)
                    randNum = rand.nextInt(255) + 2;

                // Partition file into blocks of 512 bytes each
                block = Arrays.copyOfRange(fileBytes, (i * 512), (i * 512 + 512));                

                // write block into disk at the random position and add position to index
                index += Integer.toString(randNum) + "/"; // add randNum to index along with a delimiter '/'            
                disk.write(randNum, block);    

                // Update bitmap
                bitMap.set(randNum, true);
            }

            // Write the index to random free block on disk        
            while (bitMap.get(randNum) == true)         // Find free block
                    randNum = rand.nextInt(255) + 2;          
            byte[] indexBytes = index.getBytes();       // Convert index to bytes
            disk.write(randNum, indexBytes);            // Write to disk

            // Update bitmap with index
            bitMap.set(randNum, true);            

            // Update FAT
            fatEntry = String.format("%s\t%d\t%d\n", file.getName(), randNum, fileLength);
        }
        
        // Write bitmap to disk
        bitMapBytes = bitMap.toByteArray();            
        disk.write(1, bitMapBytes);
            
        // Write FAT to disk
        byte[] fatBytes = disk.read(0);    // read from disk
        String fat = new String(fatBytes); // convert to string         
        fat += fatEntry;                   // update fat with new entry
        fatBytes = fat.getBytes();         // convert to bytes        
        disk.write(0, fatBytes);           // write to disk
        
        System.out.println("File copied successfully");
    }

    // Choice 7
    public void deleteFile() {          
                
        // Get file name
        System.out.println("Enter the file to delete: ");
        fileName = in.next();
        
        // Read disk for FAT
        byte[] fatBytes = disk.read(0);
        String fat = new String(fatBytes);

        // Split by entry
        String[] fileEntries = fat.split("\n");

        // Find the file by name and get information
        String[] fileInfo;            
        fat = "";
        boolean found = false;
        for (int i = 0; i < fileEntries.length; i++) {                         
            fileInfo = fileEntries[i].split("\t");

            if (fileInfo[0].equals(fileName)) {                          
                startBlock = Integer.parseInt(fileInfo[1]);
                fileLength = Integer.parseInt(fileInfo[2]);  
                found = true;
            } else
                fat += fileEntries[i] + "\n";    
            
            if (i == fileEntries.length - 1 && found == false) {
                System.out.println("File not found");
                return;
            }                
        }

        // Update FAT        
        fatBytes = fat.getBytes();         // convert to bytes        
        disk.write(0, fatBytes);           // write to disk

        // Read disk for bitmap
        byte[] bitMapBytes = disk.read(1);
        BitSet bitMap = BitSet.valueOf(bitMapBytes);
        
        if (contiguous) {            
            /* Set blocks to null where file was allocated to
            and update bitmap */            
            for (int i = 0; i < fileLength; i++) {                
                bitMap.set(startBlock + i, false);
                disk.write(startBlock + i, null);
            }                      
        } else if (chained) {
            int cur = 0;
            int next = 0;
            byte[] nextBytes;
            byte[] block;
            // Delete rest of blocks containing a partition of the file        
            for (int i = 0; i < fileLength; i++) {
                // Read new block
                if (i == 0) {
                    block = disk.read(startBlock);  
                    cur = startBlock;
                }
                else
                    block = disk.read(cur);                            

                // Get next block position
                nextBytes = Arrays.copyOfRange(block, 508, 512);            
                next = ByteBuffer.wrap(nextBytes).getInt();                 

                bitMap.set(cur, false);
                disk.write(cur, null);

                cur = next;
            }     
        } else {
            // Read disk for index and parse
            String index = new String(disk.read(startBlock));                
            String[] blockIndices = index.split("/");                                                             

            // Delete block containing index
            bitMap.set(startBlock, false);
            disk.write(startBlock, null);

            /* Set blocks to null where file was allocated to
               and update bitmap */ 
            int blockIndex = 0;
            for (int i = 0; i < fileLength; i++) {
                blockIndex = Integer.parseInt(blockIndices[i]);                
                bitMap.set(blockIndex, false);
                disk.write(blockIndex, null);
            }                  
        }
        
        // Write bitmap to disk
        bitMapBytes = bitMap.toByteArray();
        disk.write(1, bitMapBytes);
            
        System.out.println("File deleted successfully");
    }
}
