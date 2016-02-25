
import org.geotools.data.ows.Layer;
import org.geotools.data.wms.WMS1_0_0;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.response.GetMapResponse;
import org.geotools.map.MapContent;
import org.geotools.map.WMSLayer;
import org.geotools.ows.ServiceException;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.wms.WMSLayerChooser;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by SerP on 25.02.2016.
 */
public class TestGeoTool extends JFrame  {
    public static void main(String[] args) throws IOException, ServiceException {
        // display a data store file chooser dialog for shapefiles
        /*
        URL capabilitiesURL = WMSChooser.showChooseWMS();
        if( capabilitiesURL == null ){
            System.exit(0); // canceled
        }
        */
        //testWebServer();


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
