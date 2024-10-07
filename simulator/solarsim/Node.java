import java.util.*;

public class Node  {
    public static final double CPU = 300; // Watts (6.5 for raspberry pi, 35 for marvin)
    public static final int CONTAINERS = 3; // ~ 3 for both, maybe 4 for marvin

    private int functionsReceived = 0;

    public Queue<Workload> queue = new LinkedList<Workload>();

    public Node fallbackNode = null;

    public SolarProfile profile;
    public boolean online = true;
    public Battery battery;

    public ArrayList<Container>  containers = new ArrayList<Container>();

    public Node(SolarProfile profile){
        this.profile = profile;
        this.battery = profile.batt;
        for(int i = 0; i < CONTAINERS; i++) containers.add(new Container());
    }

    public void invoke(Workload w){
        int fallbackQueueSize = fallbackNode != null ? fallbackNode.getQueueSize() : SolarSim.MAX_NODE_QUEUE_SIZE;
        if(this.queue.size() >= SolarSim.MAX_NODE_QUEUE_SIZE && (fallbackQueueSize < SolarSim.MAX_NODE_QUEUE_SIZE) && (fallbackQueueSize < this.queue.size())) {
            fallbackNode.invoke(w);
            return;
        }else if(!this.online){
            functionsReceived++;
            if(!RetryQueue.add(w)) Statistics.functionFailed();
            Statistics.functionRetried();
            return;
        }


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
        if(fallbackNode != null) fallbackNode.tick(time);
        
        double solarPower = profile.getSolarPower(time);
        battery.charge(solarPower);


        if(!online) {
            if(!battery.isCritical()){
                online = true;
                Statistics.restarted();
            }else{
                Statistics.down();
                return;
            }
        }

        





        // Add workloads
        while(!queue.isEmpty() && existsFreeContainer()){
            executeWorkload(queue.poll());
        }


        // Work on workloads
        try {
            boolean working = false;
            battery.draw(0.2 * CPU); // Start at 0.2 for idle CPU usage
            for(Container c : containers){
                if(c.free) continue;
                if(!working){ // Add another 0.2 if not idle
                    working = true;
                    battery.draw(0.2 * CPU);
                }
                battery.draw(Workload.UTIL * CPU);
                if(work(c)){
                    // Work completed
                    Statistics.functionCompleted();
                    c.evict();
                }
            }
        }catch(Exception e){
            // Not enough battery power
            online = false;
            Statistics.serverShutdown();

            // Send all functions waiting to the retry queue
            while(!queue.isEmpty()){
                if(!RetryQueue.add(queue.poll())) Statistics.functionFailed();
                Statistics.functionRetried();
            }

        }

    }













    // UTILITY FUNCTIONS

    public int getQueueSize(){
        return Math.min(this.queue.size(), fallbackNode != null ? fallbackNode.getQueueSize() : SolarSim.MAX_NODE_QUEUE_SIZE);
    }
    public double util(){
        int total = 0;
        for (Container c : containers) total += c.free ? 0 : 1;
        return ((total == 0 ? 0.2 : (total * Workload.UTIL) + 0.4));
    }


    public double getCPUEnergyDraw(){
        return util() * CPU;
    }

    public double getTotalAvailableEnergy(int time){
        return battery.getLevel();
    }

    public double getMetric(int time){
        double total = getTotalAvailableEnergy(time);
        double totalQueueTime = queue.size() * ((Workload.coldPenalty * 0.4) + Workload.executionTime);
        double totalQueueDraw = totalQueueTime * Workload.UTIL * CPU;
        total -= totalQueueDraw;

        return total;
    }

    public void dump(){
        System.out.println("Functions Received: " + functionsReceived);
    }
}
