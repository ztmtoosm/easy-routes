package org.openstreetmap.josm.plugins.EasyRoutes.Routing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.util.*;

import javax.swing.Action;
import javax.swing.Icon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListenerAdapter;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.NodeConnectException;
import org.openstreetmap.josm.tools.ImageProvider;

public class RoutingLayer extends Layer implements DataSetListenerAdapter.Listener {

	Node selectedNode;
	MapView mv;
	String desc;
	List<OsmPrimitive> otherNodes = new ArrayList<>();

	RoutingBis routingBis;
	

	
	public RoutingLayer(String description, RoutingBis routingBis) {
		super(description);
		//TODO
		setVisible(false);
		desc = description;
		this.routingBis = routingBis;
	}

	public void dragAction(int x, int y) throws NodeConnectException {
		if (mv == null || selectedNode == null)
			return;
		if (!routingBis.crucialNodes.contains(selectedNode))
			return;

		LatLon gg = mv.getLatLon(x, y);
		Node nodeAfterDrag = routingBis.getClosestPoint(gg);
		if (nodeAfterDrag == null)
			return;

		routingBis.drag(nodeAfterDrag, selectedNode);

		Main.map.repaint();
	}

	public void addMiddleNode() {
		routingBis.addMiddleNode(selectedNode);
		Main.map.repaint();
	}
	
	@Override
	public Icon getIcon() {
		return ImageProvider.get("layer", "osmdata_small");
	}

	@Override
	public Object getInfoComponent() {
		return null;
	}

	@Override
	public Action[] getMenuEntries() {
		return new Action[0];
	}

	@Override
	public String getToolTipText() {
		return "easy-routes layer";
	}

	@Override
	public boolean isMergable(Layer other) {
		return false;
	}

	@Override
	public void mergeFrom(Layer from) {
		// Merging is not supported
	}

	static public void drawLine(MapView view, Graphics2D g, List<Node> tmpNodes) {
		if(tmpNodes==null)
			return;
		for (int i = 0; i < tmpNodes.size() - 1; i++) {
			LatLon alfa = tmpNodes.get(i).getCoor();
			LatLon beta = tmpNodes.get(i + 1).getCoor();
			Point pa = view.getPoint(alfa);
			Point pb = view.getPoint(beta);
			g.draw(new Line2D.Double(pa, pb));
		}
	}

	@Override
	public void paint(Graphics2D g, MapView mv, Bounds bounds) {
		Color poczatkowy = new Color(255, 200, 0, 128);
		for(int i=0; i<otherNodes.size(); i++)
		{
			if(otherNodes.get(i).getType() == OsmPrimitiveType.NODE)
			{
				Node akt = (Node) otherNodes.get(i);
				g.setColor(poczatkowy);
				Point pa = mv.getPoint(akt.getCoor());
				g.fillOval(pa.x - 10, pa.y - 10, 20, 20);	
			}
			if(otherNodes.get(i).getType() == OsmPrimitiveType.WAY)
			{
				//smr.drawWay(way, color, line, dashes, dashedColor, offset, showOrientation, showHeadArrowOnly, showOneway, onewayReversed);
				//smr.drawWay((Way) otherNodes.get(i), poczatkowy, new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), poczatkowy, 0, false, false, false, true);
				/*
				Node akt = (Node) otherNodes.get(i);
				g.setColor(poczatkowy);
				Point pa = mv.getPoint(akt.getCoor());
				g.fillOval(pa.x - 10, pa.y - 10, 20, 20);	*/			
			}
		}
		
		boolean selected = true;
		this.mv = mv;
		if (routingBis.getMiddleNodes() == null)
			return;
		Color c = new Color(128, 128,128);
		if(selected)
			c = new Color(255, 0, 0);
		Font font = new Font("SansSerif", Font.BOLD, 20);
		Stroke str = new BasicStroke(8, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		g.setStroke(str);
		g.setFont(font);
		g.setColor(Color.WHITE);
		boolean cale = true;
		List<List<Node>> middleNodes = routingBis.getMiddleNodes();
		for(int j=0; j<middleNodes.size(); j++) {
			List<Node> tmpNodes = middleNodes.get(j);
			if(middleNodes.get(j)==null)
			{
				setName(desc+" NIEPOŁĄCZONE LINIE!");
				cale = false;
			}
			drawLine(mv, g, tmpNodes);
		}
		if(cale)
			setName(desc);
		Stroke str2 = new BasicStroke(5, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		g.setStroke(str2);
		g.setColor(c);
		for(int j=0; j<middleNodes.size(); j++) {
			List<Node> tmpNodes = middleNodes.get(j);
			drawLine(mv, g, tmpNodes);
		}
		if (selectedNode != null) {
			g.setColor(c);
			Point pa = mv.getPoint(selectedNode.getCoor());
			g.fillOval(pa.x - 10, pa.y - 10, 20, 20);
		}
		for (int i = 0; i < routingBis.crucialNodes.size(); i++) {
			LatLon alfa = routingBis.crucialNodes.get(i).getCoor();
			Point pa = mv.getPoint(alfa);
			g.setColor(c);
			if (routingBis.crucialNodes.get(i) == selectedNode) {
				g.setColor(Color.BLACK);
				g.fillOval(pa.x - 12, pa.y - 12, 24, 24);
			} else {
				g.fillOval(pa.x - 6, pa.y - 6, 12, 12);
			}
			g.setColor(Color.WHITE);
			g.drawString(String.valueOf(i), pa.x + 2, pa.y + 12);
			g.setColor(new Color(190, 190, 190));
			if(selected)
				g.setColor(new Color(0, 0, 255));
			g.drawString(String.valueOf(i), pa.x, pa.y + 10);
		}
	}

	@Override
	public void visitBoundingBox(BoundingXYVisitor v) {

	}

	@Override
	public void processDatasetEvent(AbstractDatasetChangedEvent event) {
	}

	public void setProposedPoint(int x, int y) {
		if (mv == null)
			return;
		Point pp = new Point(x, y);
		double distance = 20;
		Node wynik = null;
		for (Node n : routingBis.crucialNodes) {
			Point pa = mv.getPoint(n.getCoor());
			double dist = pa.distance(pp);
			if (dist < distance) {
				wynik = n;
			}
		}
		if (wynik == null) {

			for (Node n : routingBis.getAllNodes()) {
				Point pa = mv.getPoint(n.getCoor());
				double dist = pa.distance(pp);
				if (dist < distance) {
					wynik = n;
				}
			}
		}
		if (selectedNode != wynik) {
			selectedNode = wynik;
			Main.map.repaint();
		}
	}

	public void refresh(boolean cale) {
		if(cale)
			setName(desc);
		else
			setName(desc+" NIEPOŁĄCZONE LINIE!");
		Main.map.repaint();
	}
}