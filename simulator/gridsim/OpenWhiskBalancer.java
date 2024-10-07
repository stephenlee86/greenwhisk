
import java.util.ArrayList;

public class OpenWhiskBalancer implements Balancer { 
    // The current implementation OpenWhisk uses to load balance workloads
    
    public ArrayList<Node> balance(ArrayList<Node> nodes, Workload w){
        // Shift the nodes array so that the first node is equal to nodes.get(w.getHash() % numNodes)

        int numNodes = nodes.size();
        if(numNodes == 0) return new ArrayList<Node>();
        int shift = w.hashCode() % numNodes;
        ArrayList<Node> shiftedNodes = new ArrayList<Node>();
        for (int i = 0; i < numNodes; i++) {
            shiftedNodes.add(nodes.get((i + shift) % numNodes));
        }

        return shiftedNodes;
    }

    
}
 