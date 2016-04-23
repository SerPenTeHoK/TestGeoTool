import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.data.*;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.wms.WebMapServer;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.Identifier;
import org.opengis.filter.spatial.Intersects;
import resources.GeoServerParametersResource;
import sax.ReadXMLFileSAX;

import java.awt.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Level;

/**
 * Created by SerP on 17.04.2016.
 */
public class WorkingExample {
    /**
     * Before running this application please install and start geoserver on your local machine.
     * @param args
     */
    public static void main( String[] args ){
        //String getCapabilities =    "http://localhost:8080/geoserver/wfs?service=WFS&request=GetCapabilities&version=1.0.0";
        //String getCapabilities = "http://192.168.1.80:8180/geoserver/wfs?REQUEST=GetCapabilities&version=1.0.0";
        GeoServerParametersResource geoServerResource = (GeoServerParametersResource) ReadXMLFileSAX.readXML("./data/GeoServer.xdb");
        String computername ="";
        try {
            computername = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if(!computername.equals("SerP"))
        {
            geoServerResource.setHost("localhost");
            geoServerResource.setPort("8080");
        }

        String getCapabilities = geoServerResource.getConnection();
        if( args.length != 0 ){
            getCapabilities = args[0];
        }
        try {
            supressInfo();
            dataAccess( getCapabilities );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void supressInfo(){
        org.geotools.util.logging.Logging.getLogger("org.geotools.gml").setLevel( Level.SEVERE );
        org.geotools.util.logging.Logging.getLogger("net.refractions.xml").setLevel( Level.SEVERE);
    }


    static StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
    static FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();

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

    public static void dataAccess( String getCapabilities ) throws Exception {
        // Step 1 - connection parameters
        //
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

        FeatureSource<SimpleFeatureType, SimpleFeature> veg_source = data.getFeatureSource( "ForOracleWS_VEG" );
        Query query_veg= new DefaultQuery( "ForOracleWS_VEG", filter2, new String[]{ geomName, "TYPE" } );

        SimpleFeatureCollection features3 = (SimpleFeatureCollection) veg_source.getFeatures(query_veg);
        SimpleFeatureIterator iterator3 = features.features();

        // POI
        FeatureSource<SimpleFeatureType, SimpleFeature> source_poi = data.getFeatureSource( "ForOracleWS_POI_OSM" );

        FeatureSource<SimpleFeatureType, SimpleFeature> source_reqion = data.getFeatureSource( "ForOracleWS_REGIONS2010" );

        MapContent mapcontent = new MapContent();

        Style allStyle =  SLD.createPolygonStyle(Color.green, Color.black, (float) 0.5);
        Style polygonStyle =  SLD.createPolygonStyle(Color.green, Color.YELLOW, (float) 0.5);
        Style pointStyle = SLD.createPointStyle("Circle", Color.RED, Color.RED, 0.5f, 10);
        Style vegStyle =  SLD.createPolygonStyle(Color.yellow, Color.green, (float) 0.2);
        Style markerStyle = SLD.createPointStyle("Circle", Color.magenta, Color.magenta, 0.1f, 10);

        FeatureSource<SimpleFeatureType, SimpleFeature> source_veg = data.getFeatureSource( "ForOracleWS_VEG" );

        CachingFeatureSource cache = new CachingFeatureSource(source);
        Layer allLayer = new FeatureLayer(cache, allStyle);
        Layer polygonLayer = new FeatureLayer(features, polygonStyle);
        Layer pointLayer = new FeatureLayer(features, pointStyle);//mapcontent.addLayer(allLayer);
        CachingFeatureSource cache2 = new CachingFeatureSource(source_veg);
        Layer vegLayer = new FeatureLayer(cache2, vegStyle);

        Layer vegLayer2 = new FeatureLayer(features3, vegStyle);

        Layer poiLayer = new FeatureLayer(source_poi, markerStyle);
        Layer reqionLayer = new FeatureLayer(source_reqion, polygonStyle);

        // WMS
        // Home
        URL capabilitiesURL = new URL("http://192.168.1.80:8180/geoserver/wms?service=WMS&request=GetCapabilities");
        // Work
        //URL capabilitiesURL = new URL("http://localhost:8080/geoserver/wms?service=WMS&request=GetCapabilities");
        WebMapServer wms = new WebMapServer( capabilitiesURL );

        mapcontent.addLayer(allLayer);

        SimpleFeatureType TYPE = null;
        try {
            TYPE = DataUtilities.createType("Location",
                    "location:Point:srid=4326," + // <- the geometry attribute:
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


        JMapFrameExtra.setDataStore(data);
        JMapFrameExtra.showMap(mapcontent);

    }



    public static void dataUpdate( String getCapabilities ) throws Exception {
        // Step 1 - connection parameters
        //
        Map connectionParameters = new HashMap();
        connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getCapabilities );

        // Step 2 - connection
        DataStore data = DataStoreFinder.getDataStore( connectionParameters );

        // Step 3 - discouvery
        String typeNames[] = data.getTypeNames();
        String typeName = "ForOracleWS_REGIONS2010";//typeNames[0];
        SimpleFeatureType schema = data.getSchema( typeName );
        //System.out.println( "Schema Attributes:"+schema.getAttributeCount() );

        // Step 4 - target
        FeatureSource<SimpleFeatureType, SimpleFeature> source = data.getFeatureSource( typeName );
        //  System.out.println( "Metadata Bounds:"+ source.getBounds() );

        // CachingFeatureSource is deprecated as experimental (not yet production ready)


        // Step 5 - query
        FilterFactory ff = CommonFactoryFinder.getFilterFactory( GeoTools.getDefaultHints() );

        DefaultQuery query = new DefaultQuery( typeName, Filter.INCLUDE );
        query.setMaxFeatures(2);
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures( query );

        // step 6 modify
        Transaction t = new DefaultTransaction();

        FeatureStore<SimpleFeatureType, SimpleFeature> store = (FeatureStore<SimpleFeatureType, SimpleFeature>) source;
        store.setTransaction( t );
        Set<Identifier> ids = new HashSet<Identifier>();
    }

}
