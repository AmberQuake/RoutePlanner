package gui;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class FocusPane extends ScrollPane {
    private double scaleValue = 0.7;
    private double zoomIntensity = 0.02;
    private Node target;
    private Node zoomNode;

    public FocusPane(Node target) {
        super();
        this.target = target;
        this.zoomNode = new Group(target);
        setContent(outerNode(zoomNode));

        setPannable(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.NEVER);
        setFitToHeight(true); //center
        setFitToWidth(true); //center

        updateScale();
    }

    private Node outerNode(Node node) {
        Node outerNode = centeredNode(node);
        outerNode.setOnScroll(e -> {
            e.consume();
            onScroll(e.getTextDeltaY(), new Point2D(e.getX(), e.getY()));
        });
        return outerNode;
    }

    private Node centeredNode(Node node) {
        VBox vBox = new VBox(node);
        vBox.setAlignment(Pos.CENTER);
        return vBox;
    }

    private void updateScale() {
        target.setScaleX(scaleValue);
        target.setScaleY(scaleValue);
    }

    private boolean onScroll(double wheelDelta, Point2D mousePoint) {
        double zoomFactor = Math.exp(wheelDelta * zoomIntensity);

        Bounds innerBounds = zoomNode.getLayoutBounds();
        Bounds viewportBounds = getViewportBounds();

        // calculate pixel offsets from [0, 1] range
        double boundsWidthDiff = innerBounds.getWidth() - viewportBounds.getWidth();
        double boundsHeightDiff = innerBounds.getHeight() - viewportBounds.getHeight();
        double valX = this.getHvalue() * (boundsWidthDiff);
        double valY = this.getVvalue() * (boundsHeightDiff);
        if(boundsWidthDiff<=0 && boundsHeightDiff <= 0 && wheelDelta < 0) return false; // max zoom out

        scaleValue = scaleValue * zoomFactor;
        updateScale();
        this.layout(); // refresh ScrollPane scroll positions & target bounds

        // convert target coordinates to zoomTarget coordinates
        Point2D posInZoomTarget = target.parentToLocal(zoomNode.parentToLocal(mousePoint));

        // calculate adjustment of scroll position (pixels)
        Point2D adjustment = target.getLocalToParentTransform().deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));

        // convert back to [0, 1] range
        // (too large/small values are automatically corrected by ScrollPane)
        Bounds updatedInnerBounds = zoomNode.getBoundsInLocal();
        this.setHvalue((valX + adjustment.getX()) / (updatedInnerBounds.getWidth() - viewportBounds.getWidth()));
        this.setVvalue((valY + adjustment.getY()) / (updatedInnerBounds.getHeight() - viewportBounds.getHeight()));
        return true;
    }

    public void zoomOut(){
        while(onScroll(-1, new Point2D(1,1))) {
            onScroll(-1, new Point2D(1,1));
        }
    }
    public void zoomIn(){
        while(!onScroll(-1, new Point2D(1,1))) {
            onScroll(2, new Point2D(1,1));
        }
    }
    public void fitZoom(){
        zoomIn();
        zoomOut();
    }
}
