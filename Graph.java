package graphs;
import java.util.*;

public class Graph {

    /**
     * a map to represent an adjacency list for our graph.
     */
    private Map<String, ArrayList<Edge>> adjacencyList;

    /**
     * a Node class example you might like to use.
     */
    private class Node {
        String name;
        private HashMap<Node, Integer> neighbors;

        public Node(String name){
            this.name = name;
        }

        public Node(String name, HashMap<Node, Integer> neighbors) {
            this.name = name;
            this.neighbors = neighbors;
        }
    }

    /**
     * an Edge class example you might like to use. Hint: this was particularly helpful in the staff solution.
     */
    private class Edge {
        private String from;
        private String to;
        private String routeName;
        private double weight;

        Edge(String from, Double weight){
            this.from = from;
            this.weight = weight;
        }

        Edge(String from, String to, Double weight, String routeName) {
            this.from = from;
            this.to = to;
            this.weight = weight;
            this.routeName = routeName;
        }
    }

    /**
     * Getting this show on the road!
     */
    public Graph(){
        adjacencyList = new HashMap<>();
    }

    /** Task 1: Add Transit Paths - add Transit Paths to the graph design you have developed.
     *
     * @param stops
     * @param routeName
     * @param travelTimes
     */
    public void addTransitRoute(List<String> stops, String routeName, List<Double> travelTimes) {
        for (int i = 0; i < stops.size() - 1; i++) {
            String from = stops.get(i);
            String to = stops.get(i + 1);
            double weight = travelTimes.get(i);

            adjacencyList.putIfAbsent(from, new ArrayList<>());
            adjacencyList.putIfAbsent(to, new ArrayList<>());

            adjacencyList.get(from).add(new Edge(from, to, weight, routeName));
            adjacencyList.get(to).add(new Edge(to, from, weight, routeName));
        }
    }

    /** Task 2: Get Transit Paths - get the three transit paths from a start to destination that use the least amount of transfers.
     *          Break ties using the shorter path!
     *
     * @param start
     * @param destination
     * @return a List<List<String>> of vertices and routes for the three minimized transfer paths [[A, C, D, E, F, G, 372, 556], ...].
     * The inner list should be formatted where you add Strings in the sequential order "A" then "B" and all vertices, then "32" and all bus routes etc.
     * i.e. We want an inner list of [A, B, G, 32, 1Line] since the route from A -> B is on route 32 and from B -> G is on the 1Line.
     * Ties are broken using the shorter path!
     * Note: Do not add the same route multiple times for a path! I.e. Only add route "32" once per path.
     */
    // right now it is adding route numbers when it doesn't have to
    public List<List<String>> getTransitPaths(String start, String destination) {
        List<List<String>> paths = allPaths(start, destination);
        TreeSet<TransitPath> transitPaths = new TreeSet<>();

        // Iterate through all possible paths from start to destination
        for (List<String> path : paths) {
            int transferCount = 0;
            double totalTime = 0.0;
            String currentRoute = null;
            List<String> formattedPath = new ArrayList<>();
            List<String> routes = new ArrayList<>();

            // Iterate through each edge in the path to compute transfers and time
            for (int i = 0; i < path.size() - 1; i++) {
                String current = path.get(i);
                String next = path.get(i + 1);

                // Helper method to get edge (prioritizing not changing routes)
                Edge edge = getEdge(current, next, currentRoute);

                formattedPath.add(current);

                // if the route names do not match, increment transfer counter by 1
                if (!edge.routeName.equals(currentRoute)) {
                    currentRoute = edge.routeName;
                    routes.add(currentRoute); // Add route if it's not the current one
                    transferCount++;
                }
                totalTime += edge.weight;
            }

            formattedPath.add(destination);
            formattedPath.addAll(routes); // Append routes list to the path list for correct format

            // add TransitPath object to TreeMap for sorting
            transitPaths.add(new TransitPath(formattedPath, transferCount, totalTime));
        }

        List<List<String>> result = new ArrayList<>();
        // add sorted paths to result list
        for (TransitPath path : transitPaths) {
            result.add(path.getPath());
        }
        // return first 3
        return result.subList(0, Math.min(3, result.size()));
    }

    // Helper class to store path information
    private class TransitPath implements Comparable<TransitPath> {
        private final List<String> path;
        private final int transferCount;
        private final double totalTime;

        public TransitPath(List<String> path, int transferCount, double totalTime) {
            this.path = path;
            this.transferCount = transferCount;
            this.totalTime = totalTime;
        }

        public List<String> getPath() {
            return path;
        }

        @Override
        // Defines comparison for TreeMap
        public int compareTo(TransitPath other) {
            int transferComparison = Integer.compare(this.transferCount, other.transferCount);
            if (transferComparison != 0) {
                return transferComparison;
            }
            return Double.compare(this.totalTime, other.totalTime);
        }
    }

    // private helper method to get the edge between the from and to parameters
    private Edge getEdge(String from, String to, String currentRouteName) {
        List<Edge> edges = adjacencyList.get(from);
        if (edges != null) {
            // for multiple edges, prioritize edges that do not change routes
            for (Edge edge : edges) {
                if (edge.routeName.equals(currentRouteName) && edge.to.equals(to)) {
                    return edge;
                }
            }
            // if you have to change route
            for (Edge edge : edges) {
                if (edge.to.equals(to)) {
                    return edge;
                }
            }
        }
        return null;
    }

    /**
     * You can use this as a helper to return all paths from a start vertex to an end vertex.
     * Call this in getTransitPaths!
     * This method is designed to help give partial credit in the event that you are unable to finish getTransitPaths!
     *
     * @param start
     * @param destination
     * @return a List<List<String>> of containing all vertices among all paths from start to dest [[A, C, D, E, F, G], ...].
     * Do not add transit routes to this method! You should take care of that in getTransitPaths!
     */
    public List<List<String>> allPaths(String start, String destination){
        // List to store all paths from start to destination
        List<List<String>> paths = new ArrayList<>();
        // keep track of current path
        List<String> path = new ArrayList<>();
        // set to keep track of visited nodes
        Set<String> visited = new HashSet<>();

        allPathsHelper(start, destination, paths, path, visited);
        return paths;
    }

    // DFS -> explores all the way down a path before backtracking
    private void allPathsHelper(String current, String destination, List<List<String>> paths, List<String> path, Set<String> visited) {
        // add current node to path and mark as visited
        path.add(current);
        visited.add(current);

        // If current node is destination, add the path to the result list
        if (current.equals(destination)) { // base case
            paths.add(new ArrayList<>(path));
        } else {
            // explore all adjacent nodes
            if (adjacencyList.containsKey(current)) {
                for (Edge edge : adjacencyList.get(current)) {
                    // call the method again if node has not been visited
                    if (!visited.contains(edge.to)) {
                        allPathsHelper(edge.to, destination, paths, path, visited);
                    }
                }
            }
        }
        // Backtrack (remove current node from path and remove it from visited set)
        path.remove(path.size() - 1);
        visited.remove(current);
    }

    /**
     * Task 3: Get Shortest Coffee Path - get the shortest path from start to destination with a coffee shop on the route.
     *
     * @param start
     * @param destination
     * @param coffeeStops
     * @return a List<String> representing the shortest path from a start to a destination with a coffee shop along the way
     * return in the form of a List where you add Strings in the sequential order "A" then "B" and all vertices, then "32" and all bus routes etc.
     * i.e. We want to return [A, B, G, 32, 1Line] since the route from A -> B is on route 32 and the route from B -> G is on the 1Line.
     * Note: Do not add the same route multiple times for a path! I.e. Only add route "32" once per path.
     */
    public List<String> getShortestCoffeePath(String start, String destination, Set<String> coffeeStops) {

        List<String> shortestPath = null;
        double minTotalTime = Double.MAX_VALUE;

        for (String coffeeStop : coffeeStops) {
            // Get the shortest path from start to the coffee stop
            List<String> pathToCoffee = shortestPath(start, coffeeStop);
            // Get the shortest path from the coffee stop to the destination
            List<String> pathFromCoffee = shortestPath(coffeeStop, destination);

            // Compute the total distance of this path
            double time = computeTotalTime(pathToCoffee) + computeTotalTime(pathFromCoffee);

            // Check if this path is shorter than the previously found path
            if (time < minTotalTime) {
                minTotalTime = time;

                // Combine paths: Remove the last vertex of pathToCoffee (the coffee stop) to avoid duplication
                List<String> combinedPath = new ArrayList<>(pathToCoffee);
                combinedPath.addAll(pathFromCoffee.subList(1, pathFromCoffee.size())); // Skip the coffee stop in pathFromCoffee

                // Add routes to the combined path
                combinedPath.addAll(getRoutes(combinedPath));
                shortestPath = combinedPath;
            }
        }
        return shortestPath;
    }

    // Private helper method to return total time a path takes
    private double computeTotalTime(List<String> path) {
        double totalTime = 0.0;
        String currentRoute = null;
        List<String> routes = new ArrayList<>();

        // Iterate through each edge in the path to compute transfers and time
        for (int i = 0; i < path.size() - 1; i++) {
            String current = path.get(i);
            String next = path.get(i + 1);

            // Helper method to get edge (prioritizing not changing routes)
            Edge edge = getEdge(current, next, currentRoute);
            totalTime += edge.weight;
        }
        return totalTime;
    }

    // Private helper method to return the different routes along a path
    private List<String> getRoutes(List<String> path) {
    List<String> routes = new ArrayList<>();
    String currentRoute = null;
    for (int i = 0; i < path.size() - 1; i++) {
        String from = path.get(i);
        String to = path.get(i + 1);
        Edge edge = getEdge(from, to, currentRoute);
        if (edge != null) {

            // if route name is different from current route name, add current route name to routes
            // and update currentRoute to current edges route name
            if (!edge.routeName.equals(currentRoute)) {
                if (currentRoute != null) {
                    routes.add(currentRoute);
                }
                currentRoute = edge.routeName;
            }
        }
    }
    if (currentRoute != null) {
        routes.add(currentRoute);
    }
    return routes;
}

    /**
     * A helper method used to actually find the shortest path between any start node and destination node.
     * Call this in getShortestCoffeePaths!
     * This method is designed to help give partial credit in the event that you are unable to finish getShortestCoffeePath!
     *
     * @param start
     * @param destination
     * @return a List<String> containing all vertices along the shortest path in the form [A, C, D, E, F, G].
     * Do not add transit routes to this method! You should take care of that in getShortestCoffeePaths!
     */
    public List<String> shortestPath(String start, String destination) {
        Map<String, Double> times = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        // priority queue to select vertex with smallest distance
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingDouble(times::get)); // compare by time retrieved
                                                                                                   // from times map

        // Initialize all distance to all vertices as max value and add to HashMap
        for (String vertex : adjacencyList.keySet()) {
            times.put(vertex, Double.MAX_VALUE);
        }
        times.put(start, 0.0); // add start node with time of 0
        queue.add(start);

        while (!queue.isEmpty()) {
            String current = queue.poll(); // get the smallest distance

            // exit while loop if the destination is reached
            if (current.equals(destination)) {
                break;
            }

            // loop to iterate through all edges of current vertex, if current has no edges it returns empty list
            for (Edge edge : adjacencyList.getOrDefault(current, new ArrayList<>())) {
                double newDist = times.get(current) + edge.weight;
                // if faster path is found, update times map and predecessors map
                if (newDist < times.get(edge.to)) {
                    times.put(edge.to, newDist);
                    predecessors.put(edge.to, current);
                    queue.add(edge.to);
                }
            }
        }

        // Reconstruct path from destination to start
        List<String> path = new LinkedList<>();
        // Loop runs until "at" equals null and each vertex added to beginning of path
        for (String at = destination; at != null; at = predecessors.get(at)) {
            path.add(0, at);
        }
        return path;
    }
}
