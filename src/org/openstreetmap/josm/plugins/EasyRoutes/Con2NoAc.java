package org.openstreetmap.josm.plugins.EasyRoutes;

//License: GPL. For details, see LICENSE file.

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.plugins.EasyRoutes.Routing.RoutingLayer;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.NodeConnectException;

//public final class Con2NoAc extends JosmAction {
	/*
	void foo3(RoutingLayer l) {
		try {
			Main.getLayerManager().getEditDataSet().clearSelection();
			List<Way> xd = l.splitWays();
			for (Way x : xd) {
				Main.getLayerManager().getEditDataSet().addSelected(x);
			}
		} catch (NodeConnectException e) {
			e.printStackTrace();
			new Notification(tr("Nodes are not connected together")).setIcon(
					JOptionPane.WARNING_MESSAGE).show();
		}
	}

	public Con2NoAc() {
		super(tr("Layer to ways"), null, tr("Select ways from routing layer"), null, true, true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!isEnabled() || !Main.map.mapView.isActiveLayerVisible())
			return;
		if(Main.getLayerManager().getActiveLayer().getClass()==RoutingLayer.class) {
			RoutingLayer l = (RoutingLayer)Main.getLayerManager().getActiveLayer();
			if(l.crucialNodes != null)
				foo3(l);
		}
	}

	@Override
	protected void updateEnabledState() {
		setEnabled(false);
		if(Main.getLayerManager().getActiveLayer() == null)
			return;
		if(Main.getLayerManager().getActiveLayer().getClass() == RoutingLayer.class) {
			setEnabled(true);
		}
	}
	*/
//}
