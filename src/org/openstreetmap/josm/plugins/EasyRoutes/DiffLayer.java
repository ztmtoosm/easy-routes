package org.openstreetmap.josm.plugins.EasyRoutes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.Icon;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;

public class DiffLayer extends Layer {
	MapView mv;
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
	
	
	List<Relation> oldRelations;
	List<RoutingLayer> layers;
	Set<Pair<Node, Node> > pairsLayers;
	Set<Pair<Node, Node> > pairsRelations;	
	public DiffLayer(List<Relation> oldRelations, List<RoutingLayer> layers) {
		super("DIFF LAYER");
		this.oldRelations=oldRelations;
		this.layers=layers;
		// TODO Auto-generated constructor stub
	}
	
	private void extractLayer(RoutingLayer layer) {
		List<List <Node> > genList = layer.getMiddleNodes();
		if(genList==null)
			return;
		for(List<Node> tmp : genList) {
			if(tmp!=null) {
				for(int i=0; i<tmp.size()-1; i++) {
					Node a = tmp.get(i);
					Node b = tmp.get(i+1);
					Pair <Node, Node> tmp2 = new Pair<>(a,b);
					pairsLayers.add(tmp2);
				}
			}
		}
	}
	
	private void extractRelation(Relation rel) {
		List <RelationMember> members = rel.getMembers();
		for(RelationMember x : members) {
			if(x.getRole().equals("forward") || x.getRole().equals("backward") || x.getRole().equals(""))
				if(x.getType()==OsmPrimitiveType.WAY) {
					Way w = x.getWay();
					List <Node> y = w.getNodes();
					for(int i=0; i<y.size()-1; i++) {
						Node a = y.get(i);
						Node b = y.get(i+1);
						Pair <Node, Node> tmp2 = new Pair<>(a,b);
						pairsRelations.add(tmp2);
					}
				}
		}
	}
	
	private void update() {
		pairsLayers = new HashSet<>();
		pairsRelations = new HashSet<>();
		for(RoutingLayer foo : layers) {
			if(foo!=null)
				extractLayer(foo);
		}
		for(Relation foo : oldRelations) {
			if(foo!=null)
				extractRelation(foo);
		}
	}
	
	private void drawLine(Graphics2D g, Node a, Node b) {
		List <Node> tmpNodes = new ArrayList<>();
		tmpNodes.add(a);
		tmpNodes.add(b);
		for (int i = 0; i < tmpNodes.size() - 1; i++) {
			LatLon alfa = tmpNodes.get(i).getCoor();
			LatLon beta = tmpNodes.get(i + 1).getCoor();
			Point pa = mv.getPoint(alfa);
			Point pb = mv.getPoint(beta);
			g.draw(new Line2D.Double(pa, pb));
		}
	}
	
	@Override
	public void paint(Graphics2D g, MapView mv, Bounds box) {
		update();
		this.mv = mv;
		g.setColor(Color.GREEN);
		Stroke str = new BasicStroke(8, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		g.setStroke(str);
		for(Pair<Node, Node> xyz : pairsLayers) {
			if(!pairsRelations.contains(xyz)) {
				drawLine(g, xyz.getFirst(), xyz.getSecond());
			}
		}
		g.setColor(Color.RED);
		for(Pair<Node, Node> xyz : pairsRelations) {
			if(!pairsLayers.contains(xyz)) {
				drawLine(g, xyz.getFirst(), xyz.getSecond());
			}
		}
	}

	@Override
	public Icon getIcon() {
		return ImageProvider.get("layer", "osmdata_small");
	}

	@Override
	public String getToolTipText() {
		// TODO Auto-generated method stub
		return "foo";
	}

	@Override
	public void mergeFrom(Layer from) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isMergable(Layer other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void visitBoundingBox(BoundingXYVisitor v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getInfoComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Action[] getMenuEntries() {
		return new Action[0];
	}

}