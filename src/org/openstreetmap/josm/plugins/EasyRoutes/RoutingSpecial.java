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
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.data.osm.event.NodeMovedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesAddedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesRemovedEvent;
import org.openstreetmap.josm.data.osm.event.RelationMembersChangedEvent;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.DijkstraData;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.NodeConnectException;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.RoutingNode;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.RoutingVertex;
import org.openstreetmap.josm.tools.Pair;

public class RoutingSpecial implements DataSetListener  {
	private Collection<Collection<String> > aktPreferences;
	private DijkstraData dijkstraData;
	private Map<Pair<Node, Node>, Way> connections;
	private Set <Node> connectedNodes;
	private Map<Node, RoutingNode> osmNodeToRoutingNode;
	private Map<RoutingNode, Node> routingNodeToOsmNode;
	private DataSet ds = null;
	public RoutingSpecial(Collection<Collection<String>> aktPreferences) {
		this.aktPreferences = aktPreferences;
		Main.main.getCurrentDataSet().addDataSetListener(this);
		ds = Main.main.getCurrentDataSet();
	}
	public DataSet getDataSet() {
		return ds;
	}
	int licznik=0;
	private void updateAllData() {
		licznik++;
		System.out.println("UPDATE ALL DATA "+licznik+" "+Main.main.getCurrentDataSet()+" "+ds);
		DataSet dataSet = Main.main.getCurrentDataSet();
			connectedNodes = new TreeSet <Node>();
			connections = new HashMap<Pair<Node, Node>, Way>();
			Collection<Node> nodes = dataSet.getNodes();
			Collection<Way> ways = dataSet.getWays();
			osmNodeToRoutingNode = new HashMap<Node, RoutingNode>();
			routingNodeToOsmNode = new HashMap<RoutingNode, Node>();
			dijkstraData = new DijkstraData();
			for (Node n : nodes) {
				RoutingNode nowy = new RoutingNode();
				osmNodeToRoutingNode.put(n, nowy);
				routingNodeToOsmNode.put(nowy, n);
				dijkstraData.add(nowy);
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
							if(a1!=null && a2!=null)
							{
								LatLon coor1 = a1.getCoor();
								LatLon coor2 = a2.getCoor();
								if(coor1!=null && coor2!=null)
								{
									double len = coor1.greatCircleDistance(coor2) * waga;
									double len_rev = coor1.greatCircleDistance(coor2)
											* waga_rev;
									if (waga > 0) {
										connectedNodes.add(a1);
										connectedNodes.add(a2);
										RoutingVertex nowa = new RoutingVertex(osmNodeToRoutingNode.get(a1),
												osmNodeToRoutingNode.get(a2), len);
									}
									if (waga_rev > 0) {
										connectedNodes.add(a1);
										connectedNodes.add(a2);
										RoutingVertex nowa = new RoutingVertex(osmNodeToRoutingNode.get(a2),
												osmNodeToRoutingNode.get(a1), len_rev);
									}
									Pair<Node, Node> p1 = Pair.create(a1, a2);
									connections.put(p1, w);
								}
							}
						}
					}
				}
			}
	}
	Node getClosestPoint(LatLon akt) {
		if(connectedNodes == null)
			return null;
		double wynik = 1000000.0;
		Node wyn = null;
		for(Node n : connectedNodes) {
			double dist = n.getCoor().greatCircleDistance(akt);
			if(dist<wynik) {
				wynik=dist;
				wyn = n;
			}
		}
		return wyn;
	}
	private double getWeight(Way w, boolean isNormalDirection) {
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
			if (isNormalDirection) {
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

	public List<Node> completeNetwork(List<Node> middleNodes, boolean newDData)
			throws NodeConnectException {
		if(newDData || osmNodeToRoutingNode==null || routingNodeToOsmNode==null)
			updateAllData();
		List<RoutingNode> wezly = new ArrayList<RoutingNode>();
		for (int i = 0; i < middleNodes.size() - 1; i++) {
			List<RoutingNode> wezlyTmp = dijkstraData.calculate(
					osmNodeToRoutingNode.get(middleNodes.get(i)),
					osmNodeToRoutingNode.get(middleNodes.get(i + 1)));
			if (i > 0)
				wezlyTmp = wezlyTmp.subList(1, wezlyTmp.size());
			wezly.addAll(wezlyTmp);
		}
		List<Node> wynik = new ArrayList<Node>();
		for (int i = 0; i < wezly.size(); i++) {
			wynik.add(routingNodeToOsmNode.get(wezly.get(i)));
		}
		return wynik;
	}
	public double getDistance(List<Node> middleNodes, boolean newDData)
			throws NodeConnectException {
		double wynik = 0.0;
		if(newDData || osmNodeToRoutingNode==null || routingNodeToOsmNode==null)
			updateAllData();
		List<RoutingNode> wezly = new ArrayList<RoutingNode>();
		for (int i = 0; i < middleNodes.size() - 1; i++) {
			wynik += dijkstraData.calculateDistance(
					osmNodeToRoutingNode.get(middleNodes.get(i)),
					osmNodeToRoutingNode.get(middleNodes.get(i + 1)));
		}
		return wynik;
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

	public void splitWays(List<Node> middleNodes) throws NodeConnectException {
		List<Node> nodes = completeNetwork(middleNodes, false);
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
				} else if (i>1 && nodes.get(i-2)==n) {
					dodajWierzcholek(grenzeNodes, aktWId, nodes.get(i - 1));
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
				System.out.println("CHUNKS\n"+chunks+"\n LL \n"+ll);
				if (e1.getValue().size() > 0 && chunks!=null) {
					SplitWayResult sp = SplitWayAction.splitWay(
							Main.map.mapView.getEditLayer(), akt, chunks, ll);
					Main.main.undoRedo.add(sp.getCommand());

				}
			}
		}
	}
	
	private int isReversed(Way w, Node n1, Node n2) {
		List <Node> xd=w.getNodes();
		for(int i=0; i<xd.size()-1; i++) {
			if (xd.get(i)==n1 && xd.get(i+1)==n2)
				return 1;
			if (xd.get(i)==n2 && xd.get(i+1)==n1)
				return -1;
		}
		return 0;
	}
	
	private void addForBackStatus(Way w, Node n1, Node n2, Set<Way> nor, Set<Way> bac) {
		int wynik = isReversed(w,n1,n2);
		if(wynik<1)
			bac.add(w);
		if(wynik>-1)
			nor.add(w);
	}
	
	public List<Way> getWaysAfterSplit(List<Node> middleNodes, List<String> forwardBackward)
			throws NodeConnectException {
		connections = new HashMap<Pair<Node, Node>, Way>();
		List<Node> nodes = completeNetwork(middleNodes, true);
		Way aktWId = null;
		List<Way> wynik = new ArrayList<Way>();
		Set<Way> waysNormalDirection = new TreeSet<>();
		Set<Way> waysReverseDirection = new TreeSet<>();
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
				addForBackStatus(aktWId, nprim, n, waysNormalDirection, waysReverseDirection);
			}
		}
		if (nodes.size() > 0) {
			wynik.add(aktWId);
		}
		if(forwardBackward==null)
			return wynik;
		for(int i=0; i<wynik.size(); i++) {
			int licznik = 0;
			if(waysNormalDirection.contains(wynik.get(i)))
				licznik++;
			if(waysReverseDirection.contains(wynik.get(i)))
				licznik--;
			if(licznik==-1)
				forwardBackward.add("backward");
			if(licznik==0)
				forwardBackward.add("");
			if(licznik==1)
				forwardBackward.add("forward");
			System.out.println("LICZNIK "+licznik);
		}
		return wynik;
	}
	
	public List<Way> getWaysAfterSplit(List<Node> middleNodes) throws NodeConnectException {
		List <String> fb = null;
		return getWaysAfterSplit(middleNodes, fb);
	}
	
	private List <WaySplitterDataListener> listeners = new ArrayList<>();
	public void registerListener(WaySplitterDataListener lis) {
		listeners.add(lis);
	}
	public void unregisterListener(WaySplitterDataListener lis) {
		listeners.remove(lis);
	}
	private void powiadom() {
		for(WaySplitterDataListener lis : listeners) {
			lis.onWaySplitterDataChange();
		}
	}
	
	public void changeDelay() {
		final RoutingSpecial ws = this;
		/*
		if(changed)
			return;
		ws.changed=true;
		new java.util.Timer().schedule( 
		        new java.util.TimerTask() {
		            @Override
		            public void run() {
		            			ws.changed=false;*/
		            			ws.updateAllData();
		            			ws.powiadom();/*
		            }
		        }, 500);*/
	}
	

	
	@Override
	public void primitivesAdded(PrimitivesAddedEvent event) {
		if(listeners.size()==0)
			return;
		System.out.println("PRIMITIVES ADD");
		changeDelay();
	}
	@Override
	public void primitivesRemoved(PrimitivesRemovedEvent event) {
		if(listeners.size()==0)
			return;
		System.out.println("PRIMITIVES REMOVED");
		changeDelay();
	}
	@Override
	public void tagsChanged(TagsChangedEvent event) {
		if(listeners.size()==0)
			return;
		List<? extends OsmPrimitive> foo = event.getPrimitives();
		boolean ok =false;
		for(OsmPrimitive x : foo) {
			
			if(x.getDisplayType()!=OsmPrimitiveType.RELATION && x.getDisplayType()!=OsmPrimitiveType.MULTIPOLYGON)
				ok = true;
		}
		if(!ok)
			return;
		System.out.println("TAGS CHANGED");
		changeDelay();
	}
	@Override
	public void nodeMoved(NodeMovedEvent event) {
	}
	@Override
	public void wayNodesChanged(WayNodesChangedEvent event) {
		if(listeners.size()==0)
			return;
		System.out.println("WAY NODES");
		changeDelay();
	}
	@Override
	public void relationMembersChanged(RelationMembersChangedEvent event) {
	}
	@Override
	public void otherDatasetChange(AbstractDatasetChangedEvent event) {
		if(listeners.size()==0)
			return;
		Collection<? extends OsmPrimitive> foo = event.getPrimitives();
		boolean ok =false;
		for(OsmPrimitive x : foo) {
			
			if(x.getDisplayType()!=OsmPrimitiveType.RELATION && x.getDisplayType()!=OsmPrimitiveType.MULTIPOLYGON)
				ok = true;
		}
		if(!ok)
			return;
		System.out.println("OTHER");
		changeDelay();
	}
	@Override
	public void dataChanged(DataChangedEvent event) {
		if(listeners.size()==0)
			return;
		System.out.println("DCC");
		changeDelay();
	}
}
