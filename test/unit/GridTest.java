import javafx.util.Pair;
import org.openstreetmap.josm.JOSMFixture;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.SaveActionBase;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.*;


import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Rule;
import org.junit.Test;
import org.junit.BeforeClass;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.EasyRoutes.PreferenceGenerator;
import org.openstreetmap.josm.plugins.EasyRoutes.RelationsBuilder.FirstRelationsBuilder;
import org.openstreetmap.josm.plugins.EasyRoutes.RelationsBuilder.RelationsBuilder;

import javax.json.*;

class UniqueIds {
    private static int currentValue = 1;
    public static int getNext() {
        currentValue = currentValue + 1;
        return currentValue - 1;
    }
}

public class GridTest {
    static public int NORTH = 0;
    static public int EAST = 1;
    static public int SOUTH = 2;
    static public int WEST = 3;
    static private double SCALE_X = 0.005;
    static private double SCALE_Y = 0.005;
    static private double SHIFT_X = 0;
    static private double SHIFT_Y = 0;

    //static private Map <String, Pair<Long, Long>> stopList = new Map<String, Pair<Long, Long> >();

    static public String getRef(int x, int y, int direction) {
        int calc1 = x*50 + y + 1001;
        int direction1 = direction + 1;
        return String.valueOf(calc1) + "0" + String.valueOf(direction1);
    }
    static public Node createCenter(DataSet ds, int x, int y) {
        Node mainNode = new Node(UniqueIds.getNext(), 1);
        LatLon mainCoordinates = new LatLon(x*SCALE_X + SHIFT_Y, y*SCALE_Y + SHIFT_Y);
        mainNode.setCoor(mainCoordinates);
        ds.addPrimitive(mainNode);
        return mainNode;
    }
    static public Node createStop(DataSet ds, int x, int y, int direction, boolean isStopPosition, boolean isBusStop) {
        Node mainNode = new Node(UniqueIds.getNext(), 1);
        Map <String, String> mainKeys = new HashMap<>();
        double mainShiftX = 0;
        double mainShiftY = 0;
        if(direction == NORTH) {
            mainShiftY = SCALE_Y * 0.1;
        }
        if(direction == EAST) {
            mainShiftX = SCALE_X * 0.1;
        }
        if(direction == SOUTH) {
            mainShiftY = SCALE_Y * -0.1;
        }
        if(direction == WEST) {
            mainShiftX = SCALE_X * -0.1;
        }
        LatLon mainCoordinates = new LatLon(x*SCALE_X + SHIFT_Y + mainShiftX, y*SCALE_Y + SHIFT_Y + mainShiftY);
        mainNode.setCoor(mainCoordinates);
        if(isStopPosition) {
        mainKeys.put("ref", getRef(x, y, direction));
        mainKeys.put("public_transport", "stop_position");
        mainKeys.put("name", "aaaa 0"+(direction+1));
        mainNode.setKeys(mainKeys);
        }

        ds.addPrimitive(mainNode);

        if(direction == NORTH) {
            mainShiftX = SCALE_X * 0.02;
        }
        if(direction == EAST) {
            mainShiftY = SCALE_Y * -0.02;
        }
        if(direction == SOUTH) {
            mainShiftX = SCALE_X * -0.02;
        }
        if(direction == WEST) {
            mainShiftY = SCALE_Y * 0.02;
        }

        if(isBusStop) {
            Node stopNode = new Node(UniqueIds.getNext(), 1);
            Map <String, String> stopKeys = new HashMap<>();
            stopKeys.put("ref", getRef(x, y, direction));
            stopKeys.put("highway", "bus_stop");
            stopKeys.put("name", "Aaaa 0"+(direction+1));
            stopNode.setKeys(stopKeys);
            LatLon stopCoordinates = new LatLon(x*SCALE_X + SHIFT_Y + mainShiftX, y*SCALE_Y + SHIFT_Y + mainShiftY);
            stopNode.setCoor(stopCoordinates);
            ds.addPrimitive(stopNode);
        }
        return mainNode;
    }
    static public List <String> getRefs(int[] route) {
        List <String> ret = new ArrayList<>();
        int isEnd = 0;
        for(int i=0; i<route.length-2; i+=2) {
            if(i+2 > (route.length-2)) {
                isEnd = 1;
            }
            if(route[i] == route[i+2]) {
                if(route[i+1] < route[i+3]) {
                    for(int j=route[i+1]; j<(route[i+3]+isEnd); j++) {
                        ret.add(getRef(route[i], j, NORTH));
                    }
                }
                else {
                    System.out.println("eeeeeelse "+route[i+3] + " " +route[i+1]);
                    for(int j=route[i+1]; j>(route[i+3]-isEnd); j--) {
                        ret.add(getRef(route[i], j, SOUTH));
                    }
                }
            }
            else if(route[i+1] == route[i+3]) {
                if(route[i] < route[i+2]) {
                    for(int j=route[i]; j<(route[i+2]+isEnd); j++) {
                        ret.add(getRef(j, route[i+1], EAST));
                    }
                }
                else {
                    for(int j=route[i]; j>(route[i+2]-isEnd); j--) {
                        ret.add(getRef(j, route[i+1], WEST));
                    }
                }
            }
        }
        System.out.println("RET SIZE " + ret.size());
        return ret;
    }
    static public int[] reverseArray2 (int[] array) {
        int[] ret = new int[array.length];
        for(int i=0; i<array.length; i+=2) {
            ret[array.length-i-2] = array[i];
            ret[array.length-i-1] = array[i+1];
        }
        System.out.println(Arrays.toString(ret));
        return ret;
    }

    static public void createGrid(DataSet ds) {
        Random rand = new Random();
        List<Way> ways = new ArrayList<>();
        for(int i=0; i<40; i++) {
            Way w = new Way();
            Map <String, String> wayTags = new HashMap<>();


            wayTags.put("highway", "tertiary");

            w.setKeys(wayTags);
            ways.add(w);
        }
        for(int i=0; i<20; i++) {
            for(int j=0; j<20; j++) {
                Node n = createStop(ds, i, j, NORTH, true, true);
                Node s = createStop(ds, i, j, SOUTH, true, true);
                Node e = createStop(ds, i, j, EAST, true, true);
                Node w = createStop(ds, i, j, WEST, true, true);
                Node center = createCenter(ds, i, j);
                ways.get(i).addNode(s);
                ways.get(i).addNode(center);
                ways.get(i).addNode(n);
                ways.get(j+20).addNode(w);
                ways.get(j+20).addNode(center);
                ways.get(j+20).addNode(e);
            }
        }
        for(int i=0; i<40; i++) {
            ds.addPrimitive(ways.get(i));
        }
        Relation parent = new Relation(UniqueIds.getNext(), 1);
        ds.addPrimitive(parent);
        String str1 = createJson(parent.getId(), "101", new int[]{2, 2, 9, 2, 9, 9});
        System.out.println(str1);
        FirstRelationsBuilder builder = new FirstRelationsBuilder(new String[]{str1}, ds);
        builder.getUnsupportedStops();
        RelationsBuilder builder2 = builder.createRelationsBuilder();
        builder2.createNecessaryRelations();
        builder2.onLoadTrackNodes();
        builder2.createTracks();
        builder2.doFinally();
    }

    static public JsonArray createJsonArray(long[] refs) {
        JsonArrayBuilder factory = Json.createArrayBuilder();
        for(long x : refs) {
            factory.add(x);
        }
        return factory.build();
    }

    static public JsonArray createJsonArray(List <List <String> > refs) {
        JsonArrayBuilder factory = Json.createArrayBuilder();
        for(List <String> x : refs) {
            JsonArrayBuilder factory2 = Json.createArrayBuilder();
            for(String y : x) {
                JsonObjectBuilder factory3 = Json.createObjectBuilder();
                factory3.add("ref", y);
                factory3.add("name_operator", y + " " + y.substring(4));
                factory2.add(factory3);
            }
            factory.add(factory2.build());
        }
        return factory.build();
    }

    static public String createJson(long parent, String line, List <List<String> > refs) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JsonWriter writer = Json.createWriter(outputStream);
        JsonObjectBuilder builderMain = Json.createObjectBuilder()
                .add("type_small", "bus")
                .add("type_large", "Bus")
                .add("line", line)
                .add("parent", createJsonArray(new long[]{parent}))
                .add("masters", createJsonArray(new long[]{}))
                .add("slaves", createJsonArray(new long[]{}))
                .add("stops", createJsonArray(refs));
        JsonObject obj = builderMain.build();
        writer.writeObject(obj);
        writer.close();
        return new String( outputStream.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
    }

    static public String createJson(long parent, String line, int[] route) {
        List <List <String> > ptt = new ArrayList<>();
        ptt.add(getRefs(route));
        ptt.add(getRefs(reverseArray2(route)));
        return createJson(parent, line, ptt);
    }


    @BeforeClass
    public static void init() throws Exception{
        new JOSMFixture("../../core/test/config/functional-josm.home").init(true);
        PreferenceGenerator.setPrefOnInit(PreferenceGenerator.serverPref(), "easy-routes.server");
        PreferenceGenerator.setPrefOnInit(PreferenceGenerator.typicalPref(), "easy-routes.weights");
        PreferenceGenerator.setPrefOnInit(PreferenceGenerator.typicalPref(), "easy-routes.weights.bus");
        PreferenceGenerator.setPrefOnInit(PreferenceGenerator.railwayPref(), "easy-routes.weights.tram");
    }

    @Test
    public void testIt() throws Exception {
        DataSet ds = new DataSet();
        Layer layer = new OsmDataLayer(ds, "lay", null);
        Main.main.setEditDataSet(ds);
        createGrid(ds);
        try {
            SaveActionBase.doSave(layer, new File("/home/dell/foo.osm"), false);
        }
        catch (NullPointerException e) {

        }
    }

}