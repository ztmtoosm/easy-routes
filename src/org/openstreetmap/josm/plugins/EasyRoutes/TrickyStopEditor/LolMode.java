package org.openstreetmap.josm.plugins.EasyRoutes.TrickyStopEditor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Point;
import java.awt.event.MouseEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;

public class LolMode extends MapMode {

MapView mv;
PrzystankiAction act;
    public LolMode(MapFrame mapFrame, PrzystankiAction act) {
        // TODO Use constructor with shortcut
        super(tr("aaaa"), "dzik",
                tr("cccccccc"),
                mapFrame, ImageProvider.getCursor("normal", "move"));
        this.act = act;
    }

    @Override public void enterMode() {
        super.enterMode();
        Main.map.mapView.addMouseListener(this);
        Main.map.mapView.addMouseMotionListener(this);
    }

    @Override public void exitMode() {
        super.exitMode();
        Main.map.mapView.removeMouseListener(this);
        act.onExitMode();
    }

    @Override public void mouseClicked(MouseEvent e) {
    	Point x = e.getPoint();
    	Layer y = Main.getLayerManager().getActiveLayer();
    	LatLon ll = Main.map.mapView.getLatLon(x.x, x.y);
    	act.receiveClickedLatLon(ll);
    }
    @Override public boolean layerIsSupported(Layer l) {
        return true;
    }

}