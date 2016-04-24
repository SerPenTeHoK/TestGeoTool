package MapPanelComponent; /**
 * Created by SerP on 28.02.2016.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import java.awt.event.ActionEvent;
import org.geotools.swing.MapPane;
import org.geotools.swing.action.MapAction;

public class PanActionMapPanel extends MapAction {
    GeoServerMapPanel Extra;

    public PanActionMapPanel(MapPane mapPane, GeoServerMapPanel frameExtra) {

        this(mapPane, false, frameExtra);
    }

    public PanActionMapPanel(MapPane mapPane, boolean showToolName, GeoServerMapPanel frameExtra) {
        String toolName = showToolName? PanToolMapPanel.TOOL_NAME:null;
        super.init(mapPane, toolName, PanToolMapPanel.TOOL_TIP, "/org/geotools/swing/icons/mActionPan.png");
        this.Extra = frameExtra;
    }

    public void actionPerformed(ActionEvent ev) {
        this.getMapPane().setCursorTool(new PanToolMapPanel(Extra));
    }
}
