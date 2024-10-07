import java.util.ArrayList;  


public class Statistics {
    private static ArrayList<Double> emissionsSaved = new ArrayList<Double>();

    private static int totalFunctionsSent = 0;

    private static int totalFunctionsCompleted = 0;

    private static int coldStarts = 0;

    private static int warmStarts = 0;


    private static double totalSavedEmissions = 0;


    // Calculated warm start to cold start ratio is around
    // 40% warm starts


    public static void dump(){
        System.out.println("Total functions sent: " + totalFunctionsSent);
        System.out.println("Total functions completed: " + totalFunctionsCompleted);
        System.out.println("Total warm starts: " + warmStarts);
        System.out.println("Total cold starts: " + coldStarts);
        System.out.println("Total emissions avoided: " + totalSavedEmissions);
    }


    public static synchronized void functionSent(){
        totalFunctionsSent++;
    }

    public static synchronized void functionCompleted(){
        totalFunctionsCompleted++;
    }

    public static void coldStart(){
        coldStarts++;
    }

    public static void warmStart(){
        warmStarts++;
    }


    public static void savedEmissions(int time, double amount){
        time = time - EmissionsSim.START_TIME;
        time = time / 60 / 60; // Logs by the minute

        amount = amount / 3_600_000_000.0; // lbs/mwh to lbs/ws
        
        totalSavedEmissions += amount; // This is in lbs
        
        amount *= 453.592; // lbs to grams

        if(emissionsSaved.size() <= time){
            emissionsSaved.add(amount);
            return;
        }


        emissionsSaved.set(emissionsSaved.size()-1, amount + emissionsSaved.get(emissionsSaved.size()-1));
    }
}
