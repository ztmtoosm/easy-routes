package org.openstreetmap.josm.plugins.EasyRoutes.RelationsBuilder;

import java.util.*;

import javafx.util.Pair;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.SimplePrimitiveId;
import org.openstreetmap.josm.plugins.EasyRoutes.StopWatcher.SingleStop;
import org.openstreetmap.josm.plugins.EasyRoutes.StopWatcher.StopWatcher;

public class FirstRelationsBuilder {
	DataSet ds;
	long currentId = -1000;
	String[] jsonTextInternal;
	RelationsBuilder nextBuilder;
	Set<PrimitiveId> prims0 = new HashSet<>();
	StopWatcher wtch;
	List<Long> prims1 = new LinkedList<Long>();

	void addToPrims0(String key, JSONObject obj) {
		JSONArray master = (JSONArray) obj.get(key);
		for (int i = 0; i < master.size(); i++) {
			Long sp = (Long) master.get(i);
			if (sp != null) {
				prims0.add(new SimplePrimitiveId(sp, OsmPrimitiveType.RELATION));
				prims1.add(Long.valueOf(sp));
			}
		}
	}

	public Collection<SingleStop> getUnsupportedStops() {
		return wtch.getUnsupportedStops();
	}

	private void prepareChildRelation(Map<String, String> tags,
			SingleRelationBuilder builder, long parentId, String[] refStopArray) {
		Map<String, String> tags2 = new HashMap<String, String>();
		tags2.putAll(tags);

		List<SingleStop> st = new ArrayList<>();
		for (String str : refStopArray) {
			st.add(wtch.getSingleStop(str));
		}
		String nn = tags2.get("name");
		nn += wtch.getSingleStop(refStopArray[0]).getNameShort();
		nn += " => ";
		nn += wtch.getSingleStop(refStopArray[refStopArray.length-1]).getNameShort();
		tags2.put("name", nn);
		tags2.put("from", wtch.getSingleStop(refStopArray[0]).getNameShort());
		tags2.put("to", wtch.getSingleStop(refStopArray[refStopArray.length-1]).getNameShort());
		builder.tagsFromJson(tags2);
		builder.trackFromJson(st);
		builder.parentRelFromJson(parentId);
		builder.relMembFromJson(st);
	}

	private void prepareParentRelation(Map<String, String> tags,
			SingleRelationBuilder builder, long parentId, long[] childs) {
		builder.tagsFromJson(tags);
		builder.parentRelFromJson(parentId);
		builder.relMembFromJson(childs);
	}

	void prepareLine(String str, List<SingleRelationBuilder> relationList) {
		JSONParser parser = new JSONParser();
		try {
			JSONObject obj = (JSONObject) parser.parse(str);
			JSONArray mastersArray = (JSONArray) obj.get("masters");
			JSONArray slavesArray = (JSONArray) obj.get("slaves");
			JSONArray parentArray = (JSONArray) obj.get("parent");
			JSONArray stopsArray = (JSONArray) obj.get("stops");
			String typeSmall = (String) obj.get("type_small");
			String typeLarge = (String) obj.get("type_large");
			long[] slaves = new long[stopsArray.size()];
			long parent = 0;
			long master = 0;
			if (mastersArray.size() > 0) {
				master = (Long) mastersArray.get(0);
			} else {
				master = currentId;
				currentId--;
			}
			if (parentArray.size() > 0) {
				parent = (Long) parentArray.get(0);
			} else {
				return;
			}
			for (int i = 0; i < Math.min(stopsArray.size(), slavesArray.size()); i++) {
				slaves[i] = (Long) slavesArray.get(i);
			}
			for (int i = Math.min(stopsArray.size(), slavesArray.size()); i < stopsArray
					.size(); i++) {
				slaves[i] = currentId;
				currentId--;
			}
			for (int i = Math.min(stopsArray.size(), slavesArray.size()); i < slavesArray.size(); i++) {
				SingleRelationBuilder childRel = new SingleRelationBuilder(
						(long) slavesArray.get(i), true, ds);
				relationList.add(childRel);
			}
			SingleRelationBuilder masterRel = new SingleRelationBuilder(master,
					false, ds);
			relationList.add(masterRel);
			Map<String, String> tags = new HashMap<String, String>();
			Map<String, String> tags2 = new HashMap<String, String>();
			tags2.put("type", "route");
			tags.put("type", "route_master");
			tags2.put("public_transport:version", "2");
			tags.put("network", "ZTM Warszawa");
			tags2.put("network", "ZTM Warszawa");
			tags2.put("name", typeLarge + " " + obj.get("line") + ": ");
			tags2.put("route", typeSmall);
			tags.put("route_master", typeSmall);
			tags2.put("ref", (String) obj.get("line"));
			tags.put("ref", (String) obj.get("line"));
			tags.put("name", typeLarge + " " + obj.get("line"));
			prepareParentRelation(tags, masterRel, parent, slaves);
			for (int i = 0; i < stopsArray.size(); i++) {
				JSONArray c1 = (JSONArray) stopsArray.get(i);
				String[] refStopArray = new String[c1.size()];
				for (int j = 0; j < c1.size(); j++) {
					JSONObject c2 = (JSONObject) c1.get(j);
					refStopArray[j] = (String) c2.get("ref");
				}
				SingleRelationBuilder childRel = new SingleRelationBuilder(
						slaves[i], false, typeSmall, ds);
				relationList.add(childRel);
				prepareChildRelation(tags2, childRel, master, refStopArray);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Collection<LatLon> getCurrentLatLon() {
		return wtch.getCurrentLatLon();
	}

	public RelationsBuilder createRelationsBuilder() {
		List<SingleRelationBuilder> relationList = new ArrayList<SingleRelationBuilder>();
		for (String str : jsonTextInternal) {
			prepareLine(str, relationList);
		}
		if(ds == null)
			nextBuilder = new RelationsBuilder(relationList);
		else
			nextBuilder = new RelationsBuilder(relationList, ds);
		return nextBuilder;
	}

	public FirstRelationsBuilder(String[] jsonText, DataSet ds) {
		this.ds = ds;
		List <Pair <String, String> > toGenerateWtch = new ArrayList<>();
		jsonTextInternal = jsonText;
		for (int j = 0; j < jsonText.length; j++) {
			JSONParser parser = new JSONParser();
			JSONObject obj;
			try {
				obj = (JSONObject) parser.parse(jsonText[j]);
				JSONArray stops = (JSONArray) obj.get("stops");
				for (int g = 0; g < stops.size(); g++) {
					JSONArray obx = (JSONArray) stops.get(g);
					for (int i = 0; i < obx.size(); i++) {
						JSONObject obb = (JSONObject) obx.get(i);
						Long sp = (Long) obb.get("stop_position");
						Long st = (Long) obb.get("stop");
						String st1 = (String) obb.get("ref");
						String st2 = (String) obb.get("name_operator");
						toGenerateWtch.add(new Pair<String, String> (st1, st2));
						if (sp != null) {
							prims0.add(new SimplePrimitiveId(sp,
									OsmPrimitiveType.NODE));
						}
						if (st != null) {
							prims0.add(new SimplePrimitiveId(st,
									OsmPrimitiveType.NODE));
						}
					}
				}
				addToPrims0("masters", obj);
				addToPrims0("slaves", obj);
				addToPrims0("parent", obj);
			} catch (ParseException e2) {
				e2.printStackTrace();
			}
		}
		wtch = new StopWatcher(ds, toGenerateWtch);
	}

	public Set<PrimitiveId> getNeccesaryPrimitives() {
		return prims0;
	}

	public List<Long> getExistRelations() {
		return prims1;
	}
}
