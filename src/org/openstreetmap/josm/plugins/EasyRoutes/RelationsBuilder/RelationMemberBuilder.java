package org.openstreetmap.josm.plugins.EasyRoutes.RelationsBuilder;


public class RelationMemberBuilder {
	public RelationMemberBuilder(long id, String role,
			RelationMemberType category) {
		super();
		this.id = id;
		this.role = role;
		this.category = category;
	}

	public long id;
	public String role;

	public enum RelationMemberType {
		NODE, WAY, RELATION;
	}

	public RelationMemberType category;

	public static RelationMemberType createCategory(String category2) {
		if("N".equals(category2))
			return RelationMemberType.NODE;
		if("W".equals(category2))
			return RelationMemberType.WAY;
		return RelationMemberType.RELATION;
	}
	
}
