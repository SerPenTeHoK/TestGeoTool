/**
 * Created by SerP on 28.02.2016.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import java.awt.event.ActionEvent;
import org.geotools.swing.MapPane;
import org.geotools.swing.action.MapAction;
import org.geotools.swing.tool.PanTool;

public class PanAction1 extends MapAction {
    JMapFrameExtra Extra;

    public PanAction1(MapPane mapPane, JMapFrameExtra frameExtra) {

        this(mapPane, false, frameExtra);
    }

    public PanAction1(MapPane mapPane, boolean showToolName, JMapFrameExtra frameExtra) {
        String toolName = showToolName?PanTool1.TOOL_NAME:null;
        super.init(mapPane, toolName, PanTool1.TOOL_TIP, "/org/geotools/swing/icons/mActionPan.png");
        this.Extra = frameExtra;
    }

    public void actionPerformed(ActionEvent ev) {
        this.getMapPane().setCursorTool(new PanTool1(Extra));
    }
}
