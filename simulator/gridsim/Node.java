import java.util.*;

public class Node  {
    private static int count;
    public static final double CPU = 300; // Watts (6.5 for raspberry pi, 35 for marvin)
    public static final int CONTAINERS = 3; // ~ 3 for both, maybe 4 for marvin

    private int functionsReceived = 0;

    private int id = count++;

    public Queue<Workload> queue = new LinkedList<Workload>();

    public static ArrayList<Workload> retryQueue;

    public EmissionsProfile profile;
    public boolean online = true;

    public ArrayList<Container>  containers = new ArrayList<Container>();

    public Node(EmissionsProfile profile){
        this.profile = profile;

        for(int i = 0; i < CONTAINERS; i++) containers.add(new Container());
    }

    public void invoke(Workload w){
        functionsReceived++;
        queue.add(w);
    }
    public void executeWorkload(Workload w){
        for(Container c : containers){
            if(c.free && c.type == w.type){
                c.stageWorkload(w);
                return;
            }
        }
        for(Container c : containers){
            if(c.free){
                c.stageWorkload(w);
                return;
            }
        }
        System.out.println("ERRORORORORRO");
    }

    public boolean existsFreeContainer(){
        for(Container c : containers){
            if(c.free) return true;
        }
        return false;
    }

    public boolean work(Container c){
        return ((c.age++) >= Workload.executionTime + (c.coldStart ? Workload.coldPenalty : 0));
    }

    public void tick(int time){



        // Add workloads
        while(!queue.isEmpty() && existsFreeContainer()){
            executeWorkload(queue.poll());
        }

        // Work on workloads
        // boolean working = false;
        // battery.draw(0.2 * CPU); // Start at 0.2 for idle CPU usage
        for(Container c : containers){
            if(c.free) continue;
            // if(!working){ // Add another 0.2 if not idle
            //     working = true;
            //     battery.draw(0.2 * CPU);
            // }
            // battery.draw(Workload.UTIL * CPU);
            if(work(c)){
                // Work completed
                Statistics.functionCompleted();
                c.evict();
            }
        }
        

    }













    // UTILITY FUNCTIONS


    public double util(boolean addWorkload){
        int total = addWorkload ? 1 : 0;
        for (Container c : containers) total += c.free ? 0 : 1;
        return total == 0 ? 0.2 : (total * Workload.UTIL) + 0.4; 
    }
    public double util(){
        return util(false);
    }

    public double getCPUEnergyDraw(boolean addWorkload){
        return util(addWorkload) * CPU;
    }

    public double getMetric(int time, boolean addWorkload){
        double total = getCPUEnergyDraw(addWorkload);
        double totalQueueTime = queue.size() * ((Workload.coldPenalty * 0.4) + Workload.executionTime);
        double totalQueueDraw = totalQueueTime * Workload.UTIL * CPU;
        total += totalQueueDraw;

        total *= profile.getMetric(time);

        return -total;
    }
    public double getMetric(int time){
        return getMetric(time, false);
    }

    public double getMOER(int time, boolean addWorkload){
        double total = getCPUEnergyDraw(addWorkload);
        return total *= -profile.getMetric(time);
    }

    public void dump(){
        System.out.println("Functions Received: " + functionsReceived);
    }

    public String toString(){
        return "[Node " + this.id + "] " + profile.toString(); 
    }
}
