package org.openstreetmap.josm.plugins.EasyRoutes.StopWatch;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;

public class SingleStop {
	long stopId = 0;
	long stopPositionId = 0;
	long platformId = 0;
	OsmPrimitiveType platformType = OsmPrimitiveType.NODE;
	String operatorName = "";
	String refId = "";
	DataSet ds;
	SingleStop(DataSet ds) {
		this.ds = ds;
	}
	public String getRefId() { return refId; }
	public long getStopId() {
		return stopId;
	}
	public long getPlatformId() {
		return platformId;
	}
	public long getStopPositionId() {
		return stopPositionId;
	}
	public OsmPrimitiveType getPlatformType() {
		return platformType;
	}
	public LatLon getGeneralCoordinates() {
		OsmPrimitive p1 = ds.getPrimitiveById(stopId, OsmPrimitiveType.NODE);
		if (p1 != null) {
			Node g = (Node) p1;
			return g.getCoor();
		}
		OsmPrimitive p2 = ds.getPrimitiveById(stopPositionId, OsmPrimitiveType.NODE);
		if (p2 != null) {
			Node g = (Node) p2;
			return g.getCoor();
		}
		return null;
	}
	public String getName() {
		Node x = null;
		if (stopId != 0) {
			x = (Node) ds.getPrimitiveById(stopId, OsmPrimitiveType.NODE);
		} else if (stopPositionId != 0) {
			x = (Node) ds.getPrimitiveById(stopPositionId, OsmPrimitiveType.NODE);
		}
		if (x == null) {
			return "";
		} else {
			if(x.getName() != null)
				return x.getName();
			else
				return "";
		}
	}

	public boolean isUseless() {
		if(refId == "") {
			return true;
		}
		if(stopPositionId == 0) {
			return true;
		}
		return false;
	}

	public String getNameShort() {
		return getName().replaceAll("([0-9]+)\\s*$", "");
	}
}
