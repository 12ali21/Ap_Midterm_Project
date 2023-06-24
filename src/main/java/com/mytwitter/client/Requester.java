package com.mytwitter.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mytwitter.tweet.Tweet;
import com.mytwitter.user.User;
import com.mytwitter.user.UserProfile;
import com.mytwitter.util.OutputType;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class Requester {
    private String jwt;
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = ClientGson.getGson();

    public static void signup(User user){
        String jsonRequest = gson.toJson(user);
        try {
            HttpRequest signupRequest = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8000/signup"))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();
            HttpResponse<String> GETResponse = httpClient.send(signupRequest, HttpResponse.BodyHandlers.ofString());
            // TODO: check response codes (duplicates)

        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Requester login(String username, String password){
        User user = new User(username, password);
        String jsonRequest = gson.toJson(user);
        String jwt;
        try {
            HttpRequest loginRequest = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8000/login"))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();
            HttpResponse<String> GETResponse = httpClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
            if(GETResponse.statusCode() == 200)
                jwt = GETResponse.headers().map().get("authorization").get(0);
            else
                return null;

        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        return new Requester(jwt);
    }

    private Requester(String jwt) {
        this.jwt = jwt;
    }

    public String getJwt() {
        return jwt;
    }

    public List<Tweet> getTimeline(){
        // create a custom gson to be able to separate different subclasses of Tweet
        Gson timelineGson = new GsonBuilder()
                .registerTypeAdapter(Tweet.class, new TweetDeserializer())
                .create();

        try {
            HttpRequest GETRequest = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8000/home"))
                    .GET()
                    .header("authorization", jwt)
                    .build();
            HttpResponse<String> GETResponse = httpClient.send(GETRequest, HttpResponse.BodyHandlers.ofString());

            // Define a type to be able to get a list of tweets
            Type type = new TypeToken<List<Tweet>>(){}.getType();
            return timelineGson.fromJson(GETResponse.body(), type);

        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public UserProfile getProfile(String username){
        // create a custom gson to be able to separate different subclasses of Tweet


        try {
            HttpRequest GETRequest = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8000/user/"+username))
                    .GET()
                    .build();

            HttpResponse<String> GETResponse = httpClient.send(GETRequest, HttpResponse.BodyHandlers.ofString());
            return ClientGson.getTimelineGson().fromJson(GETResponse.body(), UserProfile.class);

        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public OutputType usersOperationRequest(String username, String operation, boolean isDeleteRequest){
        HttpRequest opRequest = null;
        try {
            HttpRequest.Builder opRequestBuilder = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8000/users/" + operation + "/" + username))
                    .header("authorization", jwt);
            if(isDeleteRequest){
                opRequestBuilder.DELETE();
            } else {
                opRequestBuilder.POST(HttpRequest.BodyPublishers.noBody());
            }
            opRequest = opRequestBuilder.build();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        try {
            HttpResponse<String> response = httpClient.send(opRequest, HttpResponse.BodyHandlers.ofString());
            return OutputType.parseCode(response.statusCode());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public OutputType follow(String username) {
        return usersOperationRequest(username, "follow", false);
    }
    public OutputType unfollow(String username) {
        return usersOperationRequest(username, "follow", true);
    }
    public OutputType block(String username) {
        return usersOperationRequest(username, "block", false);
    }
    public OutputType unblock(String username) {
        return usersOperationRequest(username, "block", true);
    }

}