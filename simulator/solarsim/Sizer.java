import java.io.File;
import java.util.HashMap;
import java.util.Scanner;

public class Sizer {
    private static Sizer instance = null;

    private static HashMap<String, int[]> sizes = new HashMap<String, int[]>();

    public static int[] getSize(String profileName){
        int[] size = new int[2];
        size[0] = sizes.get(profileName)[0];
        size[1] = sizes.get(profileName)[1];
        
        return size;
    }

    private Sizer(String file) {
        try{
            Scanner scanner = new Scanner(new File(file));
            scanner.nextLine();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                String profileName = parts[1];
                int[] size = new int[2];
                size[0] = (int)(Double.parseDouble(parts[2]));
                size[1] = 0;
                sizes.put(profileName, size);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static Sizer init(String file) {
        if (instance == null) {
            instance = new Sizer(file);
        }
        return instance;
    }



}
