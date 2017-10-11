package org.openstreetmap.josm.plugins.EasyRoutes.RelationsBuilder;

import org.openstreetmap.josm.data.osm.OsmPrimitiveType;

public class RelationMemberBuilder {
	public RelationMemberBuilder(long id, String role,
			OsmPrimitiveType category) {
		super();
		this.id = id;
		this.role = role;
		this.category = category;
	}
	
	public RelationMemberBuilder(long id, String role,
			String category) {
		super();
		this.id = id;
		this.role = role;
		if(category=="R")
			this.category = OsmPrimitiveType.RELATION;
		if(category=="N")
			this.category = OsmPrimitiveType.NODE;
		if(category=="W")
			this.category = OsmPrimitiveType.WAY;
	}

	public long id;
	public String role;
	public OsmPrimitiveType category;

	public OsmPrimitiveType getPrimitiveType() {
		return category;
	}
}
