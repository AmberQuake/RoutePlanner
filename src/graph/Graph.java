package graph;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A graph implementation that stores {@link Vertex Vertices}
 * and provides convenience features for adding and removing them.
 *
 * The shortest path between two Vertices can be calculated with the
 * {@link Pathfind} class.
 *
 * Vertices are not checked on removal/entry to be reachable.
 */
public class Graph{
    private final HashMap<String,Vertex> vertices; // retrieve vertex by its name

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
     * @return If vertex was found in graph
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
    public Vertex getRandomVertex(){
        int size = vertices.size();
        if(size==0) return null;
        else return vertices.values().toArray(Vertex[]::new)[new Random().nextInt(size)];
    }

    /**
     * Generates a random graph using an "organic" algorithm. The general idea for this is expanding
     * the graph at random locations, choosing areas of less known density to expand into.
     * @param numVertices How many vertices to populate the graph with
     * @param edgeLevel Loosely correlates with average edges/connections-- higher values will choose to avoid dense areas less
     * @param avgDist The average distance new vertices have from their starting vertex. Normal distribution with standard deviation of 1/6.
     *                See {@link Vertex#getLeastDenseAngleRandomized()} for how angles are chosen.
     * @param trafficLevel Higher values increase the average traffic. See {@link Vertex#addTraffickedEdge(Vertex, double)}
     */
    public static Graph generateRandomOrganic(int numVertices, double edgeLevel, double avgDist, int trafficLevel){
        long start = System.currentTimeMillis();
        Graph graph = new Graph();
        for(int i=0;i<numVertices;i++){
            graph.generateNextOrganic(edgeLevel, avgDist, trafficLevel);
        }
        System.out.println("(#"+numVertices+")Generation time: "+(System.currentTimeMillis()-start)+" ms");
        return graph;
    }
    private void generateNextOrganic(double edgeLevel, double avgDist, int trafficLevel){
        // try to avoid crowding
        edgeLevel = MathHelp.clamp(edgeLevel, 2, 8);
        avgDist = Math.max(avgDist, 80);

        final double minDist = Math.max((avgDist / 5), 80)+80;

        int currVertices = vertices.size();

        int selections = 0;

        String name = String.valueOf(currVertices+1);

        Random rand = new Random();

        double x = 0;
        double y = 0;

        if(currVertices > 0){
            Vertex next = null;
            List<Vertex> neighbours = new ArrayList<>();

            while(next == null){
                Vertex selected = getRandomVertex();
                selections++;

                // determine acceptable edge levels, ignore edgy nodes
                // random distribution like this helps avoid uniformity
                int maxEdges = (int)(edgeLevel+(rand.nextInt(5)-2));
                maxEdges = MathHelp.clamp(maxEdges, 1, 8);
                // relax our maximum edges in case we cannot find a suitable node
                // (this is very unlikely)
                maxEdges += selections > 2*currVertices ? (int)((selections-2*currVertices)/10) : 0;

                if(selected.connections.size() < maxEdges){
                    double relaxedMinDist = minDist;
                    while(next == null){
                        // find the x,y of the new node
                        double randDist = avgDist+(rand.nextGaussian()*avgDist/6);
                        // choose the most uncrowded angle
                        double randAngle = selected.getLeastDenseAngleRandomized();
                        x = MathHelp.xFromPolar(randDist, randAngle);
                        x += selected.x;
                        y = MathHelp.yFromPolar(randDist, randAngle);
                        y += selected.y;

                        // grow the new node from the node we randomly selected to expand from
                        neighbours.add(selected);
                        // determine if we should re-create this new node because it is too crowded
                        double closestDist = selected.distTo(x,y);
                        // find the closest node in the graph and also the acceptably close neighbours
                        // (this is very slow at O(n) for every vertex)
                        for(Vertex v : vertices.values()){
                            double dist = v.distTo(x,y);
                            closestDist = Math.min(closestDist, dist);
                            double acceptedDist = avgDist+(rand.nextGaussian()*avgDist/6);
                            acceptedDist = Math.max(acceptedDist, minDist);
                            if(dist <= acceptedDist) neighbours.add(v);
                        }
                        // if it is too crowded, try again at another location
                        if(closestDist > relaxedMinDist){
                            next = new Vertex(name,x,y);
                        }
                        else{
                            neighbours.clear();
                            relaxedMinDist-=minDist*0.01; // relax over time to be lenient
                        }
                    }
                    // connect neighbours we decided on
                    for(Vertex neighbour : neighbours){
                        next.addTraffickedEdge(neighbour, trafficLevel);
                        if(!neighbour.connections.containsKey(next)){
                            neighbour.addTraffickedEdge(next, trafficLevel);
                        }
                    }
                }
            }
            addVertex(next);
        }
        else{
            // first node
            addVertex(new Vertex(name,x,y));
        }
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
