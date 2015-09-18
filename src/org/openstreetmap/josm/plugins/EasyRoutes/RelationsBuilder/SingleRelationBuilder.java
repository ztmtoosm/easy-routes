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
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.SimplePrimitiveId;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingLayer;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingSpecial;
import org.openstreetmap.josm.plugins.EasyRoutes.RelationsBuilder.RelationMemberBuilder.RelationMemberType;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.NodeConnectException;

public class SingleRelationBuilder {
	static Map <String, RoutingSpecial> splitters = new HashMap<>();
	List<Long> track = new ArrayList<Long>();
	List<Node> trackNodes = null;
	List<Long> parentRel = new ArrayList<Long>();
	long id;
	RoutingLayer lay;
	String track_type="bus";
	Map<String, String> tags = new TreeMap<String, String>();
	List<RelationMemberBuilder> relationMembers = new ArrayList<RelationMemberBuilder>();
	boolean todelete = false;
	private void trackFromJson(JSONArray array) {
		for (int j = 0; j < array.size(); j++) {
			track.add((Long) array.get(j));
		}
	}
	public void putRelationMembers(Relation x, RelationsBuilder bui) {
		for(RelationMemberBuilder y: relationMembers) {
			String role = y.role;
			if(role==null)
				role="";
			RelationMember member;
			if(y.id<0)
				member = new RelationMember(role, bui.relationMap.get(y.id));
			else
			{
				System.out.println("ID DO DODANIA "+y.id+" "+y.category);
				OsmPrimitive memb = null;
				if(y.category==RelationMemberType.NODE)
					memb = Main.main.getCurrentDataSet().getPrimitiveById(y.id, OsmPrimitiveType.NODE);
				if(y.category==RelationMemberType.WAY)
					memb = Main.main.getCurrentDataSet().getPrimitiveById(y.id, OsmPrimitiveType.WAY);
				if(y.category==RelationMemberType.RELATION)
					memb = Main.main.getCurrentDataSet().getPrimitiveById(y.id, OsmPrimitiveType.RELATION);
				member = new RelationMember(role, memb);
			}
			x.addMember(member);
		}
	}
	public void putRelationWays(Relation x) {
		if(lay==null)
			return;
		List<Way> toAdd;
		List<String> forBack = new ArrayList<>();
		try {
			toAdd = lay.splitWays(forBack);
		int i=0;
		for(Way y : toAdd) {
			String foo = "";
			if(forBack!=null && forBack.size()>i && forBack.get(i)!=null)
				foo=forBack.get(i);
			RelationMember member = new RelationMember(foo, y);
			x.addMember(member);
			i++;
		}
		}catch (NodeConnectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void putTagsAndToDelete(Relation x, RelationsBuilder bui) {
		Set<Entry<String, String> > z=tags.entrySet();
		for(Entry<String, String> y : z) {
			x.put(y.getKey(), y.getValue());
		}
		if(todelete)
			x.setDeleted(true);
	}
	void putMeMaster(Relation x, RelationsBuilder bui) {
		for(int i=0; i<parentRel.size(); i++) {
			Relation xyz=bui.relationMap.get(parentRel.get(i));
			if(xyz!=null) {
				List<RelationMember> lista=xyz.getMembers();
				boolean czyDodawać = true;
				for(RelationMember foo : lista) {
					if(foo.getMember()==x) {
						czyDodawać = false;
					}
				}
				if(czyDodawać) {
					RelationMember member = new RelationMember("", x);
					xyz.addMember(member);
					xyz.setModified(true);
				}
			}
		}
	}
	Set <Long> getNecessaryRelations() {
		Set<Long> wynik = new TreeSet<>();
		for(int i=0; i<parentRel.size(); i++)
			wynik.add(parentRel.get(i));
		wynik.add(id);
		for(int i=0; i<relationMembers.size(); i++) {
			RelationMemberBuilder akt = relationMembers.get(i);
			if(akt.category==RelationMemberType.RELATION) {
				wynik.add(akt.id);
			}
		}
		return wynik;
	}
	private void parentRelFromJson(JSONArray array, int przesuniecieUjemne) {
		if(array==null)
			return;
		for (int j = 0; j < array.size(); j++) {
			long id = (Long) array.get(j);
			if(id<0)
				id-=przesuniecieUjemne;
			parentRel.add(id);
		}
	}
	
	private void tagsFromJson(JSONArray array) {
		for (int j = 0; j < array.size(); j++) {
			JSONObject xd = (JSONObject) array.get(j);
			String key = (String)xd.get("key");
			String value = (String)xd.get("value");
			tags.put(key, value);
		}
	}
	
	private void relMembFromJson(JSONArray array, int przesuniecieUjemne) {
		for (int j = 0; j < array.size(); j++) {
			JSONObject xd = (JSONObject) array.get(j);
			String id = (String)xd.get("id");
			String category = (String)xd.get("category");
			String role = (String)xd.get("role");
			long id2 = Long.valueOf(id);
			if(id2<0)
				id2-=przesuniecieUjemne;
			relationMembers.add(new RelationMemberBuilder(id2, role, RelationMemberBuilder.createCategory(category)));
		}
	}
	
	SingleRelationBuilder(JSONObject jsonObject, int przesuniecieUjemne) {
		trackFromJson((JSONArray) jsonObject.get("track"));
		tagsFromJson((JSONArray) jsonObject.get("tags"));
		parentRelFromJson((JSONArray) jsonObject.get("parentrel"), przesuniecieUjemne);
		relMembFromJson((JSONArray) jsonObject.get("members"), przesuniecieUjemne);
		id = (long)jsonObject.get("id");
		if(id<0)
			id-=przesuniecieUjemne;
		String trackTypeTmp = (String)jsonObject.get("track_type");
		if(trackTypeTmp!=null)
			track_type = trackTypeTmp;
		if(jsonObject.get("todelete")!=null)
			todelete = (boolean)jsonObject.get("todelete");
	}
	public List <Long> getTrack() {
		List <Long> wynik = new ArrayList<Long>();
		for(int i=0; i<track.size(); i++)
			wynik.add(track.get(i).longValue());
		return wynik;
	}
	
	public List <Node> getTrackNodes() {
		List <Node> wynik = new ArrayList<Node>();
		if(trackNodes==null)
			return wynik;
		for(int i=0; i<trackNodes.size(); i++)
			wynik.add(trackNodes.get(i));
		return wynik;
	}

	void onLoadTrackNodes() {
		trackNodes = new ArrayList<Node>();
		for(long x : track) {
			PrimitiveId pid = new SimplePrimitiveId(x, OsmPrimitiveType.NODE);
			Node n = (Node) Main.main.getCurrentDataSet().getPrimitiveById(pid);
			if(n!=null) {
				trackNodes.add(n);
			}
		}
		
	}
	void onLoadRouting() {
		if(getTrackNodes().size()>1) {
			String name = "";
			if(tags.containsKey("name"))
				name = tags.get("name")+" ["+id+"]";
			if(splitters.get(track_type) == null || splitters.get(track_type).getDataSet()!=Main.main.getCurrentDataSet()) {
				splitters.put(track_type, new RoutingSpecial(Main.pref.getArray("easy-routes.weights."+track_type)));
			}
			lay = new RoutingLayer(getTrackNodes(), name, splitters.get(track_type));
			Main.main.addLayer(lay);
		}
	}
	public void eraseLayer() {
		if(lay!=null)
		{
			if(lay.ws!=null)
				lay.ws.unregisterListener(lay);
			Main.main.removeLayer(lay);
		}
	}
	public Collection<PrimitiveId> getNecessaryPrimitives() {
		List <PrimitiveId> wyn = new ArrayList<PrimitiveId>();
		for(Long x : getTrack()) {
			wyn.add(new SimplePrimitiveId(x, OsmPrimitiveType.NODE));
		}
		for(RelationMemberBuilder x : relationMembers) {
			PrimitiveId id = null;
			if(x.id>0)
			{
				if(x.category==RelationMemberType.NODE)
					id = new SimplePrimitiveId(x.id, OsmPrimitiveType.NODE);
				if(x.category==RelationMemberType.WAY)
					id = new SimplePrimitiveId(x.id, OsmPrimitiveType.WAY);
				if(x.category==RelationMemberType.RELATION)
					id = new SimplePrimitiveId(x.id, OsmPrimitiveType.RELATION);
				wyn.add(id);
			}
		}
		return wyn;
	}
}
