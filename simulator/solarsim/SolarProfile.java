import java.util.*;
import java.io.*;

public class SolarProfile {

    // Watt-seconds required for a 6.5 W Pi to run for 24 hours
    // 561600 = 6.5 * 24 * 60 * 60
    // Watt-seconds required for a 35 W server to run for 24 hours
    // 
    private double PANEL_WATTAGE;
    private int numberOfNodes;
    public double batterySize = 0;

    public Battery batt;

    private ArrayList<Trace> traces = new ArrayList<Trace>();

    public SolarProfile(String fileName, int numberOfNodes) {
        this.numberOfNodes = numberOfNodes + 1;
        this.batt = new Battery(this.numberOfNodes);
        readFromCSV(fileName);
    }
    
    
    private void readFromCSV(String fileName) {
        String fileNameWithoutPath = fileName.substring(fileName.lastIndexOf('/') + 1);

        this.PANEL_WATTAGE = Sizer.getSize(fileNameWithoutPath)[0] * numberOfNodes;
        // this.batterySize = Sizer.getSize(fileNameWithoutPath)[1];


        try{
            ArrayList<String> traceLines = new ArrayList<String>();
            Scanner trace = new Scanner(new File(fileName));

            // Skip header
            trace.nextLine();

            // skip the first day
            for(int i = 0; i < 24 * 6; i++){
                trace.nextLine();
            }

            while(trace.hasNext()){
                traceLines.add(trace.nextLine());
            }



            trace.close();

            for(String line : traceLines){
                String[] lineSplit = line.split(",");

                int period = 10;
                double power = Double.parseDouble(lineSplit[7]);

                traces.add(new Trace(period, power));
            }
            
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + fileName);
        }
    }

    // 11th gen intel core i7-11700T @ 1.4GHz x 16
    private double getPower(int time){
        if(traces.size() == 0){
            return 0;
        }
        // Time is in seconds, while trace is in 10 minute intervals
        time /= 60;
        time /= 10;

        Trace trace = traces.get(time);

        return (trace.power * PANEL_WATTAGE);
    }

    public double getSolarPower(int time){
        return getPower(time);
    }
    
}
