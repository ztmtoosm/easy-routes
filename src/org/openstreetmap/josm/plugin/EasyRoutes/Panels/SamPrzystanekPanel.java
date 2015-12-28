package org.openstreetmap.josm.plugin.EasyRoutes.Panels;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.json.simple.JSONObject;

public class SamPrzystanekPanel extends JPanel {

	public SamPrzystanekPanel(JSONObject obj)
	{
		setVisible(true);
		setLayout(new FlowLayout());
		add(new JButton("Przycisk 1"));
		add(new JButton("Przycisk 2"));
		add(new JButton("Przycisk 3"));
	}
}
