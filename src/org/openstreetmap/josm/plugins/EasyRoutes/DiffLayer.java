package org.openstreetmap.josm.plugins.EasyRoutes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.EasyRoutes.DiffLayerTech.Pair;
import org.openstreetmap.josm.tools.ImageProvider;

public class DiffLayer extends Layer {
	MapView mv;
	List <DiffLayerTech> techLayers;
	public DiffLayer(List <DiffLayerTech> techLayers) {
		super("DIFF LAYER");
		this.techLayers=techLayers;
	}
	
	private void drawLine(Graphics2D g, Node a, Node b) {
		List <Node> tmpNodes = new ArrayList<>();
		tmpNodes.add(a);
		tmpNodes.add(b);
		RoutingLayer.drawLine(mv, g, tmpNodes);
	}
	
	@Override
	public void paint(Graphics2D g, MapView mv, Bounds box) {
		for(DiffLayerTech tl : techLayers)
			tl.update();
		this.mv = mv;
		g.setColor(Color.GREEN);
		Stroke str = new BasicStroke(8, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		g.setStroke(str);
		for(DiffLayerTech tl : techLayers) {
			for(Pair<Node, Node> xyz : tl.pairsLayers) {
				if(!tl.pairsRelations.contains(xyz)) {
					drawLine(g, xyz.getFirst(), xyz.getSecond());
				}
			}
		}
		g.setColor(Color.PINK);
		Stroke str2 = new BasicStroke(6, BasicStroke.CAP_ROUND,
		BasicStroke.JOIN_ROUND);
		g.setStroke(str2);
		for(DiffLayerTech tl : techLayers) {
			for(Pair<Node, Node> xyz : tl.pairsRelations) {
				if(!tl.pairsLayers.contains(xyz)) {
					drawLine(g, xyz.getFirst(), xyz.getSecond());
				}
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