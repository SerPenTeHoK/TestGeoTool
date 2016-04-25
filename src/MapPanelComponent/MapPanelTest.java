package MapPanelComponent;

import com.vividsolutions.jts.geom.*;
import net.miginfocom.swing.MigLayout;
import org.geotools.data.*;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.wms.WebMapServer;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.*;
import org.geotools.swing.JMapPane;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.Intersects;
import resources.GeoServerParametersResource;
import sax.ReadXMLFileSAX;

import javax.swing.*;
import java.awt.*;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Level;

import static org.geotools.swt.utils.Utils.filterFactory;
import static org.geotools.swt.utils.Utils.styleFactory;

/**
 * Created by SerP on 24.04.2016.
 */
public class MapPanelTest {
    private static JFrame frame;

    private JPanel GL_panel;

    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JButton button4;
    private JButton button5;

    GeoServerMapPanel geoServerMapPanel;


    private JPanel panel_menu;
    private JPanel panel_Work;


    public static void main(String[] args) {
        frame = new JFrame("MapPanelTest");
        frame.setContentPane(new MapPanelTest().GL_panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(600, 600);
        //frame.resize(600, 600);
        frame.repaint();
        frame.setVisible(true);
    }

    public MapPanelTest() {
        GL_panel = new JPanel();
        GL_panel.setLayout(new BorderLayout());
        panel_menu = new JPanel();
        GL_panel.add(panel_menu, BorderLayout.NORTH);

        panel_menu.setLayout(new FlowLayout(FlowLayout.LEFT));
        button1 = new JButton();
        button1.setText("Load map");
        panel_menu.add(button1);
        button2 = new JButton("Добавить БС");
        panel_menu.add(button2);
        button3 = new JButton("Тест2");
        panel_menu.add(button3);
        button4 = new JButton("Тест3");
        panel_menu.add(button4);

        panel_Work = new JPanel();
        panel_Work.setSize(GL_panel.getWidth(), GL_panel.getHeight());
        GL_panel.add(panel_Work, BorderLayout.WEST);
        //panel_Work.setLayout(new GridLayout(1, 1));
        panel_Work.setLayout(new MigLayout());

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GeoServerParametersResource geoServerResource = (GeoServerParametersResource) ReadXMLFileSAX.readXML("./data/GeoServer.xdb");
                String computername = "";
                try {
                    computername = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e3) {
                    e3.printStackTrace();
                }
                if(!computername.equals("SerP"))
                {
                    geoServerResource.setHost("localhost");
                    geoServerResource.setPort("8180");
                }

                String getCapabilities = geoServerResource.getConnection();
                try {
                    supressInfo();
                    Map connectionParameters = new HashMap();
                    connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getCapabilities );
                    connectionParameters.put("WFSDataStoreFactory:WFS_STRATEGY", "geoserver" );

                    // Step 2 - connection
                    DataStore data = DataStoreFinder.getDataStore( connectionParameters );

                    // Step 3 - discouvery
                    String typeNames[] = data.getTypeNames();
                    String typeName = "ForOracleWS_REGIONS2010";// typeNames[0];
                    SimpleFeatureType schema = data.getSchema( typeName );

                    // Step 4 - target
                    FeatureSource<SimpleFeatureType, SimpleFeature> source = data.getFeatureSource( typeName );
                    /*
                    // Step 5 - query
                    String geomName = schema.getGeometryDescriptor().getLocalName();
                    String strProp = schema.getDescriptor("REGION").getLocalName();
                    Envelope bbox = new Envelope( 32.036285400390625, 37.563629150390625, 54.66497802734375, 55.89157104492185 );

                    FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( GeoTools.getDefaultHints() );
                    Object polygon = JTS.toGeometry( bbox );
                    Intersects filter = ff.intersects( ff.property( geomName ), ff.literal( polygon ) );

                    Query query = new DefaultQuery( typeName, filter, new String[]{ geomName, strProp } );

                    SimpleFeatureCollection features = (SimpleFeatureCollection) source.getFeatures(query);
                    SimpleFeatureIterator iterator2 = features.features();
                    while( iterator2.hasNext() ){
                        SimpleFeature sf = (SimpleFeature) iterator2.next();
                        java.util.List<Object> attr = sf.getAttributes();

                    }

                    Envelope bbox2 = new Envelope(79.036285400390625, 82.563629150390625, 53.66497802734375, 55.89157104492185 );

                    FilterFactory2 ff2 = CommonFactoryFinder.getFilterFactory2( GeoTools.getDefaultHints() );
                    Object polygon2 = JTS.toGeometry( bbox2 );
                    Intersects filter2 = ff.intersects( ff.property( geomName ), ff.literal( polygon2 ) );
                    */
                    MapContent mapcontent = new MapContent();

                    Style allStyle =  SLD.createPolygonStyle(Color.green, Color.black, (float) 0.5);

                    CachingFeatureSource cache = new CachingFeatureSource(source);
                    Layer allLayer = new FeatureLayer(cache, allStyle);

                    mapcontent.addLayer(allLayer);

                    SimpleFeatureType TYPE = null;
                    try {
                        TYPE = DataUtilities.createType("Location",
                                "location:Point:srid=4200," + // <- the geometry attribute:
                                        // Point type
                                        "name:String," + // <- a String attribute
                                        "number:Integer" // a number attribute
                        );
                    } catch (SchemaException e1) {
                        e1.printStackTrace();
                    }
                    // create feature collection
                    DefaultFeatureCollection collection = new DefaultFeatureCollection(
                            null, TYPE);
                    // create geometry factory
                    GeometryFactory geometryFactory = JTSFactoryFinder
                            .getGeometryFactory(null);
                    // create feature builder
                    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);

                    com.vividsolutions.jts.geom.Point point = geometryFactory.createPoint(new Coordinate(37.3, 55.3));//create point

                    //set new feature attributes in featureBuilder
                    featureBuilder.add(point);
                    featureBuilder.add("tmpPoint");
                    featureBuilder.add(1);
                    //create new feature
                    SimpleFeature feature = featureBuilder.buildFeature(null);
                    //add feature to collection
                    collection.add(feature);

                    mapcontent.setTitle("Map");
                    Style style = createPointStyle();
                    Layer layer = new FeatureLayer(collection, style);

                    //geoServerMapPanel.setDataStore(data);
                    //geoServerMapPanel.showMap(mapcontent);

                    if (geoServerMapPanel != null)
                    {
                        panel_Work.remove(geoServerMapPanel);
                        geoServerMapPanel = null;
                        panel_Work.repaint();
                    }

                    geoServerMapPanel = new GeoServerMapPanel(mapcontent);
                    panel_Work.add(geoServerMapPanel);
                    //geoServerMapPanel.showMap(mapcontent);
                    //geoServerMapPanel.repaint();
                    geoServerMapPanel.enableToolBar(true);
                    geoServerMapPanel.enableStatusBar(true);
                    geoServerMapPanel.enableLayerTable(true);
                    geoServerMapPanel.setVisible(true);
                    geoServerMapPanel.mapPane.setVisible(true);
                    geoServerMapPanel.mapPane.repaint();
                    geoServerMapPanel.repaint();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                panel_Work.repaint();
                frame.repaint();
                // ToDo поправить бред
                frame.resize(999, 600);
                frame.resize(1000, 600);
            }
        });

        button2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if(geoServerMapPanel != null)
                {
                    // Координаты точки
                    SimpleFeatureTypeBuilder ftbPoint = new SimpleFeatureTypeBuilder();
                    ftbPoint.setName("BS");
                    ftbPoint.add("geom", Geometry.class);
                    SimpleFeatureType typePoint = ftbPoint.buildFeatureType();

                    java.util.List<Coordinate> coordinateBS = new ArrayList<>();
                    coordinateBS.add((new Coordinate(44, 57)));
                    coordinateBS.add((new Coordinate(44, 37)));

                    GeometryFactory gf = new GeometryFactory();
                    MemoryDataStore ds2 = new MemoryDataStore();
                    java.util.List<SimpleFeature> simpleFeatureList = new ArrayList<>();
                    for(Coordinate coord:coordinateBS)
                    {
                        com.vividsolutions.jts.geom.Point point = gf.createPoint(coord);
                        SimpleFeature sfp = SimpleFeatureBuilder.build(typePoint, new Object[] { point }, null);
                        simpleFeatureList.add(sfp);
                    }
                    SimpleFeature[] simpleFeatures = new SimpleFeature[simpleFeatureList.size()];
                    simpleFeatureList.toArray(simpleFeatures);
                    ds2.addFeatures(simpleFeatures);
                    FeatureSource fs2 = null;
                    try {
                        fs2 = ds2.getFeatureSource("BS");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    Style pointStyle = SLD.createPointStyle("Circle", Color.BLUE, Color.BLUE, 0.7f, 5);
                    Layer layerPointBS = new FeatureLayer(fs2, pointStyle);

                    ((JMapPane) geoServerMapPanel.getMapPane()).getMapContent().addLayer(layerPointBS);
                }

            }
        });

    }
    public static void supressInfo(){
        org.geotools.util.logging.Logging.getLogger("org.geotools.gml").setLevel( Level.SEVERE );
        org.geotools.util.logging.Logging.getLogger("net.refractions.xml").setLevel( Level.SEVERE);
    }

    private static Style createPointStyle() {
        Graphic gr = styleFactory.createDefaultGraphic();

        Mark mark = styleFactory.getCircleMark();

        mark.setStroke(styleFactory.createStroke(
                filterFactory.literal(Color.RED), filterFactory.literal(1)));

        mark.setFill(styleFactory.createFill(filterFactory.literal(Color.CYAN)));
        gr.graphicalSymbols().clear();
        gr.graphicalSymbols().add(mark);
        gr.setSize(filterFactory.literal(7));

		/*
		 * Setting the geometryPropertyName arg to null signals that we want to
		 * draw the default geomettry of features
		 */
        PointSymbolizer sym = styleFactory.createPointSymbolizer(gr, null);

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(sym);
        FeatureTypeStyle fts = styleFactory
                .createFeatureTypeStyle(new Rule[] { rule });
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }

}
