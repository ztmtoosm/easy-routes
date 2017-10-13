package org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm;

import java.util.List;
import java.util.Map;

public interface RoutingInterface {
    /**
     * Creates and returns RoutingNode. The node is already added to RoutingInterface base
     * @return new RoutingNode
     */
    public RoutingNode createNewNode();
    /**
     * Calculate path between given nodes
     * @return Path between given nodes as list of nodes along the path
     * First node on list is always first given node and last node on list is always second given node.
     * @throws NodeConnectException  If there is no path between given nodes
     */
    public List<RoutingNode> calculate(RoutingNode p, RoutingNode k) throws NodeConnectException;
    /**
     * @throws NodeConnectException  If there is no path between given nodes
     */
    public double calculateDistance(RoutingNode p, RoutingNode k) throws NodeConnectException;
}
