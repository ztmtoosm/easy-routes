package org.openstreetmap.josm.plugins.EasyRoutes.Routing;

import javafx.util.Pair;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.EasyRoutes.CityEnviroment.RoutingPreferences;
import org.openstreetmap.josm.plugins.EasyRoutes.NewRouting.RoutingCalculator;
import org.openstreetmap.josm.plugins.EasyRoutes.NewRouting.SplitException;
import org.openstreetmap.josm.plugins.EasyRoutes.NewRouting.WaySplitter;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.NodeConnectException;

import java.util.*;

public class RoutingBis {
    List<Node> crucialNodes;
    List<List <Node> > middleNodes;
    private RoutingLayer layer;
    private RoutingPreferences preferences;
    DataSet dataSet;
    public RoutingBis(DataSet ds, RoutingPreferences preferences, List<Node> crucialNodes, String description, boolean gui) {
        this.crucialNodes = crucialNodes;
        this.preferences = preferences;
        dataSet = ds;
        if(gui) {
            layer = new RoutingLayer(description, this);
            Main.getLayerManager().addLayer(layer);
        }
    }

    public RoutingLayer getRoutingLayer() {
        return layer;
    }

    public void drag(Node nowy, Node stary) {
        int z = this.crucialNodes.indexOf(stary);
        this.crucialNodes.set(z, nowy);
        if(this.crucialNodes.size()>(z+1)) {
            Node left = crucialNodes.get(z);
            Node right = crucialNodes.get(z+1);
            try {
                middleNodes.set(z, new RoutingCalculator(left, right, preferences).getPath());
            } catch (NodeConnectException e) {
                middleNodes.set(z, null);
            }
        }
        if(z != 0) {
            Node left = crucialNodes.get(z-1);
            Node right = crucialNodes.get(z);
            List <Node> tmp = new ArrayList<>();
            tmp.add(left);
            tmp.add(right);
            try {
                middleNodes.set(z-1, new RoutingCalculator(left, right, preferences).getPath());
            } catch (NodeConnectException e) {
                middleNodes.set(z-1, null);
            }
        }
    }

    private int getMiddleNodeId(Node n) {
        boolean containsAll = false;
        for(int i=0; i<middleNodes.size(); i++) {
            if(middleNodes.get(i)!=null && middleNodes.get(i).contains(n))
                containsAll = true;
        }
        if(!containsAll)
            return -1;
        if(crucialNodes.contains(n))
            return -1;
        for(int i=0; i<middleNodes.size(); i++) {
            if(middleNodes.get(i)!=null && middleNodes.get(i).contains(n))
                return i+1;
        }
        return -1;
    }

    public void addMiddleNode(Node n) {
        int id = getMiddleNodeId(n);
        if(id==-1)
            return;
        List <Node> newCrucial = new ArrayList<>();
        List <List<Node> > newMiddle = new ArrayList<>();
        for(int i=0; i<id; i++) {
            newCrucial.add(crucialNodes.get(i));
            if(i<(id-1)) {
                newMiddle.add(middleNodes.get(i));
            }
        }
        List <Node> partial1 = middleNodes.get(id-1).subList(0, middleNodes.get(id-1).indexOf(n)+1);
        List <Node> partial2 = middleNodes.get(id-1).subList(middleNodes.get(id-1).indexOf(n), middleNodes.get(id-1).size());
        newMiddle.add(partial1);
        newMiddle.add(partial2);
        newCrucial.add(n);
        for(int i=id; i<crucialNodes.size(); i++) {
            newCrucial.add(crucialNodes.get(i));
            if(i<crucialNodes.size()-1)
                newMiddle.add(middleNodes.get(i));
        }
        crucialNodes = newCrucial;
        middleNodes = newMiddle;
    }

    public List<List<Node>> getMiddleNodes() {
        return middleNodes;
    }
    public Collection<Node> getAllNodes() {
        if(middleNodes == null)
            return null;
        Set<Node> ret = new TreeSet<>();
        for(int i=0; i<middleNodes.size(); i++) {
            List <Node> mid = middleNodes.get(i);
            if(mid != null)
                for(int j=0; j<mid.size(); j++) {
                    Node akt = mid.get(j);
                    ret.add(akt);
                }
        }
        return ret;
    }

    public void refresh() {
        middleNodes = new ArrayList<>();
        boolean cale = true;
        for(int i=0; i<crucialNodes.size()-1; i++) {
            Node left = crucialNodes.get(i);
            Node right = crucialNodes.get(i+1);
            try {
                middleNodes.add(new RoutingCalculator(left, right, preferences).getPath());
            } catch (NodeConnectException e) {
                middleNodes.add(null);
                cale = false;
            }
        }
        layer.refresh(cale);
    }

    public List<Pair<Way, String>> getWaysAfterSplit() throws NodeConnectException, SplitException {
        return WaySplitter.getWaysAfterSplit(crucialNodes, preferences);
    }
    public void splitWays() throws NodeConnectException {
        WaySplitter.splitWays(crucialNodes, preferences);
    }

    public void eraseLayer() {
        if (layer == null)
            return;
        Main.getLayerManager().removeLayer(layer);
    }

    Node getClosestPoint(LatLon akt) {
        double wynik = 1000000.0;
        Node wyn = null;
        for(Node n : WaySplitter.getAllConnectedNodes(dataSet, preferences)) {
            double dist = n.getCoor().greatCircleDistance(akt);
            if(dist<wynik) {
                wynik=dist;
                wyn = n;
            }
        }
        return wyn;
    }
}