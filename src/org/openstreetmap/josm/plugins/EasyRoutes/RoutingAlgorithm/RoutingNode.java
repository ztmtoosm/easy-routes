package org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class RoutingNode implements Comparable<RoutingNode> {
	static final double MAX_DISTANCE = 1000000.0;
	List<RoutingVertex> vertices;
	boolean isVisited;
	double distanceToStart;

	RoutingNode() {
		distanceToStart = MAX_DISTANCE;
		isVisited = false;
		vertices = new LinkedList<RoutingVertex>();
	}

	static void addEdge(RoutingNode start, RoutingNode stop,
						RoutingVertex edge) {
		start.vertices.add(edge);
	}

	double getDistance() {
		return distanceToStart;
	}

	void visitFirst(PriorityQueue<RoutingNode> queue,
			Map<RoutingNode, RoutingNode> visitedBefore) {
		distanceToStart = 0;
		visit(queue, visitedBefore);
	}


	void visit(PriorityQueue<RoutingNode> queue,
			Map<RoutingNode, RoutingNode> visitedBefore) {
		isVisited = true;
		for (RoutingVertex k : vertices) {
			RoutingNode nextNode = k.getNextNode(this);
			if (nextNode.distanceToStart > distanceToStart + k.getWeight()
					&& !nextNode.isVisited) {
				queue.remove(nextNode);
				nextNode.distanceToStart = distanceToStart + k.getWeight();
				queue.add(nextNode);
				visitedBefore.put(nextNode, this);
			}
		}
	}

	@Override
	public String toString() {
		String wynik = hashCode() + " " + this.isVisited + " ";
		for (RoutingVertex k : vertices) {
			wynik += k.getNextNode(this).hashCode() + ", ";
		}
		return wynik;
	}

	@Override
	public int compareTo(RoutingNode arg0) {
		return Double.compare(distanceToStart, arg0.distanceToStart);
	}
}
