package org.openstreetmap.josm.plugins.EasyRoutes.Panels;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.plugins.EasyRoutes.ZtmToOsmAction;

public class SelectFileOrUrlPanel extends JPanel {
	final ZtmToOsmAction handler;
	public SelectFileOrUrlPanel(final ZtmToOsmAction handler) {
		super();
		this.handler = handler;
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		JLabel emptyLabel = new JLabel("");
		emptyLabel.setText("1.");
		emptyLabel.setPreferredSize(new Dimension(175, 100));
		add(emptyLabel);
		JButton but = new JButton("Wybierz plik lokalny");
		add(but);
		JButton but2 = new JButton("Wybierz plik z serwera");
		add(but2);
		final SelectFileOrUrlPanel pan = this;
		
		but.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae){
		   		final JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(pan);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();

					try {
						String fileString = new String(
								Files.readAllBytes(file.toPath()));
						String[] fileStringAr = new String[1];
						fileStringAr[0] = fileString;
						handler.fileChosen(fileStringAr);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				
		       }
			}
		});
		but2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae){
		   		new SelectFromUrlFrame(pan);
				
		       }
		});
	}

	public void urlDownloaded(String[] wynik) {
		handler.fileChosen(wynik);
		
	}
}
