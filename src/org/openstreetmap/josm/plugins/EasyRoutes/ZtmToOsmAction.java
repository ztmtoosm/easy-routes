// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.EasyRoutes;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

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

public class ZtmToOsmAction extends DownloadAlongAction {


	RelationsBuilder builder = null;
	JPanel panel;
	JFrame frame;
	JLabel emptyLabel;
	public ZtmToOsmAction() {
		super(tr("ztmtoosm creator"), "dzik",
				tr("Update for public transport networks with ztmtoosm special file"), null, true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Handle open button action.
		// if (e.getSource() == openButton) {

		// }
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
			final ZtmToOsmAction xa = ZtmToOsmAction.this;
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
	private String bbox (double a, double b, double c, double d) {
		return "("+a+","+b+","+c+","+d+")";
	}
	private String lineOverPass(String bbox, String type, String key) {
		return type+"[\""+key+"\"]"+bbox+";";
	}
	private String relWayNode(String bbox, String key) {
		return lineOverPass(bbox, "node", key)+lineOverPass(bbox, "way", key)+lineOverPass(bbox, "relation", key);
	}
	public void downloadDataOverPass(List<Rectangle2D> toDownload, String key) {
		
		String serverPath = "http://overpass-api.de/api/interpreter?data=";
		String txt0 = "[out:xml];(";
		String txt9 = new String(txt0);
		String txt2 = ")->.a;\n.a << ->.b;\n.a > ->.c;\n( .a; .b; .c;);\nout meta;";
		
		for(int i=0; i<toDownload.size(); i++)
		{
			Rectangle2D foo = toDownload.get(i);
			String bboxx = bbox(foo.getMinY(),foo.getMinX(),foo.getMaxY(),foo.getMaxX());
			String txt1 = lineOverPass(bboxx, "way", key);
			txt9 += txt1;
			if(i%7==6 && i!=toDownload.size()-1) {
				try {
					String txt35 = URLEncoder.encode(txt9+txt2, "UTF-8").replace("+", "%20");
					String txt4 = serverPath+txt35;
					txt9 = new String(txt0);
					openUrl(false, txt4);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {
			String txt35 = URLEncoder.encode(txt9+txt2, "UTF-8").replace("+", "%20");
			String txt4 = serverPath+txt35;
			openUrl(false, txt4);
			txt9 = new String(txt0);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
		}
	}
	public void downloadDataOldLine() {
		List <Long> ids = builder.getExistRelations();
		String serverPath = "http://overpass-api.de/api/interpreter?data=";
		String txt = "[out:xml];(";
		for(long x : ids)
		{
			txt+="relation("+x+"); ";
		}
		txt +=")->.a; .a >> ->.b; .b << ->.c; (.b; relation.c; ); out meta;";
		String txt35;
		try {
			txt35 = URLEncoder.encode(txt, "UTF-8").replace("+", "%20");
		
		String txt4 = serverPath+txt35;
		openUrl(false, txt4);
		}
		catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			 final ZtmToOsmAction xa = ZtmToOsmAction.this;
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
				SwingUtilities.updateComponentTreeUI(frame);
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


	public void fileChosen(String[] fileString) {
		Set<PrimitiveId> prims0 = new HashSet<>();
		frame.getContentPane().removeAll();
		frame.getContentPane().invalidate();
		frame.getContentPane().add(panel);
		frame.getContentPane().revalidate();
		builder = new RelationsBuilder(fileString);
		for (SingleRelationBuilder rel : builder.getRelations()) {
			prims0.addAll(rel.getNecessaryPrimitives());
		}
		for(Long id1 : builder.getNecessaryRelations()) {
			PrimitiveId ggg = new SimplePrimitiveId(id1,
					OsmPrimitiveType.RELATION);
			prims0.add(ggg);
		}
		List <PrimitiveId> lis = new ArrayList<>();
		lis.addAll(prims0);
		DownloadNecessaryObjectsZtmToOsm dd = new DownloadNecessaryObjectsZtmToOsm(false, lis, false, false, "",
				null, this);
		dd.run();
	}
}