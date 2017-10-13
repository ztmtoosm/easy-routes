package org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Implementation of routing interface with Dijkstra algorithm.
 */
public class DijkstraData implements RoutingInterface {
	List<RoutingNode> nodes;
	public DijkstraData() {
		nodes = new ArrayList<RoutingNode>();
	}

	@Override
	public RoutingNode createNewNode() {
		RoutingNode w = new RoutingNode();
		nodes.add(w);
		return w;
	}

	Map<RoutingNode, RoutingNode> calculateCore(RoutingNode p, RoutingNode k) {
		for (RoutingNode w : nodes) {
			w.distanceToStart = RoutingNode.MAX_DISTANCE;
			w.isVisited = false;
		}
		Map<RoutingNode, RoutingNode> pop = new HashMap<RoutingNode, RoutingNode>();
		PriorityQueue<RoutingNode> kolejka = new PriorityQueue<RoutingNode>();
		p.visitFirst(kolejka, pop);
		boolean ok = true;
		while (kolejka.size() > 0 && ok) {
			RoutingNode x = kolejka.poll();
			x.visit(kolejka, pop);
			if (x == k)
				ok = false;
		}
		return pop;
	}

	@Override
	public List<RoutingNode> calculate(RoutingNode p, RoutingNode k)
			throws NodeConnectException {
		Map<RoutingNode, RoutingNode> pop = calculateCore(p,k);
		RoutingNode akt = k;
		List<RoutingNode> wynik = new ArrayList<RoutingNode>();
		while (pop.containsKey(akt)) {
			wynik.add(akt);
			akt = pop.get(akt);
		}
		wynik.add(akt);
		if (wynik.size() < 2)
			throw new NodeConnectException();
		if (wynik.get(0) != k)
			throw new NodeConnectException();
		if (wynik.get(wynik.size() - 1) != p)
			throw new NodeConnectException();
		Collections.reverse(wynik);
		return wynik;
	}

	@Override
	public double calculateDistance(RoutingNode p, RoutingNode k) throws NodeConnectException {
		calculateCore(p,k);
		if(k.getDistance()==RoutingNode.MAX_DISTANCE)
			throw new NodeConnectException();
		return k.getDistance();
	}
}
