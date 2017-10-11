package org.openstreetmap.josm.plugins.EasyRoutes.Panels;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javafx.util.Pair;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.plugins.EasyRoutes.ZtmToOsmAction;

public class SelectDownloadPanel extends JPanel {
	final ZtmToOsmAction handler;
	final List<Rectangle2D> toDownload;

	public SelectDownloadPanel(final ZtmToOsmAction handler,
			List<Rectangle2D> toDown,
			List<Pair<String, String>> unsupportedStops) {
		super();
		this.toDownload = toDown;
		this.handler = handler;
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		JLabel emptyLabel = new JLabel("");
		emptyLabel.setText("2.");
		emptyLabel.setPreferredSize(new Dimension(175, 100));
		add(emptyLabel);
		JButton but = new JButton("overpass-highway");
		add(but);
		JButton but4 = new JButton("overpass-railway");
		add(but4);
		JButton but9 = new JButton("overpass-e");
		add(but9);
		JButton but2 = new JButton("osm");
		add(but2);
		JButton but3 = new JButton("DALEJ");
		add(but3);
		setVisible(true);
		String lll = "Unsupported\n";
		for (Pair<String, String> x : unsupportedStops) {
			lll += x.getKey() + " " + x.getValue() + "\n";
		}
		JLabel emptyLabel2 = new JLabel(lll);
		add(emptyLabel2);
		final SelectDownloadPanel pan = this;

		but.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				handler.downloadDataOverPass(toDownload, "highway");
			}
		});
		but4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				handler.downloadDataOverPass(toDownload, "railway");
			}
		});
		but2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				handler.downloadDataOsm(toDownload);
			}
		});
		but9.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				handler.downloadDataOldLine();
			}
		});
		but3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				handler.task3();
			}
		});
	}
}