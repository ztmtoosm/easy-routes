package org.openstreetmap.josm.plugins.EasyRoutes.Routing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.EasyRoutes.Routing.RoutingLayer;

public class DiffLayerTech {
	class Pair<A, B> {
	    private A first;
	    private B second;

	    public Pair(A first, B second) {
	    	super();
	    	this.first = first;
	    	this.second = second;
	    }

	    @Override
		public int hashCode() {
	    	int hashFirst = first != null ? first.hashCode() : 0;
	    	int hashSecond = second != null ? second.hashCode() : 0;

	    	return (hashFirst + hashSecond);
	    }
		private boolean equalsSwap(Object other) {
	    	if (other instanceof Pair) {
	    		Pair otherPair = (Pair) other;
	    		return 
	    		((  this.first == otherPair.second ||
	    			( this.first != null && otherPair.second != null &&
	    			  this.first.equals(otherPair.second))) &&
	    		 (	this.second == otherPair.first ||
	    			( this.second != null && otherPair.first != null &&
	    			  this.second.equals(otherPair.first))) );
	    	}

	    	return false;
	    }
		
	    @Override
		public boolean equals(Object other) {
	    	if(equalsSwap(other))
	    		return true;
	    	if (other instanceof Pair) {
	    		Pair otherPair = (Pair) other;
	    		return 
	    		((  this.first == otherPair.first ||
	    			( this.first != null && otherPair.first != null &&
	    			  this.first.equals(otherPair.first))) &&
	    		 (	this.second == otherPair.second ||
	    			( this.second != null && otherPair.second != null &&
	    			  this.second.equals(otherPair.second))) );
	    	}

	    	return false;
	    }

	    @Override
		public String toString()
	    { 
	           return "(" + first + ", " + second + ")"; 
	    }

	    public A getFirst() {
	    	return first;
	    }

	    public void setFirst(A first) {
	    	this.first = first;
	    }

	    public B getSecond() {
	    	return second;
	    }

	    public void setSecond(B second) {
	    	this.second = second;
	    }
	}
	
	
	private List<Relation> oldRelations;
	private List<RoutingBis> routingBiss;
	public Set<Pair<Node, Node> > pairsLayers;
	public Set<Pair<Node, Node> > pairsRelations;
	public String lineId;
	public DiffLayerTech(List<Relation> oldRelations, List<RoutingBis> layers, String lineId) {
		this.lineId = lineId;
		this.oldRelations=oldRelations;
		this.routingBiss=layers;
	}

	private void extract(List<Node> tmp, Set<Pair<Node, Node> > toAdd) {
		if(tmp!=null) {
			for(int i=0; i<tmp.size()-1; i++) {
				Node a = tmp.get(i);
				Node b = tmp.get(i+1);
				Pair <Node, Node> tmp2 = new Pair<>(a,b);
				toAdd.add(tmp2);
			}
		}
	}

	private void extractRoutingBis(RoutingBis routingBis) {
		List<List <Node> > genList = routingBis.getMiddleNodes();
		if(genList==null)
			return;
		for(List<Node> tmp : genList) {
			extract(tmp, pairsLayers);
		}
	}
	
	private void extractRelation(Relation rel) {
		List <RelationMember> members = rel.getMembers();
		for(RelationMember x : members) {
			if(x.getRole().equals("forward") || x.getRole().equals("backward") || x.getRole().equals(""))
				if(x.getType()==OsmPrimitiveType.WAY) {
					Way w = x.getWay();
					List <Node> y = w.getNodes();
					extract(y, pairsRelations);
				}
		}
	}
	
	public void update() {
		pairsLayers = new HashSet<>();
		pairsRelations = new HashSet<>();
		for(RoutingBis foo : routingBiss) {
			if(foo!=null)
				extractRoutingBis(foo);
		}
		for(Relation foo : oldRelations) {
			if(foo!=null)
				extractRelation(foo);
		}
	}
}