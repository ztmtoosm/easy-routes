package org.openstreetmap.josm.plugins.EasyRoutes;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Preferences;

import java.util.ArrayList;
import java.util.Collection;

public class PreferenceGenerator {

    private static Collection<String> createCol(String[] a) {
        Collection<String> wynik = new ArrayList<String>();
        for (int i = 0; i < a.length; i++) {
            wynik.add(a[i]);
        }
        return wynik;
    }

    public static Collection<Collection<String>> typicalPref() {
        Collection<Collection<String>> wynik = new ArrayList<Collection<String>>();
        wynik.add(createCol(new String[] { "highway", "motorway", "0.8", "-1" }));
        wynik.add(createCol(new String[] { "highway", "motorway_link", "0.8",
                "-1" }));
        wynik.add(createCol(new String[] { "highway", "trunk", "0.8", "0.8" }));
        wynik.add(createCol(new String[] { "highway", "trunk_link", "0.8", "0.8" }));
        wynik.add(createCol(new String[] { "highway", "primary", "0.9", "0.9" }));
        wynik.add(createCol(new String[] { "highway", "primary_link", "0.9",
                "0.9" }));
        wynik.add(createCol(new String[] { "highway", "secondary", "1", "1" }));
        wynik.add(createCol(new String[] { "highway", "secondary_link", "1",
                "1" }));
        wynik.add(createCol(new String[] { "highway", "tertiary", "1", "1" }));
        wynik.add(createCol(new String[] { "highway", "tertiary_link", "1", "1" }));
        wynik.add(createCol(new String[] { "highway", "residential", "1.3",
                "1.3" }));
        wynik.add(createCol(new String[] { "highway", "unclassified", "1.36",
                "1.36" }));
        wynik.add(createCol(new String[] { "highway", "service", "1.3", "1.3" }));
        wynik.add(createCol(new String[] { "routing:ztm", "yes", "0.8", "0.8" }));
        wynik.add(createCol(new String[] { "oneway", "yes", "", "-1" }));
        wynik.add(createCol(new String[] { "oneway", "-1", "-1", "" }));
        wynik.add(createCol(new String[] { "junction", "roundabout", "", "-1" }));
        wynik.add(createCol(new String[] { "routing:ztm", "no", "-1", "-1" }));
        return wynik;
    }

    public static Collection<Collection<String>> railwayPref() {
        Collection<Collection<String>> wynik = new ArrayList<Collection<String>>();
        wynik.add(createCol(new String[] { "railway", "", "0.8", "0.8" }));
        wynik.add(createCol(new String[] { "routing:ztm", "yes", "0.8", "0.8" }));
        wynik.add(createCol(new String[] { "oneway", "yes", "", "-1" }));
        wynik.add(createCol(new String[] { "oneway", "-1", "-1", "" }));
        wynik.add(createCol(new String[] { "routing:ztm", "no", "-1", "-1" }));
        return wynik;
    }

    public static Collection<Collection<String>> serverPref() {
        Collection<Collection<String>> wynik = new ArrayList<>();
        Collection <String> foo = new ArrayList<>();
        foo.add("http://vps134914.ovh.net/wyszuk/");
        wynik.add(foo);
        return wynik;
    }
    public static void setPrefOnInit(Collection<Collection<String>> array, String name) {
        Preferences p = Main.pref;
        Collection<Collection<String>> pref = p.getArray(name);
        if (pref == null || pref.size() == 0) {
            p.putArray(name, array);
        }
    }
}
