package org.openstreetmap.josm.plugins.EasyRoutes;

import java.util.List;

import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.gui.io.DownloadPrimitivesWithReferrersTask;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;

public class GownoTask extends DownloadPrimitivesWithReferrersTask {
	DownloadAlongWayAction xxx;
        	public GownoTask(boolean newLayer, List<PrimitiveId> ids, boolean downloadReferrers,
                    boolean full, String newLayerName, ProgressMonitor monitor, DownloadAlongWayAction xxx) {
        		super(newLayer, ids, downloadReferrers, full, newLayerName, monitor);
        		this.xxx=xxx;
        	}
            @Override
            protected void finish() {
            	super.finish();
            	xxx.runNext();
            }
}
