/**
 * Created by SerP on 28.02.2016.
 */

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thoughtworks.xstream.core.Caching;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wfs.internal.parsers.CachingGetFeatureParser;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.jaitools.tilecache.DiskCachedTile;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.Identifier;
import org.opengis.filter.spatial.Intersects;

import com.vividsolutions.jts.geom.Envelope;

import  org.geotools.map.MapContent;
import org.geotools.data.CachingFeatureSource;

import javax.media.jai.CachedTile;


public class WFSExample {
    /**
     * Before running this application please install and start geoserver on your local machine.
     * @param args
     */
    public static void main( String[] args ){
        //String getCapabilities =    "http://localhost:8080/geoserver/wfs?service=WFS&request=GetCapabilities";
        String getCapabilities = "http://192.168.1.80:8180/geoserver/wfs?REQUEST=GetCapabilities&version=1.0.0";
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
        System.out.println( "Schema Attributes:"+schema.getAttributeCount() );

        // Step 4 - target
        FeatureSource<SimpleFeatureType, SimpleFeature> source = data.getFeatureSource( typeName );
        System.out.println( "Metadata Bounds:"+ source.getBounds() );

        // Step 5 - query
        String geomName = schema.getGeometryDescriptor().getLocalName();
        //Envelope bbox = new Envelope( -100.0, -70, 25, 40 );
        Envelope bbox = new Envelope( 32.036285400390625, 37.563629150390625, 54.66497802734375, 55.89157104492185 );

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( GeoTools.getDefaultHints() );
        Object polygon = JTS.toGeometry( bbox );
        Intersects filter = ff.intersects( ff.property( geomName ), ff.literal( polygon ) );

        Query query = new DefaultQuery( typeName, filter, new String[]{ geomName } );
        //FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures( query );

        ReferencedEnvelope bounds = new ReferencedEnvelope();
        //Iterator<SimpleFeature> iterator = features.iterator();

        SimpleFeatureCollection features = (SimpleFeatureCollection) source.getFeatures(query);
        SimpleFeatureIterator iterator = features.features();

        try {
            while( iterator.hasNext() ){
                Feature feature = (Feature) iterator.next();
                bounds.include( feature.getBounds() );
                System.out.printf(feature.toString() + "\n");
                System.out.printf(feature.getIdentifier().toString() + "\n");
            }
            System.out.println( "Calculated Bounds:"+ bounds );
        }
        finally {
           // features.close( iterator );
           ;
        }
        SimpleFeatureIterator iterator2 = features.features();
        while( iterator2.hasNext() ){
            SimpleFeature sf = (SimpleFeature) iterator2.next();
            List<Object> attr = sf.getAttributes();
            System.out.println(sf);
            System.out.println(sf.getID());
        }

        MapContent mapcontent = new MapContent();

        Style allStyle =  SLD.createPolygonStyle(Color.green, Color.black, (float) 0.5);
        Style polygonStyle =  SLD.createPolygonStyle(Color.green, Color.YELLOW, (float) 0.5);
        Style pointStyle = SLD.createPointStyle("Circle", Color.RED, Color.RED, 0.5f, 10);

        CachingFeatureSource cache = new CachingFeatureSource(source);
        Layer allLayer = new FeatureLayer(cache, allStyle);
        Layer polygonLayer = new FeatureLayer(features, polygonStyle);
        Layer pointLayer = new FeatureLayer(features, pointStyle);
        mapcontent.addLayer(allLayer);
        mapcontent.addLayer(polygonLayer);
        mapcontent.addLayer(pointLayer);
        /*
        for( Layer wmsLayer : wmsLayers ){
            WMSLayer displayLayer = new WMSLayer(wms, wmsLayer );
            mapcontent.addLayer(displayLayer);
        }
        */

        // Now display the map
        //JMapFrame.showMap(mapcontent);
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
        System.out.println( "Schema Attributes:"+schema.getAttributeCount() );

        // Step 4 - target
        FeatureSource<SimpleFeatureType, SimpleFeature> source = data.getFeatureSource( typeName );
        System.out.println( "Metadata Bounds:"+ source.getBounds() );

        // CachingFeatureSource is deprecated as experimental (not yet production ready)


        // Step 5 - query
        FilterFactory ff = CommonFactoryFinder.getFilterFactory( GeoTools.getDefaultHints() );

        DefaultQuery query = new DefaultQuery( typeName, Filter.INCLUDE );
        query.setMaxFeatures(2);
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures( query );

        /*
        String fid = null;
        Iterator<SimpleFeature> iterator = features.iterator();
        try {
            while( iterator.hasNext() ){
                SimpleFeature feature = (SimpleFeature) iterator.next();
                fid = feature.getID();
            }
        }
        finally {
            features.close( iterator );
        }
        */
        // step 6 modify
        Transaction t = new DefaultTransaction();

        FeatureStore<SimpleFeatureType, SimpleFeature> store = (FeatureStore<SimpleFeatureType, SimpleFeature>) source;
        store.setTransaction( t );
        Set<Identifier> ids = new HashSet<Identifier>();
        /*
        ids.add( ff.featureId(fid) );
        Filter filter = ff.id( ids );
        try {
            store.removeFeatures( filter );
        }
        finally {
            t.rollback();
        }
        */
    }

}