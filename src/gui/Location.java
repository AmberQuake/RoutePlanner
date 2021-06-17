package gui;

import graph.MathHelp;
import graph.Vertex;
import javafx.animation.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Constructs a "Location" given a vertex: A titled circle
 * of the vertex and lines for each connection.
 * <b>To preserve z-order, use {@link Location#getChildren()} instead of this object.</b>
 */
public class Location extends Group {
    public enum State{
        START,
        GOAL,
        NONE
    };
    public final Vertex vertex;
    private final Circle circle;
    private final Text text;
    private final Spot spot;
    private final Group lines;
    private final Circle marker;
    private TranslateTransition markerTransition;

    public static final double RADIUS = 40;
    public static final double EDGE_RADIUS = 37;
    public static final double EDGE_X_OFFSET = 12;
    public static final double EDGE_Y_OFFSET = Math.sqrt((RADIUS*RADIUS)-(EDGE_X_OFFSET*EDGE_X_OFFSET));
    public static final double EDGE_RADIAN_OFFSET = -((Math.PI/2)-Math.acos(EDGE_X_OFFSET / RADIUS));

    public Location(Vertex vertex){
        double x = vertex.x;
        double y = vertex.y;

        circle = new Circle(x,y,RADIUS);
        this.vertex = vertex;
        circle.setFill(Color.CYAN);
        circle.setStroke(Color.BLUE);
        circle.setStrokeWidth(4);
        circle.setViewOrder(3);

        text = new Text(vertex.name);
        text.setTextAlignment(TextAlignment.CENTER);
        text.setFont(Font.font("calibri",20));
        text.setX(x - text.getLayoutBounds().getWidth()/2);
        text.setY(y + text.getLayoutBounds().getHeight()/2);
        text.setViewOrder(2);

        spot = new Spot(this, circle, text);

        lines = new Group();
        // create a line for each connection, color based on relative cost
        for(Vertex other : vertex.connections.keySet()){
            lines.getChildren().add(new Edge(vertex, other));
        }

        marker = new Circle(x,y,10);
        marker.setFill(Color.BLUE);
        marker.setViewOrder(6);
        marker.setVisible(false);

        // construct the finished group
        //getChildren().add(marker);
        getChildren().add(lines);
        getChildren().add(spot);
    }

    public static Location BLANK(){
        return new Location(new Vertex("",0,0));
    }

    public void defineClickEvent(Consumer<Location> consumer){
        text.setOnMouseClicked(event -> consumer.accept(this));
        circle.setOnMouseClicked(event -> consumer.accept(this));
    }

    public void setState(State state){
        if(state == State.GOAL){
            circle.setFill(Color.HOTPINK);
            circle.setStroke(Color.PURPLE);
        }
        else if(state == State.START){
            circle.setFill(Color.LAWNGREEN);
            circle.setStroke(Color.FORESTGREEN);
        }
        else{
            circle.setFill(Color.CYAN);
            circle.setStroke(Color.BLUE);
        }
        marker.setVisible(false);
        if(markerTransition != null) markerTransition.stop();
    }
    public void setOnPath(Location to, boolean start){
        // color path nodes yellow
        if(!start){
            circle.setFill(Color.YELLOW);
            circle.setStroke(Color.ORANGE);
        }
        // offset found from edge to other location
        double fromX = 0;
        double fromY = 0;
        double toX = 0;
        double toY = 0;
        // color all edges(only one) that point to the path target
        for(Node node : lines.getChildren().filtered(node -> node instanceof Edge)){
            Edge edge = ((Edge)node);
            if(edge.to.equals(to.vertex)) {
                edge.setStroke(Color.GREEN);
                fromX = edge.fromX;
                fromY = edge.fromY;
                toX = edge.toX;
                toY = edge.toY;
            }
        }

        // place our marker on the edge and translate across it
        double offsetx = Edge.offsetx(vertex.x,vertex.y,to.vertex.x,to.vertex.y);
        double offsety = Edge.offsety(vertex.x,vertex.y,to.vertex.x,to.vertex.y);
        //double angle = Math.toDegrees(Edge.angle(vertex.x,vertex.y,to.vertex.x,to.vertex.y));

        // vary speed by how trafficked the edge is
        double trafficRatio = Edge.trafficRatio(vertex, to.vertex);
        //System.out.println("X: "+vertex.x+" offset: "+offsetx);
        //System.out.println("-- X: ");
        marker.setTranslateX(fromX-vertex.x);
        //System.out.println("Y: "+vertex.y+" offset: "+offsety);
        marker.setTranslateY(fromY-vertex.y);
        /*
        KeyFrame xFrame = new KeyFrame(
                Duration.millis(1000),
                new KeyValue(marker.translateXProperty(),(to.vertex.x+offsetx)-(vertex.x+offsetx))
        );
        KeyFrame yFrame = new KeyFrame(
                Duration.millis(1000),
                new KeyValue(marker.translateYProperty(),(to.vertex.y+offsety)-(vertex.y+offsety))
        );
         */
        markerTransition = new TranslateTransition();
        markerTransition.setNode(marker);
        //markerTransition.setToX((to.vertex.x+offsetx)-(vertex.x));
        //markerTransition.setToY((to.vertex.y+offsety)-(vertex.y));
        markerTransition.setToX(toX-vertex.x);
        markerTransition.setToY(toY-vertex.y);
        markerTransition.setDuration(Duration.millis(3*vertex.connections.get(to.vertex)));
        markerTransition.setInterpolator(Interpolator.SPLINE(0.5,0.1,0.5,0.9));
        markerTransition.setCycleCount(Animation.INDEFINITE);
        markerTransition.play();

        //System.out.println("Visible");
        marker.setVisible(true);
        marker.setManaged(false);
    }

    public Circle getMarker(){
        return marker;
    }
    public Group getLines(){
        return lines;
    }
    public Group getSpot(){
        return spot;
    }
    public void updateTraffic(){
        for(Node node : lines.getChildren().filtered(node -> node instanceof Edge)){
            ((Edge)node).updateColor();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(vertex, location.vertex);
    }
}

class Spot extends Group{
    public final Location from;
    public Spot(Location from, Circle circle, Text text){
        super(circle, text);
        this.from = from;
    }
}

class Edge extends Line{
    public final Vertex from;
    public final Vertex to;
    // actual drawn coordinates
    public final double fromX;
    public final double fromY;
    public final double toX;
    public final double toY;

    public Edge(Vertex from, Vertex to){
        // store the vertices so we can update color later
        this.from = from;
        this.to = to;
        // directed edges are always to the right when facing the to vertex
        double angle = angle(from.x,from.y,to.x,to.y);
        double startAngle = angle+Location.EDGE_RADIAN_OFFSET;
        double endAngle = (Math.PI+angle)-Location.EDGE_RADIAN_OFFSET;
        double startoffx = MathHelp.xFromPolar(Location.EDGE_RADIUS, startAngle);
        double startoffy = -MathHelp.yFromPolar(Location.EDGE_RADIUS, startAngle);
        fromX = from.x + startoffx;
        fromY = from.y + startoffy;
        setStartX(fromX);
        setStartY(fromY);
        double endoffx = MathHelp.xFromPolar(Location.EDGE_RADIUS, endAngle);
        double endoffy = -MathHelp.yFromPolar(Location.EDGE_RADIUS, endAngle);
        toX = to.x + endoffx;
        toY = to.y + endoffy;
        setEndX(toX);
        setEndY(toY);
        updateColor();
        setStrokeWidth(5);
        setViewOrder(5);
    }

    public static double angle(double x1,double y1,double x2,double y2){
        return Math.atan2(y1-y2,x2-x1);
    }
    public static double offsetx(double x1,double y1,double x2,double y2){
        double angle = angle(x1,y1,x2,y2);
        return Location.EDGE_X_OFFSET*Math.sin(angle)+Location.EDGE_Y_OFFSET*Math.cos(angle);
    }
    public static double offsety(double x1,double y1,double x2,double y2){
        double angle = angle(x1,y1,x2,y2);
        return Location.EDGE_X_OFFSET*Math.cos(angle)-Location.EDGE_Y_OFFSET*Math.sin(angle);
    }

    /**
     * @return Ratio of trafficked distance versus actual distance
     */
    public static double trafficRatio(Vertex from, Vertex to){
        double expectedDist = from.distTo(to);
        double actualDist = from.connections.get(to);
        return actualDist / expectedDist;
    }

    public void updateColor(){
        // trafficked distance versus actual distance
        double trafficRatio = trafficRatio(from,to);
        trafficRatio = (trafficRatio-1) / (3 - 1); // convert to percentage from 1.0 to 3.0
        trafficRatio = MathHelp.clamp(trafficRatio, 0, 1);
        setStroke(Color.color(trafficRatio,0,0.25));
    }
}
