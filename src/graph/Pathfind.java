package graph;

import java.util.*;

public class Pathfind {
    public static void printDijkstra(Graph graph, Vertex from){ // to all other nodes
        for(Vertex v : graph.getVertices()){
            if(v.equals(from)) continue;
            printDijkstra(graph, from, v);
        }
    }
    public static void printDijkstra(Graph graph, Vertex from, Vertex to){
        List<Vertex> path = dijkstraPath(graph, from, to);
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
        graph.resetPaths();
    }
    public static List<Vertex> dijkstraPath(Graph graph, Vertex from, Vertex to){
        Collection<Vertex> values = graph.getVertices();

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
        graph.resetPaths();
        return path;
    }

    public static List<Vertex> aStarPath(Graph graph, Vertex from, Vertex to){
        Collection<Vertex> values = graph.getVertices();

        if(!values.contains(from)||!values.contains(to)){
            System.out.println("A-star path vertices not in graph");
            return null;
        }

        // used to decide which frontier vertex to visit next(smallest of shortest path + closest distance to end)
        Comparator<Vertex> comparator = Comparator.comparingDouble(v -> v.pathDistance + v.distTo(to));

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
        graph.resetPaths();
        return path;
    }
}
