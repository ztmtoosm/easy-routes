package org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm;

public class RoutingVertex {
	private RoutingNode start;
	private RoutingNode stop;
	private double weight;

	public RoutingVertex(RoutingNode start, RoutingNode stop, double weight) {
		this.start = start;
		this.stop = stop;
		this.weight = weight;
		RoutingNode.addEdge(start, stop, this);
	}

	public double getWeight() {
		return weight;
	}

	public RoutingNode getNextNode(RoutingNode wezel) {
		if (wezel == start)
			return stop;
		return start;
	}
}
