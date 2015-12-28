package org.openstreetmap.josm.plugins.EasyRoutes;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugin.EasyRoutes.Panels.SamPrzystanekPanel;
import org.openstreetmap.josm.plugin.EasyRoutes.Panels.SelectFromUrlFrame;

public class PrzystankiAction extends JosmAction {
	
	public static LatLon getLatLon(JSONObject obj, String keyLat, String keyLon)
	{
		double lat = 0;
		double lon = 0;
		if(obj.get(keyLat) != null)
			lat = (Double) obj.get(keyLat);
		if(obj.get(keyLon) != null)
			lon = (Double) obj.get(keyLon);		
		if(lat>1 && lon>1)
			return new LatLon(lat, lon);
		return null;
	}
	
	public static LatLon getAvgLatLon(LatLon l1, LatLon l2)
	{
		if(l1 == null && l2 == null)
			return null;
		if(l1 == null)
			return l2;
		if(l2 == null)
			return l1;
		return new LatLon((l1.lat()+l2.lat())/2, (l1.lon()+l2.lon())/2);
	}
	
	public static String getLatLonShortInfo(JSONObject obj)
	{
		LatLon ll1 = getLatLon(obj, "lat", "lon");
		LatLon ll2 = getLatLon(obj, "lat2", "lon2");
		String ok1 = (ll1 != null) ? "Y" : "N";
		String ok2 = (ll2 != null) ? "Y" : "N";
		long latlon_jakosc = -1;
		if(obj.get("latlon_jakosc")!=null)
			latlon_jakosc = (Long) obj.get("latlon_jakosc");
		return ok1+ok2+"/"+latlon_jakosc;
	}
	
	JFrame frame;
	JPanel pan = new JPanel();
	JPanel pan2;
	JPanel pan3;
	PrzystankiLayer lay;
    public PrzystankiAction() {
        super(tr("Przystanki"), null, tr("aaaaaaaaaa"),
        		null, true, true);
    }
	@Override
	public void actionPerformed(ActionEvent e) {
		lay = new PrzystankiLayer();
	       Main.main.addLayer(lay);
		 frame = new JFrame("BoxLayoutDemo");
		 frame.setVisible(true);
		 frame.setSize(400, 200);
		 frame.setLocationRelativeTo(null);
			final List<String> l1 = new ArrayList<>();
			final List<String> l2 = new ArrayList<>();
		SelectFromUrlFrame.getCities(l1,l2);
		String[] l3 = new String[l1.size()];
		for(int i=0; i<l1.size(); i++)
			l3[i]=l1.get(i)+" "+l2.get(i);
		 JComboBox foox = new JComboBox(l3);
		 final PrzystankiAction hand = this;
			foox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae){
					JComboBox cb = (JComboBox)ae.getSource();
					int pos=cb.getSelectedIndex();
					
					 dupa(SelectFromUrlFrame.downUrl(l1.get(pos)+"PrzystankiErr"+l2.get(pos)+".json"));
				}});
			pan.setLayout(new BoxLayout(pan, BoxLayout.PAGE_AXIS));
		pan.add(foox);
		 frame.add(pan);
	}
	void dupa(String tab)
	{
		pan3 = null;
		if(pan2!=null)
			pan.remove(pan2);
		pan2 = new JPanel();
		pan2.setLayout(new FlowLayout());
		pan.add(pan2);
		JSONParser parser = new JSONParser();
		Object obj;
		
		try {
			obj = parser.parse(tab);
			final JSONArray array = (JSONArray) obj;
			String[] l5 = new String[array.size()];
			for (int i = 0; i < array.size(); i++) {
				JSONObject obb = (JSONObject) array.get(i);
				String foo = (String)obb.get("long_name");
				l5[i] = foo + " " + getLatLonShortInfo(obb);
			}
			JComboBox foo9 = new JComboBox(l5);
			final PrzystankiAction hand = this;
			foo9.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae){
					JComboBox cb = (JComboBox)ae.getSource();
					int pos=cb.getSelectedIndex();
					dupa2((JSONObject) array.get(pos));
				}});
			pan2.add(foo9);
		} catch (ParseException e2) {
			e2.printStackTrace();
		}
		SwingUtilities.updateComponentTreeUI(frame);
	}
	void dupa2(JSONObject obj)
	{
		if(pan3!=null)
			pan2.remove(pan3);
		pan3 = new SamPrzystanekPanel(obj);
		pan2.add(pan3);
		LolMode mod = new LolMode(Main.map);
		Main.map.selectMapMode(mod);
		LatLon ll1 = getLatLon(obj, "lat", "lon");
		LatLon ll2 = getLatLon(obj, "lat2", "lon2");
		lay.setLatLon(ll1, ll2);
		SwingUtilities.updateComponentTreeUI(frame);
		LatLon ll3 = getAvgLatLon(ll1, ll2);
		if(ll3 != null)
			Main.map.mapView.zoomTo(ll3);
	}
}