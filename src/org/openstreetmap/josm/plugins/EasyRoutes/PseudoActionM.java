package org.openstreetmap.josm.plugins.EasyRoutes;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.tools.Geometry;

public class PseudoActionM{


    static void actionPerformed(Node n) {/*
    	boolean joinWayToNode = true;
            Collection<Node> selectedNodes = new LinkedList<>();
            selectedNodes.add(n);
            Collection<Command> cmds = new LinkedList<>();
            Map<Way, MultiMap<Integer, Node>> data = new HashMap<>();

            // If the user has selected some ways, only join the node to these.
            boolean restrictToSelectedWays = false;

            // Planning phase: decide where we'll insert the nodes and put it all in "data"
            for (Node node : selectedNodes) {
                List<WaySegment> wss = Main.map.mapView.getNearestWaySegments(
                        Main.map.mapView.getPoint(node), OsmPrimitive);

                MultiMap<Way, Integer> insertPoints = new MultiMap<>();
                for (WaySegment ws : wss) {
                    // Maybe cleaner to pass a "isSelected" predicate to getNearestWaySegments, but this is less invasive.
                    if (restrictToSelectedWays && !ws.way.isSelected()) {
                        continue;
                    }

                    if (!ws.getFirstNode().equals(node) && !ws.getSecondNode().equals(node)) {
                        insertPoints.put(ws.way, ws.lowerIndex);
                    }
                }
                for (Map.Entry<Way, Set<Integer>> entry : insertPoints.entrySet()) {
                    final Way w = entry.getKey();
                    final Set<Integer> insertPointsForWay = entry.getValue();
                    for (int i : pruneSuccs(insertPointsForWay)) {
                        MultiMap<Integer, Node> innerMap;
                        if (!data.containsKey(w)) {
                            innerMap = new MultiMap<>();
                        } else {
                            innerMap = data.get(w);
                        }
                        innerMap.put(i, node);
                        data.put(w, innerMap);
                    }
                }
            }

            // Execute phase: traverse the structure "data" and finally put the nodes into place
            for (Map.Entry<Way, MultiMap<Integer, Node>> entry : data.entrySet()) {
                final Way w = entry.getKey();
                final MultiMap<Integer, Node> innerEntry = entry.getValue();

                List<Integer> segmentIndexes = new LinkedList<>();
                segmentIndexes.addAll(innerEntry.keySet());
                Collections.sort(segmentIndexes, Collections.reverseOrder());

                List<Node> wayNodes = w.getNodes();
                for (Integer segmentIndex : segmentIndexes) {
                    final Set<Node> nodesInSegment = innerEntry.get(segmentIndex);
                    if (joinWayToNode) {
                        for (Node node : nodesInSegment) {
                            EastNorth newPosition = Geometry.closestPointToSegment(w.getNode(segmentIndex).getEastNorth(),
                                                                                w.getNode(segmentIndex+1).getEastNorth(),
                                                                                node.getEastNorth());
                            MoveCommand c = new MoveCommand(node, Projections.inverseProject(newPosition));
                            // Avoid moving a given node several times at the same position in case of overlapping ways
                            if (!cmds.contains(c)) {
                                cmds.add(c);
                            }
                        }
                    }
                    List<Node> nodesToAdd = new LinkedList<>();
                    nodesToAdd.addAll(nodesInSegment);
                    Collections.sort(nodesToAdd, new NodeDistanceToRefNodeComparator(
                            w.getNode(segmentIndex), w.getNode(segmentIndex+1), !joinWayToNode));
                    wayNodes.addAll(segmentIndex + 1, nodesToAdd);
                }
                Way wnew = new Way(w);
                wnew.setNodes(wayNodes);
                cmds.add(new ChangeCommand(w, wnew));
            }

            if (cmds.isEmpty()) return;
            Main.main.undoRedo.add(new SequenceCommand("Siejbik".toString(), cmds));
            Main.map.repaint();*/
        }

    private static SortedSet<Integer> pruneSuccs(Collection<Integer> is) {
        SortedSet<Integer> is2 = new TreeSet<>();
        for (int i : is) {
            if (!is2.contains(i - 1) && !is2.contains(i + 1)) {
                is2.add(i);
            }
        }
        return is2;
    }


    private static class NodeDistanceToRefNodeComparator implements Comparator<Node>, Serializable {

        private static final long serialVersionUID = 1L;

        private final EastNorth refPoint;
        private EastNorth refPoint2;
        private final boolean projectToSegment;

        NodeDistanceToRefNodeComparator(Node referenceNode, Node referenceNode2, boolean projectFirst) {
            refPoint = referenceNode.getEastNorth();
            refPoint2 = referenceNode2.getEastNorth();
            projectToSegment = projectFirst;
        }

        @Override
        public int compare(Node first, Node second) {
            EastNorth firstPosition = first.getEastNorth();
            EastNorth secondPosition = second.getEastNorth();

            if (projectToSegment) {
                firstPosition = Geometry.closestPointToSegment(refPoint, refPoint2, firstPosition);
                secondPosition = Geometry.closestPointToSegment(refPoint, refPoint2, secondPosition);
            }

            double distanceFirst = firstPosition.distance(refPoint);
            double distanceSecond = secondPosition.distance(refPoint);
            double difference =  distanceFirst - distanceSecond;

            if (difference > 0.0) return 1;
            if (difference < 0.0) return -1;
            return 0;
        }
    }
}