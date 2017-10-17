package org.openstreetmap.josm.plugins.EasyRoutes.NewRouting;

import javafx.util.Pair;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.EasyRoutes.CityEnviroment.RoutingPreferences;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.NodeConnectException;

import java.util.*;

public class RoutingCalculator {

    class QueueTuple implements Comparable<QueueTuple> {
        Node current;
        Node before;
        double val;
        Way w;
        QueueTuple(Node current, Node before, double val, Way w) {
            this.current = current;
            this.before = before;
            this.val = val;
            this.w = w;
        }
        public int compareTo(QueueTuple arg0) {
            return Double.compare(val, arg0.val);
        }
    }

    private PriorityQueue<QueueTuple> queue = new PriorityQueue<>();
    private Set<Node> visited = new TreeSet<>();
    private Map<Node, QueueTuple> nodeTree = new HashMap<>();
    private RoutingPreferences routingPreferences;
    private Node start;
    private Node stop;

    public RoutingCalculator(Node n1, Node n2, RoutingPreferences routingPreferences) throws NodeConnectException {
        start = n1;
        stop = n2;
        this.routingPreferences = routingPreferences;
        queue.add(new QueueTuple(n1, null, 0.0, null));
        while(!visited.contains(n2)) {
            QueueTuple current = queue.poll();
            if(current == null) {
                throw new NodeConnectException();
            }
            visit(current);
        }
    }

    private void visit(Way w, double entryVal, Node entryNode) {
        double ratio = routingPreferences.getWeight(w, true);
        double ratioReversed = routingPreferences.getWeight(w, false);
        if(ratio <= 0 && ratioReversed <= 0)
            return;
        List <Node> nodeList = w.getNodes();

        boolean entryNodeRecived = false;
        double distanceFromEntryNode = 0;
        if(ratio > 0) {
            for (int i = 0; i < nodeList.size(); i++) {
                if(nodeList.get(i) == entryNode) {
                    entryNodeRecived = true;
                    distanceFromEntryNode = 0;
                }
                else {
                    if(entryNodeRecived) {
                        LatLon coor1 = nodeList.get(i-1).getCoor();
                        LatLon coor2 = nodeList.get(i).getCoor();
                        distanceFromEntryNode += coor1.greatCircleDistance(coor2) * ratio;
                        queue.add(new QueueTuple(nodeList.get(i), nodeList.get(i-1), entryVal + distanceFromEntryNode, w));
                    }
                }
            }
        }
        entryNodeRecived = false;
        distanceFromEntryNode = 0;
        if(ratioReversed > 0) {
            for (int i = nodeList.size()-1; i >= 0; i--) {
                if(nodeList.get(i) == entryNode) {
                    entryNodeRecived = true;
                    distanceFromEntryNode = 0;
                }
                else {
                    if(entryNodeRecived) {
                        LatLon coor1 = nodeList.get(i+1).getCoor();
                        LatLon coor2 = nodeList.get(i).getCoor();
                        distanceFromEntryNode += coor1.greatCircleDistance(coor2) * ratioReversed;
                        queue.add(new QueueTuple(nodeList.get(i), nodeList.get(i+1), entryVal + distanceFromEntryNode, w));
                    }
                }
            }
        }
    }

    private void visit(QueueTuple q) {
        if(visited.contains(q.current))
            return;
        visited.add(q.current);
        nodeTree.put(q.current, q);
        for(Way w : q.current.getParentWays()) {
            visit(w, q.val, q.current);
        }
    }

    public List<Node> getPath() throws NodeConnectException {
        Node akt = stop;
        List<Node> ret = new ArrayList<>();
        while (nodeTree.containsKey(akt)) {
            ret.add(akt);
            akt = nodeTree.get(akt).before;
        }
        if (ret.size() < 2) {
            throw new NodeConnectException();
        }
        if (ret.get(0) != stop) {
            throw new NodeConnectException();
        }
        if (ret.get(ret.size() - 1) != start) {
            throw new NodeConnectException();
        }
        Collections.reverse(ret);
        return ret;
    }
    public List<Way> getWays() throws NodeConnectException {
        List <Node> path = getPath();
        List <Way> ret = new ArrayList<>();
        for(int i=1; i<path.size(); i++) {
            ret.add(nodeTree.get(path.get(i)).w);
        }
        return ret;
    }
}