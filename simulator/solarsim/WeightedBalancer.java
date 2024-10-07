
import java.util.*;


public class WeightedBalancer implements Balancer {

    private DataCenterComparator comparator = new DataCenterComparator();
    
    public static double height(double r, double s){
        return -(Math.log(mod1(1 - (r - s))));
    }
    
    public static double weight(Node node, double totalEnergy){
        return (node.getMetric(SolarSim.time)) / totalEnergy;
    }

    public static double metric(Node node, Workload w, double totalEnergy){
        return height(((double)(node.hashCode())) / Integer.MAX_VALUE, ((double)(w.hashCode())/Integer.MAX_VALUE))  / weight(node, totalEnergy);
    }

    public static double mod1(double x){
        return x - Math.floor(x);
    }

    public ArrayList<Node> balance(ArrayList<Node> nodes, Workload w){
        // Sort the nodes by their metric

        for (Node n : nodes) {
            comparator.totalEnergy += n.getMetric(SolarSim.time);
        }   

        comparator.w = w;
        comparator.nodes = nodes;
        Collections.sort(nodes, comparator);
        return nodes;
    }


    @Override
    public String toString() {
        return "WeightedConsistentHashing";
    }


    

}

// Create a comparator for the DataCenter class
class DataCenterComparator implements Comparator<Node> {
    public Workload w = null;
    public ArrayList<Node> nodes = null;
    public double totalEnergy = 0;

    @Override
    public int compare(Node a, Node b) {
        int result = (WeightedBalancer.metric(a, w, totalEnergy) - WeightedBalancer.metric(b, w, totalEnergy)) > 0 ? 1 : -1;
        return result;
    }
}