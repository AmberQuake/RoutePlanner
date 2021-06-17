package graph;

import java.util.*;

public class Vertex{
    public final String name;
    // coordinates:
    public final double x;
    public final double y;

    public double pathDistance = Double.MAX_VALUE; // for pathfinding
    public Vertex pathFrom = null; // for pathfinding

    public HashMap<Vertex,Double> connections;

    public Vertex(String name, double x, double y){
        this.name = name;
        this.x = x;
        this.y = y;
        this.connections = new HashMap<>();
    }

    /**
     * @return Cartesian distance to other location
     */
    public double distTo(double x, double y){
        double width = x-this.x;
        double height = y-this.y;
        return Math.sqrt(width*width+height*height);
    }
    /**
     * @return Cartesian distance to other vertex
     */
    public double distTo(Vertex other){
        return distTo(other.x,other.y);
    }

    public boolean addTraffickedEdge(Vertex adjacent, double trafficLevel){
        double dist = distTo(adjacent);
        trafficLevel = MathHelp.clamp(trafficLevel, 0, 4);
        double traffic = dist*(new Random().nextGaussian()+trafficLevel*0.5);
        traffic = Math.max(traffic, 0);
        return addEdge(adjacent, dist+traffic);
    }
    public boolean addEdge(Vertex adjacent, double distance){
        if(adjacent==null) return false;
        Double result = connections.put(adjacent, distance);
        // null signifies a previous lack of the key
        return result == null || result != distance;
    }
    public boolean removeEdge(Vertex adjacent){
        if(adjacent==null) return false;
        return connections.remove(adjacent) != null; // distances should never be null and valid
    }

    public Vertex copyWithoutConnections(){
        return new Vertex(name, x, y);
    }

    @Override
    public String toString() {
        return name+"("+x+", "+y+")";
    }

    @Override
    public boolean equals(Object other){
        if(this==other) return true;
        if(other instanceof Vertex){
            Vertex o = (Vertex)other;
            return hashCode()==o.hashCode();
        }
        return false;
    }

    @Override
    public int hashCode() {
        // name alone should be sufficient hashing assuming
        // locations are unique, but coordinates ensure this
        return Objects.hash(name, x, y);
    }
}