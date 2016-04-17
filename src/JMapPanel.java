import com.vividsolutions.jts.geom.Coordinate;
import net.miginfocom.swing.MigLayout;
import org.geotools.data.DataStore;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.map.MapContent;
import org.geotools.swing.JMapPane;
import org.geotools.swing.MapLayerTable;
import org.geotools.swing.action.*;
import org.geotools.swing.control.JMapStatusBar;
import org.geotools.swing.event.MapMouseEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Created by SerP on 17.04.2016.
 */
import java.awt.*;
import java.awt.Dimension;
import java.awt.Polygon;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import javax.servlet.ServletException;
import javax.swing.*;

import com.vividsolutions.jts.geom.*;
import net.miginfocom.swing.MigLayout;
import org.geotools.data.*;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.*;
import org.geotools.swing.JMapPane;
import org.geotools.swing.MapLayerTable;
import org.geotools.swing.action.InfoAction;
import org.geotools.swing.action.NoToolAction;
import org.geotools.swing.action.ResetAction;
import org.geotools.swing.action.ZoomInAction;
import org.geotools.swing.action.ZoomOutAction;
import org.geotools.swing.control.JMapStatusBar;
import org.geotools.swing.event.MapMouseEvent;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

import java.awt.event.MouseEvent;
import java.util.List;


public class JMapPanel extends JPanel {
    public static final String TOOLBAR_INFO_BUTTON_NAME = "ToolbarInfoButton";
    public static final String TOOLBAR_PAN_BUTTON_NAME = "ToolbarPanButton";
    public static final String TOOLBAR_POINTER_BUTTON_NAME = "ToolbarPointerButton";
    public static final String TOOLBAR_RESET_BUTTON_NAME = "ToolbarResetButton";
    public static final String TOOLBAR_ZOOMIN_BUTTON_NAME = "ToolbarZoomInButton";
    public static final String TOOLBAR_ZOOMOUT_BUTTON_NAME = "ToolbarZoomOutButton";
    public static DataStore data;
    public List<Coordinate> coordinateMPointList = new ArrayList<>();
    public Polygon PoligonPoint = new Polygon();
    public List<Coordinate> mpointList = new ArrayList<>();
    private boolean showToolBar;
    private Set<Tool> toolSet;
    private JMapPane mapPane;
    private MapLayerTable mapLayerTable;
    private JToolBar toolBar;
    private boolean showStatusBar;
    private boolean showLayerTable;
    private boolean uiSet;

    public JMapPanel() {
        this((MapContent) null);
    }

    public JMapPanel(MapContent content) {
        super();
        //super(content == null ? "" : content.getTitle());
        //this.setDefaultCloseOperation(3);
        this.showLayerTable = false;
        this.showStatusBar = false;
        this.showToolBar = false;
        this.toolSet = EnumSet.noneOf(JMapPanel.Tool.class);
        this.mapPane = new JMapPane(content);
        this.mapPane.setBackground(Color.WHITE);
        this.mapPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        /*
        this.addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                JMapPanel.this.mapPane.requestFocusInWindow();
            }
        });
        */

        this.mapPane.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                JMapPanel.this.mapPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            }

            public void focusLost(FocusEvent e) {
                JMapPanel.this.mapPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            }
        });
        this.mapPane.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JMapPanel.this.mapPane.requestFocusInWindow();
            }

        });
    }


    public void setDataStore(DataStore dataIn) {
        data = dataIn;
    }

    public void showMap(final MapContent content) {

        JMapPanel frame = new JMapPanel(content);
        frame.enableStatusBar(true);
        frame.enableToolBar(true);
        frame.initComponents();
        frame.setSize(200, 200);
        frame.setVisible(true);
    }

    private void doShowMap(MapContent content) {
        /*
        JMapPanel frame = new JMapPanel(content);
        frame.enableStatusBar(true);
        frame.enableToolBar(true);
        frame.initComponents();
        frame.setSize(800, 600);
        frame.setVisible(true);
        */
    }

    public void enableToolBar(boolean enabled) {
        if (enabled) {
            this.toolSet = EnumSet.allOf(JMapPanel.Tool.class);
        } else {
            this.toolSet.clear();
        }

        this.showToolBar = enabled;
    }

    public void enableTool(JMapPanel.Tool... tool) {
        if (tool != null && tool.length != 0) {
            this.toolSet = EnumSet.copyOf(Arrays.asList(tool));
            this.showToolBar = true;
        } else {
            this.enableToolBar(false);
        }
    }

    public void enableStatusBar(boolean enabled) {
        this.showStatusBar = enabled;
    }

    public void enableLayerTable(boolean enabled) {
        this.showLayerTable = enabled;
    }

    public void setVisible(boolean state) {
        if (state && !this.uiSet) {
            this.initComponents();
        }

        super.setVisible(state);
    }

    public void initComponents() {
        if (!this.uiSet) {
            StringBuilder sb = new StringBuilder();
            if (!this.toolSet.isEmpty()) {
                sb.append("[]");
            }

            sb.append("[grow]");
            if (this.showStatusBar) {
                sb.append("[min!]");
            }

            JPanel panel = new JPanel(new MigLayout("wrap 1, insets 0", "[grow]", sb.toString()));
            if (this.showToolBar) {
                this.toolBar = new JToolBar();
                this.toolBar.setOrientation(0);
                this.toolBar.setFloatable(false);
                ButtonGroup cursorToolGrp = new ButtonGroup();
                JButton splitPane;
                if (this.toolSet.contains(JMapPanel.Tool.POINTER)) {
                    splitPane = new JButton(new NoToolAction(this.mapPane));
                    splitPane.setName("ToolbarPointerButton");
                    this.toolBar.add(splitPane);
                    cursorToolGrp.add(splitPane);
                }

                if (this.toolSet.contains(JMapPanel.Tool.ZOOM)) {
                    splitPane = new JButton(new ZoomInAction(this.mapPane));
                    splitPane.setName("ToolbarZoomInButton");
                    this.toolBar.add(splitPane);
                    cursorToolGrp.add(splitPane);
                    splitPane = new JButton(new ZoomOutAction(this.mapPane));
                    splitPane.setName("ToolbarZoomOutButton");
                    this.toolBar.add(splitPane);
                    cursorToolGrp.add(splitPane);
                    this.toolBar.addSeparator();
                }

                if (this.toolSet.contains(JMapPanel.Tool.PAN)) {
                    splitPane = new JButton(new PanActionPanel(this.mapPane, this));
                    //this.mapPane.getCursorTool().
                    splitPane.setName("ToolbarPanButton");
                    this.toolBar.add(splitPane);
                    cursorToolGrp.add(splitPane);
                    this.toolBar.addSeparator();
                }

                if (this.toolSet.contains(JMapPanel.Tool.INFO)) {
                    splitPane = new JButton(new InfoAction(this.mapPane));
                    splitPane.setName("ToolbarInfoButton");
                    this.toolBar.add(splitPane);
                    this.toolBar.addSeparator();
                }

                if (this.toolSet.contains(JMapPanel.Tool.RESET)) {
                    splitPane = new JButton(new ResetAction(this.mapPane));
                    splitPane.setName("ToolbarResetButton");
                    this.toolBar.add(splitPane);
                }

                panel.add(this.toolBar, "grow");
            }

            if (this.showLayerTable) {
                this.mapLayerTable = new MapLayerTable(this.mapPane);
                this.mapLayerTable.setPreferredSize(new Dimension(200, -1));
                JSplitPane splitPane1 = new JSplitPane(1, false, this.mapLayerTable, this.mapPane);
                panel.add(splitPane1, "grow");
            } else {
                panel.add(this.mapPane, "grow");
            }

            if (this.showStatusBar) {
                panel.add(JMapStatusBar.createDefaultStatusBar(this.mapPane), "grow");
            }

            this.add(panel);
            this.uiSet = true;

            this.mapPane.addMouseWheelListener(new MouseWheelListener() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    int mouserot = e.getWheelRotation();
                    MapMouseEvent mme = new MapMouseEvent(mapPane, e);
                    DirectPosition2D mapPos = mme.getWorldPos();
                    Rectangle paneArea = ((JComponent) mapPane).getVisibleRect();
                    double scale = mapPane.getWorldToScreenTransform().getScaleX();
                    double newScale = 0;
                    if (mouserot < 0) {
                        newScale = scale * 1.5D;
                    } else {
                        newScale = scale / 1.5D;
                    }
                    try {
                        DirectPosition2D corner = new DirectPosition2D(mapPos.getX() - 0.5D * paneArea.getWidth() / newScale, mapPos.getY() + 0.5D * paneArea.getHeight() / newScale);
                        Envelope2D newMapArea = new Envelope2D();
                        newMapArea.setFrameFromCenter(mapPos, corner);
                        mapPane.setDisplayArea(newMapArea);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });

        }
    }

    public MapContent getMapContent() {
        return this.mapPane.getMapContent();
    }

    public void setMapContent(MapContent content) {
        if (content == null) {
            throw new IllegalArgumentException("map content must not be null");
        } else {
            this.mapPane.setMapContent(content);
        }
    }

    public JMapPane getMapPane() {
        return this.mapPane;
    }

    public JToolBar getToolBar() {
        if (!this.uiSet) {
            this.initComponents();
        }
        return this.toolBar;
    }


    public static enum Tool {
        POINTER,
        INFO,
        PAN,
        RESET,
        ZOOM;

        private Tool() {
        }
    }
}

