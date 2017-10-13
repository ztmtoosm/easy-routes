package org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm;

public class RoutingVertex {
	private RoutingNode start;
	private RoutingNode stop;
	private double weight;

	/**
	 * Creates and connects vertex between two nodes with a given weight
	 * @param start Node on begin of created vertex
	 * @return stop Node on end of created vertex
	 * @return weight Given weight
	 */
	public RoutingVertex(RoutingNode start, RoutingNode stop, double weight) {
		this.start = start;
		this.stop = stop;
		this.weight = weight;
		RoutingNode.addEdge(start, stop, this);
	}

	public double getWeight() {
		return weight;
	}

	/**
	 * Returns node on opposite side of vertex for given node
	 * @param node Some node on vertex
	 * @return  Node on opposite side of vertex
	 */
	public RoutingNode getNextNode(RoutingNode node) {
		if (node == start)
			return stop;
		return start;
	}
}
