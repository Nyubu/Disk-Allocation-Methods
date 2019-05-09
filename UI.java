import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author huynh
 */
public class UI {

    private Handler handler = new Handler();
    private Scanner in = new Scanner(System.in);    

    public UI() {        
        System.out.println("Choose an allocation method");
        System.out.println("1. Contiguous allocation");
        System.out.println("2. Chained allocation");
        System.out.println("3. Indexed allocation");
        System.out.print("Choice: ");
        int method = in.nextInt();

        handler.setMethod(method);
        System.out.println("--------------------------------");
    }

    public void runInterface() throws IOException {        
        int option = 0;

        while (option != 8) {
            System.out.println("1) Display a file");
            System.out.println("2) Display the file table");
            System.out.println("3) Display the free space bitmap");
            System.out.println("4) Display a disk block");
            System.out.println("5) Copy a file from the simulation to a file on the real system");
            System.out.println("6) Copy a file from the real system to a file in the simulation");
            System.out.println("7) Delete a file");
            System.out.println("8) Exit");
            System.out.print("Choice: ");
            option = in.nextInt();
            
            switch(option) {
                case 1:
                    handler.displayFile();
                    break;
                case 2:
                    handler.displayFileTable();
                    break;
                case 3:
                    handler.displayBitmap();
                    break;
                case 4:
                    handler.displayDiskBlock();
                    break;
                case 5:
                    handler.copyFileFromSim();
                    break;
                case 6:
                    handler.copyFileFromSys();
                    break;
                case 7:
                    handler.deleteFile();
                    break;
                default:
                    System.out.println("Exiting . . .");
            }
            System.out.println("--------------------------------");
        }

    }   
}
