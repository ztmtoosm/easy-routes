package org.openstreetmap.josm.plugins.EasyRoutes.StopWatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javafx.util.Pair;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.SystemOfMeasurement;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.data.osm.event.*;

public class StopWatch implements DataSetListener {
	Map<String, SingleStop> singleStops = new HashMap<String, SingleStop>();
	private DataSet ds;
	public StopWatch(DataSet ds, Collection <Pair<String, String> > refsAndNames)
	{
		this.ds = ds;
		for(Pair<String, String> x : refsAndNames) {
			SingleStop stop = new SingleStop(ds);
			stop.operatorName = x.getValue();
			stop.refId = x.getKey();
			singleStops.put(stop.refId, stop);
		}
	}

	public Collection <LatLon> getCurrentLatLon() {
		List<LatLon> ret = new ArrayList<LatLon>();
		for (SingleStop x : singleStops.values()) {
			LatLon xCoordinates = x.getGeneralCoordinates();
			if(xCoordinates != null) {
				ret.add(xCoordinates);
			}
		}
		return ret;
	}

	private String isStopPosition(Node node) {
		TagMap mp1 = node.getKeys();
		if (mp1.containsKey("public_transport")) {
			if (mp1.get("public_transport").equals("stop_position")) {
				if (mp1.containsKey("ref")) {
					return mp1.get("ref");
				}
			}
		}
		return null;
	}


	private String isBusStop(Node node) {
		TagMap mp1 = node.getKeys();
		if (mp1.containsKey("highway")) {
			if (mp1.get("highway").equals("bus_stop")) {
				if (mp1.containsKey("ref")) {
					return mp1.get("ref");
				}
			}
		}
		if (mp1.containsKey("railway")) {
			if (mp1.get("railway").equals("tram_stop")) {
				if (mp1.containsKey("ref")) {
					return mp1.get("ref");
				}
			}
		}
		return null;
	}

	private void addToSingleStops(Node node) {
		System.out.println("addToSingleStops!");
		String ref1 = isStopPosition(node);
		String ref2 = isBusStop(node);
		String ref = ref1;
		if (ref1 != null)
			System.out.println(ref1);
		if (ref2 != null)
			System.out.println(ref2);
		if (ref2 != null)
			ref = ref2;
		if (!singleStops.containsKey(ref)) {
			return;
		}
		SingleStop current = singleStops.get(ref);
		if (ref1 != null) {
			if (current.stopId == 0) {
				current.stopId = node.getUniqueId();
			}
			current.stopPositionId = node.getUniqueId();
		}
		if (ref2 != null) {
			current.stopId = node.getUniqueId();
		}
	}
	
	private String isPlatform(OsmPrimitive foo) {
		TagMap mp1 = foo.getKeys();
		if (mp1.containsKey("public_transport")) {
			if (mp1.get("public_transport").equals("platform")) {
				if (mp1.containsKey("ref")) {
					return mp1.get("ref");
				}
			}
		}
		if (mp1.containsKey("highway")) {
			if (mp1.get("highway").equals("platform")) {
				if (mp1.containsKey("ref")) {
					return mp1.get("ref");
				}
			}
		}
		if (mp1.containsKey("railway")) {
			if (mp1.get("railway").equals("platform")) {
				if (mp1.containsKey("ref")) {
					return mp1.get("ref");
				}
			}
		}
		if (mp1.containsKey("platform")) {
			if (mp1.get("platform").equals("yes")) {
				if (mp1.containsKey("ref")) {
					return mp1.get("ref");
				}
			}
		}
		return null;
	}
	
	private void addToSingleStopsPlatform(OsmPrimitive way) {
		String ref = isPlatform(way);

		if (!singleStops.containsKey(ref)) {
			return;
		}

		SingleStop current = singleStops.get(ref);
		if (ref != null) {
			if (current.platformId == 0) {
				current.platformId = way.getUniqueId();
				current.platformType = way.getType();
			}
		}
	}

	public Collection <SingleStop> getUnsupportedStops() {
		Collection<Node> lNodes = ds.getNodes();
		Collection<Way> lWays = ds.getWays();
		Collection<Relation> lRelations = ds.getRelations();
		for (Node x : lNodes) {
			addToSingleStops(x);
		}
		for (Way x : lWays) {
			addToSingleStopsPlatform(x);
		}
		for (Relation x : lRelations) {
			addToSingleStopsPlatform(x);
		}
		List <SingleStop> ret = new LinkedList<>();
		for (Entry<String, SingleStop> x : singleStops.entrySet()) {
			if(x.getValue().isUseless()) {
				ret.add(x.getValue());
			}
		}
		return ret;
	}

	public SingleStop getSingleStop(String ref) {
		return singleStops.get(ref);
	}

	@Override
	public void primitivesAdded(PrimitivesAddedEvent event) {

	}

	@Override
	public void primitivesRemoved(PrimitivesRemovedEvent event) {

	}

	@Override
	public void tagsChanged(TagsChangedEvent event) {

	}

	@Override
	public void nodeMoved(NodeMovedEvent event) {

	}

	@Override
	public void wayNodesChanged(WayNodesChangedEvent event) {

	}

	@Override
	public void relationMembersChanged(RelationMembersChangedEvent event) {

	}

	@Override
	public void otherDatasetChange(AbstractDatasetChangedEvent event) {

	}

	@Override
	public void dataChanged(DataChangedEvent event) {

	}
}
