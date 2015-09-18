package org.openstreetmap.josm.plugins.EasyRoutes.RelationsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.plugins.EasyRoutes.DiffLayer;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingLayer;

public class RelationsBuilder {
	List <SingleRelationBuilder> relationList = new ArrayList<SingleRelationBuilder>();
	Map <Long, Relation> relationMap = new HashMap<>();
	public RelationsBuilder(String[] jsonText) {
		for(int j=0; j<jsonText.length; j++) {
			System.out.println(jsonText[j]);
			JSONParser parser = new JSONParser();
			Object obj;
			try {
				obj = parser.parse(jsonText[j]);
				JSONArray array = (JSONArray) obj;
				for (int i = 0; i < array.size(); i++) {
					JSONObject obb = (JSONObject) array.get(i);
					relationList.add(new SingleRelationBuilder(obb, j*1000));
				}
			} catch (ParseException e2) {
				e2.printStackTrace();
			}
		}
	}
	public List <Long> getExistRelations()
	{
		List <Long> wynik = new ArrayList<>();
		for(SingleRelationBuilder bul : relationList) {
			if(bul!=null && bul.id>0)
				wynik.add(bul.id);
		}
		return wynik;
	}
	public List <SingleRelationBuilder> getRelations() {
		return new ArrayList<SingleRelationBuilder>(relationList);
	}
	public void onLoadTrackNodes() {
		for(SingleRelationBuilder foo : relationList) {
			foo.onLoadTrackNodes();
		}
	}
	public void createTracks() {
		List <RoutingLayer> layers = new ArrayList<>();
		List <Relation> rels = new ArrayList<>();
		for(SingleRelationBuilder foo : relationList) {
			foo.onLoadRouting();
			layers.add(foo.lay);
			if(foo.id>0)
				rels.add((Relation)Main.main.getCurrentDataSet().getPrimitiveById(foo.id, OsmPrimitiveType.RELATION));
		}
		DiffLayer dff = new DiffLayer(rels, layers);
		Main.main.addLayer(dff);
	}
	public Set <Long> getNecessaryRelations() {
		Set <Long> relList = new TreeSet<>();
		Set <Long> relList2 = new TreeSet<>();
		for(SingleRelationBuilder foo : relationList) {
			relList.addAll(foo.getNecessaryRelations());
		}
		for(long x : relList) {
			if(x>0)
				relList2.add(x);
		}
		return relList2;
	}
	public void createNecessaryRelations() {
		Set <Long> relList = new TreeSet<>();
		for(SingleRelationBuilder foo : relationList) {
			relList.addAll(foo.getNecessaryRelations());
		}
		for(long x : relList) {
			if(x>0) {
				Relation rel = (Relation)Main.main.getCurrentDataSet().getPrimitiveById(x, OsmPrimitiveType.RELATION);
				relationMap.put(x, rel);
			}
			else {
				Relation rel = new Relation();
				relationMap.put(x, rel);
			}
		}
	}
	public void doFinally() {
		Set<Long> ids = relationMap.keySet();
		for(long id : ids) {
			Relation akt = relationMap.get(id);
			if(id<0) {
				Main.main.getCurrentDataSet().addPrimitive(akt);
			}
		}
		for(SingleRelationBuilder x : relationList) {
			Relation p = relationMap.get(x.id);
			p.setMembers(null);
			p.setKeys(null);
			x.putTagsAndToDelete(p, this);
			x.putRelationMembers(p, this);
			x.putRelationWays(p);
			p.setModified(true);
		}
		for(SingleRelationBuilder x : relationList) {
			Relation p = relationMap.get(x.id);
			x.putMeMaster(p, this);
		}
		for(SingleRelationBuilder x : relationList) {
			x.eraseLayer();
		}
	}
}
