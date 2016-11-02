package org.openstreetmap.josm.plugins.EasyRoutes.RelationsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.openstreetmap.josm.plugins.EasyRoutes.DiffLayerTech;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingLayer;

public class RelationsBuilder {
	List <SingleRelationBuilder> relationList = new ArrayList<SingleRelationBuilder>();
	Map <Long, Relation> relationMap = new HashMap<>();
	DiffLayer dff;
	public RelationsBuilder(String[] jsonText) {
		for(int j=0; j<jsonText.length; j++) {
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
	/*public List <Long> getExistRelations()
	{
		List <Long> wynik = new ArrayList<>();
		for(SingleRelationBuilder bul : relationList) {
			if(bul!=null && bul.id>0)
				wynik.add(bul.id);
		}
		return wynik;
	}*/
	public List <SingleRelationBuilder> getRelations() {
		return new ArrayList<SingleRelationBuilder>(relationList);
	}
	public void onLoadTrackNodes() {
		for(SingleRelationBuilder foo : relationList) {
			foo.onLoadTrackNodes();
		}
	}
	public void createTracks() {
		Map <String, List<RoutingLayer> > layers = new HashMap<>();
		Map <String, List<Relation> > rels = new HashMap<>();
		for(SingleRelationBuilder foo : relationList) {
			String ref = foo.onLoadRouting();
			if(ref!=null)
			{
				if(!layers.containsKey(ref))
					layers.put(ref, new ArrayList<RoutingLayer>());
				if(!rels.containsKey(ref))
					rels.put(ref, new ArrayList<Relation>());
				layers.get(ref).add(foo.lay);
				if(foo.id>0)
					rels.get(ref).add((Relation)Main.getLayerManager().getEditDataSet().getPrimitiveById(foo.id, OsmPrimitiveType.RELATION));
			}
		}
		List <DiffLayerTech> tech = new ArrayList<>();
		for(Entry<String, List<RoutingLayer> > e : layers.entrySet())
		{
			List <Relation> rels2 = rels.get(e.getKey());
			if(rels2==null)
			{
				rels2 = new ArrayList<>();
			}
			tech.add(new DiffLayerTech(rels2, e.getValue(), e.getKey()));
		}
		dff = new DiffLayer(tech);
		Main.getLayerManager().addLayer(dff);
	}
	/*public Set <Long> getNecessaryRelations() {
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
	}*/
	public void createNecessaryRelations() {
		Set <Long> relList = new TreeSet<>();
		for(SingleRelationBuilder foo : relationList) {
			relList.addAll(foo.getNecessaryRelations());
		}
		for(long x : relList) {
			if(x>0) {
				Relation rel = (Relation)Main.getLayerManager().getEditDataSet().getPrimitiveById(x, OsmPrimitiveType.RELATION);
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
				Main.getLayerManager().getEditDataSet().addPrimitive(akt);
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
		if(dff!=null)
			Main.getLayerManager().removeLayer(dff);
	}
}
