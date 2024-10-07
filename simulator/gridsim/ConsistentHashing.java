import java.util.*;
public class ConsistentHashing implements Balancer {

    private static ConsistentComparator comparator = new ConsistentComparator();

    public ArrayList<Node> balance(ArrayList<Node> nodes, Workload w){
        // Sort the nodes by their metric
        comparator.w = w;
        Collections.sort(nodes, comparator);
        return nodes;
    }


    @Override
    public String toString() {
        return "ConsistentHashing";
    }

}

// Create a comparator for the DataCenter class
class ConsistentComparator implements Comparator<Node> {
    public Workload w;

    @Override
    public int compare(Node a, Node b) {
        // Modulo each hash by 360 to get a value between 0 and 360
        int aHash = a.hashCode() % 1000;
        int bHash = b.hashCode() % 1000;
        int wHash = w.hashCode() % 1000;

        // The closest to w going clockwise should be first
        int aDist = Math.abs(aHash - wHash);
        int bDist = Math.abs(bHash - wHash);

        // aDist = (aDist < 0) ? 1000 + Math.abs(aDist) : aDist;
        // bDist = (bDist < 0) ? 1000 + Math.abs(bDist) : bDist;

        return aDist - bDist;
    }

}
