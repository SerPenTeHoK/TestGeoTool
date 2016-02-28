/**
 * Created by SerP on 28.02.2016.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.map.event.MapBoundsEvent;
import org.geotools.map.event.MapBoundsListener;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.renderer.lite.LabelCache;
import org.geotools.swing.DefaultRenderingExecutor;
import org.geotools.swing.MapPane;
import org.geotools.swing.MouseDragBox;
import org.geotools.swing.RenderingExecutor;
import org.geotools.swing.RenderingExecutorEvent;
import org.geotools.swing.RenderingExecutorListener;
import org.geotools.swing.event.DefaultMapMouseEventDispatcher;
import org.geotools.swing.event.MapMouseEventDispatcher;
import org.geotools.swing.event.MapMouseListener;
import org.geotools.swing.event.MapPaneEvent;
import org.geotools.swing.event.MapPaneKeyHandler;
import org.geotools.swing.event.MapPaneListener;
import org.geotools.swing.event.MapPaneEvent.Type;
import org.geotools.swing.tool.CursorTool;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public abstract class AbstractMapPane1 extends JPanel implements MapPane, RenderingExecutorListener, MapLayerListListener, MapBoundsListener {
    public static final int DEFAULT_PAINT_DELAY = 500;
    public static final Color DEFAULT_BACKGROUND_COLOR;
    protected final ScheduledExecutorService paneTaskExecutor;
    protected Future<?> resizedFuture;
    protected int paintDelay;
    protected final AtomicBoolean acceptRepaintRequests;
    protected final AtomicBoolean baseImageMoved;
    protected Future<?> imageMovedFuture;
    protected final Point imageOrigin;
    protected final Lock drawingLock;
    protected final ReadWriteLock paramsLock;
    protected final Set<MapPaneListener> listeners = new HashSet();
    protected final MouseDragBox dragBox;
    protected ReferencedEnvelope pendingDisplayArea;
    protected ReferencedEnvelope fullExtent;
    protected MapContent mapContent;
    protected RenderingExecutor renderingExecutor;
    protected KeyListener keyHandler;
    protected MapMouseEventDispatcher mouseEventDispatcher;
    protected LabelCache labelCache;
    protected AtomicBoolean clearLabelCache;
    protected CursorTool currentCursorTool;

    public AbstractMapPane1(MapContent content, RenderingExecutor executor) {
        this.setBackground(DEFAULT_BACKGROUND_COLOR);
        this.setFocusable(true);
        this.drawingLock = new ReentrantLock();
        this.paramsLock = new ReentrantReadWriteLock();
        this.paneTaskExecutor = Executors.newSingleThreadScheduledExecutor();
        this.paintDelay = 500;
        this.acceptRepaintRequests = new AtomicBoolean(true);
        this.clearLabelCache = new AtomicBoolean(true);
        this.baseImageMoved = new AtomicBoolean();
        this.imageOrigin = new Point(0, 0);
        this.dragBox = new MouseDragBox(this);
        this.mouseEventDispatcher = new DefaultMapMouseEventDispatcher(this);
        this.addMouseListener(this.dragBox);
        this.addMouseMotionListener(this.dragBox);
        this.addMouseListener(this.mouseEventDispatcher);
        this.addMouseMotionListener(this.mouseEventDispatcher);
        this.addMouseWheelListener(this.mouseEventDispatcher);
        this.addMouseListener(new MouseInputAdapter() {
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                if(AbstractMapPane1.this.currentCursorTool != null) {
                    AbstractMapPane1.this.setCursor(AbstractMapPane1.this.currentCursorTool.getCursor());
                }

            }
        });
        this.keyHandler = new MapPaneKeyHandler(this);
        this.addKeyListener(this.keyHandler);
        this.addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent he) {
                if((he.getChangeFlags() & 4L) != 0L && AbstractMapPane1.this.isShowing()) {
                    AbstractMapPane1.this.onShownOrResized();
                }

            }
        });
        this.addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
            public void ancestorResized(HierarchyEvent he) {
                if(AbstractMapPane1.this.isShowing()) {
                    AbstractMapPane1.this.onShownOrResized();
                }

            }
        });
        this.doSetMapContent(content);
        this.doSetRenderingExecutor(executor);
    }

    protected abstract void drawLayers(boolean var1);

    public RenderingExecutor getRenderingExecutor() {
        if(this.renderingExecutor == null) {
            this.doSetRenderingExecutor(new DefaultRenderingExecutor());
        }

        return this.renderingExecutor;
    }

    public MapMouseEventDispatcher getMouseEventDispatcher() {
        return this.mouseEventDispatcher;
    }

    public void setMouseEventDispatcher(MapMouseEventDispatcher dispatcher) {
        if(this.mouseEventDispatcher != null) {
            this.mouseEventDispatcher.removeAllListeners();
        }

        this.mouseEventDispatcher = dispatcher;
    }

    public void setRenderingExecutor(RenderingExecutor executor) {
        this.doSetRenderingExecutor(executor);
    }

    private void doSetRenderingExecutor(RenderingExecutor newExecutor) {
        if(this.renderingExecutor != null) {
            this.renderingExecutor.shutdown();
        }

        this.renderingExecutor = newExecutor;
    }

    public KeyListener getKeyHandler() {
        return this.keyHandler;
    }

    public void setKeyHandler(KeyListener controller) {
        if(this.keyHandler != null) {
            this.removeKeyListener(this.keyHandler);
        }

        if(controller != null) {
            this.addKeyListener(controller);
        }

        this.keyHandler = controller;
    }

    public long getPaintDelay() {
        this.paramsLock.readLock().lock();

        long var1;
        try {
            var1 = (long)this.paintDelay;
        } finally {
            this.paramsLock.readLock().unlock();
        }

        return var1;
    }

    public void setPaintDelay(int delay) {
        this.paramsLock.writeLock().lock();

        try {
            if(delay < 0) {
                this.paintDelay = 500;
            } else {
                this.paintDelay = delay;
            }
        } finally {
            this.paramsLock.writeLock().unlock();
        }

    }

    public void setIgnoreRepaint(boolean ignoreRepaint) {
        this.drawingLock.lock();

        try {
            super.setIgnoreRepaint(ignoreRepaint);
            this.acceptRepaintRequests.set(!ignoreRepaint);
        } finally {
            this.drawingLock.unlock();
        }

    }

    public boolean isAcceptingRepaints() {
        return this.acceptRepaintRequests.get();
    }

    protected void onShownOrResized() {
        if(this.resizedFuture != null && !this.resizedFuture.isDone()) {
            this.resizedFuture.cancel(true);
        }

        this.resizedFuture = this.paneTaskExecutor.schedule(new Runnable() {
            public void run() {
                AbstractMapPane1.this.setForNewSize();
                AbstractMapPane1.this.repaint();
            }
        }, (long)this.paintDelay, TimeUnit.MILLISECONDS);
    }

    protected void setForNewSize() {
        this.drawingLock.lock();

        try {
            if(this.mapContent == null) {
                return;
            }

            if(!this.mapContent.getViewport().getScreenArea().equals(this.getVisibleRect())) {
                this.mapContent.getViewport().setScreenArea(this.getVisibleRect());
                if(this.pendingDisplayArea != null) {
                    this.doSetDisplayArea(this.pendingDisplayArea);
                    this.pendingDisplayArea = null;
                } else if(this.mapContent.getViewport().getBounds().isEmpty()) {
                    this.setFullExtent();
                    this.doSetDisplayArea(this.fullExtent);
                }

                this.publishEvent(new MapPaneEvent(this, Type.DISPLAY_AREA_CHANGED, this.getDisplayArea()));
                this.acceptRepaintRequests.set(true);
                this.drawLayers(true);
                return;
            }
        } finally {
            this.drawingLock.unlock();
        }

    }

    public void moveImage(int dx, int dy) {
        this.drawingLock.lock();

        try {
            if(this.isShowing() && !this.getVisibleRect().isEmpty()) {
                this.imageOrigin.translate(dx, dy);
                this.baseImageMoved.set(true);
                this.repaint();
                this.onImageMoved();
            }
        } finally {
            this.drawingLock.unlock();
        }

    }

    protected void onImageMoved() {
        if(this.imageMovedFuture != null && !this.imageMovedFuture.isDone()) {
            this.imageMovedFuture.cancel(true);
        }

        this.imageMovedFuture = this.paneTaskExecutor.schedule(new Runnable() {
            public void run() {
                AbstractMapPane1.this.afterImageMoved();
                AbstractMapPane1.this.clearLabelCache.set(true);
                AbstractMapPane1.this.drawLayers(false);
                AbstractMapPane1.this.repaint();
            }
        }, (long)this.paintDelay, TimeUnit.MILLISECONDS);
    }

    protected void afterImageMoved() {
        this.paramsLock.writeLock().lock();

        try {
            int dx = this.imageOrigin.x;
            int dy = this.imageOrigin.y;
            DirectPosition2D newPos = new DirectPosition2D((double)dx, (double)dy);
            this.mapContent.getViewport().getScreenToWorld().transform(newPos, newPos);
            ReferencedEnvelope env = new ReferencedEnvelope(this.mapContent.getViewport().getBounds());
            env.translate(env.getMinimum(0) - newPos.x, env.getMaximum(1) - newPos.y);
            this.doSetDisplayArea(env);
            this.imageOrigin.setLocation(0, 0);
            this.baseImageMoved.set(false);
        } finally {
            this.paramsLock.writeLock().unlock();
        }

    }

    public MapContent getMapContent() {
        this.paramsLock.readLock().lock();

        MapContent var1;
        try {
            var1 = this.mapContent;
        } finally {
            this.paramsLock.readLock().unlock();
        }

        return var1;
    }

    public void setMapContent(MapContent content) {
        this.paramsLock.writeLock().lock();

        try {
            this.doSetMapContent(content);
        } finally {
            this.paramsLock.writeLock().unlock();
        }

    }

    private void doSetMapContent(MapContent newMapContent) {
        if(this.mapContent != newMapContent) {
            if(this.mapContent != null) {
                this.mapContent.removeMapLayerListListener(this);
                Iterator event = this.mapContent.layers().iterator();

                while(event.hasNext()) {
                    Layer rect = (Layer)event.next();
                    if(rect instanceof ComponentListener) {
                        this.removeComponentListener((ComponentListener)rect);
                    }
                }
            }

            this.mapContent = newMapContent;
            if(this.mapContent != null) {
                MapViewport event1 = this.mapContent.getViewport();
                event1.setMatchingAspectRatio(true);
                Rectangle rect1 = this.getVisibleRect();
                if(!rect1.isEmpty()) {
                    event1.setScreenArea(rect1);
                }

                this.mapContent.addMapLayerListListener(this);
                this.mapContent.addMapBoundsListener(this);
                if(!this.mapContent.layers().isEmpty()) {
                    Iterator i$ = this.mapContent.layers().iterator();

                    while(i$.hasNext()) {
                        Layer layer = (Layer)i$.next();
                        layer.setSelected(true);
                        if(layer instanceof ComponentListener) {
                            this.addComponentListener((ComponentListener)layer);
                        }
                    }

                    this.setFullExtent();
                    this.doSetDisplayArea(this.mapContent.getViewport().getBounds());
                }
            }

            MapPaneEvent event2 = new MapPaneEvent(this, Type.NEW_MAPCONTENT, this.mapContent);
            this.publishEvent(event2);
            this.drawLayers(false);
        }

    }

    public ReferencedEnvelope getDisplayArea() {
        this.paramsLock.readLock().lock();

        ReferencedEnvelope var1;
        try {
            if(this.mapContent != null) {
                var1 = this.mapContent.getViewport().getBounds();
                return var1;
            }

            if(this.pendingDisplayArea == null) {
                var1 = new ReferencedEnvelope();
                return var1;
            }

            var1 = new ReferencedEnvelope(this.pendingDisplayArea);
        } finally {
            this.paramsLock.readLock().unlock();
        }

        return var1;
    }

    public void setDisplayArea(Envelope envelope) {
        this.paramsLock.writeLock().lock();

        try {
            if(envelope == null) {
                throw new IllegalArgumentException("envelope must not be null");
            }

            this.doSetDisplayArea(envelope);
            if(this.mapContent != null) {
                this.clearLabelCache.set(true);
                this.drawLayers(false);
            }
        } finally {
            this.paramsLock.writeLock().unlock();
        }

    }

    protected void doSetDisplayArea(Envelope envelope) {
        if(this.mapContent != null) {
            CoordinateReferenceSystem crs = envelope.getCoordinateReferenceSystem();
            if(crs == null) {
                crs = this.mapContent.getCoordinateReferenceSystem();
            }

            ReferencedEnvelope refEnv = new ReferencedEnvelope(envelope.getMinimum(0), envelope.getMaximum(0), envelope.getMinimum(1), envelope.getMaximum(1), crs);
            this.mapContent.getViewport().setBounds(refEnv);
        } else {
            this.pendingDisplayArea = new ReferencedEnvelope(envelope);
        }

        this.publishEvent(new MapPaneEvent(this, Type.DISPLAY_AREA_CHANGED, this.getDisplayArea()));
    }

    public void reset() {
        this.paramsLock.writeLock().lock();

        try {
            if(this.fullExtent != null) {
                this.setDisplayArea(this.fullExtent);
            }
        } finally {
            this.paramsLock.writeLock().unlock();
        }

    }

    public AffineTransform getScreenToWorldTransform() {
        this.paramsLock.readLock().lock();

        AffineTransform var1;
        try {
            if(this.mapContent == null) {
                var1 = null;
                return var1;
            }

            var1 = this.mapContent.getViewport().getScreenToWorld();
        } finally {
            this.paramsLock.readLock().unlock();
        }

        return var1;
    }

    public AffineTransform getWorldToScreenTransform() {
        this.paramsLock.readLock().lock();

        AffineTransform var1;
        try {
            if(this.mapContent == null) {
                var1 = null;
                return var1;
            }

            var1 = this.mapContent.getViewport().getWorldToScreen();
        } finally {
            this.paramsLock.readLock().unlock();
        }

        return var1;
    }

    public void addMapPaneListener(MapPaneListener listener) {
        if(listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        } else {
            this.listeners.add(listener);
        }
    }

    public void removeMapPaneListener(MapPaneListener listener) {
        if(listener != null) {
            this.listeners.remove(listener);
        }

    }

    public void addMouseListener(MapMouseListener listener) {
        if(listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        } else {
            this.mouseEventDispatcher.addMouseListener(listener);
        }
    }

    public void removeMouseListener(MapMouseListener listener) {
        if(listener != null) {
            this.mouseEventDispatcher.removeMouseListener(listener);
        }

    }

    public CursorTool getCursorTool() {
        return this.currentCursorTool;
    }

    public void setCursorTool(CursorTool tool) {
        this.paramsLock.writeLock().lock();

        try {
            if(this.currentCursorTool != null) {
                this.mouseEventDispatcher.removeMouseListener(this.currentCursorTool);
            }

            this.currentCursorTool = tool;
            if(this.currentCursorTool == null) {
                this.setCursor(Cursor.getDefaultCursor());
                this.dragBox.setEnabled(false);
            } else {
                this.setCursor(this.currentCursorTool.getCursor());
                this.dragBox.setEnabled(this.currentCursorTool.drawDragBox());
                this.currentCursorTool.setMapPane(this);
                this.mouseEventDispatcher.addMouseListener(this.currentCursorTool);
            }
        } finally {
            this.paramsLock.writeLock().unlock();
        }

    }

    public void layerAdded(MapLayerListEvent event) {
        this.paramsLock.writeLock().lock();

        try {
            Layer layer = event.getElement();
            if(layer instanceof ComponentListener) {
                this.addComponentListener((ComponentListener)layer);
            }

            this.setFullExtent();
            MapViewport viewport = this.mapContent.getViewport();
            if(viewport.getBounds().isEmpty()) {
                viewport.setBounds(this.fullExtent);
            }
        } finally {
            this.paramsLock.writeLock().unlock();
        }

        this.drawLayers(false);
        this.repaint();
    }

    public void layerRemoved(MapLayerListEvent event) {
        this.paramsLock.writeLock().lock();

        try {
            Layer layer = event.getElement();
            if(layer instanceof ComponentListener) {
                this.removeComponentListener((ComponentListener)layer);
            }

            if(this.mapContent.layers().isEmpty()) {
                this.fullExtent = null;
            } else {
                this.setFullExtent();
            }
        } finally {
            this.paramsLock.writeLock().unlock();
        }

        this.drawLayers(false);
        this.repaint();
    }

    public void layerChanged(MapLayerListEvent event) {
        this.paramsLock.writeLock().lock();

        try {
            int reason = event.getMapLayerEvent().getReason();
            if(reason == 3) {
                this.setFullExtent();
            }

            if(reason != 6) {
                this.clearLabelCache.set(true);
                this.drawLayers(false);
            }
        } finally {
            this.paramsLock.writeLock().unlock();
        }

        this.repaint();
    }

    public void layerMoved(MapLayerListEvent event) {
        this.drawLayers(false);
        this.repaint();
    }

    public void layerPreDispose(MapLayerListEvent event) {
        this.getRenderingExecutor().cancelAll();
    }

    public void mapBoundsChanged(MapBoundsEvent event) {
        this.paramsLock.writeLock().lock();

        try {
            int type = event.getType();
            if((type & 2) != 0) {
                this.setFullExtent();
                this.reset();
            }
        } finally {
            this.paramsLock.writeLock().unlock();
        }

    }

    protected void publishEvent(MapPaneEvent ev) {
        Iterator i$ = this.listeners.iterator();
/*
        while(i$.hasNext()) {
            MapPaneListener listener = (MapPaneListener)i$.next();
            switch(AbstractMapPane1.SyntheticClass_1.$SwitchMap$org$geotools$swing$event$MapPaneEvent$Type[ev.getType().ordinal()]) {
                case 1:
                    listener.onNewMapContent(ev);
                    break;
                case 2:
                    listener.onDisplayAreaChanged(ev);
                    break;
                case 3:
                    listener.onRenderingStarted(ev);
                    break;
                case 4:
                    listener.onRenderingStopped(ev);
            }
        }
*/
    }

    protected boolean setFullExtent() {
        if(this.mapContent != null && !this.mapContent.layers().isEmpty()) {
            try {
                this.fullExtent = this.mapContent.getMaxBounds();
                if(this.fullExtent == null) {
                    this.fullExtent = new ReferencedEnvelope(-1.0D, 1.0D, -1.0D, 1.0D, this.mapContent.getCoordinateReferenceSystem());
                } else {
                    double ex = this.fullExtent.getWidth();
                    double h = this.fullExtent.getHeight();
                    double x = this.fullExtent.getMinimum(0);
                    double y = this.fullExtent.getMinimum(1);
                    double xmin = x;
                    double xmax = x + ex;
                    if(ex <= 0.0D) {
                        xmin = x - 1.0D;
                        xmax = x + 1.0D;
                    }

                    double ymin = y;
                    double ymax = y + h;
                    if(h <= 0.0D) {
                        ymin = y - 1.0D;
                        ymax = y + 1.0D;
                    }

                    this.fullExtent = new ReferencedEnvelope(xmin, xmax, ymin, ymax, this.mapContent.getCoordinateReferenceSystem());
                }
            } catch (Exception var17) {
                throw new IllegalStateException(var17);
            }
        } else {
            this.fullExtent = null;
        }

        return this.fullExtent != null;
    }

    public void onRenderingStarted(RenderingExecutorEvent ev) {
        this.publishEvent(new MapPaneEvent(this, Type.RENDERING_STARTED));
    }

    public void onRenderingCompleted(RenderingExecutorEvent event) {
        if(this.clearLabelCache.get()) {
            this.labelCache.clear();
        }

        this.clearLabelCache.set(false);
        this.repaint();
        this.publishEvent(new MapPaneEvent(this, Type.RENDERING_STOPPED));
    }

    public void onRenderingFailed(RenderingExecutorEvent ev) {
        this.publishEvent(new MapPaneEvent(this, Type.RENDERING_STOPPED));
    }

    static {
        DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    }
}
