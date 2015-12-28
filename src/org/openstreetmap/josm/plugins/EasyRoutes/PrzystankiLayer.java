package org.openstreetmap.josm.plugins.EasyRoutes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;

import javax.swing.Action;
import javax.swing.Icon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;

public class PrzystankiLayer extends Layer  {

	LatLon gg = null;
	LatLon ff = null;
	public void setLatLon(LatLon g, LatLon f)
	{
		gg = g;
		ff = f;
		Main.map.repaint();
	}
	
	public PrzystankiLayer() {
		super("PrzystankiLayer");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void paint(Graphics2D g, MapView mv, Bounds box) {
		Color c = new Color(128, 0, 0);
		Color c1 = new Color(0, 0, 128);
		Stroke str2 = new BasicStroke(5, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		g.setStroke(str2);
		if(gg != null)
		{
			g.setColor(c);
			Point pa = mv.getPoint(gg);
			g.fillOval(pa.x - 10, pa.y - 10, 10, 10);
		}
		if(ff != null)
		{
			g.setColor(c1);
			Point pb = mv.getPoint(ff);
			g.fillOval(pb.x - 10, pb.y - 10, 10, 10);
		}
	}

	@Override
	public Icon getIcon() {
		// TODO Auto-generated method stub
		return ImageProvider.get("layer", "osmdata_small");
	}

	@Override
	public String getToolTipText() {
		// TODO Auto-generated method stub
		return "gasfas";
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
		// TODO Auto-generated method stub
		return null;
	}

}
