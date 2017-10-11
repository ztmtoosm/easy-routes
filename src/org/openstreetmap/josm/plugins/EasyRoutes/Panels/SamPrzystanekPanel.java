package org.openstreetmap.josm.plugins.EasyRoutes.Panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openstreetmap.josm.plugins.EasyRoutes.TrickyStopEditor.PrzystankiAction;
import org.openstreetmap.josm.plugins.EasyRoutes.TrickyStopEditor.PrzystankiAction.TrybClick;

public class SamPrzystanekPanel extends JPanel {

	public JSONObject obj;
	private String getRouteLabel(JSONObject route)
	{
		String wynik = "";
		String akt = (String)route.get("id");
		String linia = (String)route.get("linia");
		String aktName = (String)route.get("name");
		String pierwszy, pierwszyName, poprzedni, poprzedniName, kolejny, kolejnyName, ostatni, ostatniName;
		pierwszy = route.get("pierwszy") == null ? "" : (String)route.get("pierwszy");
		pierwszyName = route.get("pierwszyName") == null ? "" : (String)route.get("pierwszyName");
		poprzedni = route.get("poprzedni") == null ? "" : (String)route.get("poprzedni");
		poprzedniName = route.get("poprzedniName") == null ? "" : (String)route.get("poprzedniName");
		kolejny = route.get("kolejny") == null ? "" : (String)route.get("kolejny");
		kolejnyName = route.get("kolejnyName") == null ? "" : (String)route.get("kolejnyName");
		ostatni = route.get("ostatni") == null ? "" : (String)route.get("ostatni");
		ostatniName = route.get("ostatniName") == null ? "" : (String)route.get("ostatniName");
		wynik = linia + " " + pierwszyName + " - " + ostatniName + ", ";
		if(!("").equals(poprzedni))
		{
			wynik += "poprzedni: "+poprzedniName;
		}
		if(!("").equals(kolejny) && !("").equals(poprzedni))
			wynik += ", ";
		if(!("").equals(kolejny))
		{
			wynik += "kolejny: "+kolejnyName;
		}
		return wynik;
	}
	public SamPrzystanekPanel(JSONObject obj, final PrzystankiAction parent)
	{
		this.obj = obj;
		setVisible(true);
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		JButton b1 = new JButton("bus_stop");
		JButton b2 = new JButton("stop_position");
		JButton b3 = new JButton("bus_stop+stop_position");
		b1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.updateTryb(TrybClick.STOP);
			}
		});
		b2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.updateTryb(TrybClick.POSITION);
			}
		});
		b3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.updateTryb(TrybClick.STOP_AND_POSITION);
			}
		});
		String labelText = "<html>";
		
		labelText += "additional: ";
		labelText += obj.get("additional") == null ? "" : (String)obj.get("additional");
		labelText += "<br>";
		
		JSONArray kierunki = (JSONArray) obj.get("kierunki");
		for(Object obj2 : kierunki.toArray())
		{
			labelText += getRouteLabel((JSONObject) obj2) + "<br>";
		}
		labelText += "</html>";
		c.gridx = 0; c.gridy=0; c.gridwidth = 1;
		add(b1, c);
		c.gridx = 1; c.gridy=0; c.gridwidth = 1;
		add(b2, c);
		c.gridx = 2; c.gridy=0; c.gridwidth = 1;
		add(b3, c);
		c.gridx = 0; c.gridy=1; c.gridwidth = 3;
		add(new JLabel(labelText), c);
	}
	public void onExitMode()
	{
		
	}
}
