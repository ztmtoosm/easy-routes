package org.openstreetmap.josm.plugins.EasyRoutes;

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
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListenerAdapter;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.NodeConnectException;
import org.openstreetmap.josm.tools.ImageProvider;

public class RoutingLayer extends Layer implements DataSetListenerAdapter.Listener, WaySplitterDataListener {
	List<Node> crucialNodes;
	List<List <Node> > middleNodes;
	Node selectedNode;
	public RoutingSpecial ws;
	MapView mv;
	String desc;
	List<OsmPrimitive> otherNodes;

	public List<List<Node>> getMiddleNodes() {
		return middleNodes;
	}
	public Collection<Node> getAllNodes() {
		if(middleNodes == null)
			return null;
		Set <Node> wynik = new TreeSet<>();
		for(int i=0; i<middleNodes.size(); i++) {
			List <Node> mid = middleNodes.get(i);
			if(mid != null)
				for(int j=0; j<mid.size(); j++) {
					Node akt = mid.get(j);
					wynik.add(akt);
				}
		}
		return wynik;
	}
	

	
	public RoutingLayer(List<Node> crucialNodes, List<OsmPrimitive> otherNodes, String description, RoutingSpecial ws) {
		super(description);
		this.otherNodes = otherNodes;
		setVisible(false);
		desc = description;
		this.crucialNodes = crucialNodes;
		this.ws = ws;
		ws.registerListener(this);
		odswiez();
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
		if(this.crucialNodes.size()>(z+1)) {
			Node left = crucialNodes.get(z);
			Node right = crucialNodes.get(z+1);
			List <Node> tmp = new ArrayList<>();
			tmp.add(left);
			tmp.add(right);
			try {
				middleNodes.set(z, ws.completeNetwork(tmp, false));
			} catch (NodeConnectException e) {
				middleNodes.set(z, null);
			}
		}
		if(z!=0) {
			Node left = crucialNodes.get(z-1);
			Node right = crucialNodes.get(z);
			List <Node> tmp = new ArrayList<>();
			tmp.add(left);
			tmp.add(right);
			try {
				middleNodes.set(z-1, ws.completeNetwork(tmp, false));
			} catch (NodeConnectException e) {
				middleNodes.set(z-1, null);
			}
		}
		Main.map.repaint();
	}
	
	private int getMiddleNodeId(Node n) {
		boolean containsAll = false;
		for(int i=0; i<middleNodes.size(); i++) {
			if(middleNodes.get(i)!=null && middleNodes.get(i).contains(n))
				containsAll = true;
		}
		if(!containsAll)
			return -1;
		if(crucialNodes.contains(n))
			return -1;
		for(int i=0; i<middleNodes.size(); i++) {
			if(middleNodes.get(i)!=null && middleNodes.get(i).contains(n))
				return i+1;
		}
		return -1;
	}
	
	public void addMiddleNode() {
		Node n = selectedNode;
		int id = getMiddleNodeId(n);
		if(id==-1)
			return;
		List <Node> newCrucial = new ArrayList<>();
		List <List<Node> > newMiddle = new ArrayList<>();
		for(int i=0; i<id; i++) {
			newCrucial.add(crucialNodes.get(i));
			if(i<(id-1)) {
				newMiddle.add(middleNodes.get(i));
			}
		}
		List <Node> partial1 = middleNodes.get(id-1).subList(0, middleNodes.get(id-1).indexOf(n)+1);
		List <Node> partial2 = middleNodes.get(id-1).subList(middleNodes.get(id-1).indexOf(n), middleNodes.get(id-1).size());
		newMiddle.add(partial1);
		newMiddle.add(partial2);
		newCrucial.add(n);
		for(int i=id; i<crucialNodes.size(); i++) {
			newCrucial.add(crucialNodes.get(i));
			if(i<crucialNodes.size()-1)
				newMiddle.add(middleNodes.get(i));
		}
		crucialNodes = newCrucial;
		middleNodes = newMiddle;
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

	static void drawLine(MapView view, Graphics2D g, List<Node> tmpNodes) {
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
			if(otherNodes.get(i).getType()==OsmPrimitiveType.NODE)
			{
				Node akt = (Node) otherNodes.get(i);
				g.setColor(poczatkowy);
				Point pa = mv.getPoint(akt.getCoor());
				g.fillOval(pa.x - 10, pa.y - 10, 20, 20);	
			}
			if(otherNodes.get(i).getType()==OsmPrimitiveType.WAY)
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
		if (middleNodes == null)
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
		for (Node n : crucialNodes) {
			Point pa = mv.getPoint(n.getCoor());
			double dist = pa.distance(pp);
			if (dist < distance) {
				wynik = n;
			}
		}
		if (wynik == null) {

			for (Node n : getAllNodes()) {
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

	public List<Way> splitWays(List<String> tmp) throws NodeConnectException {
			ws.splitWays(crucialNodes);
			List<Way> xd = ws.getWaysAfterSplit(crucialNodes, tmp);
			return xd;
	}
	public List<Way> splitWays() throws NodeConnectException {
		System.out.println("SPLIT WAYS --->>>>> >>>> >>>>>");
		List<String> tmp = null;
		return splitWays(tmp);
	}
	public void odswiez() {
		middleNodes = new ArrayList<>();
		boolean cale = true;
		for(int i=0; i<crucialNodes.size()-1; i++) {
			Node left = crucialNodes.get(i);
			Node right = crucialNodes.get(i+1);
			List <Node> tmp = new ArrayList<>();
			tmp.add(left);
			tmp.add(right);
			try {
				middleNodes.add(ws.completeNetwork(tmp, false));
			} catch (NodeConnectException e) {
				middleNodes.add(null);
				setName(desc+" NIEPOŁĄCZONE LINIE!");
				cale = false;
			}
		}
		if(cale)
			setName(desc);
		Main.map.repaint();
	}

	@Override
	public void onWaySplitterDataChange() {
		odswiez();
	}
}