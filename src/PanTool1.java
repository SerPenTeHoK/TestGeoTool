/**
 * Created by SerP on 28.02.2016.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import java.awt.*;
import java.awt.Point;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.swing.ImageIcon;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.styling.*;
import org.geotools.swing.JMapPane;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.locale.LocaleUtils;
import org.geotools.swing.tool.CursorTool;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.Intersects;

public class PanTool1 extends CursorTool {
    public static final String TOOL_NAME = LocaleUtils.getValue("CursorTool", "Pan");
    public static final String TOOL_TIP = LocaleUtils.getValue("CursorTool", "PanTooltip");
    public static final String CURSOR_IMAGE = "/org/geotools/swing/icons/mActionPan.png";
    public static final Point CURSOR_HOTSPOT = new Point(15, 15);
    public static final String ICON_IMAGE = "/org/geotools/swing/icons/mActionPan.png";
    static StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
    static FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();
    boolean panning;
    JMapFrameExtra Extra;

    //public static List<Coordinate> coordinateMPointList = new ArrayList<>();
    private Cursor cursor;
    private Point panePos;

    public PanTool1(JMapFrameExtra frameExtra) {
        this.Extra = frameExtra;
        Toolkit tk = Toolkit.getDefaultToolkit();
        ImageIcon imgIcon = new ImageIcon(this.getClass().getResource("/org/geotools/swing/icons/mActionPan.png"));
        this.cursor = tk.createCustomCursor(imgIcon.getImage(), CURSOR_HOTSPOT, TOOL_NAME);
        this.panning = false;
    }
    public PanTool1() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        ImageIcon imgIcon = new ImageIcon(this.getClass().getResource("/org/geotools/swing/icons/mActionPan.png"));
        this.cursor = tk.createCustomCursor(imgIcon.getImage(), CURSOR_HOTSPOT, TOOL_NAME);
        this.panning = false;
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
                .createFeatureTypeStyle(new Rule[]{rule});
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }

    public void onMousePressed(MapMouseEvent ev) {
        this.panePos = ev.getPoint();
        this.panning = true;

        MapMouseEvent mme = new MapMouseEvent(((JMapPane) this.getMapPane()), ev);
        DirectPosition2D mapPos = mme.getWorldPos();

        if (ev.isControlDown()) {
            if (Extra.coordinateMPointList.size() > 4) {
                int remLayer;
                remLayer = ((JMapPane) this.getMapPane()).getMapContent().layers().size();
                ((JMapPane) this.getMapPane()).getMapContent().removeLayer(((JMapPane) this.getMapPane()).getMapContent().layers().get(remLayer-1));
                ((JMapPane) this.getMapPane()).getMapContent().removeLayer(((JMapPane) this.getMapPane()).getMapContent().layers().get(remLayer-2));
                ((JMapPane) this.getMapPane()).getMapContent().removeLayer(((JMapPane) this.getMapPane()).getMapContent().layers().get(remLayer-3));
                Extra.coordinateMPointList.clear();
                ((JMapPane) this.getMapPane()).repaint();
                ((JMapPane) this.getMapPane()).updateUI();
            }
        }

        if (ev.isShiftDown()) {
            if (Extra.coordinateMPointList.size() > 1) {
                if (Extra.coordinateMPointList.size() > 2)
                    Extra.coordinateMPointList.remove(Extra.coordinateMPointList.size() - 1);
                Extra.coordinateMPointList.add(new Coordinate(mapPos.getX(), mapPos.getY()));
                Extra.coordinateMPointList.add(Extra.coordinateMPointList.get(0));
                Coordinate[] coordinates = new Coordinate[Extra.coordinateMPointList.size()];
                Extra.coordinateMPointList.toArray(coordinates);

                GeometryFactory gf = new GeometryFactory();
                com.vividsolutions.jts.geom.Polygon polygon = gf.createPolygon(gf
                        .createLinearRing(coordinates), null);
                SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
                ftb.setName("test");
                ftb.add("geom", Geometry.class);
                SimpleFeatureType type = ftb.buildFeatureType();

                SimpleFeature f3 = SimpleFeatureBuilder.build(type, new Object[]{polygon}, null);
                MemoryDataStore ds = new MemoryDataStore();
                try {
                    ds.createSchema(type);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Полигон
                ds.addFeatures(new SimpleFeature[]{f3});
                FeatureSource fs = null;
                try {
                    fs = ds.getFeatureSource("test");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Координаты точки
                MemoryDataStore ds2 = new MemoryDataStore();
                List<SimpleFeature> simpleFeatureList = new ArrayList<>();
                for(Coordinate coord:Extra.coordinateMPointList)
                {
                    com.vividsolutions.jts.geom.Point point = gf.createPoint(coord);
                    SimpleFeature sfp = SimpleFeatureBuilder.build(type, new Object[] { point }, null);
                    simpleFeatureList.add(sfp);
                }
                SimpleFeature[] simpleFeatures = new SimpleFeature[simpleFeatureList.size()];
                simpleFeatureList.toArray(simpleFeatures);
                ds2.addFeatures(simpleFeatures);
                FeatureSource fs2 = null;
                try {
                    fs2 = ds2.getFeatureSource("test");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Style style = createPointStyle();
                Style polygonStyle = SLD.createPolygonStyle(Color.green, Color.blue, (float) 0.2);
                Layer layerPolygon = new FeatureLayer(fs, polygonStyle);
                Style pointStyle = SLD.createPointStyle("Circle", Color.RED, Color.RED, 0.5f, 5);
                Layer layerPoint = new FeatureLayer(fs2, pointStyle);

                if (Extra.coordinateMPointList.size() > 4) {
                    int remLayer;
                    remLayer = ((JMapPane) this.getMapPane()).getMapContent().layers().size();
                    ((JMapPane) this.getMapPane()).getMapContent().removeLayer(((JMapPane) this.getMapPane()).getMapContent().layers().get(remLayer-1));
                    ((JMapPane) this.getMapPane()).getMapContent().removeLayer(((JMapPane) this.getMapPane()).getMapContent().layers().get(remLayer-2));
                    ((JMapPane) this.getMapPane()).getMapContent().removeLayer(((JMapPane) this.getMapPane()).getMapContent().layers().get(remLayer-3));
                }
                ((JMapPane) this.getMapPane()).getMapContent().addLayer(layerPolygon);
                ((JMapPane) this.getMapPane()).getMapContent().addLayer(layerPoint);

                // REG_POI
                try {
                    String typeName = "ForOracleWS_REGIONS2010";// typeNames[0];
                    //String typeName = "ForOracleWS_POI_OSM";
                    //String typeName = "sf_roads";
                    SimpleFeatureType schema = Extra.data.getSchema( typeName );

                    // Step 4 - target
                    FeatureSource<SimpleFeatureType, SimpleFeature> source = Extra.data.getFeatureSource( typeName );

                    // Step 5 - query
                    String geomName = schema.getGeometryDescriptor().getLocalName();
                    //String strProp = schema.getDescriptor("REGION").getLocalName();

                    //Envelope bbox = new Envelope( 32.036285400390625, 37.563629150390625, 54.66497802734375, 55.89157104492185 );

                    FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( GeoTools.getDefaultHints() );
                    //Object polygon = JTS.toGeometry( bbox );
                    Intersects filter = ff.intersects( ff.property( geomName ), ff.literal( polygon ) );

                    //Query query = new DefaultQuery( typeName, filter, new String[]{ geomName, strProp } );
                    Query query = new DefaultQuery( typeName, filter, new String[]{ geomName } );
                    //FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures( query );

                    ReferencedEnvelope bounds = new ReferencedEnvelope();
                    //Iterator<SimpleFeature> iterator = features.iterator();

                    SimpleFeatureCollection features = (SimpleFeatureCollection) source.getFeatures(query);
                    SimpleFeatureIterator iterator = features.features();

                    /*
                    try {
                        while( iterator.hasNext() ){
                            Feature feature = (Feature) iterator.next();
                            bounds.include( feature.getBounds() );
                        }
                    }
                    finally {
                        iterator.close();

                    }

                    SimpleFeatureIterator iterator2 = features.features();
                    while( iterator2.hasNext() ){
                        SimpleFeature sf = (SimpleFeature) iterator2.next();
                        List<Object> attr = sf.getAttributes();
                        //System.out.println(sf);
                        //System.out.println(sf.getID());
                    }
                    */

                    Style polygonStyleRegion =  SLD.createPolygonStyle(Color.green, Color.orange, (float) 0.5);
                    Layer polygonLayerReg = new FeatureLayer(features, polygonStyleRegion);
                    //Style markerStyle = SLD.createPointStyle("Circle", Color.orange, Color.green, 0.9f, 10);
                    //Layer polygonLayerReg = new FeatureLayer(features, markerStyle);

                    ((JMapPane) this.getMapPane()).getMapContent().addLayer(polygonLayerReg);
                    /*
                    //String  typeName = "ForOracleWS_POI_OSM";
                    String  typeName = "ForOracleWS_REGIONS2010";
                    FeatureSource<SimpleFeatureType, SimpleFeature> source_poi = Extra.data.getFeatureSource(typeName );
                    SimpleFeatureType schema = Extra.data.getSchema( typeName );
                    FeatureSource<SimpleFeatureType, SimpleFeature> source = Extra.data.getFeatureSource( typeName );
                    // Step 5 - query
                    String geomName = schema.getGeometryDescriptor().getLocalName();

                    FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( GeoTools.getDefaultHints() );
                    Object polygonQ = polygon;
                    Intersects filter = ff.intersects( ff.property( geomName ), ff.literal( polygonQ ) );
                    Query query = new DefaultQuery( typeName, filter, new String[]{ geomName } );
                    //FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures( query );
                    ReferencedEnvelope bounds = new ReferencedEnvelope();
                    SimpleFeatureCollection features = (SimpleFeatureCollection) source.getFeatures(query);

                    Style markerStyle = SLD.createPointStyle("Circle", Color.orange, Color.green, 0.9f, 10);

                    Layer poiLayer = new FeatureLayer(source_poi, markerStyle);
                    ((JMapPane) this.getMapPane()).getMapContent().addLayer(poiLayer);
                    */
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Extra.getMapPane().setPaintDelay(500);

                /*
                GeometryFactory gf = new GeometryFactory();

                com.vividsolutions.jts.geom.Polygon polygon = gf.createPolygon(gf
                        .createLinearRing(coordinates), null);

                SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
                ftb.setName("test");
                ftb.add("geom", Geometry.class);
                SimpleFeatureType type = ftb.buildFeatureType();

                SimpleFeature f3 = SimpleFeatureBuilder.build(type, new Object[] { polygon }, null);

                MemoryDataStore ds = new MemoryDataStore();
                try {
                    ds.createSchema(type);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ds.addFeatures(new SimpleFeature[] {  f3 });

                FeatureSource fs = null;
                try {
                    fs = ds.getFeatureSource("test");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Style style = createPointStyle();
                Style polygonStyle =  SLD.createPolygonStyle(Color.green, Color.YELLOW, (float) 0.5);
                Layer layer = new FeatureLayer(fs, polygonStyle);
                ((JMapPane)this.getMapPane()).getMapContent().addLayer(layer);
*/
                /*
                SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
                ftb.setName("test");
                ftb.add("geom", Geometry.class);
                SimpleFeatureType type = ftb.buildFeatureType();

                GeometryFactory gf = new GeometryFactory();
                MemoryDataStore ds = new MemoryDataStore();
                com.vividsolutions.jts.geom.Polygon polygon = gf.createPolygon(gf
                        .createLinearRing(coordinates), null);
                SimpleFeature feature = SimpleFeatureBuilder.build(type, new Object[] { polygon }, null);

                try {
                    ds.createSchema(type);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ds.addFeatures(new SimpleFeature[] {  feature });

                FeatureSource fs = null;
                try {
                    fs = ds.getFeatureSource("test");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Style polygonStyle =  SLD.createPolygonStyle(Color.green, Color.YELLOW, (float) 0.2);
                Layer layer = new FeatureLayer(fs, polygonStyle);
                int remLayer;
                remLayer = ((JMapPane)this.getMapPane()).getMapContent().layers().size();
                if(Extra.coordinateMPointList.size() > 3)
                    ((JMapPane)this.getMapPane()).getMapContent().layers().remove(remLayer-1);
                ((JMapPane)this.getMapPane()).getMapContent().addLayer(layer);
                */
            } else {
                Extra.coordinateMPointList.add(new Coordinate(mapPos.getX(), mapPos.getY()));
            }

            /*
            GeometryFactory gf = new GeometryFactory();
            com.vividsolutions.jts.geom.Point point = gf.createPoint(new Coordinate(10, 10));
            LineString line = gf.createLineString(new Coordinate[] {
                    new Coordinate(50, 50), new Coordinate(100, 100) });
            com.vividsolutions.jts.geom.Polygon polygon = gf.createPolygon(gf
                    .createLinearRing(new Coordinate[] { new Coordinate(0, 0),
                            new Coordinate(0, 50), new Coordinate(50, 50),
                            new Coordinate(50, 0), new Coordinate(0, 0) }), null);

            SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
            ftb.setName("test");
            ftb.add("geom", Geometry.class);
            SimpleFeatureType type = ftb.buildFeatureType();

            SimpleFeature f1 = SimpleFeatureBuilder.build(type, new Object[] { point }, null);
            //SimpleFeature f2 = SimpleFeatureBuilder.build(type, new Object[] { line }, null);
            SimpleFeature f3 = SimpleFeatureBuilder.build(type, new Object[] { polygon }, null);

            MemoryDataStore ds = new MemoryDataStore();
            try {
                ds.createSchema(type);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //ds.addFeatures(new SimpleFeature[] { f1, f2, f3 });
            ds.addFeatures(new SimpleFeature[] {  f3 });

            FeatureSource fs = null;
            try {
                fs = ds.getFeatureSource("test");
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Style style = createPointStyle();
            Style polygonStyle =  SLD.createPolygonStyle(Color.green, Color.YELLOW, (float) 0.5);
            Layer layer = new FeatureLayer(fs, polygonStyle);
            ((JMapPane)this.getMapPane()).getMapContent().addLayer(layer);

            Layer lay = ((JMapPane)this.getMapPane()).getMapContent().layers().get(((JMapPane)this.getMapPane()).getMapContent().layers().size()-1);

            try {
                lay.getFeatureSource().getFeatures().size();
            } catch (IOException e) {
                e.printStackTrace();
            }
            SimpleFeatureIterator iterator3 = null;
            try {
                iterator3 = (SimpleFeatureIterator) lay.getFeatureSource().getFeatures().features();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                while( iterator3.hasNext() ){
                    SimpleFeature featureI = iterator3.next();
                    GeometryFactory gf2 = new GeometryFactory();
                    com.vividsolutions.jts.geom.Polygon polygon2 = gf2.createPolygon(gf2
                            .createLinearRing(new Coordinate[] { new Coordinate(0, 0),
                                    new Coordinate(0, 200), new Coordinate(200, 200),
                                    new Coordinate(200, 0), new Coordinate(0, 0) }), null);
                    //SimpleFeature f4 = SimpleFeatureBuilder.build(type, new Object[] { polygon2 }, null);
                    java.util.List<Polygon> polygons = new ArrayList<>();
                    polygons.add(polygon2);
                    //setValue(new Object[] {polygon2});
                    featureI = SimpleFeatureBuilder.build(type, new Object[] { polygon2 }, null);

                }
            }
            finally {
                iterator3.close();
            }
            // Перерисовать слой надо
            ((JMapPane)this.getMapPane()).repaint();

            /// TEST Сохранения данных
            SimpleFeature sf3 = null;
            try {
                iterator3 = (SimpleFeatureIterator) lay.getFeatureSource().getFeatures().features();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                while( iterator3.hasNext() ){
                    sf3 = iterator3.next();
                }
            }
            finally {
                iterator3.close();
            }


            MapMouseEvent mme = new MapMouseEvent( ((JMapPane)this.getMapPane()), ev);
            DirectPosition2D mapPos = mme.getWorldPos();

            coordinateMPointList.add(new Coordinate(0, 0));
            coordinateMPointList.add(new Coordinate(0, 200));
            coordinateMPointList.add(new Coordinate(200, 200));
            coordinateMPointList.add(new Coordinate(200, 0));
            coordinateMPointList.add(new Coordinate(mapPos.getX(), mapPos.getY()));
            coordinateMPointList.add(new Coordinate(0, 0));
            Coordinate[] coordinates = new Coordinate[coordinateMPointList.size()];
            coordinateMPointList.toArray(coordinates);
            coordinateMPointList.clear();

            GeometryFactory gf2 = new GeometryFactory();
            com.vividsolutions.jts.geom.Polygon polygon2 = gf2.createPolygon(gf2
                    .createLinearRing(coordinates), null);
            SimpleFeature feature = SimpleFeatureBuilder.build(type, new Object[] { polygon2 }, null);

            try {
                ds.removeSchema("test");
            } catch (IOException e) {
                e.printStackTrace();
            }

            SimpleFeatureTypeBuilder ftb2 = new SimpleFeatureTypeBuilder();
            ftb2.setName("test2");
            ftb2.add("geom", Geometry.class);
            SimpleFeatureType type2 = ftb2.buildFeatureType();
            try {
                ds.createSchema(type2);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ds.addFeatures(new SimpleFeature[] {  feature });

            FeatureSource fs2 = null;
            try {
                fs2 = ds.getFeatureSource("test2");
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Style style = createPointStyle();
            Style polygonStyle2 =  SLD.createPolygonStyle(Color.green, Color.YELLOW, (float) 0.5);
            Layer layer2 = new FeatureLayer(fs2, polygonStyle2);
            int remLayer;
            remLayer = ((JMapPane)this.getMapPane()).getMapContent().layers().size();
            ((JMapPane)this.getMapPane()).getMapContent().layers().remove(remLayer-1);
            ((JMapPane)this.getMapPane()).getMapContent().addLayer(layer2);
            */
/*
            SimpleFeature feature = featureBuilder.buildFeature(null);
            //add feature to collection
            collection.add(feature);
            Style style = createPointStyle();
            Layer layer = new FeatureLayer(collection, style);
            ((JMapPane)this.getMapPane()).getMapContent().addLayer(layer);
            */
            /*
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

            com.vividsolutions.jts.geom.Point point = geometryFactory.createPoint(new Coordinate(55.75, 37.61));//create point

            //set new feature attributes in featureBuilder
            featureBuilder.add(point);
            featureBuilder.add("tmpPoint");
            featureBuilder.add(1);

            point = geometryFactory.createPoint(new Coordinate(56.21, 37.31));//create point

            //set new feature attributes in featureBuilder
            featureBuilder.add(point);
            featureBuilder.add("tmpPoint");
            featureBuilder.add(1);
            //create new feature
            SimpleFeature feature = featureBuilder.buildFeature(null);
            //add feature to collection
            collection.add(feature);

            Style style = createPointStyle();
            Layer layer = new FeatureLayer(collection, style);

            ((JMapPane)this.getMapPane()).getMapContent().addLayer(layer);
            */
            //onAddLayer(layer);
        }
    }

    public void onMouseDragged(MapMouseEvent ev) {
        if (this.panning) {
            ((JMapPane) this.getMapPane()).setPaintDelay(60);
            Point pos = ev.getPoint();
            if (!pos.equals(this.panePos)) {
                ((JMapPane) this.getMapPane()).moveImage(pos.x - this.panePos.x, pos.y - this.panePos.y);
                this.panePos = pos;
            }
        }

    }

    public void onMouseReleased(MapMouseEvent ev) {
        this.panning = false;
    }

    public Cursor getCursor() {
        return this.cursor;
    }

    public boolean drawDragBox() {
        return false;
    }
}
