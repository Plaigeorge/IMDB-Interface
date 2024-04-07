package com.example.temapoo2;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;


public class IMDB {
    private List<Regular> regularUsers;
    private List<Contributor> contributorUsers;
    private List<Admin> adminUsers;
    private List<Actor> actors;
    private List<Request> requests;
    private List<Movie> movies;
    private List<Series> series;
    private IMDB imdb;
    private Scanner scanner;
    private User currentUser;

    public IMDB() {
        this.regularUsers = new ArrayList<>();
        this.contributorUsers = new ArrayList<>();
        this.adminUsers = new ArrayList<>();
        this.actors = new ArrayList<>();
        this.requests = new ArrayList<>();
        this.movies = new ArrayList<>();
        this.series = new ArrayList<>();
        this.imdb = imdb;
        this.scanner = new Scanner(System.in);
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
    public void loadActors() {
        String filePath = "D:\\George\\actors.json";

        try {
            FileInputStream fis = new FileInputStream(filePath);
            JSONTokener tokener = new JSONTokener(fis);
            JSONArray jsonArray = new JSONArray(tokener);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String name = jsonObject.getString("name");
                JSONArray performancesArray = jsonObject.getJSONArray("performances");
                List<String> performances = jsonArrayToTitleList(performancesArray);
                String biography = jsonObject.optString("biography", "");

                Actor actor = new Actor(name, performances, biography);
                this.actors.add(actor);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    private List<String> jsonArrayToTitleList(JSONArray jsonArray) {
        List<String> titles = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject performance = jsonArray.getJSONObject(i);
            String title = performance.getString("title");
            titles.add(title);
        }
        return titles;
    }
    public void loadAccounts() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("D:\\George\\accounts.json")));
            JSONArray jsonArray = new JSONArray(content);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject userJson = jsonArray.getJSONObject(i);
                JSONObject info = userJson.getJSONObject("information");
                JSONObject credentials = info.getJSONObject("credentials");

                String email = credentials.getString("email");
                String password = credentials.getString("password");
                String userType = userJson.getString("userType");

                switch (userType) {
                    case "Regular":
                        Regular regularUser = new Regular(info.getString("name"), email, password);
                        regularUsers.add(regularUser);
                        break;
                    case "Contributor":
                        Contributor contributorUser = new Contributor(info.getString("name"), email, password);
                        contributorUsers.add(contributorUser);
                        break;
                    case "Admin":
                        Admin adminUser = new Admin(info.getString("name"), email, password);
                        adminUsers.add(adminUser);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProductions() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("D:\\George\\production.json")));
            JSONArray jsonArray = new JSONArray(content);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject productionJson = jsonArray.getJSONObject(i);
                String type = productionJson.getString("type");

                String title = productionJson.getString("title");
                String plot = productionJson.getString("plot");
                int releaseYear = productionJson.optInt("releaseYear");
                JSONArray genresJson = productionJson.getJSONArray("genres");
                List<String> genres = new ArrayList<>();
                for (int j = 0; j < genresJson.length(); j++) {
                    genres.add(genresJson.getString(j));
                }

                if (type.equals("Movie")) {
                    String duration = productionJson.getString("duration");
                    Movie movie = new Movie(title, genres, plot, duration, releaseYear);
                    movies.add(movie);
                } else if (type.equals("Series")) {
                    int numSeasons = productionJson.getInt("numSeasons");
                    JSONObject seasonsJson = productionJson.getJSONObject("seasons");
                    Series seriesObj = new Series(title, genres, plot, releaseYear, numSeasons);

                    for (String seasonKey : seasonsJson.keySet()) {
                        JSONArray seasonEpisodes = seasonsJson.getJSONArray(seasonKey);
                        for (int k = 0; k < seasonEpisodes.length(); k++) {
                            JSONObject episodeJson = seasonEpisodes.getJSONObject(k);
                            String episodeName = episodeJson.getString("episodeName");
                            String episodeDuration = episodeJson.getString("duration");
                            Episode episode = new Episode(episodeName, episodeDuration);
                            seriesObj.addEpisode(seasonKey, episode);
                        }
                    }
                    series.add(seriesObj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void run() {
        boolean exitApplication = false;

        while (!exitApplication) {
            loadAccounts();
            loadProductions();
            loadActors();
            System.out.println("Welcome to IMDB Terminal Interface");
            this.currentUser = null;

            if (authenticate()) {
                switch (currentUser.getClass().getSimpleName()) {
                    case "Regular":
                        showRegularMenu();
                        break;
                    case "Contributor":
                        showContributorMenu();
                        break;
                    case "Admin":
                        showAdminMenu();
                        break;
                }
            } else {
                System.out.println("Authentication failed.");
            }

            System.out.println("Do you want to exit the application? (yes/no)");
            String response = scanner.nextLine();
            if (response.equalsIgnoreCase("yes")) {
                exitApplication = true;
            }
        }
        System.out.println("Exiting IMDB Terminal Interface.");
    }

    public static void main(String[] args) {
        IMDB imdb = new IMDB();
        imdb.run();
    }
    private void showAdminMenu() {
        boolean exitMenu = false;

        while (!exitMenu) {
            System.out.println("\nAdmin Menu:");
            System.out.println("1. View production details");
            System.out.println("2. View actor details");
            System.out.println("3. View notifications");
            System.out.println("4. Search a film/series/actor");
            System.out.println("5. Add or delete production/actor from favorites");
            System.out.println("6. Add or delete a production/actor from system");
            System.out.println("7. View and resolve requests");
            System.out.println("8. Update production/actor details");
            System.out.println("9. Add or delete a user from the system");
            System.out.println("10. Logout");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewProductionDetails();
                    break;
                case "2":
                    viewActorDetails();
                    break;
                case "3":
                    viewNotifications();
                    break;
                case "4":
                    searchFilmSeriesActor();
                    break;
                case "5":
                    manageFavorites();
                    break;
                case "6":
                    manageProductionsAndActors();
                    break;
                case "7":
                    viewAndResolveRequests();
                    break;
                case "8":
                    updateProductionActorDetails();
                    break;
                case "9":
                    manageUsers();
                    break;
                case "10":
                    exitMenu = true;
                    System.out.println("Logging out...");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }
    private void showRegularMenu() {
        boolean exitMenu = false;

        while (!exitMenu) {
            System.out.println("\nRegular User Menu:");
            System.out.println("1. View production details");
            System.out.println("2. View actor details");
            System.out.println("3. View notifications");
            System.out.println("4. Search a film/series/actor");
            System.out.println("5. Add or remove from favorites");
            System.out.println("6. Add or delete a review");
            System.out.println("7. Create/Retract a Request");
            System.out.println("8. Logout");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewProductionDetails();
                    break;
                case "2":
                    viewActorDetails();
                    break;
                case "3":
                    viewNotifications();
                    break;
                case "4":
                    searchFilmSeriesActor();
                    break;
                case "5":
                    manageFavorites();
                    break;
                case "6":
                    addOrDeleteReview();
                    break;
                case "7":
                    manageRequests();
                    break;
                case "8":
                    exitMenu = true;
                    System.out.println("Logging out...");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }
    private void showContributorMenu() {
        boolean exitMenu = false;

        while (!exitMenu) {
            System.out.println("\nContributor Menu:");
            System.out.println("1. View production details");
            System.out.println("2. View actor details");
            System.out.println("3. View notifications");
            System.out.println("4. Search a film/series/actor");
            System.out.println("5. Add or remove from favorites");
            System.out.println("6. Add or retract a request");
            System.out.println("7. Add or delete a production/actor from the system");
            System.out.println("8. View and resolve requests");
            System.out.println("9. Update production/actor details");
            System.out.println("10. Logout");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewProductionDetails();
                    break;
                case "2":
                    viewActorDetails();
                    break;
                case "3":
                    viewNotifications();
                    break;
                case "4":
                    searchFilmSeriesActor();
                    break;
                case "5":
                    manageFavorites();
                    break;
                case "6":
                    manageRequests();
                    break;
                case "7":
                    manageProductionsAndActors();
                    break;
                case "8":
                    viewAndResolveRequests();
                    break;
                case "9":
                    updateProductionActorDetails();
                    break;
                case "10":
                    exitMenu = true;
                    System.out.println("Logging out...");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }
    private void updateProductionActorDetails() {
        System.out.println("Would you like to update a (1) Production or (2) Actor?");
        System.out.print("Enter choice (1 or 2): ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                updateProductionDetails();
                break;
            case "2":
                updateActorDetails();
                break;
            default:
                System.out.println("Invalid choice.");
                break;
        }
    }
    private void manageUsers() {
        boolean exitMenu = false;

        while (!exitMenu) {
            System.out.println("\nUser Management Menu:");
            System.out.println("1. Add a new user");
            System.out.println("2. Delete an existing user");
            System.out.println("3. Back to main menu");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    addUser();
                    break;
                case "2":
                    deleteUser();
                    break;
                case "3":
                    exitMenu = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }

    private void addUser() {
        System.out.print("Enter user type (Regular/Contributor/Admin): ");
        String userType = scanner.nextLine();
        System.out.print("Enter user name: ");
        String name = scanner.nextLine();
        System.out.print("Enter user email: ");
        String email = scanner.nextLine();
        System.out.print("Enter user password: ");
        String password = scanner.nextLine();

        switch (userType.toLowerCase()) {
            case "regular":
                Regular newRegular = new Regular(name, email, password);
                regularUsers.add(newRegular);
                break;
            case "contributor":
                Contributor newContributor = new Contributor(name, email, password);
                contributorUsers.add(newContributor);
                break;
            case "admin":
                Admin newAdmin = new Admin(name, email, password);
                adminUsers.add(newAdmin);
                break;
            default:
                System.out.println("Invalid user type.");
                break;
        }
        System.out.println("User added successfully.");
    }

    private void deleteUser() {
        System.out.print("Enter user email to delete: ");
        String email = scanner.nextLine();
        boolean userDeleted = regularUsers.removeIf(user -> user.getEmail().equals(email));
        if (!userDeleted) {
            userDeleted = contributorUsers.removeIf(user -> user.getEmail().equals(email));
        }
        if (!userDeleted) {
            userDeleted = adminUsers.removeIf(user -> user.getEmail().equals(email));
        }

        if (userDeleted) {
            System.out.println("User deleted successfully.");
        } else {
            System.out.println("User not found.");
        }
    }

    private void updateProductionDetails() {
        System.out.print("Enter the title of the Production: ");
        String title = scanner.nextLine();
        Production production = findProductionByTitle(title);

        if (production != null) {
            System.out.print("Enter new plot: ");
            String newPlot = scanner.nextLine();
            System.out.print("Enter new release year: ");
            int newYear = Integer.parseInt(scanner.nextLine());


            production.setDescription(newPlot);
            production.setYear(newYear);

            System.out.println("Production updated successfully.");
        } else {
            System.out.println("Production not found.");
        }
    }

    private void updateActorDetails() {
        System.out.print("Enter the name of the Actor: ");
        String name = scanner.nextLine();
        Actor actor = findActorByName(name);

        if (actor != null) {
            System.out.print("Enter new biography: ");
            String newBiography = scanner.nextLine();


            actor.setBiography(newBiography);

            System.out.println("Actor updated successfully.");
        } else {
            System.out.println("Actor not found.");
        }
    }


    private Actor findActorByName(String name) {
        for (Actor actor : actors) {
            if (actor.getName().equalsIgnoreCase(name)) {
                return actor;
            }
        }
        return null;
    }




    private void manageProductionsAndActors() {
        boolean exitMenu = false;

        while (!exitMenu) {
            System.out.println("\nProduction and Actor Management Menu:");
            System.out.println("1. Add a new production");
            System.out.println("2. Delete a production");
            System.out.println("3. Add a new actor");
            System.out.println("4. Delete an actor");
            System.out.println("5. Back to main menu");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    addNewProduction();
                    break;
                case "2":
                    deleteProduction();
                    break;
                case "3":
                    addNewActor();
                    break;
                case "4":
                    deleteActor();
                    break;
                case "5":
                    exitMenu = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }

    private void addNewProduction() {
        System.out.println("Add a New Production");
        System.out.print("Enter production type (Movie/Series): ");
        String type = scanner.nextLine().trim().toLowerCase();

        System.out.print("Enter production title: ");
        String title = scanner.nextLine().trim();

        System.out.print("Enter production plot: ");
        String plot = scanner.nextLine().trim();

        System.out.print("Enter production release year: ");
        int releaseYear = Integer.parseInt(scanner.nextLine().trim());


        System.out.print("Enter production genres (comma-separated): ");
        String genresInput = scanner.nextLine().trim();
        List<String> genres = Arrays.asList(genresInput.split("\\s*,\\s*"));

        if (type.equals("movie")) {
            System.out.print("Enter movie duration (in minutes): ");
            String duration = scanner.nextLine().trim();

            Movie newMovie = new Movie(title, genres, plot, duration, releaseYear);
            this.movies.add(newMovie);
            System.out.println("New movie added successfully.");
        } else if (type.equals("series")) {
            System.out.print("Enter number of seasons: ");
            int numSeasons = Integer.parseInt(scanner.nextLine().trim());

            Series newSeries = new Series(title, genres, plot, releaseYear, numSeasons);
            this.series.add(newSeries);
            System.out.println("New series added successfully.");
        } else {
            System.out.println("Invalid production type.");
        }
    }


    private void deleteProduction() {
        System.out.print("Enter production title to delete: ");
        String title = scanner.nextLine();


        boolean foundInMovies = movies.removeIf(movie -> movie.getName().equalsIgnoreCase(title));
        if (foundInMovies) {
            System.out.println("Movie '" + title + "' has been deleted.");
            return;
        }


        boolean foundInSeries = series.removeIf(serie -> serie.getName().equalsIgnoreCase(title));
        if (foundInSeries) {
            System.out.println("Series '" + title + "' has been deleted.");
            return;
        }


        System.out.println("Production '" + title + "' not found.");
    }


    private void addNewActor() {
        System.out.print("Enter actor name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter actor's biography: ");
        String biography = scanner.nextLine().trim();

        Actor newActor = new Actor(name, new ArrayList<>(), biography);

        StaffInterface staffInterface = new StaffInterface();
        boolean actorAdded = staffInterface.addActorSystem(newActor);

        if (actorAdded) {
            System.out.println("Actor '" + name + "' has been added to the system.");
        } else {
            System.out.println("Actor '" + name + "' already exists in the system.");
        }
    }




    private void deleteActor() {
        System.out.print("Enter actor name to delete: ");
        String name = scanner.nextLine().trim();

        StaffInterface staffInterface = new StaffInterface();
        boolean actorRemoved = staffInterface.removeActorSystem(name);

        if (actorRemoved) {
            System.out.println("Actor '" + name + "' has been successfully removed from the system.");
        } else {
            System.out.println("Actor '" + name + "' was not found in the system.");
        }
    }



    private void viewAndResolveRequests() {
        List<Request> allRequests = RequestsHolder.getInstance().getRequests();

        if (allRequests.isEmpty()) {
            System.out.println("No requests to resolve.");
            return;
        }

        System.out.println("Available Requests:");
        for (int i = 0; i < allRequests.size(); i++) {
            Request request = allRequests.get(i);
            System.out.println((i + 1) + ". " + request.getDescription() + " (Type: " + request.getType() + ")");
        }

        System.out.print("Enter the number of the request to resolve or 0 to return: ");
        int choice = Integer.parseInt(scanner.nextLine());

        if (choice > 0 && choice <= allRequests.size()) {
            Request selectedRequest = allRequests.get(choice - 1);
            resolveRequest(selectedRequest);
            allRequests.remove(selectedRequest);
            System.out.println("Request resolved.");
        } else if (choice == 0) {
            System.out.println("Returning to main menu.");
        } else {
            System.out.println("Invalid choice. Please try again.");
        }
    }

    private void resolveRequest(Request request) {
        System.out.println("Resolving request: " + request.getDescription());

        switch (request.getType()) {
            case DELETE_ACCOUNT:
                resolveDeleteAccountRequest(request);
                break;
            case ACTOR_ISSUE:
                resolveActorIssueRequest(request);
                break;
            case MOVIE_ISSUE:
                resolveMovieIssueRequest(request);
                break;
            case OTHERS:
                resolveOtherRequest(request);
                break;
        }
    }

    private void resolveDeleteAccountRequest(Request request) {
        System.out.println("Deleting account for user: " + request.getRequester());

        boolean isRemoved = regularUsers.removeIf(user -> user.getName().equals(request.getRequester()));
        if (!isRemoved) {
            isRemoved = contributorUsers.removeIf(user -> user.getName().equals(request.getRequester()));
        }
        if (!isRemoved) {
            isRemoved = adminUsers.removeIf(user -> user.getName().equals(request.getRequester()));
        }

        if (isRemoved) {
            System.out.println("Account successfully deleted for " + request.getRequester());
        } else {
            System.out.println("No account found for username: " + request.getRequester());
        }
    }

    private void resolveActorIssueRequest(Request request) {
        System.out.println("Resolving actor issue for: " + request.getTitleOrName());


        Optional<Actor> actorOptional = actors.stream()
                .filter(actor -> actor.getName().equalsIgnoreCase(request.getTitleOrName()))
                .findFirst();

        if (actorOptional.isPresent()) {
            Actor actor = actorOptional.get();
            System.out.print("Enter the updated biography for " + actor.getName() + ": ");
            String updatedBiography = scanner.nextLine();
            actor.setBiography(updatedBiography);

            System.out.println("Actor issue resolved: Biography updated for " + actor.getName());
        } else {
            System.out.println("Actor not found: " + request.getTitleOrName());
        }
    }


    private void resolveMovieIssueRequest(Request request) {
        System.out.println("Resolving movie issue for: " + request.getTitleOrName());


        Optional<Movie> movieOptional = movies.stream()
                .filter(movie -> movie.getName().equalsIgnoreCase(request.getTitleOrName()))
                .findFirst();

        if (movieOptional.isPresent()) {
            Movie movie = movieOptional.get();
            System.out.print("Enter the corrected duration for " + movie.getName() + " (in minutes): ");
            String updatedDuration = scanner.nextLine();
            movie.setDuration(updatedDuration);

            System.out.println("Movie issue resolved: Duration updated for " + movie.getName());
        } else {
            System.out.println("Movie not found: " + request.getTitleOrName());
        }
    }


    private void resolveOtherRequest(Request request) {
        System.out.println("Resolving request: " + request.getDescription());
    }
    private void manageRequests() {
        boolean exitMenu = false;

        while (!exitMenu) {
            System.out.println("\nRequest Management Menu:");
            System.out.println("1. Create a request");
            System.out.println("2. Retract a request");
            System.out.println("3. Back to main menu");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    createRequest();
                    break;
                case "2":
                    retractRequest();
                    break;
                case "3":
                    exitMenu = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }

    private void createRequest() {
        System.out.println("Select the type of your request:");
        for (RequestTypes type : RequestTypes.values()) {
            System.out.println(type.ordinal() + 1 + ". " + type.name());
        }
        System.out.print("Enter your choice: ");
        int choice = Integer.parseInt(scanner.nextLine());

        RequestTypes selectedType = RequestTypes.values()[choice - 1];
        String titleOrName = "";
        String description = "";

        switch (selectedType) {
            case DELETE_ACCOUNT:
                System.out.print("Enter the reason for deleting your account: ");
                break;
            case ACTOR_ISSUE:
                System.out.print("Enter the name of the actor: ");
                titleOrName = scanner.nextLine();
                System.out.print("Describe the issue with the actor: ");
                break;
            case MOVIE_ISSUE:
                System.out.print("Enter the title of the movie: ");
                titleOrName = scanner.nextLine();
                System.out.print("Describe the issue with the movie: ");
                break;
            case OTHERS:
                System.out.print("Describe your request: ");
                break;
        }

        description = scanner.nextLine();
        Request newRequest = new Request(selectedType, currentUser.getName(), titleOrName, description);
        RequestsHolder.getInstance().addRequest(newRequest);
        System.out.println("Your request has been created.");
    }


    private void retractRequest() {
        List<Request> userRequests = RequestsHolder.getInstance().getRequests().stream()
                .filter(r -> r.getRequester().equals(currentUser.getName()))
                .collect(Collectors.toList());

        if (userRequests.isEmpty()) {
            System.out.println("You have no active requests.");
            return;
        }

        System.out.println("Your Requests:");
        for (int i = 0; i < userRequests.size(); i++) {
            Request request = userRequests.get(i);
            System.out.println((i + 1) + ". " + request.getType() + " - " + request.getDescription());
        }

        System.out.print("Select the request to retract (enter number): ");
        int choice = Integer.parseInt(scanner.nextLine()) - 1;

        if (choice >= 0 && choice < userRequests.size()) {
            Request selectedRequest = userRequests.get(choice);
            RequestsHolder.getInstance().getRequests().remove(selectedRequest);
            System.out.println("Request retracted successfully.");
        } else {
            System.out.println("Invalid selection. Please try again.");
        }
    }


    private void viewProductionDetails() {
        System.out.print("Enter the name of the production: ");
        String productionName = scanner.nextLine();


        for (Movie movie : movies) {
            if (movie.getName().equalsIgnoreCase(productionName)) {
                displayMovieDetails(movie);
                return;
            }
        }


        for (Series series : series) {
            if (series.getName().equalsIgnoreCase(productionName)) {
                displaySeriesDetails(series);
                return;
            }
        }

        System.out.println("Production not found.");
    }

    private void displayMovieDetails(Movie movie) {
        System.out.println("Movie Details:");
        System.out.println("Title: " + movie.getName());
        System.out.println("Duration: " + movie.getDuration());
        System.out.println("Genres: " + String.join(", ", movie.getGenre()));
        System.out.println("Description: " + movie.getDescription());
        if (movie.getReviews().isEmpty()) {
            System.out.println("No reviews available.");
        } else {
            for (Review review : movie.getReviews()) {
                System.out.println("Review by " + review.getUser().getName() + ": " + review.getText());
            }
        }

    }

    private void displaySeriesDetails(Series series) {
        System.out.println("Series Details:");
        System.out.println("Title: " + series.getName());
        System.out.println("Number of Seasons: " + series.getNumberOfSeasons());
        System.out.println("Genres: " + String.join(", ", series.getGenre()));
        System.out.println("Description: " + series.getDescription());

    }


    private void viewActorDetails() {
        System.out.println("Enter the name of the actor: ");
        String actorName = scanner.nextLine();

        Optional<Actor> actorOptional = actors.stream()
                .filter(actor -> actor.getName().equalsIgnoreCase(actorName))
                .findFirst();

        if (actorOptional.isPresent()) {
            Actor actor = actorOptional.get();
            System.out.println("Name: " + actor.getName());
            System.out.println("Biography: " + actor.getBiography());
        } else {
            System.out.println("Actor not found.");
        }
    }

    private void viewNotifications() {
        if (currentUser != null) {
            List<String> notifications = currentUser.getNotifications();
            if (notifications.isEmpty()) {
                System.out.println("No notifications available.");
            } else {
                System.out.println("Notifications:");
                for (String notification : notifications) {
                    System.out.println("- " + notification);
                }
            }
        } else {
            System.out.println("No user is currently logged in.");
        }
    }


    private void searchFilmSeriesActor() {
        System.out.println("Searching for a film/series/actor...");
        System.out.print("Enter search query: ");
        String query = scanner.nextLine().toLowerCase();

        System.out.println("Search Results:");


        System.out.println("\nMovies:");
        for (Movie movie : movies) {
            if (movie.getName().toLowerCase().contains(query)) {
                displayMovieDetails(movie);

            }
        }


        System.out.println("\nSeries:");
        for (Series serie : series) {
            if (serie.getName().toLowerCase().contains(query)) {
                displaySeriesDetails(serie);

            }
        }


        System.out.println("\nActors:");
        for (Actor actor : actors) {
            if (actor.getName().toLowerCase().contains(query)) {
                System.out.println("Name: " + actor.getName());
                System.out.println("Biography: " + actor.getBiography());

            }
        }
    }


    private void manageFavorites() {
        System.out.println("Managing favorites...");

        boolean exitMenu = false;
        while (!exitMenu) {
            System.out.println("\nFavorite Management Menu:");
            System.out.println("1. View my favorites");
            System.out.println("2. Add to favorites");
            System.out.println("3. Remove from favorites");
            System.out.println("4. Back to main menu");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewFavorites();
                    break;
                case "2":
                    addToFavorites();
                    break;
                case "3":
                    removeFromFavorites();
                    break;
                case "4":
                    exitMenu = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }

    private void viewFavorites() {
        System.out.println("\nFavorite Productions:");
        currentUser.getFavoriteProductions().forEach(System.out::println);

        System.out.println("\nFavorite Actors:");
        currentUser.getFavoriteActors().forEach(System.out::println);
    }

    private void addToFavorites() {
        System.out.println("Would you like to add a (1) Movie/Series or (2) Actor to your favorites?");
        System.out.print("Enter choice (1 or 2): ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                System.out.print("Enter the title of the Movie/Series: ");
                String productionTitle = scanner.nextLine();
                if (currentUser instanceof Regular) {
                    ((Regular) currentUser).addFavoriteProduction(productionTitle);
                    System.out.println(productionTitle + " added to your favorite productions.");
                }
                break;
            case "2":
                System.out.print("Enter the name of the Actor: ");
                String actorName = scanner.nextLine();
                if (currentUser instanceof Regular) {
                    ((Regular) currentUser).addFavoriteActor(actorName);
                    System.out.println(actorName + " added to your favorite actors.");
                }
                break;
            default:
                System.out.println("Invalid choice.");
                break;
        }
    }

    private void removeFromFavorites() {
        System.out.println("Would you like to remove a (1) Movie/Series or (2) Actor from your favorites?");
        System.out.print("Enter choice (1 or 2): ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                System.out.print("Enter the title of the Movie/Series: ");
                String productionTitle = scanner.nextLine();
                if (currentUser instanceof Regular) {
                    ((Regular) currentUser).removeFavoriteProduction(productionTitle);
                    System.out.println(productionTitle + " removed from your favorite productions.");
                }
                break;
            case "2":
                System.out.print("Enter the name of the Actor: ");
                String actorName = scanner.nextLine();
                if (currentUser instanceof Regular) {
                    ((Regular) currentUser).removeFavoriteActor(actorName);
                    System.out.println(actorName + " removed from your favorite actors.");
                }
                break;
            default:
                System.out.println("Invalid choice.");
                break;
        }
    }


    private void addOrDeleteReview() {
        System.out.println("Would you like to (1) Add or (2) Delete a review?");
        System.out.print("Enter choice (1 or 2): ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                addReview();
                break;
            case "2":
                deleteReview();
                break;
            default:
                System.out.println("Invalid choice.");
                break;
        }
    }

    private void addReview() {
        System.out.print("Enter the title of the Movie/Series: ");
        String title = scanner.nextLine();
        Production production = findProductionByTitle(title);
        if (production != null) {
            System.out.print("Enter your review: ");
            String reviewText = scanner.nextLine();
            Review review = new Review(reviewText, currentUser);
            production.addReview(review);
            System.out.println("Your review has been added.");
        } else {
            System.out.println("Movie/Series not found.");
        }
    }

    private void deleteReview() {
        System.out.print("Enter the title of the Movie/Series: ");
        String title = scanner.nextLine();
        Production production = findProductionByTitle(title);
        if (production != null) {
            production.removeReview(currentUser);
            System.out.println("Your review has been deleted.");
        } else {
            System.out.println("Movie/Series not found.");
        }
    }

    private Production findProductionByTitle(String title) {
        for (Movie movie : movies) {
            if (movie.getName().equalsIgnoreCase(title)) {
                return movie;
            }
        }
        for (Series series : series) {
            if (series.getName().equalsIgnoreCase(title)) {
                return series;
            }
        }
        return null;
    }

    public User findUserByEmailAndPassword(String email, String password) {

        for (Regular user : regularUsers) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                return user;
            }
        }

        for (Contributor user : contributorUsers) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                return user;
            }
        }

        for (Admin user : adminUsers) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    private boolean authenticate() {
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();


        User user = findUserByEmailAndPassword(email, password);
        if (user != null) {
            this.currentUser = user;
            return true;
        }
        return false;
    }


    abstract class Production {
        private String name;
        private List<String> genre;
        private String description;
        String duration;
        private int year;
        private List<Actor> actors;
        private List<Review> reviews;
        private List<Rate> rates;
        private List<Episode> episodes;

        public Production(String name, List<String> genre, String description, String duration, int releaseYear) {
            this.name = name;
            this.genre = genre;
            this.description = description;
            this.duration = duration;
            this.year = year;
            this.actors = new ArrayList<>();
            this.reviews = new ArrayList<>();
            this.rates = new ArrayList<>();
            this.episodes = new ArrayList<>();
        }
        public void removeReview(User user) {
            reviews.removeIf(review -> review.getUser().equals(user));
        }

        public void addActor(Actor actor) {
            this.actors.add(actor);
        }

        public void addReview(Review review) {
            this.reviews.add(review);
        }

        public void addRate(Rate rate) {
            this.rates.add(rate);
        }

        public void addEpisode(Episode episode) {
            this.episodes.add(episode);
        }

        public String getName() {
            return name;
        }

        public List<String> getGenre() {
            return genre;
        }

        public String getDescription() {
            return description;
        }

        public String getDuration() {
            return duration;
        }

        public int getYear() {
            return year;
        }

        public List<Actor> getActors() {
            return actors;
        }

        public List<Review> getReviews() {
            return reviews;
        }

        public List<Rate> getRates() {
            return rates;
        }

        public List<Episode> getEpisodes() {
            return episodes;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setYear(int year) {
            this.year = year;
        }
    }

    class Movie extends Production {
        public Movie(String title, List<String> genres, String plot, String duration, int releaseYear) {
            super(title, genres, plot, duration, releaseYear);
        }
        public void setDuration(String newDuration) {
            this.duration = newDuration;
        }
    }

    class Series extends Production {
        private int numberOfSeasons;
        private Map<String, List<Episode>> seasons;

        public Series(String title, List<String> genres, String plot, int releaseYear, int numberOfSeasons) {
            super(title, genres, plot, String.valueOf(releaseYear), releaseYear);
            this.numberOfSeasons = numberOfSeasons;
            this.seasons = new HashMap<>();

        }

        public void addEpisode(String season, Episode episode) {
            this.seasons.computeIfAbsent(season, k -> new ArrayList<>()).add(episode);
        }

        public int getNumberOfSeasons() {
            return numberOfSeasons;
        }
    }

    class Episode {
        private int number;
        private String episodeName;
        private int duration;
        private List<Actor> guestStars;

        public Episode(String name, String duration) {

            this.episodeName = name;
            String numericDuration = duration.replaceAll("\\D+", "");
            this.duration = Integer.parseInt(numericDuration);
            this.guestStars = new ArrayList<>();
        }

        public String getName() {
            return  episodeName;
        }

        public int getDuration() {
            return duration;
        }
        public void addGuestStar(Actor actor) {
            this.guestStars.add(actor);
        }

        public int getNumber() {
            return number;
        }



        public List<Actor> getGuestStars() {
            return guestStars;
        }
    }

    static class RequestsHolder {
        private static RequestsHolder instance = null;
        private List<Request> requests;

        private RequestsHolder() {
            this.requests = new ArrayList<>();
        }

        public static RequestsHolder getInstance() {
            if (instance == null) {
                instance = new RequestsHolder();
            }
            return instance;
        }

        public void addRequest(Request request) {
            this.requests.add(request);
        }

        public List<Request> getRequests() {
            return requests;
        }
    }

    class Request {
        private String description;
        private RequestTypes type;
        private String requester;
        private String titleOrName;


        public Request(RequestTypes type, String requester, String titleOrName, String description) {
            this.type = type;
            this.requester = requester;
            this.titleOrName = titleOrName;
            this.description = description;
        }


        public Object getRequester() {
            return requester;
        }

        public RequestTypes getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public String getTitleOrName() {
            return titleOrName;
        }
    }

    class rating {
        private int rate;
        private User user;

        public rating(int rate, User user) {
            this.rate = rate;
            this.user = user;
        }

        public int getRate() {
            return rate;
        }

        public User getUser() {
            return user;
        }
    }

    class User {
        private String name;
        private String email;
        private String password;
        private List<Review> reviews;
        private List<Rate> rates;
        private List<String> notifications;
        private List<String> favoriteProductions;
        private List<String> favoriteActors;
        private ExperienceStrategy experienceStrategy;
        private int experience;

        public User(String name, String email, String password) {
            this.name = name;
            this.email = email;
            this.password = password;
            this.reviews = new ArrayList<>();
            this.rates = new ArrayList<>();
            this.notifications = new ArrayList<>();
            this.favoriteProductions = new ArrayList<>();
            this.favoriteActors = new ArrayList<>();
        }
        public void addFavoriteProduction(String productionTitle) {
            if (!favoriteProductions.contains(productionTitle)) {
                favoriteProductions.add(productionTitle);
            }
        }
        public void setExperienceStrategy(ExperienceStrategy experienceStrategy) {
            this.experienceStrategy = experienceStrategy;
        }
        public int getResolvedIssuesCount() {
            return 5;
        }

        public void updateExperience() {
            if (experienceStrategy != null) {
                int experienceGain = experienceStrategy.calculateExperience(this);
                this.experience += experienceGain;
            }
        }
        public void update(String message) {
            notifications.add(message);
        }
        public void removeFavoriteProduction(String productionTitle) {
            favoriteProductions.remove(productionTitle);
        }

        public void addFavoriteActor(String actorName) {
            if (!favoriteActors.contains(actorName)) {
                favoriteActors.add(actorName);
            }
        }

        public void removeFavoriteActor(String actorName) {
            favoriteActors.remove(actorName);
        }
        public List<String> getFavoriteProductions() {
            return favoriteProductions;
        }

        public List<String> getFavoriteActors() {
            return favoriteActors;
        }
        public List<String> getNotifications() {
            return notifications;
        }

        public void addNotification(String notification) {
            notifications.add(notification);
        }
        public String getPassword() {
            return password;
        }
        public void addReview(Review review) {
            this.reviews.add(review);
        }

        public void addRate(Rate rate) {
            this.rates.add(rate);
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public List<Review> getReviews() {
            return reviews;
        }

        public List<Rate> getRates() {
            return rates;
        }
    }

    class Regular extends User {
        public Regular(String name, String email, String password) {
            super(name, email, password);
        }
    }

    class staff extends User {
        public staff(String name, String email, String password) {
            super(name, email, password);
        }
    }

    class Admin extends User {
        private List<String> contributions;
        public Admin(String name, String email, String password) {
            super(name, email, password);
            this.contributions = new ArrayList<>();
        }
        public List<String> getContributions() {
            return contributions;
        }
    }

    class Contributor extends User {
        private List<String> contributions;
        public Contributor(String name, String email, String password) {
            super(name, email, password);
            this.contributions = new ArrayList<>();
        }
        public List<String> getContributions() {
            return contributions;
        }
    }

    public class Information {
        private String name, email, password, role;

        private Information(Builder builder) {
            this.name = builder.name;
            this.email = builder.email;
            this.password = builder.password;
            this.role = builder.role;
        }

        public class Builder {
            private String name, email, password, role;

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder email(String email) {
                this.email = email;
                return this;
            }



            public Information build() {
                return new Information(this);
            }
        }


    }

    class Credentials {
        private String email;
        private String password;

        public Credentials(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    class Review {
        private String text;
        private User user;

        public Review(String reviewText, User user) {
            this.text = reviewText;
            this.user = user;
        }

        public String getText() {
            return text;
        }

        public User getUser() {
            return user;
        }
    }

    class Rate {
        private int rate;
        private User user;

        public Rate(int rate, User user) {
            this.rate = rate;
            this.user = user;
        }

        public int getRate() {
            return rate;
        }

        public User getUser() {
            return user;
        }
    }


    class RequestsManager {
        private List<Request> requests;

        public RequestsManager() {
            this.requests = new ArrayList<>();
        }

        public void createRequest(Request r) {
            this.requests.add(r);
        }

        public void removeRequest(Request r) {
            this.requests.remove(r);
        }

        public List<Request> getRequests() {
            return this.requests;
        }
    }

    static class StaffInterface {
        private List<Production> productions;
        private List<Actor> actors;

        public StaffInterface() {
            this.productions = new ArrayList<>();
            this.actors = new ArrayList<>();
        }

        public void addProductionSystem(Production p) {
            this.productions.add(p);
        }
        public boolean addActorSystem(Actor a) {

            if (!actors.contains(a)) {
                actors.add(a);
                return true;
            }
            return false;
        }

        public void removeProductionSystem(String name) {
            this.productions.removeIf(p -> p.getName().equals(name));
        }

        public boolean removeActorSystem(String name) {
            return actors.removeIf(actor -> actor.getName().equalsIgnoreCase(name));
        }

        public void updateProduction(Production p) {
            for (int i = 0; i < productions.size(); i++) {
                if (productions.get(i).getName().equals(p.getName())) {
                    productions.set(i, p);
                    break;
                }
            }
        }

        public void updateActor(Actor a) {
            for (int i = 0; i < actors.size(); i++) {
                if (actors.get(i).getName().equals(a.getName())) {
                    actors.set(i, a);
                    break;
                }
            }
        }


    }

    enum AccountTypes {
        REGULAR,
        CONTRIBUTOR,
        ADMIN
    }

    enum genre {
        Action,
        Adventure,
        Animation,
        Biography,
        Comedy,
        Crime,
        Documentary,
        Drama,
        Family,
        Fantasy,
        FilmNoir,
        History,
        Horror,
        Music,
        Musical,
        Mystery,
        Romance,
        SciFi,
        ShortFilm,
        Sport,
        Superhero,
        Thriller,
        War,
        Western
    }

    enum RequestTypes {
        DELETE_ACCOUNT,
        ACTOR_ISSUE,
        MOVIE_ISSUE,
        OTHERS
    }

    enum ProductionTypes {
        MOVIE,
        SERIES
    }


    class Actor {
        private String name;
        private List<ProductionRole> roles;
        private String biography;

        public Actor(String name, List<String> performances, String biography) {
            this.name = name;
            this.roles = new ArrayList<>();
            this.biography = biography;
        }

        public void addRole(String productionName, ProductionTypes type) {
            roles.add(new ProductionRole(productionName, type));
        }


        public String getName() {
            return name;
        }

        public List<ProductionRole> getRoles() {
            return roles;
        }

        public String getBiography() {
            return biography;
        }

        public void setBiography(String updatedBiography) {
            this.biography = updatedBiography;
        }
    }

    class ProductionRole {
        private String productionName;
        private ProductionTypes type;

        public ProductionRole(String productionName, ProductionTypes type) {
            this.productionName = productionName;
            this.type = type;
        }

        public String getProductionName() {
            return productionName;
        }

        public ProductionTypes getType() {
            return type;
        }
    }
}
interface Observer {
    void update(String message);
}
class NotificationService {
    private List<Observer> observers = new ArrayList<>();

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    public void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }
}
 interface ExperienceStrategy {
     int calculateExperience(IMDB.User user);

 }

 abstract class ReviewExperienceStrategy implements ExperienceStrategy {
    @Override
    public int calculateExperience(IMDB.User user) {

        return user.getReviews().size() * 10;
    }
}
 abstract class IssueResolutionExperienceStrategy implements ExperienceStrategy {

    public int calculateExperience(IMDB.User user) {
        return user.getResolvedIssuesCount() * 20;
    }
}

