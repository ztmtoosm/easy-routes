// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.EasyRoutes;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
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
			WaySplitter ws = new WaySplitter(aktPreferences);
			ws.splitWays(lis, getCurrentDataSet());
			getCurrentDataSet().clearSelection();
			List<Way> xd = ws.getWaysAfterSplit(lis, getCurrentDataSet());
			for (Way x : xd) {
				getCurrentDataSet().addSelected(x);
			}
		} catch (NodeConnectException e) {
			e.printStackTrace();
			new Notification(tr("Nodes are not connect together")).setIcon(
					JOptionPane.WARNING_MESSAGE).show();
		}
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

		Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();
		List<Node> selection2 = OsmPrimitive.getFilteredList(selection,
				Node.class);
		if (selection2.size() <= 1) {
			new Notification(tr("Two or more nodes are necessary.")).setIcon(
					JOptionPane.WARNING_MESSAGE).show();
		}
		foo3(selection2);
	}

	@Override
	protected void updateEnabledState() {
		if (getCurrentDataSet() == null) {
			setEnabled(false);
		} else {
			updateEnabledState(getCurrentDataSet().getSelected());
		}
	}

	@Override
	protected void updateEnabledState(
			Collection<? extends OsmPrimitive> selection) {
		setEnabled(selection != null && !selection.isEmpty());
	}
}
