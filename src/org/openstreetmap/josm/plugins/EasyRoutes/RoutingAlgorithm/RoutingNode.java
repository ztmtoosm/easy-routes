package org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class RoutingNode implements Comparable<RoutingNode> {
	public static final double MAX_DISTANCE = 1000000.0;
	List<RoutingVertex> vertices;
	boolean isVisited;
	double distanceToStart;

	public RoutingNode() {
		distanceToStart = MAX_DISTANCE;
		isVisited = false;
		vertices = new LinkedList<RoutingVertex>();
	}

	static void dodajKrawedz(RoutingNode start, RoutingNode stop,
			RoutingVertex krawedz) {
		start.vertices.add(krawedz);
	}

	public double getOdleglosc() {
		return distanceToStart;
	}

	void odwiedzPierwszego(PriorityQueue<RoutingNode> kolejka,
			Map<RoutingNode, RoutingNode> poprzednie) {
		distanceToStart = 0;
		odwiedz(kolejka, poprzednie);
	}

	void odwiedz(PriorityQueue<RoutingNode> kolejka,
			Map<RoutingNode, RoutingNode> poprzednie) {
		isVisited = true;
		for (RoutingVertex k : vertices) {
			RoutingNode nextNode = k.getNextNode(this);
			if (nextNode.distanceToStart > distanceToStart + k.getWeight()
					&& !nextNode.isVisited) {
				kolejka.remove(nextNode);
				nextNode.distanceToStart = distanceToStart + k.getWeight();
				kolejka.add(nextNode);
				poprzednie.put(nextNode, this);
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
