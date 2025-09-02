package pune;

import java.io.BufferedReader; // Used to efficiently read text from a character-input stream
import java.io.BufferedWriter; // Used to efficiently write text to a character-output stream
import java.io.File; // Used to represent file and directory pathnames
import java.io.FileReader; // Used to read character files
import java.io.FileWriter; // Used to write character files
import java.io.IOException; // Represents an I/O exception
import java.util.*; // Import utility classes (Scanner, List, etc.)

public class Main {

    // Name of the file where metro data will be stored permanently
    private static final String DATA_FILE = "metro_data.txt";
    // Maps to store the metro network data in memory
    private static Map<String, List<String>> lineStations = new HashMap<>(); // Key: line name (e.g., "purple"), Value: list of station names
    private static Map<String, int[][]> lineFares = new HashMap<>(); // Key: line name, Value: 2D array of fares between stations
    private static Map<String, Integer> lineDistances = new HashMap<>(); // Key: line name, Value: arbitrary distance weight for the line

    public static void main(String[] args) {
        // Step 1: Load data from the file or initialize with defaults if the file doesn't exist.
        loadDataFromFile();

        // Step 2: Main application loop for user interaction
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("Are you a passenger or an admin? (p/a): ");
            String userType = sc.nextLine().trim();

            if (userType.equalsIgnoreCase("a")) { // If user is an admin
                if (adminLogin(sc)) { // Check admin credentials
                    adminMenu(sc); // Show admin options
                } else {
                    System.out.println("❌ Invalid credentials. Exiting.");
                }
            } else { // If user is a passenger or any other input
                passengerMenu(sc); // Show passenger options (route planning)
            }
        }
        System.out.println("\n🙏 Thank you for using Pune Metro Route Planner!");
    }

    // --- File Handling and Data Initialization ---
    /**
     * Tries to load metro data from the DATA_FILE. If the file exists, it reads
     * the data and populates the in-memory maps. If not, it initializes the
     * data with default values.
     */
    private static void loadDataFromFile() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            System.out.println("Loading metro data from file...");
            try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
                // Clear existing data to avoid duplicates or old data
                lineStations.clear();
                lineFares.clear();
                lineDistances.clear();

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue; // Skip empty lines
                    }
                    String lineName = line.trim();
                    if (lineName.endsWith(" Line")) { // Identify the start of a new line's data
                        lineName = lineName.substring(0, lineName.lastIndexOf(" ")).toLowerCase();

                        // Read stations, distances, and fares for the line
                        String stationsLine = reader.readLine();
                        List<String> stations = Arrays.asList(stationsLine.split(","));
                        lineStations.put(lineName, new ArrayList<>(stations));

                        String distanceLine = reader.readLine();
                        int distance = Integer.parseInt(distanceLine);
                        lineDistances.put(lineName, distance);

                        int n = stations.size();
                        int[][] fares = new int[n][n];
                        for (int i = 0; i < n; i++) {
                            String fareRow = reader.readLine();
                            String[] fareValues = fareRow.split(",");
                            for (int j = 0; j < n; j++) {
                                fares[i][j] = Integer.parseInt(fareValues[j]);
                            }
                        }
                        lineFares.put(lineName, fares);
                    }
                }
            } catch (IOException | NumberFormatException e) {
                // Handle file read errors gracefully by falling back to default data
                System.out.println("Error reading data file. Initializing with default data.");
                initializeDefaultData();
            }
        } else {
            // If the file doesn't exist, start with the default hardcoded data
            System.out.println("No data file found. Initializing with default data.");
            initializeDefaultData();
        }
    }

    /**
     * Initializes the application with the default metro lines and their
     * respective data. This is only used if the data file cannot be found.
     */
    private static void initializeDefaultData() {
        // Hardcoded data for the Purple Line
        lineStations.put("purple", new ArrayList<>(Arrays.asList(
                "PCMC", "Sant Tukaram Nagar", "Bhosari", "Kasarwadi", "Phugewadi",
                "Dapodi", "Bopodi", "Shivaji Nagar", "Civil Court",
                "Kasba Peth (Budhwar Peth)", "Mandal", "Swargate"
        )));
        int[][] purpleFares = {
            {0, 15, 15, 20, 20, 25, 25, 30, 30, 30, 30, 30},
            {15, 0, 10, 15, 15, 20, 25, 25, 25, 30, 30, 30},
            // ... (fares for purple line)
            {30, 30, 30, 30, 30, 30, 30, 25, 25, 25, 10, 0}
        };
        lineFares.put("purple", purpleFares);
        lineDistances.put("purple", 1); // Arbitrary distance weight for this line

        // Hardcoded data for the Aqua Line
        lineStations.put("aqua", new ArrayList<>(Arrays.asList(
                "Vanaz", "Anand Nagar", "Ideal Colony", "Nal Stop",
                "Garware College", "Deccan Gymkhana", "Chhatrapati Sambhaji Udyan",
                "PMC", "Civil Court", "Mangalwar Peth",
                "Pune Railway Station", "Ruby Hall Clinic", "Bund Garden",
                "Yerwada", "Kalyani Nagar", "Ramwadi"
        )));
        int[][] aquaFares = {
            {0, 10, 20, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 35, 35},
            // ... (fares for aqua line)
            {35, 35, 35, 35, 35, 35, 35, 25, 25, 25, 25, 25, 25, 25, 20, 0}
        };
        lineFares.put("aqua", aquaFares);
        lineDistances.put("aqua", 2); // Arbitrary distance weight for this line
    }

    /**
     * Saves the current in-memory metro data to the DATA_FILE. This method
     * overwrites the file with the new data.
     */
    private static void saveDataToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (String lineName : lineStations.keySet()) {
                writer.write(lineName + " Line\n"); // Write line name
                writer.write(String.join(",", lineStations.get(lineName)) + "\n"); // Write stations as a comma-separated list
                writer.write(String.valueOf(lineDistances.get(lineName)) + "\n"); // Write distance weight
                int[][] fares = lineFares.get(lineName);
                for (int[] row : fares) {
                    StringBuilder fareRow = new StringBuilder();
                    for (int i = 0; i < row.length; i++) {
                        fareRow.append(row[i]);
                        if (i < row.length - 1) {
                            fareRow.append(",");
                        }
                    }
                    writer.write(fareRow.toString() + "\n"); // Write each row of the fare matrix
                }
                writer.write("\n"); // Add a blank line as a separator between line data
            }
            System.out.println("Data saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving data to file: " + e.getMessage());
        }
    }

    // --- Admin Functions ---
    /**
     * Authenticates an admin user with hardcoded credentials.
     *
     * @return true if login is successful, false otherwise.
     */
    private static boolean adminLogin(Scanner sc) {
        System.out.println("\n--- Admin Login ---");
        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine().trim();
        return email.equals("snehadalvi@gmail.com") && password.equals("sneha");
    }

    /**
     * Displays the admin menu and handles user choice.
     */
    private static void adminMenu(Scanner sc) {
        boolean adminExit = false;
        while (!adminExit) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Add new station");
            System.out.println("2. Add new metro line");
            System.out.println("3. Update fare");
            System.out.println("4. Remove station");
            System.out.println("5. Logout");
            System.out.print("Enter your choice: ");
            String choiceStr = sc.nextLine().trim();

            try {
                int choice = Integer.parseInt(choiceStr);
                switch (choice) {
                    case 1 -> {
                        addNewStation(sc);
                        saveDataToFile(); // Save changes after every action
                    }
                    case 2 -> {
                        addNewLine(sc);
                        saveDataToFile();
                    }
                    case 3 -> {
                        updateFare(sc);
                        saveDataToFile();
                    }
                    case 4 -> {
                        removeStation(sc);
                        saveDataToFile();
                    }
                    case 5 ->
                        adminExit = true;
                    default ->
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    /**
     * Adds a new station to an existing line and adjusts the fare matrix.
     */
    private static void addNewStation(Scanner sc) {
        System.out.print("Enter line name (purple/aqua): ");
        String lineName = sc.nextLine().trim().toLowerCase();
        if (!lineStations.containsKey(lineName)) {
            System.out.println("Invalid line name.");
            return;
        }

        List<String> stations = lineStations.get(lineName);
        System.out.print("Enter new station name: ");
        String newStation = sc.nextLine().trim();
        System.out.printf("Enter position (1 to %d) for the new station: ", stations.size() + 1);
        int position = sc.nextInt();
        sc.nextLine();

        if (position < 1 || position > stations.size() + 1) {
            System.out.println("Invalid position.");
            return;
        }

        stations.add(position - 1, newStation);

        int oldSize = stations.size() - 1;
        int[][] oldFares = lineFares.get(lineName);
        int[][] newFares = new int[stations.size()][stations.size()];

        // This is a simplified logic to rebuild the fare matrix.
        // It copies old fares and sets new fares based on a simple heuristic (e.g., adjacent fare is 10).
        for (int i = 0; i < stations.size(); i++) {
            for (int j = 0; j < stations.size(); j++) {
                if (i == j) {
                    newFares[i][j] = 0;
                } else if (Math.abs(i - j) == 1) {
                    newFares[i][j] = 10;
                } else {
                    int oldI = i > position - 1 ? i - 1 : i;
                    int oldJ = j > position - 1 ? j - 1 : j;
                    if (oldI < oldSize && oldJ < oldSize) {
                        newFares[i][j] = oldFares[oldI][oldJ];
                    } else {
                        newFares[i][j] = 25;
                    }
                }
            }
        }
        lineFares.put(lineName, newFares);
        System.out.println("Station '" + newStation + "' added successfully to " + lineName + " line.");
    }

    /**
     * Removes a station from an existing line and rebuilds the fare matrix. It
     * prevents the removal of the Civil Court interchange.
     */
    private static void removeStation(Scanner sc) {
        System.out.print("Enter line name (purple/aqua): ");
        String lineName = sc.nextLine().trim().toLowerCase();
        if (!lineStations.containsKey(lineName)) {
            System.out.println("Invalid line name.");
            return;
        }

        List<String> stations = lineStations.get(lineName);
        if (stations.size() <= 2) {
            System.out.println("Cannot remove station. A line must have at least two stations.");
            return;
        }

        System.out.print("Enter station name to remove: ");
        String stationToRemove = sc.nextLine().trim();
        int indexToRemove = stations.indexOf(stationToRemove);

        if (indexToRemove == -1) {
            System.out.println("Station not found on this line.");
            return;
        }

        if (stationToRemove.equals("Civil Court")) {
            System.out.println("The Civil Court station cannot be removed as it is a crucial interchange point.");
            return;
        }

        stations.remove(indexToRemove);

        // Rebuild fare matrix by copying from the old matrix, skipping the removed station's row and column
        int oldSize = stations.size() + 1;
        int[][] oldFares = lineFares.get(lineName);
        int[][] newFares = new int[stations.size()][stations.size()];

        int newI = 0;
        for (int i = 0; i < oldSize; i++) {
            if (i == indexToRemove) {
                continue;
            }
            int newJ = 0;
            for (int j = 0; j < oldSize; j++) {
                if (j == indexToRemove) {
                    continue;
                }
                newFares[newI][newJ] = oldFares[i][j];
                newJ++;
            }
            newI++;
        }

        lineFares.put(lineName, newFares);
        System.out.println("Station '" + stationToRemove + "' removed successfully from " + lineName + " line.");
    }

    /**
     * Adds a completely new metro line with its stations, distance weight, and
     * fare matrix.
     */
    private static void addNewLine(Scanner sc) {
        System.out.print("Enter new line name: ");
        String newLineName = sc.nextLine().trim().toLowerCase();
        if (lineStations.containsKey(newLineName)) {
            System.out.println("Line already exists.");
            return;
        }

        System.out.print("Enter stations for the new line (comma-separated): ");
        String stationsStr = sc.nextLine().trim();
        List<String> newStations = Arrays.asList(stationsStr.split("\\s*,\\s*"));
        lineStations.put(newLineName, new ArrayList<>(newStations));

        System.out.print("Enter distance weight for the new line: ");
        int distance = sc.nextInt();
        sc.nextLine();
        lineDistances.put(newLineName, distance);

        int n = newStations.size();
        int[][] newFares = new int[n][n];
        System.out.println("Enter fares for the new line:");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    newFares[i][j] = 0;
                } else {
                    System.out.printf("Fare from %s to %s: ", newStations.get(i), newStations.get(j));
                    newFares[i][j] = sc.nextInt();
                }
            }
        }
        lineFares.put(newLineName, newFares);
        sc.nextLine();
        System.out.println("New line '" + newLineName + "' added successfully.");
    }

    /**
     * Updates the fare between two existing stations.
     */
    private static void updateFare(Scanner sc) {
        System.out.print("Enter source station name: ");
        String source = sc.nextLine().trim();
        System.out.print("Enter destination station name: ");
        String destination = sc.nextLine().trim();
        System.out.print("Enter new fare: ");
        int newFare = sc.nextInt();
        sc.nextLine();

        boolean updated = false;
        for (String lineName : lineStations.keySet()) {
            List<String> stations = lineStations.get(lineName);
            int srcIdx = stations.indexOf(source);
            int destIdx = stations.indexOf(destination);

            if (srcIdx != -1 && destIdx != -1) {
                int[][] fares = lineFares.get(lineName);
                fares[srcIdx][destIdx] = newFare;
                fares[destIdx][srcIdx] = newFare;
                updated = true;
                break;
            }
        }

        if (updated) {
            System.out.println("Fare between '" + source + "' and '" + destination + "' updated to ₹" + newFare);
        } else {
            System.out.println("One or both stations not found.");
        }
    }

    // --- Passenger Functions ---
    /**
     * Handles the main passenger interaction for route planning.
     */
    private static void passengerMenu(Scanner sc) {
        while (true) {
            // Rebuild the graph for each search to ensure it uses the latest data
            MetroGraph metro = buildMetroGraph();
            displayStations();

            System.out.print("\nSelect source station number: ");
            int srcChoice = sc.nextInt();
            System.out.print("Select destination station number: ");
            int destChoice = sc.nextInt();
            sc.nextLine();

            String source = getStationName(srcChoice);
            String destination = getStationName(destChoice);

            if (source == null || destination == null) {
                System.out.println("⚠️ Invalid station selection.");
                continue;
            }

            if (source.equals(destination)) {
                System.out.println("⚠️ Source and destination cannot be the same.");
                continue;
            }

            // Find the shortest path using Dijkstra's algorithm
            List<String> path = new ArrayList<>();
            metro.dijkstra(source, destination, path);

            // Calculate the total fare based on the shortest path
            int totalFare = 0;
            for (int i = 0; i < path.size() - 1; i++) {
                totalFare += metro.getFare(path.get(i), path.get(i + 1));
            }

            // Display the user-friendly route and total fare
            List<String> displayPath = buildDisplayPath(source, destination);
            System.out.println("\n🗺️ Route: " + String.join(" -> ", displayPath));
            System.out.println("💰 Total Fare: ₹" + totalFare);

            // Provide a helpful message if an interchange is needed
            if (path.contains("Civil Court") && !source.equals("Civil Court") && !destination.equals("Civil Court")) {
                String srcLine = findLine(source);
                String dstLine = findLine(destination);
                if (srcLine != null && dstLine != null && !srcLine.equals(dstLine)) {
                    System.out.println("   ↳ Change at Civil Court to switch lines");
                }
            }

            System.out.print("\nDo you want to search another route? (yes/no): ");
            if (sc.nextLine().equalsIgnoreCase("no")) {
                break;
            }
        }
    }

    // --- Helper Methods ---
    /**
     * Builds and returns a new MetroGraph object with the current in-memory
     * data. This ensures the graph is always up-to-date with any admin changes.
     *
     * @return a MetroGraph object representing the current metro network.
     */
    private static MetroGraph buildMetroGraph() {
        MetroGraph metro = new MetroGraph();
        for (Map.Entry<String, List<String>> entry : lineStations.entrySet()) {
            String lineName = entry.getKey();
            List<String> stations = entry.getValue();
            int[][] fares = lineFares.get(lineName);
            int distance = lineDistances.get(lineName);
            for (int i = 0; i < stations.size(); i++) {
                for (int j = 0; j < stations.size(); j++) {
                    metro.addEdge(stations.get(i), stations.get(j), distance, fares[i][j]);
                }
            }
        }
        // Add a special edge for the interchange point at Civil Court
        metro.addEdge("Civil Court", "Civil Court", 0, 0);
        return metro;
    }

    /**
     * Prints all stations on each line with a combined numbering system.
     */
    private static void displayStations() {
        int counter = 1;
        for (Map.Entry<String, List<String>> entry : lineStations.entrySet()) {
            String lineName = entry.getKey();
            List<String> stations = entry.getValue();
            System.out.printf("\n=== %s Line (%s → %s) ===%n",
                    Character.toUpperCase(lineName.charAt(0)) + lineName.substring(1),
                    stations.get(0), stations.get(stations.size() - 1));

            for (int i = 0; i < stations.size(); i++) {
                System.out.printf("%d. %s%n", counter, stations.get(i));
                if (stations.get(i).equals("Civil Court")) {
                    System.out.println("   ↳ (Interchange with other lines)");
                }
                counter++;
            }
        }
    }

    /**
     * Converts a user's station number choice to a station name.
     *
     * @param choice The number entered by the user.
     * @return The corresponding station name, or null if invalid.
     */
    private static String getStationName(int choice) {
        int counter = 1;
        for (List<String> stations : lineStations.values()) {
            if (choice >= counter && choice < counter + stations.size()) {
                return stations.get(choice - counter);
            }
            counter += stations.size();
        }
        return null;
    }

    /**
     * Builds a user-friendly path for display, handling single-line and
     * interchange journeys.
     */
    private static List<String> buildDisplayPath(String source, String destination) {
        List<String> route = new ArrayList<>();
        String civil = "Civil Court";

        String srcLine = findLine(source);
        String destLine = findLine(destination);

        if (srcLine == null || destLine == null) {
            route.add(source);
            route.add(destination);
            return route;
        }

        if (srcLine.equals(destLine)) {
            List<String> stations = lineStations.get(srcLine);
            int from = stations.indexOf(source);
            int to = stations.indexOf(destination);
            addInclusive(route, stations, from, to, false);
        } else {
            List<String> srcStations = lineStations.get(srcLine);
            List<String> destStations = lineStations.get(destLine);

            int srcFrom = srcStations.indexOf(source);
            int srcCC = srcStations.indexOf(civil);
            addInclusive(route, srcStations, srcFrom, srcCC, false);

            int destCC = destStations.indexOf(civil);
            int destTo = destStations.indexOf(destination);
            addInclusive(route, destStations, destCC, destTo, true);
        }

        return route;
    }

    /**
     * Finds which line a given station belongs to.
     */
    private static String findLine(String station) {
        for (Map.Entry<String, List<String>> entry : lineStations.entrySet()) {
            if (entry.getValue().contains(station)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * A helper method to add a range of stations from an array to a list,
     * handling both forward and backward directions.
     */
    private static void addInclusive(List<String> out, List<String> arr, int from, int to, boolean skipFirst) {
        if (from == -1 || to == -1) {
            return;
        }
        if (from <= to) {
            for (int i = skipFirst ? from + 1 : from; i <= to; i++) {
                out.add(arr.get(i));
            }
        } else {
            for (int i = skipFirst ? from - 1 : from; i >= to; i--) {
                out.add(arr.get(i));
            }
        }
    }
}
