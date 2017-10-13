package org.openstreetmap.josm.plugins.EasyRoutes.RelationsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.plugins.EasyRoutes.CityEnviroment.RoutingPreferences;
import org.openstreetmap.josm.plugins.EasyRoutes.Routing.RoutingBis;
import org.openstreetmap.josm.plugins.EasyRoutes.Routing.RoutingSpecial;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.NodeConnectException;
import org.openstreetmap.josm.plugins.EasyRoutes.StopWatch.SingleStop;

public class SingleRelationBuilder {
	static Map<String, RoutingSpecial> splitters = new HashMap<>();
	List<Long> track = new ArrayList<Long>();
	List<Node> trackNodes = null;
	List<Long> parentRel = new ArrayList<Long>();
	long id;
	RoutingBis routingBis;
	String trackType = "bus";
	Map<String, String> tags = new TreeMap<String, String>();
	List<RelationMemberBuilder> relationMembers = new ArrayList<RelationMemberBuilder>();
	boolean todelete = false;
	DataSet ds = Main.getLayerManager().getEditDataSet();

	public void trackFromJson(List<SingleStop> array) {
		for (int j = 0; j < array.size(); j++) {
			track.add(array.get(j).getStopPositionId());
		}
	}

	public void putRelationMembers(Relation x, RelationsBuilder bui) {
		for (RelationMemberBuilder y : relationMembers) {
			String role = y.role;
			if (role == null)
				role = "";
			RelationMember member;
			if (y.id < 0 && y.category == OsmPrimitiveType.RELATION) {
				member = new RelationMember(role, bui.relationMap.get(y.id));
			} else {
				OsmPrimitive memb = null;
				System.out.println(relationMembers.size() + y.role + " " + y.id +" idnull idnull");
				memb = ds.getPrimitiveById(y.id, y.getPrimitiveType());
				member = new RelationMember(role, memb);
			}
			x.addMember(member);
		}
	}

	public void putRelationWays(Relation x) {
		if (routingBis == null)
			return;
		List<Way> toAdd;
		List<String> forBack = new ArrayList<>();
		try {
			toAdd = routingBis.splitWays(forBack);
			routingBis.ws.fireMe();
			int i = 0;
			for (Way y : toAdd) {
				String foo = "";
				if (forBack != null && forBack.size() > i
						&& forBack.get(i) != null)
					foo = forBack.get(i);
				RelationMember member = new RelationMember(foo, y);
				x.addMember(member);
				i++;
			}
		} catch (NodeConnectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void putTagsAndToDelete(Relation x, RelationsBuilder bui) {
		Set<Entry<String, String>> z = tags.entrySet();
		for (Entry<String, String> y : z) {
			x.put(y.getKey(), y.getValue());
		}
		if (todelete)
			x.setDeleted(true);
	}

	void putMeMaster(Relation x, RelationsBuilder bui) {
		for (int i = 0; i < parentRel.size(); i++) {
			Relation xyz = bui.relationMap.get(parentRel.get(i));
			if (xyz != null) {
				List<RelationMember> lista = xyz.getMembers();
				boolean czyDodawać = true;
				for (RelationMember foo : lista) {
					if (foo.getMember() == x) {
						czyDodawać = false;
					}
				}
				if (czyDodawać) {
					RelationMember member = new RelationMember("", x);
					xyz.addMember(member);
					xyz.setModified(true);
				}
			}
		}
	}

	Set<Long> getNecessaryRelations() {
		Set<Long> wynik = new TreeSet<>();
		for (int i = 0; i < parentRel.size(); i++)
			wynik.add(parentRel.get(i));
		wynik.add(id);
		for (int i = 0; i < relationMembers.size(); i++) {
			RelationMemberBuilder akt = relationMembers.get(i);
			if (akt.category == OsmPrimitiveType.RELATION) {
				wynik.add(akt.id);
			}
		}
		return wynik;
	}

	public void parentRelFromJson(long id) {
		parentRel.add(id);
	}

	void tagsFromJson(Map<String, String> tags) {
		this.tags.putAll(tags);
	}

	void relMembFromJson(JSONArray array, int przesuniecieUjemne) {
		for (int j = 0; j < array.size(); j++) {
			JSONObject xd = (JSONObject) array.get(j);
			String id = (String) xd.get("id");
			String category = (String) xd.get("category");
			String role = (String) xd.get("role");
			long id2 = Long.valueOf(id);
			if (id2 < 0)
				id2 -= przesuniecieUjemne;
			relationMembers.add(new RelationMemberBuilder(id2, role, category));
		}
	}

	void relMembFromJson(List<SingleStop> array) {
		for (int j = 0; j < array.size(); j++) {
			long stopId = array.get(j).getStopId();
			String role = "stop";
			String role2 = "";
			String role3 = "platform";
			if(j==0)
				role2 = "_entry_only";
			if(j==array.size()-1)
				role2 = "_exit_only";
			
			relationMembers.add(new RelationMemberBuilder(stopId, role+role2, OsmPrimitiveType.NODE));
			long platformId = array.get(j).getPlatformId();
			OsmPrimitiveType platformType = array.get(j).getPlatformType();
			if(platformId !=0 ) {
				System.out.println("**"+platformId);
				relationMembers.add(new RelationMemberBuilder(platformId, role3+role2, platformType));
			}
		}
	}

	void relMembFromJson(long[] array) {
		for (int j = 0; j < array.length; j++) {
			String role = "";
			relationMembers.add(new RelationMemberBuilder(array[j], role, OsmPrimitiveType.RELATION));
		}
	}

	SingleRelationBuilder(long id, boolean toDelete, DataSet dataSet) {
		this.id = id;
		this.todelete = toDelete;
		if (dataSet != null)
			ds = dataSet;
	}

	SingleRelationBuilder(long id, boolean toDelete, String trackType, DataSet dataSet) {
		this(id, toDelete, dataSet);
		this.trackType = trackType;
	}

	public List<Long> getTrack() {
		List<Long> wynik = new ArrayList<Long>();
		for (int i = 0; i < track.size(); i++)
			wynik.add(track.get(i).longValue());
		return wynik;
	}

	public List<Node> getTrackNodes() {
		List<Node> wynik = new ArrayList<Node>();
		if (trackNodes == null)
			return wynik;
		for (int i = 0; i < trackNodes.size(); i++)
			wynik.add(trackNodes.get(i));
		return wynik;
	}

	void onLoadTrackNodes() {
		trackNodes = new ArrayList<Node>();
		for (long x : track) {
			PrimitiveId pid = new SimplePrimitiveId(x, OsmPrimitiveType.NODE);
			Node n = (Node) ds.getPrimitiveById(pid);
			if (n != null) {
				trackNodes.add(n);
			}
		}

	}

	String onLoadRouting() {
		if (getTrackNodes().size() > 1) {
			String name = "";
			if (tags.containsKey("name"))
				name = tags.get("name") + " [" + id + "]";
			if (splitters.get(trackType) == null
					|| splitters.get(trackType).getDataSet() != Main
							.getLayerManager().getEditDataSet()) {
				splitters
						.put(trackType,
								new RoutingSpecial(new RoutingPreferences(Main.pref
										.getArray("easy-routes.weights."
												+ trackType)), ds));
			}

			routingBis = new RoutingBis(getTrackNodes(), getNecessaryPrimitives2(),
					name, splitters.get(trackType), true);
			return tags.get("ref");
		}
		return null;
	}

	public void eraseLayer() {
		if (routingBis != null) {
			routingBis.eraseLayer();
		}
	}

	public Collection<PrimitiveId> getNecessaryPrimitives(boolean withTrack) {
		List<PrimitiveId> wyn = new ArrayList<PrimitiveId>();
		if (withTrack) {
			for (Long x : getTrack()) {
				wyn.add(new SimplePrimitiveId(x, OsmPrimitiveType.NODE));
			}
		}
		for (RelationMemberBuilder x : relationMembers) {
			PrimitiveId id = null;
			id = new SimplePrimitiveId(x.id, x.category);
			wyn.add(id);
		}
		return wyn;
	}

	public List<OsmPrimitive> getNecessaryPrimitives2() {
		List<OsmPrimitive> wyn = new ArrayList<OsmPrimitive>();
		for (PrimitiveId idx : getNecessaryPrimitives(false)) {
			OsmPrimitive prim = ds.getPrimitiveById(idx);
			if (prim != null)
				wyn.add(prim);
		}
		return wyn;
	}
}
