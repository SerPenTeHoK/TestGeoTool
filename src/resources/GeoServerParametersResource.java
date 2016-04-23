package resources;

/**
 * Created by SerP on 23.04.2016.
 */
@SuppressWarnings("UnusedDeclaration")
public class GeoServerParametersResource {
    private final String host;
    private final String port;
    private final String mapSourceType;

    public GeoServerParametersResource() {
        this.host = "";
        this.port = "";
        this.mapSourceType = "";

    }
    public GeoServerParametersResource(String host, String port, String mapSourceType) {
        this.host = host;
        this.port = port;
        this.mapSourceType = mapSourceType;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getMapSourceType() {
        return mapSourceType;
    }

    public String getConnection() {
        return host + ":" + port + "/geoserver/" + mapSourceType + "?REQUEST=GetCapabilities&version=1.0.0";
    }

    @Override
    public String toString() {
        return "GeoServerParametersResource{" +
                "host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", mapSourceType='" + mapSourceType + '\'' +
                '}';
    }

}
