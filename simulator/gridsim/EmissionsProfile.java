
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class EmissionsProfile  {
    private String ba;
    private int serverID;
    
    private List<Double> data;

    private int month = 1;

    private double lastMetric = 0;

    private static String[] balancingAuthorities = {
        "NEVP",
        "BPA",
        "SOCO",
        "PJM_SOUTHWEST_OH",
        "TVA",
        "ERCOT_NORTHCENTRAL",
        "SPP_WESTNE",
        "SPP_MEMPHIS",
    };

    private static double[] lastUsedValues = new double[balancingAuthorities.length];

    private static int[] lastUsedTimes = new int[balancingAuthorities.length];

    public EmissionsProfile(int serverID) {
        this.ba = balancingAuthorities[serverID % balancingAuthorities.length];
        this.serverID = serverID;
        // Data directory: ./watt_time/[balancing authority]_historical/[balancing
        // authority]_[year]-[month]_MOER.csv
        
        // initialize data

        for(int i = 0; i < lastUsedTimes.length; i++) lastUsedTimes[i] = -1;
        try{
            data = new ArrayList<>();
            List<String> temp = Files.readAllLines(Paths.get("./Data/" + ba + "_historical/" + ba + "_2020-01_MOER.csv"));
            for(String line : temp){
                String[] fields = line.split(",");
                if(fields[1].equals("MOER")) continue;
                Double datum = Double.parseDouble(fields[1]);
                data.add(datum);
            }
        }catch(IOException e){
            System.out.println("File not found: " + e.getMessage());
        }
    }

    public double getMetric(int time) {

        // Use the last used value if the time is the same
        int baIndex = serverID % balancingAuthorities.length;
        if(lastUsedTimes[baIndex] == time) return lastUsedValues[baIndex];
        


        Calendar cal = new GregorianCalendar(2020, 0, 1);
        cal.add(Calendar.SECOND, time);


        // If we've moved to a new month, we need to load the new data
        if(cal.get(Calendar.MONTH) + 1 != month){
            month = cal.get(Calendar.MONTH) + 1;
            try{
                List<String> temp = Files.readAllLines(Paths.get("./Data/" + ba + "_historical/" + ba + "_" + cal.get(Calendar.YEAR) + "-" + (month < 10 ? "0" : "") + month + "_MOER.csv"));
                data.clear();
                for(String line : temp){
                    String[] fields = line.split(",");
                    if(fields[1].equals("MOER")) continue;
                    Double datum = Double.parseDouble(fields[1]);
                    data.add(datum);
                }
            }catch(IOException e){
                System.out.println("File not found");
            }
        }

        
        // Use calendar to get the seconds since the start of the month
        int seconds = cal.get(Calendar.SECOND) + 60 * cal.get(Calendar.MINUTE) + 3600 * cal.get(Calendar.HOUR_OF_DAY) + (86400 * (cal.get(Calendar.DAY_OF_MONTH) - 1));

        int fiveMinute = seconds / 300; // 5 minute intervals
        
        lastMetric = data.get(fiveMinute);
        lastUsedValues[baIndex] = lastMetric;
        lastUsedTimes[baIndex] = time;

        return lastMetric;

    }

}
