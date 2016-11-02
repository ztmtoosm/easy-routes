package org.openstreetmap.josm.plugins.EasyRoutes.RelationsBuilder;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.SimplePrimitiveId;
import org.openstreetmap.josm.plugins.EasyRoutes.StopWatch.StopWatch;

public class FirstRelationsBuilder {
	String[] jsonTextInternal;
	Set<PrimitiveId> prims0 = new HashSet<>();
	public StopWatch wtch = new StopWatch();
	List<Long> prims1 = new LinkedList<Long>();
	void addToPrims0(String key, JSONObject obj) {
		JSONArray master = (JSONArray) obj.get(key);
		for(int i = 0; i< master.size(); i++) {
			Long sp = (Long) master.get(i);
			if(sp != null) {
				prims0.add(new SimplePrimitiveId(sp, OsmPrimitiveType.RELATION));
				prims1.add(new Long(sp));
			}
		}		
	}
	
	public FirstRelationsBuilder(String[] jsonText) {
		jsonTextInternal = jsonText;
		for(int j=0; j<jsonText.length; j++) {
			JSONParser parser = new JSONParser();
			JSONObject obj;
			try {
				obj = (JSONObject) parser.parse(jsonText[j]);
				JSONArray stops = (JSONArray) obj.get("stops");
				for(int g = 0; g<stops.size(); g++)
				{
					JSONArray obx = (JSONArray) stops.get(g);
					for(int i = 0; i< obx.size(); i++) {
						JSONObject obb = (JSONObject) obx.get(i);
						Long sp = (Long) obb.get("stop_position");
						Long st = (Long) obb.get("stop");
						String st1 = (String) obb.get("ref");
						String st2 = (String) obb.get("name_operator");
						wtch.addStop(st1, st2);
						if(sp != null) {
							prims0.add(new SimplePrimitiveId(sp, OsmPrimitiveType.NODE));
						}
						if(st != null) {
							prims0.add(new SimplePrimitiveId(st, OsmPrimitiveType.NODE));
						}
					}
				}
				addToPrims0("masters", obj);
				addToPrims0("slaves", obj);
				addToPrims0("parent", obj);
				/*
				JSONArray array = (JSONArray) obj;
				for (int i = 0; i < array.size(); i++) {
					JSONObject obb = (JSONObject) array.get(i);
					relationList.add(new SingleRelationBuilder(obb, j*1000));
				}*/
			} catch (ParseException e2) {
				e2.printStackTrace();
			}
		}
	}
	public Set<PrimitiveId> getNeccesaryPrimitives() {
		return prims0;
	}
	public List<Long> getExistRelations() {
		return prims1;
	}
}
