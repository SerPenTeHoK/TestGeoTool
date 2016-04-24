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
import org.geotools.data.*;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.memory.MemoryFeatureStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.styling.*;
import org.geotools.swing.JMapPane;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.locale.LocaleUtils;
import org.geotools.swing.tool.CursorTool;
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
                // Тупо Удаление слоя с выбранными регионами
                // ((JMapPane) this.getMapPane()).getMapContent().removeLayer(((JMapPane) this.getMapPane()).getMapContent().layers().get(remLayer-3));
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

                /*
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

                 */



/*
                FeatureSource<?, ?> featureSourceTest = layerPointTest.getFeatureSource();
                FeatureCollection featureCollectionTest = null;
                FeatureCollection<SimpleFeatureType, SimpleFeature> featuresCollectionTest2 = null;
                try {
                    featureCollectionTest = featureSourceTest.getFeatures();
                    featuresCollectionTest2 = (FeatureCollection<SimpleFeatureType, SimpleFeature>) featureSourceTest.getFeatures();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                layerPointTest.getFeatureSource();
*/
                //Object[] o1 = featureCollectionTest.toArray();
                //String str1 = featureCollectionTest.toString();
                //Object[] o2 = featuresCollectionTest2.toArray();
                //String str2 = featuresCollectionTest2.toString();

                /*
                FeatureIterator<?> featuresTestIterator = featureCollectionTest.features();
                ///Iterator<SimpleFeature> iterator = featuresCollectionTest2.iterator();

                String fid = null;
                try {
                    while( featuresTestIterator.hasNext() ){
                        SimpleFeature feature = (SimpleFeature) featuresTestIterator.next();
                        fid = feature.getID();
                    }
                }
                finally {
                    featuresTestIterator.close();
                }
                */

                int remLayer;
                remLayer = ((JMapPane) this.getMapPane()).getMapContent().layers().size();

                if (Extra.coordinateMPointList.size() > 4) {

                    // Тупо удаляем слой с точками
                    ((JMapPane) this.getMapPane()).getMapContent().removeLayer(((JMapPane) this.getMapPane()).getMapContent().layers().get(remLayer-1));
                    // Тупо удаляем слой с заливкой полигоном
                    ((JMapPane) this.getMapPane()).getMapContent().removeLayer(((JMapPane) this.getMapPane()).getMapContent().layers().get(remLayer-2));
                    // Тупо Удаление слоя с выбранными регионами
                    //((JMapPane) this.getMapPane()).getMapContent().removeLayer(((JMapPane) this.getMapPane()).getMapContent().layers().get(remLayer-3));
                }
                ((JMapPane) this.getMapPane()).getMapContent().addLayer(layerPolygon);
                ((JMapPane) this.getMapPane()).getMapContent().addLayer(layerPoint);


                // ToDo переделать на добавлеине объектов слой и перерисовка


                // FeatureSource
                Layer layerPointTest = ((JMapPane) this.getMapPane()).getMapContent().layers().get(remLayer-1);
                String nameLayer = layerPointTest.getTitle();
                //Map<String, Object> testLayer = layerPointTest.getUserData();
                // MemoryFeatureStore, CachingFeatureSource
                SimpleFeatureSource source = (SimpleFeatureSource) layerPointTest.getFeatureSource();
                SimpleFeatureCollection simpleFeatureCollection = null;
                try {
                    simpleFeatureCollection = source.getFeatures();
                    SimpleFeatureIterator simpleFeatureIterator = simpleFeatureCollection.features();
                    String fid = null;
                    try {
                        while( simpleFeatureIterator.hasNext() ){
                            SimpleFeature feature = (SimpleFeature) simpleFeatureIterator.next();
                            fid = feature.getID();
                        }
                    }
                    finally {
                        simpleFeatureIterator.close();
                    }

                    // Вставка точке напрямую
                    /*
                    if (source instanceof SimpleFeatureStore) {
                        SimpleFeatureStore featureStore = (SimpleFeatureStore) source;
                        MemoryDataStore mds = (MemoryDataStore) source.getDataStore();
                        SimpleFeatureStore featureStore2 = (SimpleFeatureStore) mds.getFeatureSource("test");

                        //featureStore.modifyFeatures(); // можно модифицировать значение, но не добавлять, т.к. основан на массиве
                        Transaction transaction = new DefaultTransaction(); // "create"
                        simpleFeatureList.get(0).toString();
                        Object objectTest1 = simpleFeatureList.get(0).getValue();
                        SimpleFeatureCollection collection = new ListFeatureCollection(type, simpleFeatureList); // полность обновлять содержание, но не переделывать слой
                        featureStore.setTransaction(transaction);
                        try {
                            FilterFactory2 fff = CommonFactoryFinder.getFilterFactory2( GeoTools.getDefaultHints() );
                            //Object polygon = JTS.toGeometry( bbox );
                            Intersects filter = fff.intersects( fff.property( "test" ), fff.literal( polygon ) );
                            featureStore.removeFeatures(filter);
                            featureStore.addFeatures(collection);
                            transaction.commit();
                        } catch (Exception problem) {
                            problem.printStackTrace();
                            transaction.rollback();
                        } finally {
                            transaction.close();
                        }
                    }
                    else {
                        System.out.println(" does not support read/write access");
                    }
                    */
                    /*
                           if (featureSource instanceof SimpleFeatureStore) {
                            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
                            /*
                             * SimpleFeatureStore has a method to add features from a
                             * SimpleFeatureCollection object, so we use the ListFeatureCollection
                             * class to wrap our list of features.
                             *//*
                                    SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, features);
                                    featureStore.setTransaction(transaction);
                                    try {
                                        featureStore.addFeatures(collection);
                                        transaction.commit();
                                    } catch (Exception problem) {
                                        problem.printStackTrace();
                                        transaction.rollback();
                                    } finally {
                                        transaction.close();
                                    }
                                    System.exit(0); // success!
                                } else {
                                    System.out.println(typeName + " does not support read/write access");
                                    System.exit(1);
                                }
                     */

                } catch (IOException e) {
                    e.printStackTrace();
                }
// ЧТО ЭТО?
                try {
                    MemoryDataStore mds3 = (MemoryDataStore) source.getDataStore();
                    FeatureSource fs3 = null;
                    try {
                        fs2 = mds3.getFeatureSource("test");
                        //mds3.getFeatureReader()
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }catch (Exception e)
                {
                    e.printStackTrace();
                }


                // REG_PO отбор регионов по полигону
                /*
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

                    Style polygonStyleRegion =  SLD.createPolygonStyle(Color.green, Color.orange, (float) 0.5);
                    Layer polygonLayerReg = new FeatureLayer(features, polygonStyleRegion);
                    //Style markerStyle = SLD.createPointStyle("Circle", Color.orange, Color.green, 0.9f, 10);
                    //Layer polygonLayerReg = new FeatureLayer(features, markerStyle);

                    ((JMapPane) this.getMapPane()).getMapContent().addLayer(polygonLayerReg);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                */


            } else {
                Extra.coordinateMPointList.add(new Coordinate(mapPos.getX(), mapPos.getY()));
            }
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
