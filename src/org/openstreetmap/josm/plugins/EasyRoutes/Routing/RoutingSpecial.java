package org.openstreetmap.josm.plugins.EasyRoutes.Routing;
/*
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
import org.openstreetmap.josm.plugins.EasyRoutes.CityEnviroment.RoutingPreferences;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.DijkstraData;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.NodeConnectException;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.RoutingNode;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.RoutingVertex;
import org.openstreetmap.josm.tools.Pair;

public class RoutingSpecial implements DataSetListener  {
	private RoutingPreferences currentPreferences;
	private DijkstraData dijkstraData;
	private Map<Pair<Node, Node>, Way> connections;
	private Set <Node> connectedNodes;
	private Map<Node, RoutingNode> osmNodeToRoutingNode;
	private Map<RoutingNode, Node> routingNodeToOsmNode;
	private DataSet ds = null;
	public RoutingSpecial(RoutingPreferences currentPreferences, DataSet ds2) {
		this.currentPreferences = currentPreferences;
		ds = Main.getLayerManager().getEditDataSet();
		if(ds2 != null) {
			ds = ds2;
		}
		ds.addDataSetListener(this);
	}
	public DataSet getDataSet() {
		return ds;
	}

	private void connectNodes(Way w) {
		List<Node> ll1 = w.getNodes();
		double weight = currentPreferences.getWeight(w, true);
		double reversedWeight = currentPreferences.getWeight(w, false);
		if (weight > 0 || reversedWeight > 0) {
			for (int i = 0; i < ll1.size() - 1; i++) {
				Node a1 = ll1.get(i);
				Node a2 = ll1.get(i + 1);
				if(a1!=null && a2!=null)
				{
					LatLon coor1 = a1.getCoor();
					LatLon coor2 = a2.getCoor();
					if(coor1!=null && coor2!=null)
					{
						double length = coor1.greatCircleDistance(coor2) * weight;
						double reversedLength = coor1.greatCircleDistance(coor2)
								* reversedWeight;
						if (weight > 0) {
							connectedNodes.add(a1);
							connectedNodes.add(a2);
							RoutingVertex nowa = new RoutingVertex(osmNodeToRoutingNode.get(a1),
									osmNodeToRoutingNode.get(a2), length);
						}
						if (reversedWeight > 0) {
							connectedNodes.add(a1);
							connectedNodes.add(a2);
							RoutingVertex nowa = new RoutingVertex(osmNodeToRoutingNode.get(a2),
									osmNodeToRoutingNode.get(a1), reversedLength);
						}
						Pair<Node, Node> p1 = Pair.create(a1, a2);
						connections.put(p1, w);
					}
				}
			}
		}
	}

	private void updateAllData() {
		DataSet dataSet = ds;
			connectedNodes = new TreeSet <Node>();
			connections = new HashMap<Pair<Node, Node>, Way>();
			Collection<Node> nodes = dataSet.getNodes();
			Collection<Way> ways = dataSet.getWays();
			osmNodeToRoutingNode = new HashMap<Node, RoutingNode>();
			routingNodeToOsmNode = new HashMap<RoutingNode, Node>();
			dijkstraData = new DijkstraData();
			for (Node n : nodes) {
				RoutingNode nowy = dijkstraData.createNewNode();
				osmNodeToRoutingNode.put(n, nowy);
				routingNodeToOsmNode.put(nowy, n);
			}
			for (Way w : ways) {
				if (w != null) {
					connectNodes(w);
				}
			}
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
				if (e1.getValue().size() > 0 && chunks!=null) {
					SplitWayResult sp = SplitWayAction.splitWay(
							Main.getLayerManager().getEditLayer(), akt, chunks, ll);
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
	private void callListeners() {
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
		            			ws.changed=false;
		            			ws.updateAllData();
		            			ws.callListeners();
		            }
		        }, 500);*//*
	}
	

	public void fireMe() {
		getDataSet().removeDataSetListener(this);
	}
	
	@Override
	public void primitivesAdded(PrimitivesAddedEvent event) {
		if(listeners.size()==0)
			return;
		changeDelay();
	}
	@Override
	public void primitivesRemoved(PrimitivesRemovedEvent event) {
		if(listeners.size()==0)
			return;
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
		changeDelay();
	}
	@Override
	public void nodeMoved(NodeMovedEvent event) {
	}
	@Override
	public void wayNodesChanged(WayNodesChangedEvent event) {
		if(listeners.size()==0)
			return;
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
		changeDelay();
	}
	@Override
	public void dataChanged(DataChangedEvent event) {
		if(listeners.size()==0)
			return;
		changeDelay();
	}
}
*/
