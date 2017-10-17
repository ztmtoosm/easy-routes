import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;

import java.util.*;
import java.util.function.Predicate;

public class TestTools {
    static <T extends OsmPrimitive> Map<Integer, T> getTestData(Class <T> clazz, DataSet ds) {
        Map<Integer, T> lst = new HashMap<>();
        Predicate<OsmPrimitive> predicate = new Predicate<OsmPrimitive>() {
            @Override
            public boolean test(OsmPrimitive osmPrimitive) {
                String val = osmPrimitive.getInterestingTags().get("test");
                return osmPrimitive.getClass() == clazz && val != null;
            }
        };
        for(OsmPrimitive n : ds.getPrimitives(predicate)) {
            String val = n.getInterestingTags().get("test");
            if(val != null) {
                try {
                    int x = Integer.valueOf(val);
                    lst.put(x, (T) n);
                }
                catch(NumberFormatException e) {

                }
            }
        }
        return lst;
    }
    static boolean nodesAreOnDifferentWays(Node n1, Node n2) {
        Set<Way> w1 = new HashSet<>(n1.getParentWays());
        Set<Way> w2 = new HashSet<>(n2.getParentWays());
        w1.retainAll(w2);
        return w1.isEmpty();
    }
    static <T, U> List<U> createList(Map<T, U> mp, T... objects) {
        ArrayList<U> arrayList = new ArrayList<>();
        for(T x : objects) {
            if(mp.get(x) != null) {
                arrayList.add(mp.get(x));
            }
        }
        return arrayList;
    }
}
