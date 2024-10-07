import java.util.*;

public class GreedyBalancer implements Balancer {

    private static GreedyComparator comparator = new GreedyComparator();
    
    public ArrayList<Node> balance(ArrayList<Node> nodes, Workload w){
        // Sort the nodes by their metric
        Collections.sort(nodes, comparator);
        return nodes;
    }


    @Override
    public String toString() {
        return "Greedy";
    }

}

// Create a comparator for the DataCenter class
class GreedyComparator implements Comparator<Node> {
    @Override
    public int compare(Node a, Node b) {
        return (((a.getMetric(EmissionsSim.time)) ) - (b.getMetric(EmissionsSim.time))) > 0 ? 1 : -1;
    }
}