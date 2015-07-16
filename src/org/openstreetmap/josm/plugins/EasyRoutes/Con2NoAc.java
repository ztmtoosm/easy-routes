package org.openstreetmap.josm.plugins.EasyRoutes;

//License: GPL. For details, see LICENSE file.


import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.NodeConnectException;

public final class Con2NoAc extends JosmAction {
	void foo3(List<Node> lis) {
		System.out.println("aaaa");
		try {
			Collection<Collection<String>> aktPreferences = Main.pref
					.getArray("easy-routes.weights");
			WaySplitter ws = new WaySplitter(aktPreferences);
			ws.splitWays(lis, getCurrentDataSet());
			getCurrentDataSet().clearSelection();
			List<Way> xd = ws.getWaysAfterSplit(lis, getCurrentDataSet());
			System.out.println(xd.size()+"XD SIZE SX");
			for (Way x : xd) {
				getCurrentDataSet().addSelected(x);
			System.out.println("bbbb");
			}
		} catch (NodeConnectException e) {
			e.printStackTrace();
			new Notification(tr("Nodes are not connect together")).setIcon(
					JOptionPane.WARNING_MESSAGE).show();
		}
	}

	public Con2NoAc() {
		super(tr("TEST4"), "dzik",
				tr("test4"), null, true, true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!isEnabled() || !Main.map.mapView.isActiveLayerVisible())
			return;
		if(Main.main.getActiveLayer().getClass()==XyzLayer.class) {
			XyzLayer l = (XyzLayer)Main.main.getActiveLayer();
			System.out.println("zzzz");
			if(l.crucialNodes!=null)
				foo3(l.crucialNodes);
		}
	}

	@Override
	protected void updateEnabledState() {
		setEnabled(false);
		if(Main.main.getActiveLayer()==null)
			return;
		if(Main.main.getActiveLayer().getClass()==XyzLayer.class) {
			setEnabled(true);
		}
	}
}
