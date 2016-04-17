/**
 * Created by SerP on 28.02.2016.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import org.geotools.swing.MapPane;
import org.geotools.swing.action.MapAction;

import java.awt.event.ActionEvent;

public class PanActionPanel extends MapAction {
    JMapPanel Extra;

    public PanActionPanel(MapPane mapPane, JMapPanel frameExtra) {

        this(mapPane, false, frameExtra);
    }

    public PanActionPanel(MapPane mapPane, boolean showToolName, JMapPanel frameExtra) {
        String toolName = showToolName?PanTool1.TOOL_NAME:null;
        super.init(mapPane, toolName, PanTool1.TOOL_TIP, "/org/geotools/swing/icons/mActionPan.png");
        this.Extra = frameExtra;
    }

    public void actionPerformed(ActionEvent ev) {
        this.getMapPane().setCursorTool(new PanToolPanel(Extra));
    }
}
