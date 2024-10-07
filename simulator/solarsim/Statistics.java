
public class Statistics {
    private static int totalFunctionsSent = 0;

    private static int totalFunctionsCompleted = 0;

    private static int totalRetryCount = 0;

    private static int totalShutdowns = 0;

    private static int totalFailures = 0;

    private static int coldStarts = 0;

    private static int warmStarts = 0;

    private static int softShutdowns = 0;

    private static long downTime = 0;

    private static long onlineAgain = 0;

    // Calculated warm start to cold start ratio is around
    // 40% warm starts


    public static void dump(){
        System.out.println("Total functions sent: " + totalFunctionsSent);
        System.out.println("Total functions completed: " + totalFunctionsCompleted);
        System.out.println("Total functions sent to retry queue: " + totalRetryCount);
        System.out.println("Total shutdowns: " + totalShutdowns);
        System.out.println("Total soft shutdowns: " + softShutdowns);
        System.out.println("Total failures: " + totalFailures);
        System.out.println("Total restarts: " + onlineAgain);
        System.out.println("Total warm starts: " + warmStarts);
        System.out.println("Total cold starts: " + coldStarts);
        System.out.println("Total downtime: " + downTime);
    }


    public static synchronized void functionSent(){
        totalFunctionsSent++;
    }

    public static synchronized void functionCompleted(){
        totalFunctionsCompleted++;
    }

    public static synchronized void functionRetried(){
        totalRetryCount++;
    }

    public static synchronized void serverShutdown(){
        totalShutdowns++;
    }

    public static synchronized void functionFailed(){
        totalFailures++;
    }

    public static void coldStart(){
        coldStarts++;
    }

    public static void warmStart(){
        warmStarts++;
    }

    public static void softShutdown(){
        softShutdowns++;
    }

    public static void down(){
        downTime++;
    }

    public static void restarted(){
        onlineAgain++;
    }
}
