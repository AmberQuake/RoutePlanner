package gui;

import graph.Vertex;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventTarget;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Stage;

import graph.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public class App extends Application{

    private Graph graph;

    // find "Location" graphical element by vertex instead of iterating all known locations
    private HashMap<Vertex, Location> vertexMap = new HashMap<>();
    // route planning locations
    private Location startLocation = Location.BLANK();
    private Location goalLocation = Location.BLANK();
    private List<Vertex> path = new ArrayList<>();
    private List<Location> drawnPath = new ArrayList<>();

    // user controls
    private int selNumVertices = 40;
    private int selAvgEdges = 3;
    private double selAvgDist = 300;
    private int selTrafficLevel = 1;
    private boolean selectingGoal = false;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        graph = Graph.generateRandomOrganic(selNumVertices, selAvgEdges, selAvgDist, selTrafficLevel);

        StackPane view = new StackPane(); // the "view" of our nodes and connections

        Group locations = new Group();
        Group connections = new Group(); // lines between connected locations
        Group pathMarkers = new Group(); // animated markers for the path

        createView(graph, locations, connections, pathMarkers);
        Group combined = new Group(pathMarkers,connections,locations);

        view.getChildren().addAll(combined);

        view.setPadding(new Insets(40));

        FocusPane focusView = new FocusPane(view); // view that can pan and zoom
        view.setOnMouseClicked(event ->{
            //System.out.println("Click -- X: "+event.getX()+", Y:"+event.getY());
        });

        ToolBar tools = new ToolBar(); // controls for interaction

        // user controls for graph generation
        tools.getItems().add(createSlider("# of vertices", 20, 1000, 40, val->selNumVertices=(int)val));
        tools.getItems().add(new Separator());
        tools.getItems().add(createSlider("Edge Concentration", 2, 10, 3, val->selAvgEdges=(int)val));
        tools.getItems().add(new Separator());
        tools.getItems().add(createSlider("Average Distance", 150, 1000, 300, val->selAvgDist=val));
        tools.getItems().add(new Separator());
        tools.getItems().add(createSlider("Traffic Level", 0, 4, 1, val->selTrafficLevel=(int)val));

        tools.getItems().add(new Separator());
        // Randomized graph generation based on above constraints
        Button generateButton = new Button("Generate New Graph");
        generateButton.setOnAction(event -> {
            //graph = Graph.generateExample();
            graph = Graph.generateRandomOrganic(selNumVertices, selAvgEdges, selAvgDist, selTrafficLevel);
            locations.getChildren().clear();
            connections.getChildren().clear();
            pathMarkers.getChildren().clear();
            startLocation = Location.BLANK();
            goalLocation = Location.BLANK();
            createView(graph, locations, connections, pathMarkers);
            focusView.fitZoom();
        });
        tools.getItems().add(generateButton);

        tools.getItems().add(new Separator());
        // Radio buttons for choosing start and goal locations for path
        RadioButton startRadio = new RadioButton("Select Start Location?");
        startRadio.setUserData("start");
        RadioButton goalRadio = new RadioButton("Select Goal Location?");
        goalRadio.setUserData("goal");
        ToggleGroup selectRadios = new ToggleGroup();
        startRadio.setToggleGroup(selectRadios);
        startRadio.fire();
        goalRadio.setToggleGroup(selectRadios);
        selectRadios.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            Toggle selected = selectRadios.getSelectedToggle();
            if(selected!=null){
                selectingGoal = selected.getUserData().toString().equals("goal");
            }
        });
        VBox radioBox = new VBox(startRadio,goalRadio);
        radioBox.setSpacing(10);
        tools.getItems().add(radioBox);


        VBox stack = new VBox(); // controls + view
        // add helpful text overlay
        Text help = new Text("-Use mouse to pan/zoom\n-Edit generation settings to vary graphs\n-Select start/goal node to plan the shortest route");
        help.setFont(Font.font("calibri", 15));
        help.setFill(Color.DARKGREEN);
        StackPane helpfulView = new StackPane(focusView,help);
        StackPane.setAlignment(help, Pos.TOP_LEFT);

        stack.getChildren().addAll(tools,helpfulView);
        VBox.setVgrow(focusView, Priority.ALWAYS);

        Scene scene = new Scene(stack, 960, 720);
        stage.setTitle("Route Planner");
        stage.setScene(scene);
        stage.show();

        focusView.fitZoom();
    }

    private void createView(Graph graph, final Group locations, final Group connections, final Group pathMarkers){
        vertexMap.clear();
        for(Vertex v : graph.getVertices()){
            // use our premade location-node constructor for each vertex
            Location loc = new Location(v);
            // store this association for drawing paths
            vertexMap.put(v,loc);
            // every time a location is clicked, evaluate if it should be set as start/goal
            Consumer<Location> clickHandler = location -> {
                if(!startLocation.equals(location) && !goalLocation.equals(location)){
                    if(selectingGoal){
                        goalLocation.setState(Location.State.NONE);
                        goalLocation = location;
                        location.setState(Location.State.GOAL);
                    }
                    else{
                        startLocation.setState(Location.State.NONE);
                        startLocation = location;
                        location.setState(Location.State.START);
                        //System.out.println("Start: "+startLocation.vertex.x+", "+startLocation.vertex.y);
                    }
                    drawPath(pathMarkers);
                }
            };
            loc.defineClickEvent(clickHandler);
            // separate the groups so markers < lines < spots
            //pathMarkers.getChildren().add();
            locations.getChildren().addAll(loc.getSpot());
            connections.getChildren().addAll(loc.getLines(),loc.getMarker());
        }
    }

    private void drawPath(final Group pathMarkers){
        // draw our path to the goal
        long startTime = System.currentTimeMillis();

        Vertex start = startLocation.vertex;
        Vertex goal = goalLocation.vertex;
        Location current = startLocation;
        //System.out.println("Start: "+start);
        //System.out.println("Goal: "+goal);

        path = graph.dijkstraPath(start, goal);
        if(path==null){
            System.out.println("Null path drawn...");
            return;
        }

        // reset old paths
        for(Location l : drawnPath){
            l.updateTraffic(); // undo edge color
            if(!l.equals(startLocation) && !l.equals(goalLocation)){
                l.setState(Location.State.NONE); // undo path color
            }
        }
        drawnPath.clear();

        // draw the path by associating vertices to locations
        for(int i=1;i<path.size();i++){
            Vertex target = path.get(i);
            Location targetLoc = vertexMap.get(target);
            if(current.vertex.connections.containsKey(target)){
                current.setOnPath(targetLoc, i==1); // colorize this location and connection to next
                drawnPath.add(current); // remember our drawn path
            }
            // retrieve next location from our unique vertex hash
            current = targetLoc;
            if(current == null){
                System.out.println("Failed! "+i);
                break;
            }
        }

        System.out.println("Path generation and display time: "+(System.currentTimeMillis()-startTime)+" ms");
    }

    private Node createSlider(String title, double min, double max, double defaultVal, DoubleConsumer consumer){
        Slider slider = new Slider(min,max,defaultVal);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.valueProperty().addListener((observable, oldValue, newValue) -> consumer.accept(slider.getValue()));
        Text text = new Text(title);
        VBox box = new VBox(text,slider);
        box.setAlignment(Pos.CENTER);
        return box;
    }
}
