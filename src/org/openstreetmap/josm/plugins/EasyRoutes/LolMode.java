package org.openstreetmap.josm.plugins.EasyRoutes;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;

public class LolMode extends MapMode {

MapView mv;
    public LolMode(MapFrame mapFrame) {
        // TODO Use constructor with shortcut
        super(tr("aaaa"), "dzik",
                tr("cccccccc"),
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

    @Override public void mouseClicked(MouseEvent e) {
    	Point x = e.getPoint();
    	Layer y = Main.main.getActiveLayer();
    	LatLon ll = Main.map.mapView.getLatLon(x.x, x.y);
    	System.out.println(x.x+" "+x.y);
    	DataSet ds = Main.main.getCurrentDataSet();
    	Node n = new Node(ll);
    	Map<String, String> keys = new HashMap();
    	keys.put("highway", "bus_stop");
    	n.setKeys(keys);
    	ds.addPrimitive(n);
    }
    @Override public boolean layerIsSupported(Layer l) {
        return true;
    }

}