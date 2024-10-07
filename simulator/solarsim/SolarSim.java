import java.util.*;

public class SolarSim {
    public static Random rand;

    // public static final int START_TIME = 04 * 31 * 24 * 60 * 60;
    public static final int START_TIME = 0;

    public static final int DAYS = 365;
    public static final int MAX_NODE_QUEUE_SIZE = 25;

    public ArrayList<Workload> workloads = new ArrayList<Workload>();

    public Balancer balancer;

    public String[] dataFiles = {
        "Data/32.475335_-97.010318_Solcast_PT10M.xlsx.csv",
        "Data/33.729022_-84.731556_Solcast_PT10M.xlsx.csv",
        "Data/36.039525_-114.981721_Solcast_PT10M.xlsx.csv",
        "Data/36.232243_-95.31025_Solcast_PT10M.xlsx.csv",
        "Data/36.496041_-87.460397_Solcast_PT10M.xlsx.csv",
        "Data/39.484793_-119.557168_Solcast_PT10M.xlsx.csv",
        "Data/40.08216_-82.810092_Solcast_PT10M.xlsx.csv",
        "Data/41.154526_-96.043024_Solcast_PT10M.xlsx.csv",
        "Data/45.594564_-121.178682_Solcast_PT10M.xlsx.csv"
    };



    public ArrayList<Node> nodes = new ArrayList<Node>();
    public int simTime = (60 * 60 * 24 * DAYS) + START_TIME;
    public static int time = START_TIME;





    public SolarSim(int time, Balancer balancer, int numNodes, int depth, long seed){
        TraceImporter.init("./traces/high-workload-function-trace.csv");
        Sizer.init("misc/solar-sizing-server.csv");

        this.balancer = balancer;

        if(time > 0) simTime = time;
        for(int i = 0; i < numNodes; i++){
            Node curr = new Node(new SolarProfile(dataFiles[i % dataFiles.length], depth));
            nodes.add(curr);
            for(int j = 0; j < depth; j++){
                curr.fallbackNode = new Node(curr.profile);
                curr = curr.fallbackNode;
            }
        }

        rand = new Random(seed);

        for(int i = 0; i < TraceImporter.numWorkloads(); i++){
            workloads.add(new Workload());
        }
    }

    public void run(){
        int lastPercent = 0;
        while(time < simTime){
            int percent = (int)(((float)(time - START_TIME) / (simTime - START_TIME)) * 100);
            if(percent != lastPercent){
                System.out.println("Progress: " + percent + "%");
                lastPercent = percent;
            }
            step();
            time++;
        }
    }

    public void retryWorkload(Workload w){
        if(!RetryQueue.add(w)) Statistics.functionFailed();
        Statistics.functionRetried();
    }

    public ArrayList<Node> onlineNodes(){
        ArrayList<Node> online = new ArrayList<Node>();
        for(Node n : nodes){
            if(n.online) online.add(n);
        }
        return online;
    }

    public ArrayList<Node> idealNodes(){
        // Returns a list of nodes with the ideal specifications:
        // - >20% battery life
        

        ArrayList<Node> ideal = new ArrayList<Node>();
        ArrayList<Node> online = onlineNodes();



        for(Node n : online){
            if(! n.battery.isCritical()) ideal.add(n);
        }
        
        return ideal;
    }

    public boolean send(Workload w){

        Node chosenNode = null;

        // // If we need to, wake nodes up to handle additional workloads
        // ArrayList<Node> ideal = idealNodes();
        // while(ideal.isEmpty() && wakeNode()){
        //     ideal = idealNodes();
        // }

        ArrayList<Node> candidates = balancer.balance(nodes, w);

        // if(candidates.isEmpty()){
        //     candidates = balancer.balance(nodes, w);
        // }

        // if(candidates.isEmpty()){
        //     if(!RetryQueue.add(w)) Statistics.functionFailed();
        //     Statistics.functionRetried();
        //     return false;
        // }

        for(Node candidate : candidates){
            if( candidate.getQueueSize() < MAX_NODE_QUEUE_SIZE && ! candidate.battery.isCritical()) {
                chosenNode = candidate;
                break;
            }
        }

        if(chosenNode == null){
            // chosenNode = candidates.get( rand.nextInt( candidates.size() ));
            RetryQueue.add(w);
            Statistics.functionRetried();
            return false;
        }

        chosenNode.invoke(w);
        Statistics.functionSent();
        return true;


    }



    private void step(){
        // Try to send all workloads in the retry queue
        while(!RetryQueue.isEmpty()){
            if(!send(RetryQueue.next())) break;
        }
 

        // Send all the new workloads
        for(Workload w : workloads){
            int invocations = w.numberToInvoke(time);
            for(int i = 0; i < invocations; i++){
                if(!send(w)) break;
            }
        }

        // Tick all the nodes
        for(Node n : nodes){
            n.tick(time);
        }



    }




    public static void main(String[] args){
        Balancer choice = new OpenWhiskBalancer();
        
        if(args.length > 0){
            switch(args[0]){
                case "weighted":
                    choice = new WeightedBalancer();
                    break;
            }
        }


        SolarSim sim = new SolarSim(-1, choice, 9, 1, 5312998);
        sim.run();
        Statistics.dump();
    }
}