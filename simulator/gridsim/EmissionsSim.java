import java.util.*;

public class EmissionsSim {
    public static Random rand;

    // Used to keep backwards compatibility with old seeded runs
    public static Random fauxRandom;

    public static final int START_TIME = 0;
    // public static final int START_TIME = 0;

    public static final int DAYS = 365;
    public static final int MAX_NODE_QUEUE_SIZE = 100;

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





    public EmissionsSim(int time, Balancer balancer, int numNodes, long seed){
        TraceImporter.init("./traces/high-workload-function-trace.csv");

        this.balancer = balancer;
        
        if(time > 0) simTime = time;
        System.out.println("Initializing nodes...");
        for(int i = 0; i < numNodes; i++){
            Node newNode = new Node(new EmissionsProfile(i));
            nodes.add(newNode);
            System.out.println(newNode.toString());
        }

        rand = new Random(seed);
        fauxRandom = new Random(seed + 1);

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




    public ArrayList<Node> idealNodes(){
        // Returns a list of nodes with the ideal specifications:
        // - None for grid
        
        return nodes;
    }

    public boolean send(Workload w){

        Node chosenNode = null;

        ArrayList<Node> candidates = balancer.balance(idealNodes(), w);

        for(Node candidate : candidates){
            if((candidate.util() < 1.0 && candidate.queue.size() < MAX_NODE_QUEUE_SIZE) ){
                chosenNode = candidate;
                break;
            }
        }


        if(chosenNode == null){
            chosenNode = candidates.get( rand.nextInt( candidates.size() ));
        }

        double savedEmissions = calculateSavedEmissions(chosenNode, fauxBalancer(w, nodes), w, time);
        Statistics.savedEmissions(time, savedEmissions);

        chosenNode.invoke(w);
        Statistics.functionSent();
        return true;


    }


    private void step(){

 

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
                case "greedy":
                    choice = new GreedyBalancer();
                    break;
                case "consistent":
                    choice = new ConsistentHashing();
                    break;
            }
        }
        int numNodes = 18;
        if(args.length > 1){
            numNodes = Integer.parseInt(args[1]);
        }


        EmissionsSim sim = new EmissionsSim(-1, choice, numNodes, 5312998);
        sim.run();
        Statistics.dump();
    }







    // A fake openwhisk balancer used to 
    public Node fauxBalancer(Workload w, ArrayList<Node> nodes){
        int numNodes = nodes.size();
        int shift = w.hashCode() % numNodes;
        ArrayList<Node> shiftedNodes = new ArrayList<Node>();
        for (int i = 0; i < numNodes; i++) {
            shiftedNodes.add(nodes.get((i + shift) % numNodes));
        }
        
        for(int curr = 0; curr < shiftedNodes.size(); curr++){
            if(shiftedNodes.get(curr).util() < 1.0){
                return shiftedNodes.get(curr);
            }
        }

        return shiftedNodes.get(fauxRandom.nextInt(shiftedNodes.size()));
    }





    public static double calculateSavedEmissions(Node chosen, Node standard, Workload w, int time){
        double chosenMOER = chosen.getMOER(time, true);
        double standardMOER = standard.getMOER(time, true);
        double emissionsSaved = standardMOER - chosenMOER;
        return emissionsSaved;
    }
}