package MapPanelComponent;

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
import java.awt.List;
import java.awt.event.*;
import java.util.*;

/**
 * Created by SerP on 24.04.2016.
 */
public class GeoServerMapPanel extends JPanel {
    public static final String TOOLBAR_INFO_BUTTON_NAME = "ToolbarInfoButton";
    public static final String TOOLBAR_PAN_BUTTON_NAME = "ToolbarPanButton";
    public static final String TOOLBAR_POINTER_BUTTON_NAME = "ToolbarPointerButton";
    public static final String TOOLBAR_RESET_BUTTON_NAME = "ToolbarResetButton";
    public static final String TOOLBAR_ZOOMIN_BUTTON_NAME = "ToolbarZoomInButton";
    public static final String TOOLBAR_ZOOMOUT_BUTTON_NAME = "ToolbarZoomOutButton";
    private boolean showToolBar;
    private Set<GeoServerMapPanel.Tool> toolSet;

    public static JMapPane getMapPane() {
        return mapPane;
    }

    public static JMapPane mapPane;
    private MapLayerTable mapLayerTable;
    private JToolBar toolBar;
    private boolean showStatusBar;
    private boolean showLayerTable;
    private boolean uiSet;

    public java.util.List<Coordinate> coordinateMPointList = new ArrayList<>();

    public static DataStore data;

    public Polygon PoligonPoint = new Polygon();
    public java.util.List<Coordinate> mpointList = new ArrayList<>();


    private JButton button5;
    private JButton button6;
    private JButton bSplit;

    public GeoServerMapPanel() {
        this((MapContent)null);
    }

    public GeoServerMapPanel(MapContent content) {
        this.showLayerTable = false;
        this.showStatusBar = false;
        this.showToolBar = false;
        this.toolSet = EnumSet.noneOf(GeoServerMapPanel.Tool.class);
        this.mapPane = new JMapPane(content);
        this.mapPane.setBackground(Color.WHITE);
        this.mapPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        this.setBackground(Color.black);
        this.setBorder(BorderFactory.createLineBorder(Color.YELLOW));

        /*
        button5 = new JButton("Супер5");
        this.mapPane.add(button5);


        button6 = new JButton("Супе6");
        this.add(button6);
        */
        this.mapPane.setSize(400, 400);

        this.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                GeoServerMapPanel.super.resize(600, 500);
                GeoServerMapPanel.getMapPane().resize(300, 300);
            }

            @Override
            public void componentMoved(ComponentEvent e) {

            }

            @Override
            public void componentShown(ComponentEvent e) {

            }

            @Override
            public void componentHidden(ComponentEvent e) {

            }
        });

        /*
        this.addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                GeoServerMapPanel.this.mapPane.requestFocusInWindow();
            }
        });
        */
        this.mapPane.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                GeoServerMapPanel.this.mapPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            }

            public void focusLost(FocusEvent e) {
                GeoServerMapPanel.this.mapPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            }
        });
        this.mapPane.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                GeoServerMapPanel.this.mapPane.requestFocusInWindow();
            }

        });
    }

    public void enableStatusBar(boolean enabled) {
        this.showStatusBar = enabled;
    }

    public void enableLayerTable(boolean enabled) {
        this.showLayerTable = enabled;
    }

    public void setVisible(boolean state) {
        if(state && !this.uiSet) {
            this.initComponents();
        }

        super.setVisible(state);
    }

    public void initComponents() {
        if(!this.uiSet) {
            StringBuilder sb = new StringBuilder();
            if(!this.toolSet.isEmpty()) {
                sb.append("[]");
            }

            sb.append("[grow]");
            if(this.showStatusBar) {
                sb.append("[min!]");
            }

            JPanel panel = new JPanel(new MigLayout("wrap 1, insets 0", "[grow]", sb.toString()));
            if(this.showToolBar) {
                this.toolBar = new JToolBar();
                this.toolBar.setOrientation(0);
                this.toolBar.setFloatable(false);
                ButtonGroup cursorToolGrp = new ButtonGroup();
                JButton splitPane;
                if(this.toolSet.contains(GeoServerMapPanel.Tool.POINTER)) {
                    splitPane = new JButton(new NoToolAction(this.mapPane));
                    splitPane.setName("ToolbarPointerButton");
                    this.toolBar.add(splitPane);
                    cursorToolGrp.add(splitPane);
                }

                if(this.toolSet.contains(GeoServerMapPanel.Tool.ZOOM)) {
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

                if(this.toolSet.contains(GeoServerMapPanel.Tool.PAN)) {
                    splitPane = new JButton(new PanActionMapPanel(this.mapPane, this ));
                    splitPane.setName("ToolbarPanButton");
                    this.toolBar.add(splitPane);
                    cursorToolGrp.add(splitPane);
                    this.toolBar.addSeparator();
                }

                if(this.toolSet.contains(GeoServerMapPanel.Tool.INFO)) {
                    splitPane = new JButton(new InfoAction(this.mapPane));
                    splitPane.setName("ToolbarInfoButton");
                    this.toolBar.add(splitPane);
                    this.toolBar.addSeparator();
                }

                if(this.toolSet.contains(GeoServerMapPanel.Tool.RESET)) {
                    splitPane = new JButton(new ResetAction(this.mapPane));
                    splitPane.setName("ToolbarResetButton");
                    this.toolBar.add(splitPane);
                }

                panel.add(this.toolBar, "grow");
                this.toolBar.repaint();
            }
            this.showLayerTable = true;
            if(this.showLayerTable) {
                this.mapLayerTable = new MapLayerTable(this.mapPane);
                this.mapLayerTable.setPreferredSize(new Dimension(200, -1));//-1));
                JSplitPane splitPane1 = new JSplitPane(1, false, this.mapLayerTable, this.mapPane);
                panel.add(splitPane1, "grow");
            } else {
                panel.add(this.mapPane, "grow");
            }

            this.showStatusBar = true;
            if(this.showStatusBar) {
                panel.add(JMapStatusBar.createDefaultStatusBar(this.mapPane), "grow");
            }
            panel.setSize(500, 500);

            this.add(panel);
            this.setSize(600, 600);
            this.uiSet = true;

            this.mapPane.addMouseWheelListener(new MouseWheelListener() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    int mouserot = e.getWheelRotation();
                    MapMouseEvent mme = new MapMouseEvent(mapPane, e);
                    DirectPosition2D mapPos = mme.getWorldPos();
                    Rectangle paneArea = ((JComponent)mapPane).getVisibleRect();
                    double scale = mapPane.getWorldToScreenTransform().getScaleX();
                    double newScale = 0;
                    if (mouserot < 0) {
                        newScale = scale * 1.5D;
                    } else {
                        newScale = scale / 1.5D;
                    }
                    try{
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

    public void enableToolBar(boolean enabled) {
        if(enabled) {
            this.toolSet = EnumSet.allOf(GeoServerMapPanel.Tool.class);
        } else {
            this.toolSet.clear();
        }

        this.showToolBar = enabled;
    }

    public void enableTool(GeoServerMapPanel.Tool... tool) {
        if(tool != null && tool.length != 0) {
            this.toolSet = EnumSet.copyOf(Arrays.asList(tool));
            this.showToolBar = true;
        } else {
            this.enableToolBar(false);
        }
    }

    public JToolBar getToolBar() {
        if(!this.uiSet) {
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

    public void showMap(final MapContent content) {


        /*
        if(SwingUtilities.isEventDispatchThread()) {
            doShowMap(content);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    GeoServerMapPanel.doShowMap(content);
                }
            });
        }
        */
    }

    private static void doShowMap(MapContent content) {
        /*
        GeoServerMapPanel frame = new GeoServerMapPanel(content);
        frame.enableStatusBar(true);
        frame.enableToolBar(true);
        frame.initComponents();
        frame.setSize(800, 600);
        frame.setVisible(true);
        */
    }

}
