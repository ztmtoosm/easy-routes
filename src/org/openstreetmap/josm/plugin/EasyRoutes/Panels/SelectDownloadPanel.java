package org.openstreetmap.josm.plugin.EasyRoutes.Panels;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.plugins.EasyRoutes.DownloadAlongWayAction;

public class SelectDownloadPanel extends JPanel {
	final DownloadAlongWayAction handler;
	final List<Rectangle2D> toDownload;
	public SelectDownloadPanel(final DownloadAlongWayAction handler, List<Rectangle2D> toDown) {
		super();
		this.toDownload = toDown;
		this.handler = handler;
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		JLabel emptyLabel = new JLabel("");
		emptyLabel.setText("2.");
		emptyLabel.setPreferredSize(new Dimension(175, 100));
		add(emptyLabel);
		JButton but = new JButton("overpass");
		add(but);
		JButton but2 = new JButton("osm");
		add(but2);
		JButton but3 = new JButton("DALEJ");
		add(but3);
		setVisible(true);
		final SelectDownloadPanel pan = this;
		
		but.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae){
				handler.downloadDataOverPass(toDownload);
			}
		});
		but2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae){
				handler.downloadDataOsm(toDownload);
		       }
		});
		but3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae){
				handler.task3();
		       }
		});
	}
}