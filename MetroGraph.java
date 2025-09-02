package pune;

import java.util.*;

class MetroGraph {
    // A map to represent the graph's adjacency list. Each key is a station name,
    // and its value is another map of its neighbors and the distance to each.
    private final Map<String, Map<String, Integer>> adjList = new HashMap<>();
    
    // A separate map to store the fare between stations. The structure is similar
    // to the adjacency list for easy fare lookup.
    private final Map<String, Map<String, Integer>> fareMap = new HashMap<>();

    /**
     * Adds a new edge (connection) between two stations. The graph is undirected,
     * so an edge is added in both directions.
     * @param from The starting station.
     * @param to The destination station.
     * @param distance The distance (weight) of the edge.
     * @param fare The fare for this trip segment.
     */
    public void addEdge(String from, String to, int distance, int fare) {
        // Ensure both stations exist in the adjacency list and fare map.
        // If they don't, a new empty map is created for them.
        adjList.putIfAbsent(from, new HashMap<>());
        adjList.putIfAbsent(to, new HashMap<>());
        fareMap.putIfAbsent(from, new HashMap<>());
        fareMap.putIfAbsent(to, new HashMap<>());

        // Add the connection with its distance and fare for both directions.
        adjList.get(from).put(to, distance);
        adjList.get(to).put(from, distance);
        fareMap.get(from).put(to, fare);
        fareMap.get(to).put(from, fare);
    }

    /**
     * Returns a set of all station names in the graph.
     * @return A Set of Strings representing all station names.
     */
    public Set<String> getStations() {
        return adjList.keySet();
    }

    /**
     * Retrieves the fare between two stations.
     * @param from The starting station.
     * @param to The destination station.
     * @return The integer fare, or 0 if the fare is not found.
     */
    public int getFare(String from, String to) {
        // Safely retrieves the fare. getOrDefault prevents NullPointerException
        // if a station or fare connection doesn't exist.
        return fareMap.getOrDefault(from, Collections.emptyMap())
            .getOrDefault(to, 0);
    }

    /**
     * Updates the fare between two existing stations.
     * @param from The first station.
     * @param to The second station.
     * @param newFare The new fare value.
     */
    public void updateFare(String from, String to, int newFare) {
        // Check if the connection exists before updating the fare in both directions.
        if (fareMap.containsKey(from) && fareMap.get(from).containsKey(to)) {
            fareMap.get(from).put(to, newFare);
        }
        if (fareMap.containsKey(to) && fareMap.get(to).containsKey(from)) {
            fareMap.get(to).put(from, newFare);
        }
    }

    /**
     * Finds the shortest path between a start and end station using Dijkstra's algorithm.
     * The path is stored in the `path` list.
     * @param start The starting station.
     * @param end The destination station.
     * @param path An empty list that will be populated with the shortest path.
     * @return The total distance of the shortest path.
     */
    public int dijkstra(String start, String end, List<String> path) {
        // Map to store the shortest distance from the start station to every other station.
        Map<String, Integer> dist = new HashMap<>();
        
        // Map to store the "previous" station in the shortest path, used for path reconstruction.
        Map<String, String> prev = new HashMap<>();
        
        // A priority queue to efficiently get the station with the smallest distance.
        // The comparator ensures it's sorted by the distance value.
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));

        // Initialize distances: 0 for the start station, infinity for all others.
        for (String station : adjList.keySet()) {
            dist.put(station, Integer.MAX_VALUE);
        }
        dist.put(start, 0);
        pq.add(start);

        // Main loop of Dijkstra's algorithm. Continues until the queue is empty
        // or the destination is reached.
        while (!pq.isEmpty()) {
            String current = pq.poll(); // Get the station with the smallest distance.
            if (current.equals(end)) break; // Stop if the destination is reached.

            // Handle cases where a station might not have neighbors.
            if (adjList.get(current) == null) continue;

            // Iterate over all neighbors of the current station.
            for (Map.Entry<String, Integer> neighbor : adjList.get(current).entrySet()) {
                // Calculate the new distance through the current station.
                int newDist = dist.get(current) + neighbor.getValue();
                
                // Relaxation step: If a shorter path to the neighbor is found, update it.
                if (newDist < dist.get(neighbor.getKey())) {
                    dist.put(neighbor.getKey(), newDist); // Update the distance.
                    prev.put(neighbor.getKey(), current);  // Record the path.
                    pq.add(neighbor.getKey());             // Add the neighbor to the queue.
                }
            }
        }

        // Path Reconstruction: Build the path by tracing back from the end station.
        path.clear();
        String step = end;
        while (step != null) {
            path.add(step);
            step = prev.get(step);
        }
        // The path is currently in reverse order, so we reverse it to get start-to-end.
        Collections.reverse(path);
        
        // Return the final shortest distance to the destination.
        return dist.get(end);
    }
}
