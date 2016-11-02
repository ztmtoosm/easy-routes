package org.openstreetmap.josm.plugins.EasyRoutes.Panels;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openstreetmap.josm.Main;

public class SelectFromUrlFrame extends JFrame {
	
	public static void getCities(List<String> server, List<String> city) {
		Collection<Collection<String> > servers = Main.pref.getArray("easy-routes.server");
		for(Collection<String> foo : servers) {
			Iterator it1=foo.iterator();
			if(foo.size()>0)
			{
				String serverName = (String) it1.next();
				System.out.println(serverName);
				String wynik=downUrl(serverName+"lst");
				JSONParser parser = new JSONParser();
				Object obj;
				try {
					obj = parser.parse(wynik);
					JSONArray array = (JSONArray) obj;
					for (int i = 0; i < array.size(); i++) {
						String obb = (String) array.get(i);
						city.add(obb);
						server.add(serverName);
					}
				} catch (ParseException e2) {
					e2.printStackTrace();
				}
			}
		}
	}
	String[] wybor = null;
	JScrollPane listScroller;
	JPanel pan = new JPanel();
	SelectFromUrlFrame(SelectFileOrUrlPanel parentPanel) {
		 super("Wybierz plik z serwera");
			
			this.parentPanel=parentPanel;
		 setVisible(true);
		 setSize(300, 220);
		 setLocationRelativeTo(null);
		 add(pan);
		pan.setLayout(new BoxLayout(pan, BoxLayout.PAGE_AXIS));
		final List<String> l1 = new ArrayList<String>();
		final List<String> l2 = new ArrayList<String>();
		getCities(l1,l2);
		String[] l3 = new String[l1.size()];
		for(int i=0; i<l1.size(); i++)
			l3[i]=l1.get(i)+" "+l2.get(i);
		JComboBox foox = new JComboBox(l3);
		foox.setMaximumSize(new Dimension(2000, 50));
		final SelectFromUrlFrame hand = this;
		foox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae){
					JComboBox cb = (JComboBox)ae.getSource();
					int pos=cb.getSelectedIndex();
					
					 String wynik=downUrl(l1.get(pos)+"apiline2");
					 hand.foo(wynik, l1.get(pos), l2.get(pos));
				}});
		pan.add(foox);
		foox.setSelectedIndex(0);
	}
	JButton butOk;
	JList lista;
	SelectFileOrUrlPanel parentPanel;
	void foo(String tab, final String server, final String city) {
		
		JSONParser parser = new JSONParser();
		
		try {
			JSONObject baseObject = (JSONObject)parser.parse(tab);
			
			JSONArray array = (JSONArray)baseObject.get("lines");
			System.out.println(tab);
			wybor = new String[array.size()];
			for (int i = 0; i < array.size(); i++) {
				
				wybor[i] = (String) ((JSONObject)array.get(i)).get("lin");
			}
		} catch (ParseException e2) {
			e2.printStackTrace();
		}
		if(lista!=null)
			pan.remove(listScroller);
		if(butOk!=null)
			pan.remove(butOk);
		lista = new JList(wybor);
		lista.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		listScroller = new JScrollPane(lista);
		listScroller.setPreferredSize(new Dimension(400, 70));
		lista.setLayoutOrientation(JList.VERTICAL);
		pan.add(listScroller);
		ListSelectionModel listSelectionModel = lista.getSelectionModel();
	        
	        
			butOk = new JButton("OK");
			pan.add(butOk);
			final SelectFromUrlFrame fr = this;
			SwingUtilities.updateComponentTreeUI(this);
			butOk.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae){
					fr.endx(server, city);

			       }
				
			});
	}
	static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	public static String downUrl(String url) {
		String charset = "UTF-8";  // Or in Java 7 and later, use the constant: java.nio.charset.StandardCharsets.UTF_8.name()
		String param1 = "value1";
		String param2 = "value2";
		// ...

		URLConnection connection;
		try {
			connection = new URL(url).openConnection();
		
		connection.setRequestProperty("Accept-Charset", charset);
		InputStream response = connection.getInputStream();
		return convertStreamToString(response);
		}
		catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	public void endx(String server, String city) {
		List <String> sciezki = lista.getSelectedValuesList();
		int[] wybs = lista.getSelectedIndices();
		List <String> arr = new ArrayList<>();
		for(int i=0; i<sciezki.size(); i++) {
			int wyb = wybs[i];
			if(wyb>=0) {
				String xx = sciezki.get(i);
				String wynik=downUrl(server+"apiline2/"+xx);
				arr.add(wynik);

			}
		}
		parentPanel.urlDownloaded(arr.toArray(new String[arr.size()]));
		this.setVisible(false);
		this.dispose();
	}
}
