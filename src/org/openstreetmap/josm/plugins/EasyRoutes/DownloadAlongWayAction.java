// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.EasyRoutes;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.DownloadAlongAction;
import org.openstreetmap.josm.actions.downloadtasks.DownloadTaskList;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.SimplePrimitiveId;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.gpx.DownloadAlongPanel;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.PleaseWaitProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Shortcut;

class DownloadAlongWayAction extends DownloadAlongAction {

	private static final String PREF_DOWNLOAD_ALONG_WAY_DISTANCE = "downloadAlongWay.distance";
	private static final String PREF_DOWNLOAD_ALONG_WAY_AREA = "downloadAlongWay.area";

	private static final String PREF_DOWNLOAD_ALONG_WAY_OSM = "downloadAlongWay.download.osm";
	private static final String PREF_DOWNLOAD_ALONG_WAY_GPS = "downloadAlongWay.download.gps";
	List<List<PrimitiveId>> prims = new ArrayList<>();
	List<PrimitiveId> prims0 = new ArrayList<>();
	List<List<Node> > lnod = new ArrayList<>();

	public DownloadAlongWayAction() {
		super(tr("Download along..."), "dzik",
				tr("Download OSM data along the selected ways."), Shortcut
						.registerShortcut("tools:download_along",
								tr("Tool: {0}", tr("Download Along")),
								KeyEvent.VK_D, Shortcut.ALT_SHIFT), true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Handle open button action.
		// if (e.getSource() == openButton) {

		// }

		prims = new ArrayList<>();
		JFrame frame = new JFrame();
		final JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(frame);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			List<List<Long>> fa = new ArrayList<List<Long>>();

			try {
				String fileString = new String(
						Files.readAllBytes(file.toPath()));
				JSONParser parser = new JSONParser();
				Object obj;
				try {
					obj = parser.parse(fileString);
					JSONArray array = (JSONArray) obj;
					for (int i = 0; i < array.size(); i++) {
						JSONObject obb = (JSONObject) array.get(i);
						JSONArray a2 = (JSONArray) obb.get("track");
						List<Long> tmpp = new ArrayList<>();
						for (int j = 0; j < a2.size(); j++) {

							tmpp.add((Long) a2.get(j));
						}
						if (tmpp.size() > 0)
							fa.add(tmpp);
					}
				} catch (ParseException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

				for (List<Long> fb : fa) {
					List<PrimitiveId> nova = new ArrayList<PrimitiveId>();
					for (Long aaa : fb) {
						System.out.println(aaa);
						PrimitiveId ggg = new SimplePrimitiveId(aaa,
								OsmPrimitiveType.NODE);
						prims0.add(ggg);
						nova.add(ggg);
					}
					prims.add(nova);
				}
				// This is where a real application would open the file.
				GownoTask dd = new GownoTask(false, prims0, false, false, "",
						null, this);
				dd.run();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else {
		}
		JLabel emptyLabel = new JLabel("fsacas");
		emptyLabel.setPreferredSize(new Dimension(175, 100));
		frame.getContentPane().add(emptyLabel, BorderLayout.CENTER);

		// Display the window.
		frame.pack();
		frame.setVisible(true);

	}

	public void runNext() {
		lnod = new ArrayList<>();
		List<LatLon> ll1 = new ArrayList<LatLon>();
		for (List <PrimitiveId> primx : prims) {
			List <Node> tmp = new ArrayList<Node>();
		for (PrimitiveId prim : primx) {
			Node n = (Node) Main.main.getCurrentDataSet()
					.getPrimitiveById(prim);
			tmp.add(n);
			ll1.add(n.getCoor());
		}
		lnod.add(tmp);
		}
		final DownloadAlongPanel panel = new DownloadAlongPanel(
				PREF_DOWNLOAD_ALONG_WAY_OSM, PREF_DOWNLOAD_ALONG_WAY_GPS,
				PREF_DOWNLOAD_ALONG_WAY_DISTANCE, PREF_DOWNLOAD_ALONG_WAY_AREA,
				null);

		if (0 != panel.showInDownloadDialog(
				tr("Download from OSM along selected ways"),
				HelpUtil.ht("/Tools/DownloadAlong"))) {
			return;
		}

		double latsum = 0;
		int latcnt = 0;

		for (int i = 0; i < ll1.size(); i++) {
			LatLon alfa = ll1.get(i);
			latsum += alfa.lat();
			latcnt++;
		}

		double avglat = latsum / latcnt;
		double scale = Math.cos(Math.toRadians(avglat));

		double buffer_dist = panel.getDistance();
		double buffer_y = buffer_dist / 100000.0;
		double buffer_x = buffer_y / scale;
		double max_area = panel.getArea() / 10000.0 / scale;
		Area a = new Area();
		Rectangle2D r = new Rectangle2D.Double();

		for (int i = 0; i < ll1.size() - 1; i++) {
			LatLon p1 = ll1.get(i);
			LatLon p2 = ll1.get(i + 1);
			double dist_lon = Math.abs(p1.lon() - p2.lon());
			double dist_lat = Math.abs(p1.lat() - p2.lat());
			r.setRect(Math.min(p1.lon(), p2.lon()) - buffer_x,
					Math.min(p1.lat(), p2.lat()) - buffer_y, 2 * buffer_x
							+ dist_lon, 2 * buffer_y + dist_lat);
			a.add(new Area(r));
		}
		confirmItd(a, max_area, panel.isDownloadOsmData(),
				panel.isDownloadGpxData(),
				tr("Download from OSM along selected ways"),
				NullProgressMonitor.INSTANCE);
	}

	@Override
	protected void updateEnabledState() {
		setEnabled(true);
	}

	protected void confirmItd(Area a, double maxArea, boolean osmDownload,
			boolean gpxDownload, String title, ProgressMonitor progressMonitor) {
		List<Rectangle2D> toDownload = new ArrayList<>();
		DownloadAlongAction
				.addToDownload(a, a.getBounds(), toDownload, maxArea);
		if (toDownload.isEmpty()) {
			return;
		}
		JPanel msg = new JPanel(new GridBagLayout());
		msg.add(new JLabel(
				tr("<html>This action will require {0} individual<br>"
						+ "download requests. Do you wish<br>to continue?</html>",
						toDownload.size())), GBC.eol());
		if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Main.parent,
				msg, title, JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE)) {
			return;
		}
		final PleaseWaitProgressMonitor monitor = new PleaseWaitProgressMonitor(
				tr("Download data"));
		final Future<?> future = new DownloadTaskList().download(false,
				toDownload, osmDownload, gpxDownload, monitor);
		final DownloadAlongWayAction xa = DownloadAlongWayAction.this;
		Main.worker.submit(new Runnable() {
			@Override
			public void run() {
				try {
					future.get();
				} catch (Exception e) {
					Main.error(e);
					return;
				}
				monitor.close();
				xa.task3();
			}
		});
	}

	public void task3() {
		try {
			System.out.println("coś się dzieje!");
			for(List<Node> tix : lnod) {
				Layer lay = new XyzLayer(tix);
				Main.main.addLayer(lay);
			}
		} catch (Exception e) {

		}
	}

	@Override
	protected void updateEnabledState(
			Collection<? extends OsmPrimitive> selection) {
		setEnabled(true);
	}
}