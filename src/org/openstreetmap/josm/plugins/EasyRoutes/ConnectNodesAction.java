// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.EasyRoutes;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.NodeConnectException;
import org.openstreetmap.josm.tools.Shortcut;

public final class ConnectNodesAction extends JosmAction {
	void foo3(List<Node> lis) {
		try {
			Collection<Collection<String>> aktPreferences = Main.pref
					.getArray("easy-routes.weights");
			RoutingSpecial ws = new RoutingSpecial(aktPreferences, null);
			ws.splitWays(lis);
			Main.getLayerManager().getEditDataSet().clearSelection();
			List<Way> xd = ws.getWaysAfterSplit(lis);
			for (Way x : xd) {
				Main.getLayerManager().getEditDataSet().addSelected(x);
			}
		} catch (NodeConnectException e) {
			e.printStackTrace();
			new Notification(tr("Nodes are not connect together")).setIcon(
					JOptionPane.WARNING_MESSAGE).show();
		}
	}
	private <T> List<T> tnl (T n1, T n2) {
		List<T> wynik = new ArrayList<>();
		wynik.add(n1);
		wynik.add(n2);
		return wynik;
	}
	void foo4(List<Way> lis) {
			Collection<Collection<String>> aktPreferences = Main.pref
					.getArray("easy-routes.weights");
			RoutingSpecial ws = new RoutingSpecial(aktPreferences, null);
			Double aa = 0.0;
			Double ab = 0.0;
			Double ba = 0.0;
			Double bb = 0.0;
			try {
				aa= ws.getDistance(tnl(lis.get(0).firstNode(), lis.get(1).firstNode()), false);
			} catch (NodeConnectException e) {
				aa = 1000000.0;
			}
			try {
				ab=ws.getDistance(tnl(lis.get(0).firstNode(), lis.get(1).lastNode()), false);
			} catch (NodeConnectException e) {
				ab = 1000000.0;
			}
			try {
				ba=ws.getDistance(tnl(lis.get(0).lastNode(), lis.get(1).firstNode()), false);
			} catch (NodeConnectException e) {
				ba = 1000000.0;
			}
			try {
				bb=ws.getDistance(tnl(lis.get(0).lastNode(), lis.get(1).lastNode()), false);
			} catch (NodeConnectException e) {
				bb = 1000000.0;
			}
			if(aa<ab && aa<ba && aa<bb) {
				foo3(tnl(lis.get(0).firstNode(), lis.get(1).firstNode()));
			} else if (ab<ba && ab<bb) {
				foo3(tnl(lis.get(0).firstNode(), lis.get(1).lastNode()));				
			} else if (ba<bb) {
				foo3(tnl(lis.get(0).lastNode(), lis.get(1).firstNode()));				
			} else {
				foo3(tnl(lis.get(0).lastNode(), lis.get(1).lastNode()));				
			}
			Main.getLayerManager().getEditDataSet().addSelected(lis.get(0));
			Main.getLayerManager().getEditDataSet().addSelected(lis.get(1));
	}

	public ConnectNodesAction() {
		super(tr("Ways routing between nodes"), "dzik",
				tr("Select and split ways beetween selected nodes"), Shortcut
						.registerShortcut("tools:easyrouting",
								tr("Tool: {0}", tr("EasyRouting")),
								KeyEvent.VK_B, Shortcut.ALT), true, true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!isEnabled() || !Main.map.mapView.isActiveLayerVisible())
			return;

		Collection<OsmPrimitive> selection = Main.getLayerManager().getEditDataSet().getSelected();
		List<Node> selection2 = OsmPrimitive.getFilteredList(selection,
				Node.class);
		List<Way> selection3 = OsmPrimitive.getFilteredList(selection,
				Way.class);
		if (selection2.size() > 1) {
			foo3(selection2);

		} else if (selection3.size()==2) {
			foo4(selection3);
		} else {
			new Notification(tr("Two or more nodes are necessary, or two ways.")).setIcon(
					JOptionPane.WARNING_MESSAGE).show();
		}
	}

	@Override
	protected void updateEnabledState() {
		if (Main.getLayerManager().getEditDataSet() == null) {
			setEnabled(false);
		} else {
			updateEnabledState(Main.getLayerManager().getEditDataSet().getSelected());
		}
	}

	@Override
	protected void updateEnabledState(
			Collection<? extends OsmPrimitive> selection) {
		setEnabled(selection != null && !selection.isEmpty());
	}
}
