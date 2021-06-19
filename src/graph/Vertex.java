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
    /**
     * @return Radian angle to other location
     */
    public double angleTo(double x, double y){
        return Math.atan2(y-this.y,x-this.x);
    }
    /**
     * @return Radian angle to other vertex
     */
    public double angleTo(Vertex other){
        return angleTo(other.x,other.y);
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

    /**
     * @return The angle/connection which is equidistantly farthest from its neighbours among all present connections, randomized.
     * It has a normal distribution around that found angle with standard deviation of 1/4 the distance to the closest other connection.
     * In a vertex without connections, this returns a uniformly random angle.
     */
    public double getLeastDenseAngleRandomized(){
        Random rand = new Random();
        if(connections.size()<1) return rand.nextDouble()*2*Math.PI - Math.PI;
        if(connections.size()==1) return (angleTo(connections.keySet().toArray(Vertex[]::new)[0])+Math.PI)+(rand.nextGaussian()*Math.PI/4);
        // get all angles-- sorted so we can find middles of the sequential pairs
        double[] angles = connections.keySet().stream().mapToDouble(this::angleTo).sorted().toArray();
        double[] middled = new double[angles.length];
        int bestIndex = 0;
        double bestDiff = 0;
        for(int i=0;i<angles.length;i++){
            double diff = (Math.PI - angles[i]) + (angles[0] + Math.PI);
            if(i+1 != angles.length){
                // wrap around from +PI to -PI
                diff = angles[i+1] - angles[i];
            }

            middled[i] = MathHelp.clampAngleRadians(angles[i]+(diff/2)); // e.g. 190 degrees becomes 170 degrees
            if(Math.abs(diff) > bestDiff){
                bestDiff = Math.abs(diff);
                bestIndex = i;
            }
        }
        return MathHelp.clampAngleRadians((middled[bestIndex])+(rand.nextGaussian()*(bestDiff/8)));
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