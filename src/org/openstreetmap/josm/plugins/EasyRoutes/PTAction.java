package org.openstreetmap.josm.plugins.EasyRoutes;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Map;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;

public class PTAction extends JosmAction {
    public PTAction() {
        super(tr("XXXX"), null, tr("test"),
        		null, true, true);
    }
	@Override
	public void actionPerformed(ActionEvent e) {
		DataSet ds = Main.main.getCurrentDataSet();
		Collection<Way> ajWaj = ds.getWays();
		for(Way x : ajWaj) {
			if(x.keySet().size()==0 && x.getNodes().size()==2) {
				Node stop = null;
				Node transport = null;
				if("bus_stop".equals(x.getNodes().get(0).getKeys().get("highway"))) {
					stop=x.getNodes().get(0);
					transport=x.getNodes().get(1);
				}
				if("bus_stop".equals(x.getNodes().get(1).getKeys().get("highway"))) {
					stop=x.getNodes().get(1);
					transport=x.getNodes().get(0);
				}
				if(stop!=null && transport !=null) {
					x.setDeleted(true);
					Map<String,String> foo = stop.getKeys();
					if(foo.get("ref:ztm")!=null) {
						transport.put("ref:ztm", foo.get("ref:ztm"));
					}
					if(foo.get("name")!=null) {
						transport.put("name", foo.get("name"));
					}
					transport.put("public_transport", "stop_position");
					transport.setModified(true);
				}
			}
		}
	}

}
