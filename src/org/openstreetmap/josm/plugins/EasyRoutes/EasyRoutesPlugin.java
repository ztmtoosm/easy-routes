package org.openstreetmap.josm.plugins.EasyRoutes;

import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer.LayerStateChangeListener;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.EasyRoutes.TrickyStopEditor.PrzystankiAction;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class EasyRoutesPlugin extends Plugin implements LayerStateChangeListener {
	private static EasyRoutesPreference preferences;

	static JMenu jMenu;

	public EasyRoutesPlugin(PluginInformation info) {
		super(info);
		PreferenceGenerator.setPrefOnInit(PreferenceGenerator.serverPref(), "easy-routes.server");
		PreferenceGenerator.setPrefOnInit(PreferenceGenerator.typicalPref(), "easy-routes.weights");
		PreferenceGenerator.setPrefOnInit(PreferenceGenerator.typicalPref(), "easy-routes.weights.bus");
		PreferenceGenerator.setPrefOnInit(PreferenceGenerator.railwayPref(), "easy-routes.weights.tram");
		JMenu jMenu = Main.main.menu.addMenu("easy-routes","easy-routes", KeyEvent.VK_COMMA, Main.main.menu.getDefaultMenuPos(), "help");
		jMenu.add(new JMenuItem(new ConnectNodesAction()));
		preferences = (EasyRoutesPreference) new EasyRoutesPreference.Factory()
				.createPreferenceSetting();
		jMenu.add(new JMenuItem(new LayNodesAction()));
		jMenu.add(new JMenuItem(new Con2NoAc()));
		jMenu.add(new JMenuItem(new ZtmToOsmAction()));
		jMenu.add(new JMenuItem(new PTAction()));
		jMenu.add(new JMenuItem(new PrzystankiAction()));
	}

	@Override
	public PreferenceSetting getPreferenceSetting() {
		return preferences;
	}

	/*@Override
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

	}*/

	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		if (newFrame != null) {
			EditRoutingLayerAction addRouteNodeAction = new EditRoutingLayerAction(newFrame);
			IconToggleButton removeRouteNodeButton = new IconToggleButton(
					addRouteNodeAction);
			newFrame.addMapMode(removeRouteNodeButton);
		}
	}

	@Override
	public void uploadDiscouragedChanged(OsmDataLayer layer, boolean newValue) {
		// TODO Auto-generated method stub
		
	}
}