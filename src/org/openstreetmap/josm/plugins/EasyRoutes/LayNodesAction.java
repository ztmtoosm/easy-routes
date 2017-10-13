// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.EasyRoutes;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.plugins.EasyRoutes.Routing.RoutingLayer;
import org.openstreetmap.josm.plugins.EasyRoutes.Routing.RoutingSpecial;

public final class LayNodesAction extends JosmAction {
    public LayNodesAction() {
        super(tr("Nodes to layer"), null, tr("test"),
        		null, true, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled() || !Main.map.mapView.isActiveLayerVisible())
            return;

        Collection<OsmPrimitive> selection = Main.getLayerManager().getEditDataSet().getSelected();
        List<Node> selection2 = OsmPrimitive.getFilteredList(selection, Node.class);
        if(selection2.size()<=1)
        {
            new Notification(
                    tr("Two or more nodes are necessary."))
                    .setIcon(JOptionPane.WARNING_MESSAGE)
                    .show();
        }
        RoutingLayer lay;
		lay = new RoutingLayer(selection2, new ArrayList<OsmPrimitive>(), "xxx", new RoutingSpecial(Main.pref.getArray("easy-routes.weights"), null));
	       Main.getLayerManager().addLayer(lay);
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
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        setEnabled(selection != null && !selection.isEmpty());
    }
}
