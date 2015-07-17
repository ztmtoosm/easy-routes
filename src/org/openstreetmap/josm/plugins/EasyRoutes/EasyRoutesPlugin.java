package org.openstreetmap.josm.plugins.EasyRoutes;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class EasyRoutesPlugin extends Plugin implements LayerChangeListener {
	private static EasyRoutesPreference preferences;

	static JMenu jMenu;

	private Collection<String> createCol(String[] a) {
		Collection<String> wynik = new ArrayList<String>();
		for (int i = 0; i < a.length; i++) {
			wynik.add(a[i]);
		}
		return wynik;
	}

	private Collection<Collection<String>> defaultPref() {
		Collection<Collection<String>> wynik = new ArrayList<Collection<String>>();
		wynik.add(createCol(new String[] { "highway", "motorway", "0.8", "-1" }));
		wynik.add(createCol(new String[] { "highway", "motorway_link", "0.8",
				"-1" }));
		wynik.add(createCol(new String[] { "highway", "trunk", "0.8", "0.8" }));
		wynik.add(createCol(new String[] { "highway", "primary", "0.9", "0.9" }));
		wynik.add(createCol(new String[] { "highway", "primary_link", "0.9",
				"0.9" }));
		wynik.add(createCol(new String[] { "highway", "secondary", "1", "1" }));
		wynik.add(createCol(new String[] { "highway", "secondary_link", "1",
				"1" }));
		wynik.add(createCol(new String[] { "highway", "tertiary", "1", "1" }));
		wynik.add(createCol(new String[] { "highway", "tertiary_link", "1", "1" }));
		wynik.add(createCol(new String[] { "highway", "residential", "1.3",
				"1.3" }));
		wynik.add(createCol(new String[] { "highway", "service", "1.3", "1.3" }));
		wynik.add(createCol(new String[] { "oneway", "yes", "", "-1" }));
		wynik.add(createCol(new String[] { "oneway", "-1", "-1", "" }));
		wynik.add(createCol(new String[] { "junction", "roundabout", "", "-1" }));
		return wynik;
	}

	public EasyRoutesPlugin(PluginInformation info) {
		super(info);

		Preferences p = Main.pref;
		Collection<Collection<String>> pdd = p.getArray("easy-routes.weights");
		if (pdd == null || pdd.size() == 0) {
			p.putArray("easy-routes.weights", defaultPref());
		}
		JMenu jMenu = Main.main.menu.toolsMenu;
		jMenu.add(new JMenuItem(new ConnectNodesAction()));
		preferences = (EasyRoutesPreference) new EasyRoutesPreference.Factory()
				.createPreferenceSetting();
		jMenu.add(new JMenuItem(new LayNodesAction()));
		jMenu.add(new JMenuItem(new Con2NoAc()));
		jMenu.add(new JMenuItem(new DownloadAlongWayAction()));
	}

	@Override
	public PreferenceSetting getPreferenceSetting() {
		return preferences;
	}

	@Override
	public void activeLayerChange(Layer oldLayer, Layer newLayer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void layerAdded(Layer newLayer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void layerRemoved(Layer oldLayer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		if (newFrame != null) {
			ZiwAction addRouteNodeAction = new ZiwAction(newFrame);
			IconToggleButton removeRouteNodeButton = new IconToggleButton(
					addRouteNodeAction);
			newFrame.addMapMode(removeRouteNodeButton);
		}
	}
}