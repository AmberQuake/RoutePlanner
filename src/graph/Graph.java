package graph;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A graph implementation that stores {@link Vertex Vertices}
 * and provides convenience features for adding and removing them.
 *
 * The shortest path between two Vertices can be calculated with
 * {@link Graph#dijkstraPath(Vertex, Vertex) Dijkstra's Algorithm}.
 *
 * Vertices are not checked on removal/entry to be reachable.
 */
public class Graph{
    private HashMap<String,Vertex> vertices; // retrieve vertex by its name

    /**
     * Constructs a graph using pre-existing vertices(no connections are modified).
     */
    public Graph(Vertex... vertices){
        this.vertices = new HashMap<>();
        addAll(vertices);
        resetPaths();
    }

    public void print(){
        System.out.println("Contained vertices: ");
        for(Vertex v : vertices.values()) System.out.println(v+" pathDist: "+v.pathDistance);
        System.out.println("\n");
    }

    public void printDijkstra(Vertex from){ // to all other nodes
        for(Vertex v : vertices.values()){
            if(v.equals(from)) continue;
            printDijkstra(from, v);
        }
    }
    public void printDijkstra(Vertex from, Vertex to){
        List<Vertex> path = dijkstraPath(from, to);
        System.out.print("Dijkstra from "+from+" to "+to+": ");
        if(path==null){
            System.out.println("No path found...");
            return;
        }
        if(path.size()==0){
            System.out.println("Empty path...");
            return;
        }
        System.out.println("{ Dijkstra path: ");
        for(int i=0;i<path.size()-1;i++){
            System.out.println(path.get(i)+"->");
        }
        Vertex last = path.get(path.size()-1);
        System.out.println(last+" END");
        System.out.println("}  Distance: "+path.get(path.size()-1).pathDistance);
        resetPaths();
    }
    public List<Vertex> dijkstraPath(Vertex from, Vertex to){
        Collection<Vertex> values = vertices.values();

        if(!values.contains(from)||!values.contains(to)){
            System.out.println("Dijkstra path vertices not in graph");
            return null;
        }

        // used to decide which frontier vertex to visit next(shortest distance)
        Comparator<Vertex> comparator = Comparator.comparingDouble(o -> o.pathDistance);

        // the set of vertices to be visited next
        PriorityQueue<Vertex> frontier = new PriorityQueue<>(comparator);

        // the set of vertices already visited
        List<Vertex> known = new ArrayList<>();

        // path cost originates from our start vertex
        from.pathDistance = 0;
        Vertex current = from;

        while(!known.contains(to)){
            known.add(current);
            //System.out.println("Dijkstra knew "+current.name);
            if(current.equals(to)) break;

            for(Vertex neighbour : current.connections.keySet()){
                double pathDist = current.pathDistance + current.connections.get(neighbour);
                //System.out.println("Compare: "+pathDist+" < "+neighbour.pathDistance);
                if(pathDist < neighbour.pathDistance){
                    // if we find a shorter path, update that vertex
                    neighbour.pathDistance = pathDist;
                    neighbour.pathFrom = current;
                    // rebuild the queue, because priorities do not change when we modify like above
                    PriorityQueue<Vertex> old = frontier;
                    frontier = new PriorityQueue<>(comparator);
                    frontier.addAll(old);

                    if(!known.contains(neighbour)) frontier.add(neighbour);
                }
            }

            if(frontier.isEmpty()) return null;
            else{
                current = frontier.poll();
            }
        }

        // follow end vertex back to start
        List<Vertex> path = new ArrayList<>();
        Vertex curr = to;
        Vertex prev = to.pathFrom;
        path.add(curr);
        while(prev != null){
            path.add(prev);
            curr = prev;
            prev = curr.pathFrom;
        }
        // reverse, so we go from start to end
        Collections.reverse(path);
        resetPaths();
        return path;
    }

    public void resetPaths(){
        // reset pathfinding data after a traversal
        for(Vertex v : vertices.values()){
            v.pathDistance = Double.MAX_VALUE;
            v.pathFrom = null;
        }
    }

    public Vertex get(String name){
        return vertices.get(name);
    }
    public List<Vertex> getVertices(){
        return new ArrayList<>(vertices.values());
    }

    /**
     * Adds or updates both vertices in graph and a connection of undirected distance between them.
     * @param dist Distance from a to b
     * @return If anything is updated and non-null
     */
    public boolean add(Vertex a, Vertex b, double dist){
        if(a==null||b==null) return false;
        boolean did = false;
        did = addVertex(a);
        did = addVertex(b) || did;
        did = addEdge(a, b, dist) || did;
        return did;
    }
    /**
     * Adds or updates both vertices in graph and connections of directed distances between them.
     * @param a2b Distance from a to b
     * @param b2a Distance from b to a
     * @return If anything is updated and non-null
     */
    public boolean add(Vertex a, double a2b, Vertex b, double b2a){
        if(a==null||b==null) return false;
        boolean did = false;
        did = addVertex(a);
        did = addVertex(b) || did;
        did = addEdges(a, a2b, b, b2a) || did;
        return did;
    }
    /**
     * Adds multiple vertices as-is(no modifications to connections).
     * @return If any vertices were added/updated
     */
    public boolean addAll(Vertex... vertices){
        boolean did = false;
        for(Vertex v : vertices) did = addVertex(v) || did;
        return did;
    }
    /**
     * Adds a vertex to the graph.
     */
    private boolean addVertex(Vertex vertex){
        if(vertex==null||vertex.name.isEmpty()) return false;
        return vertices.put(vertex.name, vertex) != vertex;
    }
    /**
     * Adds or updates connections of undirected distance between two vertices.
     * @param dist Distance from a to b
     * @return If changed some connection
     */
    private boolean addEdge(Vertex a, Vertex b, double dist){
        return addEdges(a, dist, b, dist);
    }
    /**
     * Adds or updates connections of directed distances between two vertices.
     * @param a2b Distance from a to b
     * @param b2a Distance from b to a
     * @return If changed some connection
     */
    private boolean addEdges(Vertex a, double a2b, Vertex b, double b2a){
        if(a==null||b==null) return false;
        boolean did = false;
        did = a.addEdge(b, a2b);
        did = b.addEdge(a, b2a) || did;
        return did; // verbose to avoid conditional evaluation order
    }

    /**
     * Removes a vertex from the graph and all connections to it.
     * @return If vertex/connections were removed
     */
    public boolean remove(Vertex a){
        if(a==null) return false;
        boolean did = false;
        for(Vertex v : a.connections.keySet()){
            did = removeEdges(v,a) || did; // unconnect other vertices to this vertex
        }
        return removeVertex(a);
    }
    /**
     * Removes a vertex from tracked vertices in graph.
     * <b>This does not remove existing connections to the vertex!</b>
     * @param a
     * @return
     */
    private boolean removeVertex(Vertex a){ // CONNECTIONS NOT REMOVED
        if(a==null) return false;
        String key = a.name;
        return vertices.remove(key) != null;
    }
    /**
     * Removes connections between vertices a and b.
     */
    private boolean removeEdges(Vertex a, Vertex b){
        if(a==null||b==null) return false;
        boolean did = false;
        did = a.removeEdge(b);
        did = b.removeEdge(a) || did;
        return did; // verbose to avoid conditional evaluation order
    }

    public boolean adjacent(Vertex a, Vertex b){
        if(a==null||b==null) return false;
        return a.connections.containsKey(b);
    }
    public List<Vertex> neighbors(Vertex vert){ // finds adjacent vertices
        if(vert==null) return null;
        return new ArrayList<>(vert.connections.keySet());
    }

    public static Graph generateRandomOrganic(int numVertices, int avgEdges, double avgDist, int trafficLevel){
        long start = System.currentTimeMillis();
        Graph graph = new Graph();

        numVertices = MathHelp.clamp(numVertices, 1, 1000);
        // try to avoid crowding
        avgEdges = MathHelp.clamp(avgEdges, 2, 8);
        avgDist = Math.max(avgDist, 80);
        double width = numVertices*20+200;
        double height = numVertices*20+200;

        final double minDist = Math.max(avgDist / 5, 80);

        String max = String.valueOf(numVertices);
        Random rand = new Random();
        int count = 0;
        while(count < numVertices){
            //System.out.println("Count: "+count);
            double x = rand.nextDouble()*width;
            double y = rand.nextDouble()*height;
            String name = String.valueOf(count);
            while(name.length() < max.length()) name = "0".concat(name);

            if(count > 0){
                Vertex next = null;
                List<Vertex> neighbours = new ArrayList<>();

                while(next == null){
                    int randIndex;
                    randIndex = rand.nextInt(graph.vertices.size());
                    Vertex selected = graph.vertices.values().toArray(new Vertex[0])[randIndex];

                    int maxEdges = avgEdges+(rand.nextInt(5)-2);
                    maxEdges = MathHelp.clamp(maxEdges, 1, 8);
                    if(selected.connections.size() < maxEdges){
                        //System.out.println("Selected: "+selected);
                        double relaxedMinDist = minDist;
                        while(next == null){
                            double randDist = avgDist+(0.25*rand.nextGaussian()*avgDist);
                            double randAngle = rand.nextDouble()*2*Math.PI;
                            x = selected.x+Math.cos(randAngle)*randDist;
                            y = selected.y+Math.sin(randAngle)*randDist;
                            neighbours.add(selected);
                            double closestDist = selected.distTo(x,y);
                            for(Vertex v : graph.vertices.values()){
                                double dist = v.distTo(x,y);
                                if(dist < closestDist){
                                    closestDist = dist;
                                }
                                double acceptedDist = avgDist+(0.25*rand.nextGaussian()*avgDist);
                                acceptedDist = Math.max(acceptedDist, minDist);
                                if(dist <= acceptedDist) neighbours.add(v);
                            }
                            //System.out.println("Close: "+closestDist+", Min: "+relaxedMinDist);
                            if(closestDist > relaxedMinDist){
                                next = new Vertex(name,x,y);
                            }
                            else{
                                neighbours.clear();
                                relaxedMinDist-=minDist*0.01; // relax over time to be lenient
                            }
                        }
                        for(Vertex neighbour : neighbours){
                            next.addTraffickedEdge(neighbour, trafficLevel);
                            if(!neighbour.connections.containsKey(next)){
                                neighbour.addTraffickedEdge(next, trafficLevel);
                            }
                        }
                    }
                }
                graph.addVertex(next);
            }
            else{
                graph.addVertex(new Vertex(name,x,y));
            }

            count++;
        }

        System.out.println("Generation time: "+(System.currentTimeMillis()-start)+" ms");
        return graph;
    }

    /**
     * @return An example of a graph with 6 vertices.
     */
    public static Graph generateExample(){
        Graph example = new Graph();
        Vertex a = new Vertex("a",0,100);
        Vertex b = new Vertex("b",210,0);
        Vertex c = new Vertex("c",200,400);
        Vertex d = new Vertex("d",600,520);
        Vertex e = new Vertex("e",300,600);
        Vertex f = new Vertex("f",20,550);
        // undirected edge weidhts of cartesian distance
        example.add(a,b,a.distTo(b));
        example.add(a,c,a.distTo(c));
        example.add(a,f,a.distTo(f));
        example.add(b,c,b.distTo(c));
        example.add(b,d,b.distTo(d));
        example.add(c,f,c.distTo(f));
        example.add(c,d,c.distTo(d));
        example.add(d,e,d.distTo(e));
        example.add(f,e,f.distTo(e));

        return example;
    }
}
