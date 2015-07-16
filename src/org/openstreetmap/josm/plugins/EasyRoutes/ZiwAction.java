package org.openstreetmap.josm.plugins.EasyRoutes;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.MouseEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.NodeConnectException;
import org.openstreetmap.josm.tools.ImageProvider;


public class ZiwAction extends MapMode {

MapView mv;
    public ZiwAction(MapFrame mapFrame) {
        // TODO Use constructor with shortcut
        super(tr("Routing"), "dzik",
                tr("Click and drag to move destination"),
                mapFrame, ImageProvider.getCursor("normal", "move"));
    }

    private boolean dragged = false;

    @Override public void enterMode() {
        super.enterMode();
        Main.map.mapView.addMouseListener(this);
        Main.map.mapView.addMouseMotionListener(this);
    }

    @Override public void exitMode() {
        super.exitMode();
        Main.map.mapView.removeMouseListener(this);
    }

    @Override public void mousePressed(MouseEvent e) {
    	System.out.println(e.getY()+" P "+e.getX());
    }

    @Override public void mouseReleased(MouseEvent e) {
    	if(dragged) {
    	Layer y = Main.main.getActiveLayer();
    	if(y.getClass()==XyzLayer.class) {
    		XyzLayer yy = (XyzLayer)(y);
    		try {
				yy.dragAction(e.getX(), e.getY());
			} catch (NodeConnectException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    	}
    	}
    	dragged = false;
    }

    @Override public void mouseDragged(MouseEvent e) {
    	dragged = true;

    }
    @Override public void mouseClicked(MouseEvent e) {
    	Layer y = Main.main.getActiveLayer();
    	if(y.getClass()==XyzLayer.class) {
    		XyzLayer yy = (XyzLayer)(y);
    		yy.addMiddleNode();
    	}
    	
    }
    @Override public void mouseMoved(MouseEvent e) {
    	Layer y = Main.main.getActiveLayer();
    	if(y.getClass()==XyzLayer.class) {
    		XyzLayer yy = (XyzLayer)(y);
    		yy.setProposedPoint(e.getX(), e.getY());
    	}
    	System.out.println(e.getY()+" motion "+e.getX());
    }

    @Override public boolean layerIsSupported(Layer l) {
        return true;
    }

}