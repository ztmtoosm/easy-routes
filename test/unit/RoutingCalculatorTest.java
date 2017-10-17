import org.openstreetmap.josm.JOSMFixture;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.io.OsmReader;
import org.openstreetmap.josm.plugins.EasyRoutes.CityEnviroment.RoutingPreferences;
import org.openstreetmap.josm.plugins.EasyRoutes.NewRouting.RoutingCalculator;
import org.openstreetmap.josm.plugins.EasyRoutes.PreferenceGenerator;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.NodeConnectException;


import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


import org.junit.Rule;
import org.junit.Test;
import org.junit.BeforeClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutingCalculatorTest {
    @BeforeClass
    public static void init() throws Exception{
        new JOSMFixture("../../core/test/config/functional-josm.home").init(false);
        PreferenceGenerator.setPrefOnInit(PreferenceGenerator.serverPref(), "easy-routes.server");
        PreferenceGenerator.setPrefOnInit(PreferenceGenerator.typicalPref(), "easy-routes.weights");
        PreferenceGenerator.setPrefOnInit(PreferenceGenerator.typicalPref(), "easy-routes.weights.bus");
        PreferenceGenerator.setPrefOnInit(PreferenceGenerator.railwayPref(), "easy-routes.weights.tram");
    }

    private static LatLon pointOnCircle(float radius, float angleInDegrees)
    {
        float x = (float)(radius * Math.cos(angleInDegrees * Math.PI / 180F));
        float y = (float)(radius * Math.sin(angleInDegrees * Math.PI / 180F));
        return new LatLon(x, y);
    }

    private static void containsWithOrder(List<Node> original, List<Node> toBeContained) {
        int tbcPosition = 0;
        for(int i=0; i<original.size(); i++) {
            if(toBeContained.get(tbcPosition) == original.get(i)) {
                tbcPosition++;
            }
        }
        if(tbcPosition != toBeContained.size())
            fail();
    }

    private static List<Node> filter(List<Node> original, int[] arry) {
        List <Node> ret = new ArrayList<>();
        for(int i=0; i<arry.length; i++) {
            ret.add(original.get(arry[i]));
        }
        return ret;
    }

    private static List<Node> filter(Map<Integer, Node> original, int[] arry) {
        List <Node> ret = new ArrayList<>();
        for(int i=0; i<arry.length; i++) {
            ret.add(original.get(arry[i]));
        }
        return ret;
    }

    private static void checkRouting(List<Node> original, int[] arry, int start, int stop, RoutingPreferences pref) {
        try {
            RoutingCalculator calc = new RoutingCalculator(original.get(start), original.get(stop), pref);
            List <Node> rtt = calc.getPath();
            containsWithOrder(rtt, filter(original, arry));
        }
        catch(NodeConnectException e) {
            fail("NodeConnectException");
        }
    }

    private static void checkRouting(Map<Integer, Node> original, int[] arry, int start, int stop, RoutingPreferences pref) {
        try {
            RoutingCalculator calc = new RoutingCalculator(original.get(start), original.get(stop), pref);
            List <Node> rtt = calc.getPath();
            containsWithOrder(rtt, filter(original, arry));
        }
        catch(NodeConnectException e) {
            fail("NodeConnectException");
        }
    }

    @Test
    public void test1() {
        DataSet ds = new DataSet();
        List<Node> tmpList = new ArrayList<>();
        int circleSize = 20;
        for(int i=0; i<circleSize; i++) {
            LatLon ll = pointOnCircle(0.1f, 360/circleSize*i);
            tmpList.add(new Node(ll));
        }
        Way w = new Way();
        for(Node x : tmpList) {
            w.addNode(x);
            ds.addPrimitive(x);
        }
        w.addNode(tmpList.get(0));
        Map<String, String> keys = new HashMap<>();
        keys.put("highway", "primary");
        w.setKeys(keys);
        ds.addPrimitive(w);
        RoutingPreferences pref = new RoutingPreferences(Main.pref.getArray("easy-routes.weights.bus"));
        checkRouting(tmpList, new int[]{0, 4, 9}, 0, 9, pref);
        checkRouting(tmpList, new int[]{0, 19, 15, 11}, 0, 11, pref);
        checkRouting(tmpList, new int[]{19, 2, 4}, 18, 4, pref);
        keys = new HashMap<>();
        keys.put("highway", "primary");
        keys.put("oneway", "yes");
        w.setKeys(keys);
        checkRouting(tmpList, new int[]{0, 4, 9}, 0, 9, pref);
        checkRouting(tmpList, new int[]{0, 7, 12, 19}, 0, 19, pref);
        checkRouting(tmpList, new int[]{5, 6}, 5, 6, pref);
        checkRouting(tmpList, new int[]{19, 2, 4}, 18, 4, pref);
    }

    @Test
    public void test2() {
        try {
            DataSet ds2 = OsmReader.parseDataSet(new FileInputStream("test/test1.osm"), null);
            RoutingPreferences pref = new RoutingPreferences(Main.pref.getArray("easy-routes.weights.bus"));
            Map<Integer, Node> tmpList = TestTools.getTestData(Node.class, ds2);
            System.out.println(tmpList.size() +" ###");
            checkRouting(tmpList, new int[]{1, 2, 3}, 1, 3, pref);
            checkRouting(tmpList, new int[]{3, 2, 1}, 3, 1, pref);
            checkRouting(tmpList, new int[]{1, 7, 6}, 1, 6, pref);
            checkRouting(tmpList, new int[]{8, 10, 9}, 8, 9, pref);
            boolean ok = false;
            try {
                RoutingCalculator calc = new RoutingCalculator(tmpList.get(4), tmpList.get(3), pref);
            }
            catch(NodeConnectException e) {
                ok = true;
            }
            assert(ok);
            //checkRouting(tmpList, new int[]{}, 4, 3, pref);
        }
        catch(Exception e) {
            fail();
        }
    }
}
