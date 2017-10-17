package org.openstreetmap.josm.plugins.EasyRoutes.NewRouting;

import javafx.util.Pair;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.SplitWayAction;
import org.openstreetmap.josm.command.SplitWayCommand;
import org.openstreetmap.josm.data.SystemOfMeasurement;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.widgets.MultiSplitLayout;
import org.openstreetmap.josm.plugins.EasyRoutes.CityEnviroment.RoutingPreferences;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.NodeConnectException;

import java.util.*;

public class WaySplitter {
    private List<Node> allNodes = new ArrayList<>();
    private List<Way> allWays = new ArrayList<>();
    private Map<Way, List<Node> > wayMap = new HashMap<>();
    private List<Pair<Way, List<Node>>> oneWayChunks = new ArrayList<>();
    private void filterSplitPoints(Way w, List <Node> nodes) throws NodeConnectException {
        List<Node> ret = new ArrayList<>();
        ret.add(nodes.get(0));
        ret.add(nodes.get(nodes.size() - 1));
        List<Integer> x = new ArrayList<>();
        int i = 0;
        int pos = 0;
        int j = 0;
        while (j < w.getNodes().size() + nodes.size() && i < nodes.size() && pos < w.getNodes().size()) {
            if (w.getNodes().get(pos) == nodes.get(i)) {
                x.add(pos);
                i++;
            }
            if ((i) < nodes.size() && pos > 0) {
                if (nodes.get(i) == w.getNodes().get(pos - 1))
                    pos--;
                else
                    pos++;
            }
            else {
                pos++;
            }
            j++;
        }
        if (x.size() != nodes.size()) {
            throw new NodeConnectException();
        }
        int currentDiff = 0;
        for(i=1; i<x.size(); i++) {
            int diff = x.get(i) - x.get(i-1);
            if(Math.abs(diff) > 1 || currentDiff * diff < 0) {
                ret.add(nodes.get(i));
                ret.add(nodes.get(i-1));
            }
            currentDiff = diff;
        }
        if(wayMap.get(w) == null) {
            wayMap.put(w, ret);
        }
        else {
            wayMap.get(w).addAll(ret);
        }
    }

    private void computeNextCalculator(RoutingCalculator cl) throws NodeConnectException{
        List<Node> nodes = cl.getPath();
        List<Way> ways = cl.getWays();
        if(allNodes.size() == 0) {
            allNodes.add(nodes.get(0));
        }
        allNodes.addAll(nodes.subList(1, nodes.size()));
        allWays.addAll(ways);
    }

    private void doSplit() throws NodeConnectException {
        for(Pair<Way, List<Node>> x : oneWayChunks) {
            filterSplitPoints(x.getKey(), x.getValue());
        }
        for(Map.Entry<Way, List<Node> > entry : wayMap.entrySet()) {
            SplitWayCommand sc = SplitWayCommand.split(entry.getKey(), entry.getValue(), new ArrayList<>());
            UndoRedoHandler ur = new UndoRedoHandler();
            if(sc != null)
                ur.add(sc);
        }
    }

    private WaySplitter(List<Node> waysToSplit, RoutingPreferences preferences) throws NodeConnectException {
        List <RoutingCalculator> calculators = new ArrayList<>();
        for(int i=0; i<waysToSplit.size()-1; i++) {
            computeNextCalculator(new RoutingCalculator(waysToSplit.get(i), waysToSplit.get(i+1), preferences));
        }
        if(allWays.size() != allNodes.size()-1)
            throw new NodeConnectException();

        Way currentWay = null;
        List<Node> tmpNode = new ArrayList<>();
        for(int i=0; i<allWays.size(); i++) {
            if(currentWay != allWays.get(i)) {
                oneWayChunks.add(new Pair<>(currentWay, tmpNode));
                tmpNode = new ArrayList<>();
                tmpNode.add(allNodes.get(i));
                currentWay = allWays.get(i);
            }
            tmpNode.add(allNodes.get(i+1));
        }
        oneWayChunks.add(new Pair<>(currentWay, tmpNode));
        oneWayChunks = oneWayChunks.subList(1, oneWayChunks.size());
    }

    private void checkWaySplit(Way w, List<Node> x) throws SplitException {
        if(x.size() != w.getNodes().size()) {
            throw new SplitException();
        }
        int ok = 0;
        for(int i=0; i<x.size(); i++) {
            if(x.get(i) == w.getNodes().get(i))
                ok++;
        }
        if(ok == x.size()) {
            return;
        }
        ok = 0;
        for(int i=0; i<x.size(); i++) {
            if(x.get(x.size()-i-1) == w.getNodes().get(i))
                ok++;
        }
        if(ok == x.size()) {
            return;
        }
        throw new SplitException();
    }

    private List<Pair<Way, String>> checkWaySplit() throws SplitException {
        List<Pair<Way, String>> ret = new ArrayList<>();
        for(Pair<Way, List<Node>> x : oneWayChunks) {
            checkWaySplit(x.getKey(), x.getValue());
            ret.add(new Pair<>(x.getKey(), ""));
        }
        return ret;
    }

    public static void splitWays(List<Node> waysToSplit, RoutingPreferences preferences) throws NodeConnectException {
        WaySplitter x = new WaySplitter(waysToSplit, preferences);
        x.doSplit();
    }
    public static List <Pair<Way, String>> getWaysAfterSplit(List<Node> waysToSplit, RoutingPreferences preferences) throws NodeConnectException, SplitException {
        WaySplitter x = new WaySplitter(waysToSplit, preferences);
        return x.checkWaySplit();
    }

    public static Collection<Node> getAllConnectedNodes(DataSet ds, RoutingPreferences preferences) {
        Set<Node> lNodes = new HashSet<>();
        for(Node n : ds.getNodes()) {
            boolean ok = false;
            for(Way w : n.getParentWays()) {
                ok = preferences.getWeight(w, false) > 0 ? true : ok;
                ok = preferences.getWeight(w, true) > 0 ? true : ok;
            }
            if(ok == true) lNodes.add(n);
        }
        return lNodes;
    }
}
