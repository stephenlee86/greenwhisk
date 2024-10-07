import java.util.ArrayList;

public class Workload {
    public static final double UTIL = 0.2; // 0.2 for pi, 0.15 for optiplex if 4 containers
    public static final int NUM_TYPES = 5;

    private ArrayList<Integer> trace;

    public int type = 0;
    public static int executionTime = 1;
    public static int coldPenalty = 4;


    private static int count = 0;
    private int id = 0;


    public Workload(){
        id = count++;
        trace = TraceImporter.getTrace(id);

        type = EmissionsSim.rand.nextInt(NUM_TYPES);

    }

    public int numberToInvoke(int time){
        int traceCount =  trace.get((time/60) % trace.size());

        if (traceCount == 0) {
            return 0;
        }

        
        // Get invocations per second
        int invocationsToExecute = traceCount / 60;
    
        int secondsInMinute = time % 60;
        int invocationsInMinute = traceCount % 60;
        int intervals = 60 / invocationsInMinute;
        if (secondsInMinute % intervals == 0) {
            invocationsToExecute++;
        }

        return invocationsToExecute;
    }



}