import org.junit.BeforeClass;
import org.junit.Test;
import org.openstreetmap.josm.JOSMFixture;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.io.OsmReader;
import org.openstreetmap.josm.plugins.EasyRoutes.CityEnviroment.RoutingPreferences;
import org.openstreetmap.josm.plugins.EasyRoutes.NewRouting.RoutingCalculator;
import org.openstreetmap.josm.plugins.EasyRoutes.NewRouting.WaySplitter;
import org.openstreetmap.josm.plugins.EasyRoutes.PreferenceGenerator;
import org.openstreetmap.josm.plugins.EasyRoutes.RoutingAlgorithm.NodeConnectException;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class WaySplitterTest {
    @BeforeClass
    public static void init() throws Exception{
        new JOSMFixture("../../core/test/config/functional-josm.home").init(false);
        PreferenceGenerator.setPrefOnInit(PreferenceGenerator.serverPref(), "easy-routes.server");
        PreferenceGenerator.setPrefOnInit(PreferenceGenerator.typicalPref(), "easy-routes.weights");
        PreferenceGenerator.setPrefOnInit(PreferenceGenerator.typicalPref(), "easy-routes.weights.bus");
        PreferenceGenerator.setPrefOnInit(PreferenceGenerator.railwayPref(), "easy-routes.weights.tram");
    }
    @Test
    public void test2() {
        try {
            DataSet ds2 = OsmReader.parseDataSet(new FileInputStream("test/test1.osm"), null);
            RoutingPreferences pref = new RoutingPreferences(Main.pref.getArray("easy-routes.weights.bus"));
            Map<Integer, Node> t = TestTools.getTestData(Node.class, ds2);
            try {
                WaySplitter.splitWays(TestTools.createList(t, 101, 102), pref);
                assertFalse(TestTools.nodesAreOnDifferentWays(t.get(104), t.get(105)));
                WaySplitter.splitWays(TestTools.createList(t, 101, 103), pref);
                assertTrue(TestTools.nodesAreOnDifferentWays(t.get(104), t.get(105)));
                WaySplitter.splitWays(TestTools.createList(t, 1, 3), pref);
                assertTrue(TestTools.nodesAreOnDifferentWays(t.get(1), t.get(7)));
                assertTrue(TestTools.nodesAreOnDifferentWays(t.get(6), t.get(3)));
            } catch (NodeConnectException e) {
                fail();
            }
        }
        catch (Exception e) {
            fail();
        }
    }

}
