package org.openstreetmap.josm.plugins.EasyRoutes;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.MouseEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.EasyRoutes.Routing.RoutingLayer;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.NodeConnectException;
import org.openstreetmap.josm.tools.ImageProvider;


public class EditRoutingLayerAction extends MapMode {

MapView mv;
    public EditRoutingLayerAction(MapFrame mapFrame) {
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
    }

    @Override public void mouseReleased(MouseEvent e) {
    	if(dragged) {
    	Layer y = Main.getLayerManager().getActiveLayer();
    	if(y.getClass()==RoutingLayer.class) {
    		RoutingLayer yy = (RoutingLayer)(y);
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
    	Layer y = Main.getLayerManager().getActiveLayer();
    	if(y.getClass()==RoutingLayer.class) {
    		RoutingLayer yy = (RoutingLayer)(y);
    		yy.addMiddleNode();
    	}
    	
    }
    @Override public void mouseMoved(MouseEvent e) {
    	Layer y = Main.getLayerManager().getActiveLayer();
    	if(y.getClass()==RoutingLayer.class) {
    		RoutingLayer yy = (RoutingLayer)(y);
    		yy.setProposedPoint(e.getX(), e.getY());
    	}
    }

    @Override public boolean layerIsSupported(Layer l) {
        return true;
    }

}