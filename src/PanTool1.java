/**
 * Created by SerP on 28.02.2016.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import org.geotools.swing.JMapPane;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.locale.LocaleUtils;
import org.geotools.swing.tool.CursorTool;

public class PanTool1 extends CursorTool {
    public static final String TOOL_NAME = LocaleUtils.getValue("CursorTool", "Pan");
    public static final String TOOL_TIP = LocaleUtils.getValue("CursorTool", "PanTooltip");
    public static final String CURSOR_IMAGE = "/org/geotools/swing/icons/mActionPan.png";
    public static final Point CURSOR_HOTSPOT = new Point(15, 15);
    public static final String ICON_IMAGE = "/org/geotools/swing/icons/mActionPan.png";
    private Cursor cursor;
    private Point panePos;
    boolean panning;

    public PanTool1() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        ImageIcon imgIcon = new ImageIcon(this.getClass().getResource("/org/geotools/swing/icons/mActionPan.png"));
        this.cursor = tk.createCustomCursor(imgIcon.getImage(), CURSOR_HOTSPOT, TOOL_NAME);
        this.panning = false;
    }

    public void onMousePressed(MapMouseEvent ev) {
        this.panePos = ev.getPoint();
        this.panning = true;
    }

    public void onMouseDragged(MapMouseEvent ev) {
        if(this.panning) {
            ((JMapPane)this.getMapPane()).setPaintDelay(60);
            Point pos = ev.getPoint();
            if(!pos.equals(this.panePos)) {
                ((JMapPane)this.getMapPane()).moveImage(pos.x - this.panePos.x, pos.y - this.panePos.y);
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
