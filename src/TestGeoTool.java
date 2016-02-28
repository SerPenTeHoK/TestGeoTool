
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.geoserver.gwc.GWC;
import org.geoserver.gwc.wms.CachingWebMapService;
import org.geoserver.wfs.WebFeatureService;

import org.geotools.data.*;
import org.geotools.data.ows.Layer;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.data.wms.WMS1_0_0;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.response.GetMapResponse;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geometry.jts.WKTReader2;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.map.WMSLayer;
import org.geotools.ows.ServiceException;
import org.geotools.referencing.CRS;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.wms.WMSLayerChooser;
import org.geotools.util.ObjectCache;
import org.geotools.util.ObjectCaches;
import org.geowebcache.GeoWebCache;
import org.opengis.feature.Feature;

import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.xml.Encoder;
import org.opengis.feature.simple.SimpleFeature;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.spatial.Intersects;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.xml.sax.SAXException;
import sun.swing.ImageCache;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * Created by SerP on 25.02.2016.
 */
public class TestGeoTool extends JFrame  {
    public static void main(String[] args) throws IOException, ServiceException, ParseException, InterruptedException, FactoryException {

        String getCapabilities = "http://192.168.1.80:8180/geoserver/wfs?REQUEST=GetCapabilities&version=1.0.0";

        Map connectionParameters = new HashMap();
        connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getCapabilities);
        connectionParameters.put("WFSDataStoreFactory:USERNAME", "admin");
        connectionParameters.put("WFSDataStoreFactory:PASSWORD","geoserver");

// Step 2 - connection
        DataStore data = DataStoreFinder.getDataStore(connectionParameters);

// Step 3 - discouvery
        String typeNames[] = data.getTypeNames();
        String typeName = typeNames[0];
        SimpleFeatureType schema = data.getSchema(typeName);

// Step 4 - target
        SimpleFeatureSource source = data.getFeatureSource( typeName );
        FilterFactory2 ff2 = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
        //FeatureSource<SimpleFeatureType, SimpleFeature> source = data.getFeatureSource(typeName);
        System.out.println("Metadata Bounds:" + source.getBounds());

// Step 5 - query
        String geomName = schema.getGeometryDescriptor().getName().getLocalPart();//"geometry";// schema.getDefaultGeometry().getLocalName();

        CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4200");
        //Envelope bbox = new Envelope(190000,190470,442000,441227);
        // Object polygon = JTS.toGeometry(bbox);
        Geometry geom = new WKTReader2().read("MULTIPOLYGON (((189792 441960,190344 441952,190304 441392,189856 441280,189792 441960)))");
        //MULTIPOLYGON (((189792 441960,190344 441952,190304 441392,189856 441280,189792 441960))))
        // Filter f = CQL.toFilter("INTERSECTS(geom, MULTIPOLYGON (((189792 441960,190344 441952,190304 441392,189856 441280,189792 441960))))");
        Intersects f = ff2.intersects( ff2.property( geomName ), ff2.literal( geom ) );
        Query query = new Query(typeName);//, f, new String[]{geomName});
        query.setFilter(f);
        query.setMaxFeatures(100);
        query.setCoordinateSystem(sourceCRS);
        query.setStartIndex(0);
        /*
        query.setSortBy(new SortBy[] {
                ff2.sort("weg_nr", "DESC".equals("ASC") ? SortOrder.DESCENDING : SortOrder.ASCENDING)
        });
        */

        SimpleFeatureCollection features = source.getFeatures(query);

        SimpleFeatureIterator iterator = features.features();
        try {

            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                // doe iets met je feature
            }
        } catch (Exception e) {
            int b = 0;
        } finally {
            if (iterator != null) {
                iterator.close();
            }
            source.getDataStore().dispose();
        }

/*
        String getCapabilities = "http://192.168.1.80:8180/geoserver/wfs?REQUEST=GetCapabilities&version=1.0.0";
        Map connectionParameters2 = new HashMap();
        connectionParameters2.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getCapabilities);
        connectionParameters2.put("WFSDataStoreFactory:OUTPUTFORMAT", "text/xml; subType=gml/3.1.1/profiles/gmlsf/1.0.0/0");
        connectionParameters2.put(WFSDataStoreFactory.LENIENT.key, true );
        connectionParameters2.put(WFSDataStoreFactory.MAXFEATURES.key, 2);
        connectionParameters2.put(WFSDataStoreFactory.TIMEOUT.key, 600000);
        connectionParameters2.put("WFSDataStoreFactory:USERNAME", "admin");
        connectionParameters2.put("WFSDataStoreFactory:PASSWORD","geoserver");

        WFSDataStoreFactory dsf = new WFSDataStoreFactory();
        WFSDataStore dataStore1 = dsf.createDataStore(connectionParameters2);
        SimpleFeatureSource source = dataStore1.getFeatureSource("ForOracleWS_REGIONS2010");
        SimpleFeatureCollection fc = source.getFeatures();
        while(fc.features().hasNext()){
            SimpleFeature sf = fc.features().next();
            System.out.println(sf.getAttribute("REGION"));
            System.out.println(sf);
            System.out.println(sf.getID());
            //sleep(1000);
        }
*/
/*
        DataStore dataStore;
        String server = "http://192.168.1.80:8180/geoserver/wfs?REQUEST=GetCapabilities&version=2.0.0";
        //String layer = "topp:states"; // Feature TypeName
        String layer = "ForOracleWS_REGIONS2010";

        Map<String, Object> connectionParameters = new HashMap<String, Object>();
        connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", server);
        dataStore = DataStoreFinder.getDataStore(connectionParameters);

        SimpleFeatureSource featureSource = dataStore.getFeatureSource(layer);
        SimpleFeatureCollection featureCollection = featureSource.getFeatures();
        SimpleFeatureIterator featuresIterator = featureCollection.features();

        SimpleFeatureType schema = dataStore.getSchema( layer );
        FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource( layer );
        System.out.println( "Metadata Bounds:"+ source.getBounds() );

        String geomName = schema.getGeometryDescriptor().getLocalName();
        Envelope bbox = new Envelope( 37.036285400390625, 37.563629150390625, 55.66497802734375, 55.89157104492185 );

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( GeoTools.getDefaultHints() );
        Object polygon = JTS.toGeometry( bbox );
        Intersects filter = ff.intersects( ff.property( geomName ), ff.literal( polygon ) );
        //не правильный звпрос про геометрию
        Query query = new DefaultQuery( layer, filter, new String[]{ geomName } );
        //FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures( query );
        SimpleFeatureIterator featuresIterator2 = (SimpleFeatureIterator) source.getFeatures( query );
        while (featuresIterator2.hasNext()) {
            SimpleFeature feature = featuresIterator2.next();
            System.out.println(feature.getAttribute("REGION"));
        }

        while (featuresIterator.hasNext()) {
            SimpleFeature feature = featuresIterator.next();
            System.out.println(feature.getAttribute("REGION"));
        }
        */
/*
        String getCapabilities = "http://192.168.1.80:8180/geoserver/wfs?REQUEST=GetCapabilities&version=1.0.0";
        Map connectionParameters2 = new HashMap();
        connectionParameters2.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getCapabilities);
        connectionParameters2.put("WFSDataStoreFactory:OUTPUTFORMAT", "text/xml; subType=gml/3.1.1/profiles/gmlsf/1.0.0/0");
        connectionParameters2.put(WFSDataStoreFactory.LENIENT.key, true );
        connectionParameters2.put(WFSDataStoreFactory.MAXFEATURES.key, 2);
        connectionParameters2.put(WFSDataStoreFactory.TIMEOUT.key, 600000);
        connectionParameters2.put("WFSDataStoreFactory:USERNAME", "admin");
        connectionParameters2.put("WFSDataStoreFactory:PASSWORD","geoserver");

        WFSDataStoreFactory dsf = new WFSDataStoreFactory();
        WFSDataStore dataStore1 = dsf.createDataStore(connectionParameters2);
        SimpleFeatureSource source = dataStore1.getFeatureSource("ForOracleWS_REGIONS2010");
        SimpleFeatureCollection fc = source.getFeatures();
        while(fc.features().hasNext()){
            SimpleFeature sf = fc.features().next();
            System.out.println(sf.getAttribute("REGION"));
            System.out.println(sf);
            System.out.println(sf.getID());
            //sleep(1000);
        }

        // display a data store file chooser dialog for shapefiles
        /*
        URL capabilitiesURL = WMSChooser.showChooseWMS();
        if( capabilitiesURL == null ){
            System.exit(0); // canceled
        }
        */
        //testWebServer();
/*
        // Home
        String getCapabilities  = "http://192.168.1.80:8180/geoserver/wms?service=WMS&request=GetCapabilities";
        // Work
        //String getCapabilities = "http://localhost:8080/geoserver/wfs?REQUEST=GetCapabilities";

        Map connectionParameters = new HashMap();
        connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getCapabilities );

// Step 2 - connection
        DataStore data = DataStoreFinder.getDataStore( connectionParameters );

// Step 3 - discouvery
        String typeNames[] = data.getTypeNames();
        String typeName = "ForOracleWS_REGIONS2010";//typeNames[0];
        SimpleFeatureType schema = data.getSchema( typeName );

// Step 4 - target
        FeatureSource<SimpleFeatureType, SimpleFeature> source = data.getFeatureSource( typeName );
        System.out.println( "Metadata Bounds:"+ source.getBounds() );

        String geomName = schema.getGeometryDescriptor().getLocalName();
        Envelope bbox = new Envelope( 37.036285400390625, 37.563629150390625, 55.66497802734375, 55.89157104492185 );

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( GeoTools.getDefaultHints() );
        Object polygon = JTS.toGeometry( bbox );
        Intersects filter = ff.intersects( ff.property( geomName ), ff.literal( polygon ) );
        //не правильный звпрос про геометрию
        Query query = new DefaultQuery( typeName, filter, new String[]{ geomName } );
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures( query );

        ReferencedEnvelope bounds = new ReferencedEnvelope();
        features.toArray();

        // Step 5 - query
 /*
        Iterator<SimpleFeature> iterator = features.iterator();
        try {
            while( iterator.hasNext() ){
                Feature feature = (Feature) iterator.next();
                bounds.include( feature.getBounds() );
            }
            System.out.println( "Calculated Bounds:"+ bounds );
        }
        finally {
            features.close( iterator );
        }
*/


        // Home
        //URL capabilitiesURL = new URL("http://192.168.1.80:8180/geoserver/wms?service=WMS&request=GetCapabilities");
        // Work
        /*
        URL capabilitiesURL = new URL("http://localhost:8080/geoserver/wms?service=WMS&request=GetCapabilities");
        WebMapServer wms = new WebMapServer( capabilitiesURL );

        List<Layer> wmsLayers = WMSLayerChooser.showSelectLayer( wms );
        if( wmsLayers == null ){
            JOptionPane.showMessageDialog(null, "Could not connect - check url");
            System.exit(0);
        }
        MapContent mapcontent = new MapContent();
        mapcontent.setTitle( wms.getCapabilities().getService().getTitle() );

        for( Layer wmsLayer : wmsLayers ){
            WMSLayer displayLayer = new WMSLayer(wms, wmsLayer );
            mapcontent.addLayer(displayLayer);
        }

        // Now display the map
        JMapFrame.showMap(mapcontent);
        */

    }
    private static void testShpFile() throws IOException {
        /*
        File file = JFileDataStoreChooser.showOpenFile("shp", null);
        Map<String,Object> params = new HashMap<String,Object>();
        params.put( "url", file.toURI().toURL() );
        params.put( "create spatial index", false );
        params.put( "memory mapped buffer", false );
        params.put( "charset", "ISO-8859-1" );
        DataStore store = DataStoreFinder.getDataStore( params );
        SimpleFeatureSource featureSource = store.getFeatureSource( store.getTypeNames()[0] );
        // CachingFeatureSource is deprecated as experimental (not yet production ready)
        CachingFeatureSource cache = new CachingFeatureSource(featureSource);
        // Create a map content and add our shapefile to it
        MapContent map = new MapContent();
        map.setTitle("Using cached features");
        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        Layer layer = new FeatureLayer(cache, style);
        map.addLayer(layer);
        // Now display the map

        JMapFrame.showMap(map);
*/
    }

    private static void testWebServer()
    {

        URL url = null;
        try {
            url = new URL("http://localhost:8080/geoserver_war/wms?bbox=-130,24,-66,50&styles=population&Format=image/png&request=GetMap&layers=topp:states&width=550&height=250&srs=EPSG:4326");
        } catch (MalformedURLException e) {
            //will not happen
        }

        WebMapServer wms = null;
        try {
            wms = new WebMapServer(url);
            //List<Layer> wmsLayers = WMSLayerChooser.showSelectLayer( wms );
            GetMapRequest request = wms.createGetMapRequest();
        request.setFormat("image/png");
        request.setDimensions("583", "420"); //sets the dimensions to be returned from the server
        request.setTransparent(true);
        request.setSRS("EPSG:4326");
        request.setBBox("-131.13151509433965,46.60532747661736,-117.61620566037737,56.34191403281659");

        GetMapResponse response = (GetMapResponse) wms.issueRequest(request);
        BufferedImage image = ImageIO.read(response.getInputStream());

            Graphics gra = image.getGraphics();

        } catch (IOException e) {
            //There was an error communicating with the server
            //For example, the server is down
        } catch (ServiceException e) {
            //The server returned a ServiceException (unusual in this case)
        } catch (SAXException e) {
            //Unable to parse the response from the server
            //For example, the capabilities it returned was not valid
        }

    }
}
