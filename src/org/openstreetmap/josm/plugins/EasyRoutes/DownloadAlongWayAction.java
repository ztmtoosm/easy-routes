// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.EasyRoutes;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.DownloadAlongAction;
import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.actions.downloadtasks.DownloadTask;
import org.openstreetmap.josm.actions.downloadtasks.DownloadTaskList;
import org.openstreetmap.josm.actions.downloadtasks.PostDownloadHandler;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.SimplePrimitiveId;
import org.openstreetmap.josm.gui.progress.PleaseWaitProgressMonitor;
import org.openstreetmap.josm.plugin.EasyRoutes.Panels.SelectDownloadPanel;
import org.openstreetmap.josm.plugin.EasyRoutes.Panels.SelectFileOrUrlPanel;
import org.openstreetmap.josm.plugins.EasyRoutes.RelationsBuilder.RelationsBuilder;
import org.openstreetmap.josm.plugins.EasyRoutes.RelationsBuilder.SingleRelationBuilder;
import org.openstreetmap.josm.tools.Shortcut;

public class DownloadAlongWayAction extends DownloadAlongAction {

	List<PrimitiveId> prims0 = new ArrayList<>();
	RelationsBuilder builder = null;
	JPanel panel;
	JFrame frame;
	JLabel emptyLabel;
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
		 prims0 = new ArrayList<>();
		 frame = new JFrame("BoxLayoutDemo");
		 frame.setVisible(true);
		 frame.setSize(400, 200);
		 frame.setLocationRelativeTo(null);
		 SelectFileOrUrlPanel aktPanel = new SelectFileOrUrlPanel(this);
		 frame.add(aktPanel);
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		 emptyLabel = new JLabel("");
		 emptyLabel.setText("1. Ładowanie węzłów pośrednich...\n");
		 emptyLabel.setPreferredSize(new Dimension(175, 100));
		 panel.add(emptyLabel);
	}
	public void openUrl(boolean newLayer, final String url) {
        PleaseWaitProgressMonitor monitor = new PleaseWaitProgressMonitor(tr("Download Data"));
        Collection<DownloadTask> tasks = new ArrayList<>();
        tasks.add(new DownloadOsmTask());
        for (final DownloadTask task : tasks) {
            try {
                Future<?> future = task.loadUrl(newLayer, url, monitor);
                Main.worker.submit(new PostDownloadHandler(task, future));
            } catch (IllegalArgumentException e) {
                Main.error(e);
            }
        }

    }
	private double calculateArea(Area a, double bufor, double maxPowierzchnia) {
		List<LatLon> ll1 = new ArrayList<LatLon>();
		for (SingleRelationBuilder primx : builder.getRelations()) {
			System.out.println("-----");
			List<Node> tmp = new ArrayList<Node>();
			for (Node n : primx.getTrackNodes()) {
				tmp.add(n);
				ll1.add(n.getCoor());
			}
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

		double buffer_y = bufor / 100000.0;
		double buffer_x = buffer_y / scale;
		double max_area = maxPowierzchnia / 10000.0 / scale;
		Rectangle2D r = new Rectangle2D.Double();
		for (int i = 0; i < ll1.size() - 1; i++) {
			LatLon p1 = ll1.get(i);
			LatLon p2 = ll1.get(i + 1);
			double dist_lon = Math.abs(p1.lon() - p2.lon());
			double dist_lat = Math.abs(p1.lat() - p2.lat());
			double x1=Math.min(p1.lon(), p2.lon()) - buffer_x;
			double y1=Math.min(p1.lat(), p2.lat()) - buffer_y;
			double x2=2 * buffer_x + dist_lon;
			double y2=2 * buffer_y + dist_lat;
			r.setFrame(x1, y1, x2, y2);
			a.add(new Area(r));
		}
		return max_area;
	}
	
	public List<Rectangle2D> getRectangleToDownload(double bufor, double maxPowierzchnia) {
		List<Rectangle2D> toDownload = new ArrayList<>();
		Area a = new Area();
		double max_area=calculateArea(a, bufor, maxPowierzchnia);
		DownloadAlongAction.addToDownload(a, a.getBounds(), toDownload, max_area);
		return toDownload;
	}
	public void downloadDataOsm(List<Rectangle2D> toDownload) {
			if (toDownload.isEmpty()) {
				return;
			}
			final PleaseWaitProgressMonitor monitor = new PleaseWaitProgressMonitor(
					tr("Download data"));
			final Future<?> future = new DownloadTaskList().download(false,
					toDownload, true, false, monitor);
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
				}
			});
	}
	public void downloadDataOverPass(List<Rectangle2D> toDownload) {
		String url1 = "http://overpass-api.de/api/interpreter?data=%5Bout%3Axml%5D%3B%0A%2F%2F%20gather%20results%0A%28%0A%20%20%2F%2F%20query%20part%20for%3A%20%E2%80%9Chighway%3D*%E2%80%9D";

		String url2 = "%2C";
		String url = url1;
		for(int i=0; i<toDownload.size(); i++)
		{
			Rectangle2D foo = toDownload.get(i);
			String url0 = "%0A%20%20way%5B%22highway%22%5D%28";
			String url2a = foo.getMinY()+url2+foo.getMinX()+url2+foo.getMaxY()+url2+foo.getMaxX();
			String url3 = "%29%3B%0A%20%20node%5B%22highway%22%5D%28";
			String url4 = "%29%3B%0A%20%20%20%20relation%5B%22highway%22%5D%28";
			String url5	= "%29%3B%0A%20%20node%5B%22public_transport%22%5D%28";
			String url6 = "%29%3B%0A%20%20%20%20way%5B%22public_transport%22%5D%28";
			String url7 = "%29%3B%0A%20%20relation%5B%22public_transport%22%5D%28";
			url += url0+url2a+url3+url2a+url4+url2a+url5+url2a+url6+url2a+url7+url2a+"%29%3B";
			if(i%7==6 && i!=toDownload.size()-1) {
				String url8 = "%0A%29-%3E.a%3B%0A.a%20%3C%3C%20-%3E.b%3B%0A.a%20%3E%20-%3E.c%3B%0A%28%0A%20%20.a%3B%0A%20%20.b%3B%0A%20%20.c%3B%0A%29%3B%0A%2F%2F%20print%20results%0Aout%20meta%3B";
				url+=url8;
				System.out.println(url);
				openUrl(false, url);
				System.out.println("AAAAAAAA");
				url = url1;
			}
		}
		String url8 = "%0A%29-%3E.a%3B%0A.a%20%3C%3C%20-%3E.b%3B%0A.a%20%3E%20-%3E.c%3B%0A%28%0A%20%20.a%3B%0A%20%20.b%3B%0A%20%20.c%3B%0A%29%3B%0A%2F%2F%20print%20results%0Aout%20meta%3B";
		url+=url8;
		System.out.println(url);
		openUrl(false, url);
	}
	public void runNext() {
		builder.createNecessaryRelations();
		builder.onLoadTrackNodes();
		List<Rectangle2D> toDownload = getRectangleToDownload(1000, 8);
		frame.getContentPane().removeAll();
		frame.getContentPane().invalidate();
		frame.getContentPane().add(new SelectDownloadPanel(this, toDownload));
		frame.getContentPane().revalidate();
	}

	@Override
	protected void updateEnabledState() {
		setEnabled(true);
	}



	public void task3() {
			builder.createTracks();
			emptyLabel.setText("3. Zweryfikuj trasę\n");
			 JButton but = new JButton("Zweryfikowano");
			 final DownloadAlongWayAction xa = DownloadAlongWayAction.this;
			 but.addActionListener(new ActionListener() {
			       @Override
				public void actionPerformed(ActionEvent ae){
			         xa.task4();
			       }
			      });

			 but.setPreferredSize(new Dimension(175, 30));
			panel.removeAll();
			 panel.add(but);
				frame.getContentPane().removeAll();
				frame.getContentPane().invalidate();
			 frame.getContentPane().add(panel);
			 frame.getContentPane().revalidate();
	}

	public void task4() {
		builder.doFinally();
		frame.setVisible(false);
		frame.removeAll();
		frame.dispose();
	}

	@Override
	protected void updateEnabledState(
			Collection<? extends OsmPrimitive> selection) {
		setEnabled(true);
	}


	public void fileChosen(String fileString) {
		
		frame.getContentPane().removeAll();
		frame.getContentPane().invalidate();
		frame.getContentPane().add(panel);
		frame.getContentPane().revalidate();
		builder = new RelationsBuilder(fileString);
		for (SingleRelationBuilder rel : builder.getRelations()) {
			for (Long aaa : rel.getTrack()) {
				PrimitiveId ggg = new SimplePrimitiveId(aaa,
						OsmPrimitiveType.NODE);
				prims0.add(ggg);
			}
		}
		for(Long id1 : builder.getNecessaryRelations()) {
			PrimitiveId ggg = new SimplePrimitiveId(id1,
					OsmPrimitiveType.RELATION);
			prims0.add(ggg);
		}
		
		GownoTask dd = new GownoTask(false, prims0, false, false, "",
				null, this);
		dd.run();
	}
}