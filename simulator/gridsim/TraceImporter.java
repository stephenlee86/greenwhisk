import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class TraceImporter {
    private static ArrayList<ArrayList<Integer>> traces = new ArrayList<ArrayList<Integer>>();

    public static void init(String traceFile){

        Scanner scanner = null;
        try { 
            scanner = new Scanner(new File(traceFile));
        }catch(Exception e){
            System.out.println("File not found");
        }

        scanner.nextLine(); // Skip the first line
        while(scanner.hasNextLine()){
            ArrayList<Integer> invocations = new ArrayList<Integer>();
            String line = scanner.nextLine();
            String[] fields = line.split(",");

            for(int i = 2; i < fields.length; i++){
                invocations.add(Integer.parseInt(fields[i]));
            }

            traces.add(invocations);
        }

    }

    public static int numWorkloads(){
        return traces.size();
    }

    public static ArrayList<Integer> getTrace(int workloadId){
        return traces.get(workloadId);
    }
}
