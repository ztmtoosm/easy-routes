package org.openstreetmap.josm.plugin.EasyRoutes.Panels;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SelectFromUrlFrame extends JFrame {
	String[] wybor = null;
	SelectFromUrlFrame(SelectFileOrUrlPanel parentPanel) {
	 super("BoxLayoutDemo");
		this.parentPanel=parentPanel;
	 setVisible(true);
	 setSize(300, 220);
	 setLocationRelativeTo(null);
	 String wynik=downUrl("http://vps134914.ovh.net/wyszuk/ListWarszawa.json");
	 foo(wynik);
	}
	JList lista;
	SelectFileOrUrlPanel parentPanel;
	void foo(String tab) {
		JPanel pan = new JPanel();
		add(pan);
		pan.setLayout(new BoxLayout(pan, BoxLayout.PAGE_AXIS));
		
		
		JLabel emptyLabel = new JLabel("");
		emptyLabel.setText("xxxx.");
		emptyLabel.setPreferredSize(new Dimension(175, 100));
		pan.add(emptyLabel);

		
		JSONParser parser = new JSONParser();
		Object obj;
		
		try {
			obj = parser.parse(tab);
			JSONArray array = (JSONArray) obj;
			wybor = new String[array.size()];
			for (int i = 0; i < array.size(); i++) {
				String obb = (String) array.get(i);
				wybor[i] = obb;
			}
		} catch (ParseException e2) {
			e2.printStackTrace();
		}
		lista = new JList(wybor);
		lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane listScroller = new JScrollPane(lista);
		listScroller.setPreferredSize(new Dimension(400, 70));
		lista.setLayoutOrientation(JList.VERTICAL);
		pan.add(lista);
		ListSelectionModel listSelectionModel = lista.getSelectionModel();
	        
	        
			JButton but = new JButton("OK");
			pan.add(but);
			final SelectFromUrlFrame fr = this;
			
			but.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae){
					fr.endx();

			       }
				
			});
	}
	static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	String downUrl(String url) {
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
	public void endx() {
		int wyb = lista.getSelectedIndex();
		if(wyb>=0) {
			String xx = (String) lista.getSelectedValue();
			String wynik=downUrl("http://vps134914.ovh.net/wyszuk/jsWarszawa"+xx+".json");
			parentPanel.urlDownloaded(wynik);
			this.setVisible(false);
			this.dispose();
		}
	}
}
