package org.openstreetmap.josm.plugins.EasyRoutes.NewRouting;

import javafx.util.Pair;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.EasyRoutes.CityEnviroment.RoutingPreferences;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.NodeConnectException;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.RoutingNode;

import java.util.*;

public class RoutingCalculator {
    private PriorityQueue<Pair<Double, Node> > queue = new PriorityQueue<>();
    private Set<Node> visited = new TreeSet<>();
    private Map<Node, Node> nodeTree = new HashMap<>();
    private RoutingPreferences routingPreferences;
    private Node start;
    private Node stop;

    public RoutingCalculator(Node n1, Node n2, RoutingPreferences routingPreferences) throws NodeConnectException {
        start = n1;
        stop = n2;
        this.routingPreferences = routingPreferences;
        visited.add(n1);
        queue.add(new Pair<>(0.0, n1));
        while(!visited.contains(n2)) {
            Pair<Double, Node> current = queue.poll();
            if(current == null) {
                throw new NodeConnectException();
            }
            visit(current.getKey(), current.getValue());
        }
    }

    private void visit(Way w, double entryVal, Node entryNode) {
        double ratio = routingPreferences.getWeight(w, true);
        double ratioReversed = routingPreferences.getWeight(w, false);
        if(ratio <= 0 && ratioReversed <= 0)
            return;
        //TODO
    }

    private void visit(double val, Node n) {
        if(visited.contains(n))
            return;
        for(Way w : n.getParentWays()) {
            visit(w, val, n);
        }
    }

    public List<Node> getPath() throws NodeConnectException {
        Node akt = stop;
        List<Node> ret = new ArrayList<>();
        while (nodeTree.containsKey(akt)) {
            ret.add(akt);
            akt = nodeTree.get(akt);
        }
        ret.add(akt);
        if (ret.size() < 2)
            throw new NodeConnectException();
        if (ret.get(0) != start)
            throw new NodeConnectException();
        if (ret.get(ret.size() - 1) != stop)
            throw new NodeConnectException();
        Collections.reverse(ret);
        return ret;
    }
}
