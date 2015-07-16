package org.openstreetmap.josm.plugins.EasyRoutes;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceSettingFactory;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.tools.GBC;

/**
 * Map preferences, including map paint styles, tagging presets and autosave
 * sub-preferences.
 */
public final class EasyRoutesPreference extends DefaultTabPreferenceSetting {

	/**
	 * Factory used to create a new {@code AudioPreference}.
	 */
	public static class Factory implements PreferenceSettingFactory {
		@Override
		public PreferenceSetting createPreferenceSetting() {
			return new EasyRoutesPreference();
		}
	}

	private EasyRoutesPreference() {
		super(/* ICON(preferences/) */"dzik", tr("easy-routes settings"),
				tr("Settings for easy-routes."));
	}

	JTable table;

	@Override
	public void addGui(PreferenceTabbedPane gui) {
		String[][] foo = new String[40][4];
		String[] foo2 = { "key", "value", "normal", "reverse" };
		Collection<Collection<String>> ar = Main.pref
				.getArray("easy-routes.weights");
		if (ar != null) {
			foo = new String[40][];
			int i = 0;
			for (Collection<String> foo3 : ar) {
				int j = 0;
				String[] fooPom = new String[4];
				for (String foo4 : foo3) {
					fooPom[j] = foo4;
					j++;
				}
				foo[i] = fooPom;
				i++;
			}
		}
		DefaultTableModel model = new DefaultTableModel(foo, foo2);
		table = new JTable(model);
		table.getTableHeader().setReorderingAllowed(false);
		JPanel audio = new JPanel(new GridBagLayout());
		audio.add(new JScrollPane(table));
		audio.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));

		createPreferenceTabWithScrollPane(gui, audio);
	}

	@Override
	public boolean ok() {
		int y = table.getColumnCount();
		int x = table.getRowCount();
		Collection<Collection<String>> ars = new ArrayList<Collection<String>>();
		for (int i = 0; i < x; i++) {
			Collection<String> tmp = new ArrayList<String>();
			for (int j = 0; j < y; j++) {
				if (table.getValueAt(i, j) == null)
					tmp.add("");
				else
					tmp.add((String) table.getValueAt(i, j));
			}
			ars.add(tmp);
		}
		Main.pref.putArray("easy-routes.weights", ars);
		return false;
	}
}