import java.util.ArrayList;

public interface Balancer {
    public ArrayList<Node> balance(ArrayList<Node> availableNodes, Workload w);
}
