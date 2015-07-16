package org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class DijkstraData {
	List<RoutingNode> wezly;

	public DijkstraData() {
		wezly = new ArrayList<RoutingNode>();
	}

	public void add(RoutingNode w) {
		wezly.add(w);
	}

	public List<RoutingNode> calculate(RoutingNode p, RoutingNode k)
			throws NodeConnectException {
		for (RoutingNode w : wezly) {
			w.distanceToStart = RoutingNode.MAX_DISTANCE;
			w.isVisited = false;
		}
		Map<RoutingNode, RoutingNode> pop = new HashMap<RoutingNode, RoutingNode>();
		PriorityQueue<RoutingNode> kolejka = new PriorityQueue<RoutingNode>();
		p.odwiedzPierwszego(kolejka, pop);
		int licznik = 0;
		boolean ok = true;
		while (kolejka.size() > 0 && ok) {
			licznik++;
			RoutingNode x = kolejka.poll();
			x.odwiedz(kolejka, pop);
			if (x == k)
				ok = false;
		}
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

}
