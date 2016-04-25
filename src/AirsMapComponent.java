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
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.Intersects;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by SerP on 17.04.2016.
 */

// Что должен уметь компонент
// 1. Получать строку подключения к серверу
// 2.
public class AirsMapComponent {
    public static JMapPanel workMapPanel;
    private String capabilitiesURL;

    private static enum GeoDataFormatSpecifier {
        WFS("wfs", "Вектор", "*.shp"), //
        WMS("wms", "Картинка", "*.png", "*.jpeg", "*.jpg", "*.tiff", "*.tif", "*.bmp"); //

        private String id;
        private String[] suffixes;

        private GeoDataFormatSpecifier( String id, String desc, String... suffixes ) {
            this.id = id;
            this.suffixes = new String[suffixes.length];
            for( int i = 0; i < suffixes.length; i++ ) {
                this.suffixes[i] = suffixes[i];
            }
        }
    };
    private static final Set<GeoDataFormatSpecifier> supportedFormat = new TreeSet<GeoDataFormatSpecifier>();
    static {
        for( GeoDataFormatSpecifier format : GeoDataFormatSpecifier.values() ) {
            supportedFormat.add(format);
        }
    }
/*
    AirsMapComponent() {
        this("http://192.168.1.80:8180/geoserver/wfs?REQUEST=GetCapabilities&version=1.0.0");
    }
    */

    AirsMapComponent() {
        this("http://localhost:8080/geoserver/wfs?service=WFS&request=GetCapabilities&version=1.0.0");
    }

    AirsMapComponent(String strGeoServerUrl) {
        capabilitiesURL = strGeoServerUrl;
        try {
            supressInfo();
            dataAccess(capabilitiesURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    AirsMapComponent(String strGeoServerHost, Integer hostGeoServerPort, String strGeoServer, List<String> geoDataType)
    {
        if(geoDataType.isEmpty())
            throw new NullPointerException("GeoDataType is empty");

        geoDataType.containsAll(supportedFormat);

        StringBuilder strGeoServerServerUrl = new StringBuilder();
        strGeoServerServerUrl.append(strGeoServerHost).append(":").append(hostGeoServerPort).append("/")
                             .append(strGeoServer).append("/").append(geoDataType.get(0)).append("?")
                             .append("service=").append(geoDataType.get(0)).append("&request=GetCapabilities");
        capabilitiesURL = strGeoServerServerUrl.toString();
        try {
            supressInfo();
            dataAccess(capabilitiesURL);
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



    private static void dataAccess( String getCapabilities ) throws Exception {
        // Шаг 1 - Параметры подключения
        Map connectionParameters = new HashMap();
        connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getCapabilities );
        connectionParameters.put("WFSDataStoreFactory:WFS_STRATEGY", "geoserver" );

        // Шаг 2 - Подключение
        DataStore data = DataStoreFinder.getDataStore( connectionParameters );

        // Шаг 3 - Находим название схемы/слоя
        String typeNames[] = data.getTypeNames();
        String typeName = "ForOracleWS_REGIONS2010";
        SimpleFeatureType schema = data.getSchema( typeName );

        // Шаг 4 - Устанавливаем источник
        FeatureSource<SimpleFeatureType, SimpleFeature> source = data.getFeatureSource( typeName );

        // Шаг 5 - Запрос к серверу
        String geomName = schema.getGeometryDescriptor().getLocalName(); // Описание
        String strProp = schema.getDescriptor("REGION").getLocalName();
        Envelope bbox = new Envelope( 32.036285400390625, 37.563629150390625, 54.66497802734375, 55.89157104492185 ); // пространство, которое будет отображаться

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( GeoTools.getDefaultHints() );
        Object polygon = JTS.toGeometry( bbox ); // полигон для фильтра
        Intersects filter = ff.intersects( ff.property( geomName ), ff.literal( polygon ) );

        Query query = new DefaultQuery( typeName, filter, new String[]{ geomName, strProp } ); // запрос к серверу

        SimpleFeatureCollection features = (SimpleFeatureCollection) source.getFeatures(query);

        SimpleFeatureIterator iterator2 = features.features();
        while( iterator2.hasNext() ){
            SimpleFeature sf = (SimpleFeature) iterator2.next();
            java.util.List<Object> attr = sf.getAttributes();
        }

        // POI
        FeatureSource<SimpleFeatureType, SimpleFeature> source_poi = data.getFeatureSource( "ForOracleWS_POI_OSM" );

        FeatureSource<SimpleFeatureType, SimpleFeature> source_reqion = data.getFeatureSource( "ForOracleWS_REGIONS2010" );

        MapContent mapcontent = new MapContent();

        Style allStyle =  SLD.createPolygonStyle(Color.green, Color.black, (float) 0.5);

        CachingFeatureSource cache = new CachingFeatureSource(source);
        Layer allLayer = new FeatureLayer(cache, allStyle);
        mapcontent.addLayer(allLayer);
        mapcontent.setTitle("Map");
        workMapPanel = new JMapPanel();
        workMapPanel.setDataStore(data);
        workMapPanel.showMap(mapcontent);
    }
}
