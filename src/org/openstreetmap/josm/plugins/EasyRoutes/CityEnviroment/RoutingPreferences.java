package org.openstreetmap.josm.plugins.EasyRoutes.CityEnviroment;

import org.openstreetmap.josm.data.osm.Way;

import java.util.Collection;
import java.util.Map;

public class RoutingPreferences {
    private Collection<Collection<String> > currentPreferences;

    public RoutingPreferences(Collection<Collection<String> > preferences) {
        currentPreferences = preferences;
    }

    public double getWeight(Way w, boolean isNormalDirection) {
        double value = -1;
        if (currentPreferences == null)
            return 1;
        for (Collection<String> foo : currentPreferences) {
            Map<String, String> map = w.getKeys();
            String[] ar = new String[foo.size()];
            int i = 0;
            for (String xxx : foo) {
                if (xxx == null)
                    ar[i] = "";
                else
                    ar[i] = xxx;
                i++;
            }
            String key = ar[0];
            String kv = ar[1];
            double wei = 0;
            if (ar[3].equals(""))
                wei = 0;
            else
                wei = Double.valueOf(ar[3]);
            if (isNormalDirection) {
                if (ar[2].equals(""))
                    wei = 0;
                else
                    wei = Double.valueOf(ar[2]);
            }
            if (!key.equals("")) {
                if (map.containsKey(key)) {
                    if (kv.equals("") || map.get(key).equals(kv)) {
                        if (wei != 0) {
                            value = wei;
                        }
                    }
                }
            }
        }
        return value;
    }
}
