package org.openstreetmap.josm.plugins.EasyRoutes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.SplitWayAction;
import org.openstreetmap.josm.actions.SplitWayAction.SplitWayResult;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.DijkstraData;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.NodeConnectException;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.RoutingNode;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.RoutingVertex;
import org.openstreetmap.josm.tools.Pair;

public class WaySplitter {
	private Collection<Collection<String>> aktPreferences;
	DijkstraData dd;
	Map<Pair<Node, Node>, Way> connections;
	Set <Node> conNodes;
	public WaySplitter(Collection<Collection<String>> aktPreferences) {
		this.aktPreferences = aktPreferences;
	}
	Node getClosestPoint(LatLon akt) {
		if(conNodes == null)
			return null;
		double wynik = 1000000.0;
		Node wyn = null;
		for(Node n : conNodes) {
			double dist = n.getCoor().greatCircleDistance(akt);
			if(dist<wynik) {
				wynik=dist;
				wyn = n;
			}
		}
		return wyn;
	}
	private double getWeight(Way w, boolean isNormal) {
		double value = -1;
		if (aktPreferences == null)
			return 1;
		for (Collection<String> foo : aktPreferences) {
			Map<String, String> map = w.getKeys();
			String[] ar = new String[foo.size()];
			int i = 0;
			for (String xxx : foo) {
				if (xxx == null)
					ar[i] = "";
				else
					ar[i] = xxx;
				i++;
			}
			String key = ar[0];
			String kv = ar[1];
			double wei = 0;
			if (ar[3].equals(""))
				wei = 0;
			else
				wei = Double.valueOf(ar[3]);
			if (isNormal) {
				if (ar[2].equals(""))
					wei = 0;
				else
					wei = Double.valueOf(ar[2]);
			}
			if (!key.equals("")) {
				if (map.containsKey(key)) {
					if (kv.equals("") || map.get(key).equals(kv)) {
						if (wei != 0) {
							value = wei;
						}
					}
				}
			}
		}
		return value;
	}

	private List<Node> completeNetwork(List<Node> middleNodes, DataSet dataSet, boolean newDData)
			throws NodeConnectException {
		conNodes = new TreeSet <Node>();
		connections = new HashMap<Pair<Node, Node>, Way>();
		Collection<Node> nodes = dataSet.getNodes();
		Collection<Way> ways = dataSet.getWays();
		Map<Node, RoutingNode> pr1 = new HashMap<Node, RoutingNode>();
		Map<RoutingNode, Node> pr2 = new HashMap<RoutingNode, Node>();
		dd = new DijkstraData();
		for (Node n : nodes) {
			RoutingNode nowy = new RoutingNode();
			pr1.put(n, nowy);
			pr2.put(nowy, n);
			dd.add(nowy);
		}
		for (Way w : ways) {
			if (w != null) {
				List<Node> ll1 = w.getNodes();
				double waga = getWeight(w, true);
				double waga_rev = getWeight(w, false);
				if (waga > 0 || waga_rev > 0) {
					for (int i = 0; i < ll1.size() - 1; i++) {
						Node a1 = ll1.get(i);
						Node a2 = ll1.get(i + 1);
						LatLon coor1 = a1.getCoor();
						LatLon coor2 = a2.getCoor();
						double len = coor1.greatCircleDistance(coor2) * waga;
						double len_rev = coor1.greatCircleDistance(coor2)
								* waga_rev;
						if (waga > 0) {
							conNodes.add(a1);
							conNodes.add(a2);
							RoutingVertex nowa = new RoutingVertex(pr1.get(a1),
									pr1.get(a2), len);
						}
						if (waga_rev > 0) {
							conNodes.add(a1);
							conNodes.add(a2);
							RoutingVertex nowa = new RoutingVertex(pr1.get(a2),
									pr1.get(a1), len_rev);
						}
						Pair<Node, Node> p1 = Pair.create(a1, a2);
						connections.put(p1, w);
					}
				}
			}
		}
		List<RoutingNode> wezly = new ArrayList<RoutingNode>();
		for (int i = 0; i < middleNodes.size() - 1; i++) {
			List<RoutingNode> wezlyTmp = dd.calculate(
					pr1.get(middleNodes.get(i)),
					pr1.get(middleNodes.get(i + 1)));
			if (i > 0)
				wezlyTmp = wezlyTmp.subList(1, wezlyTmp.size());
			wezly.addAll(wezlyTmp);
		}
		List<Node> wynik = new ArrayList<Node>();
		for (int i = 0; i < wezly.size(); i++) {
			wynik.add(pr2.get(wezly.get(i)));
		}
		return wynik;
	}

	public List<Node> completeNetwork(List<Node> middleNodes,
			boolean newDData) throws NodeConnectException {
		return completeNetwork(middleNodes, Main.main.getCurrentDataSet(), newDData);
	}

	private void dodajWierzcholek(Map<Way, Collection<Node>> grenzeNodes,
			Way id, Node n) {
		Way akt = id;
		if (n == akt.firstNode() || n == akt.lastNode())
			if (akt.firstNode() != akt.lastNode())
				return;
		if (grenzeNodes.containsKey(id)) {
			Collection<Node> nowa = grenzeNodes.get(id);
			nowa.add(n);
			grenzeNodes.put(id, nowa);
		} else {
			Collection<Node> nowa = new TreeSet<Node>();
			nowa.add(n);
			grenzeNodes.put(id, nowa);
		}
	}

	public void splitWays(List<Node> middleNodes, DataSet dataSet)
			throws NodeConnectException {
List<Node> nodes = completeNetwork(middleNodes, dataSet,
				true);
		Way aktWId = null;
		int startNId = 0;
		Map<Way, Collection<Node>> grenzeNodes = new HashMap<Way, Collection<Node>>();
		for (int i = 0; i < nodes.size(); i++) {
			Node n = nodes.get(i);
			if (i > 0) {
				Node nprim = nodes.get(i - 1);
				Pair<Node, Node> p1 = Pair.create(n, nprim);
				Pair<Node, Node> p2 = Pair.create(nprim, n);
				Way wId = null;
				if (connections.containsKey(p1))
					wId = connections.get(p1);
				else if (connections.containsKey(p2))
					wId = connections.get(p2);
				if (aktWId == null) {
					aktWId = wId;
				} else if (wId != aktWId) {
					dodajWierzcholek(grenzeNodes, aktWId, nodes.get(i - 1));
					dodajWierzcholek(grenzeNodes, aktWId, nodes.get(startNId));
					aktWId = wId;
					startNId = i - 1;
				}
			}
		}
		if (nodes.size() > 0) {
			dodajWierzcholek(grenzeNodes, aktWId, nodes.get(nodes.size() - 1));
			dodajWierzcholek(grenzeNodes, aktWId, nodes.get(startNId));
		}
		Set<Entry<Way, Collection<Node>>> sss = grenzeNodes.entrySet();
		for (Entry<Way, Collection<Node>> e1 : sss) {
			Way akt = e1.getKey();
			// (Way)getCurrentDataSet().getPrimitiveById(e1.getKey(),
			// OsmPrimitiveType.WAY);
			if (akt != null) {
				List<Node> ll = new ArrayList(Arrays.asList(e1.getValue()
						.toArray()));
				List<List<Node>> chunks = SplitWayAction.buildSplitChunks(akt,
						ll);
				if (e1.getValue().size() > 0) {
					SplitWayResult sp = SplitWayAction.splitWay(
							Main.map.mapView.getEditLayer(), akt, chunks, ll);
					Main.main.undoRedo.add(sp.getCommand());

				}
			}
		}
	}

	public List<Way> getWaysAfterSplit(List<Node> middleNodes, DataSet dataSet)
			throws NodeConnectException {
		connections = new HashMap<Pair<Node, Node>, Way>();
		List<Node> nodes = completeNetwork(middleNodes, dataSet,
				true);
		Way aktWId = null;
		List<Way> wynik = new ArrayList<Way>();
		for (int i = 0; i < nodes.size(); i++) {
			Node n = nodes.get(i);
			if (i > 0) {
				Node nprim = nodes.get(i - 1);
				Pair<Node, Node> p1 = Pair.create(n, nprim);
				Pair<Node, Node> p2 = Pair.create(nprim, n);
				Way wId = null;
				if (connections.containsKey(p1))
					wId = connections.get(p1);
				else if (connections.containsKey(p2))
					wId = connections.get(p2);
				if (aktWId == null) {
					aktWId = wId;
				} else if (wId != aktWId) {
					wynik.add(aktWId);
					aktWId = wId;
				}
			}
		}
		if (nodes.size() > 0) {
			wynik.add(aktWId);
		}
		return wynik;
	}
}
