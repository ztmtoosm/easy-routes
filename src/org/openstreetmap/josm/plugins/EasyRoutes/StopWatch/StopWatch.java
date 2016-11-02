package org.openstreetmap.josm.plugins.EasyRoutes.StopWatch;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.util.Pair;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.TagMap;

public class StopWatch {
	Map <String, String> mp = new HashMap<String, String>();
	Set <String> setStop = new HashSet<String>();
	Map <String, SingleStop> singleStops = new HashMap<String, SingleStop>();
	public StopWatch() {
		
	}
	public void addStop(String ref, String name) {
		mp.put(ref, name);
		setStop.add(ref);
	}
	private String isStopPosition(Node node) {
		TagMap mp1 = node.getKeys();
		if(mp1.containsKey("public_transport")) {
			if(mp1.get("public_transport").equals("stop_position")) {
				if(mp1.containsKey("ref")) {
					return mp1.get("ref");
				}
			}
		}
		return null;
	}
	private String isBusStop(Node node) {
		//ZASLEPKA
		return null;
	}
	private void addToSingleStops(Node node, Set <String> setStopCurrent) {
		String ref1 = isStopPosition(node);
		String ref2 = isBusStop(node);
		String ref = ref1;
		if(ref1!=null)
			System.out.println(ref1);
		if(ref2!=null)
			System.out.println(ref2);
		if(ref2 != null)
			ref = ref2;
		if(!mp.containsKey(ref))
			return;
		if(!singleStops.containsKey(ref)) {
			singleStops.put(ref, new SingleStop());
		}
		SingleStop current = singleStops.get(ref);
		if(ref1 != null) {
			if(current.stopId == 0) {
				current.stopId = node.getId();
			}
			current.stopPositionId = node.getId();
			setStopCurrent.add(ref);
		}
		if(ref2 != null) {
			current.stopId = node.getId();
		}
	}
	
	public List <Pair<String, String> >  getUnsupportedStops() {
		singleStops.clear();
		Set <String> setStopCurrent = new HashSet<String>();
		Collection <Node> lNodes = Main.getLayerManager().getEditDataSet().getNodes();
		for(Node x : lNodes) {
			addToSingleStops(x, setStopCurrent);
		}
		List <Pair<String, String> > ret = new LinkedList<Pair<String, String> >();
		for(String x : setStop) {
			if(!setStopCurrent.contains(x)) {
				ret.add(new Pair<>(x, mp.get(x)));
			}
		}
		return ret;
	}
}
