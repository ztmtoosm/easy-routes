package org.openstreetmap.josm.plugins.EasyRoutes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListenerAdapter;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.NodeConnectException;
import org.openstreetmap.josm.tools.ImageProvider;

public class XyzLayer extends Layer implements DataSetListenerAdapter.Listener {
	List<Node> crucialNodes;
	List<Node> allNodes;
	Node selectedNode;
	WaySplitter ws;
	MapView mv;

	public XyzLayer(List<Node> crucialNodes) throws NodeConnectException {
		super(new SimpleDateFormat("yyyyMMdd HHmmss")
				.format(new java.util.Date()));
		this.crucialNodes = crucialNodes;
		Collection<Collection<String>> aktPreferences = Main.pref
				.getArray("easy-routes.weights");
		ws = new WaySplitter(aktPreferences);
		allNodes = ws.completeNetwork(crucialNodes, true);
	}

	public void dragAction(int x, int y) throws NodeConnectException {
		if (ws == null || mv == null || selectedNode == null)
			return;
		if (!crucialNodes.contains(selectedNode))
			return;

		LatLon gg = mv.getLatLon(x, y);
		Node nowy = ws.getClosestPoint(gg);
		if (nowy == null)
			return;
		int z = this.crucialNodes.indexOf(selectedNode);
		this.crucialNodes.set(z, nowy);
		Collection<Collection<String>> aktPreferences = Main.pref
				.getArray("easy-routes.weights");
		allNodes = ws.completeNetwork(crucialNodes, true);
		Main.map.repaint();
	}
	
	private int getMiddleNodeId(Node n) {
		if(!allNodes.contains(n))
			return -1;
		if(crucialNodes.contains(n))
			return -1;
		int crucialCounter = 0;
		for(int i=0; i<allNodes.size(); i++) {
			if(crucialCounter<crucialNodes.size() && allNodes.get(i)==crucialNodes.get(crucialCounter)) {
				crucialCounter++;
			}
			if(allNodes.get(i)==n)
				return crucialCounter;
		}
		return -1;
	}
	
	public void addMiddleNode() {
		Node n = selectedNode;
		int id = getMiddleNodeId(n);
		if(id==-1)
			return;
		List <Node> newCrucial = new ArrayList<Node>();
		for(int i=0; i<id; i++) {
			newCrucial.add(crucialNodes.get(i));
		}
		newCrucial.add(n);
		for(int i=id; i<crucialNodes.size(); i++) {
			newCrucial.add(crucialNodes.get(i));
		}
		crucialNodes = newCrucial;
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
		return "Hiking routes";
	}

	@Override
	public boolean isMergable(Layer other) {
		return false;
	}

	@Override
	public void mergeFrom(Layer from) {
		// Merging is not supported
	}

	@Override
	public void paint(Graphics2D g, MapView mv, Bounds bounds) {
		this.mv = mv;
		if (allNodes == null)
			return;
		Color c = new Color(255, 0, 0);
		Font font = new Font("SansSerif", Font.BOLD, 20);
		Stroke str = new BasicStroke(8, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		g.setStroke(str);
		g.setFont(font);
		g.setColor(Color.WHITE);
		for (int i = 0; i < allNodes.size() - 1; i++) {
			LatLon alfa = allNodes.get(i).getCoor();
			LatLon beta = allNodes.get(i + 1).getCoor();
			Point pa = mv.getPoint(alfa);
			Point pb = mv.getPoint(beta);
			g.draw(new Line2D.Double(pa, pb));
		}
		Stroke str2 = new BasicStroke(5, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		g.setStroke(str2);
		g.setColor(Color.RED);
		for (int i = 0; i < allNodes.size() - 1; i++) {
			LatLon alfa = allNodes.get(i).getCoor();
			LatLon beta = allNodes.get(i + 1).getCoor();
			Point pa = mv.getPoint(alfa);
			Point pb = mv.getPoint(beta);
			g.draw(new Line2D.Double(pa, pb));
		}

		if (selectedNode != null) {
			g.setColor(c);
			Point pa = mv.getPoint(selectedNode.getCoor());
			g.fillOval(pa.x - 10, pa.y - 10, 20, 20);
		}
		for (int i = 0; i < crucialNodes.size(); i++) {
			LatLon alfa = crucialNodes.get(i).getCoor();
			Point pa = mv.getPoint(alfa);
			g.setColor(c);
			if (crucialNodes.get(i) == selectedNode) {
				g.setColor(Color.BLACK);
				g.fillOval(pa.x - 12, pa.y - 12, 24, 24);
			} else {
				g.fillOval(pa.x - 6, pa.y - 6, 12, 12);
			}
			g.setColor(Color.WHITE);
			g.drawString(String.valueOf(i), pa.x + 2, pa.y + 12);
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
		System.out.println("aaa");
		if (mv == null)
			return;
		System.out.println("bbb");
		Point pp = new Point(x, y);
		double distance = 20;
		Node wynik = null;
		for (Node n : crucialNodes) {
			Point pa = mv.getPoint(n.getCoor());
			double dist = pa.distance(pp);
			if (dist < distance) {
				wynik = n;
			}
		}
		if (wynik == null) {

			for (Node n : allNodes) {
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

}