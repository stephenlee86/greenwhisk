import java.util.*;

public class RetryQueue {
    private static final int MAX_QUEUE_SIZE = 500;

    private static Queue<Workload> queue = new LinkedList<Workload>();

    public static boolean add(Workload w){
        if (queue.size() >= MAX_QUEUE_SIZE) return false;
        queue.add(w);
        return true;
    }

    public static Workload next(){
        return queue.poll();
    }

    public static boolean isEmpty(){
        return queue.isEmpty();
    }


}
