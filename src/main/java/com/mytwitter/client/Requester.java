package com.mytwitter.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mytwitter.direct.Direct;
import com.mytwitter.direct.Message;
import com.mytwitter.server.ServerGson;
import com.mytwitter.tweet.Reply;
import com.mytwitter.tweet.RequestTweet;
import com.mytwitter.tweet.Tweet;
import com.mytwitter.user.User;
import com.mytwitter.user.UserProfile;
import com.mytwitter.util.OutputType;
import com.mytwitter.util.TweetDeserializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class Requester {
    private String jwt;
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = ClientGson.getGson();
    private static Requester requester;
    private static String username;

    public static Requester getRequester() {
        return requester;
    }

    public static OutputType signup(User user) {
        String jsonRequest = gson.toJson(user);
        try {
            HttpRequest signupRequest = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8000/signup"))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();
            HttpResponse<String> GETResponse = httpClient.send(signupRequest, HttpResponse.BodyHandlers.ofString());
            if (GETResponse.statusCode() == 200) {
                return OutputType.SUCCESS;
            } else {
                String response = GETResponse.body();
                if (response.equals("Username already exists")) {
                    return OutputType.DUPLICATE_USERNAME;
                } else if (response.equals("Email already exists")) {
                    return OutputType.DUPLICATE_EMAIL;
                } else if (response.equals("Phone number already exists")) {
                    return OutputType.DUPLICATE_PHONENUMBER;
                }
            }

        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return OutputType.INVALID;
    }

    public static Requester login(String username, String password) {
        User user = new User(username, password);
        String jsonRequest = gson.toJson(user);
        String jwt;
        try {
            HttpRequest loginRequest = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8000/login"))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();
            HttpResponse<String> GETResponse = httpClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
            if (GETResponse.statusCode() == 200)
                jwt = GETResponse.headers().map().get("authorization").get(0);
            else
                return null;

        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        requester = new Requester(jwt);
        setUsername(username);
        return requester;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        Requester.username = username;
    }

    private Requester(String jwt) {
        this.jwt = jwt;
    }

    public String getJwt() {
        return jwt;
    }

    public List<Tweet> getTimeline() {
        // create a custom gson to be able to separate different subclasses of Tweet
        Gson timelineGson = ClientGson.getTimelineGson();


        try {
            HttpRequest GETRequest = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8000/home/timeline"))
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

    public List<Reply> getReplies(int tweetId) {
        try {
            HttpRequest GETRequest = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8000/home/comments/" + tweetId))
                    .GET()
                    .header("authorization", jwt)
                    .build();
            HttpResponse<String> GETResponse = httpClient.send(GETRequest, HttpResponse.BodyHandlers.ofString());

            // Define a type to be able to get a list of replies for a tweet
            Type type = new TypeToken<List<Reply>>(){}.getType();
            return ClientGson.getTimelineGson().fromJson(GETResponse.body(), type);

        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public UserProfile getProfile(String request) {
        try {
            HttpRequest GETRequest = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8000/user/" + request))
                    .GET()
                    .header("authorization", jwt)
                    .build();


            HttpResponse<String> GETResponse = httpClient.send(GETRequest, HttpResponse.BodyHandlers.ofString());

            if(GETResponse.statusCode() == 403)
                return null;

            return ClientGson.getTimelineGson().fromJson(GETResponse.body(), UserProfile.class);

        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getUserAvatar(String username) {
        UserProfile userProfile = getProfile(username+"/avatar");
        if(userProfile!=null)
            return userProfile.getAvatar();
        return null;
    }

    public OutputType usersOperationRequest(String username, String operation, boolean isDeleteRequest) {
        HttpRequest opRequest = null;
        try {
            HttpRequest.Builder opRequestBuilder = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8000/users/" + operation + "/" + username))
                    .header("authorization", jwt);
            if (isDeleteRequest) {
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

    private OutputType sendTweetRequest(URI uri, HttpRequest.BodyPublisher bodyPublisher) {

        try {
            HttpRequest tweetRequest = HttpRequest.newBuilder()
                    .uri(uri)
                    .POST(bodyPublisher)
                    .header("authorization", jwt)
                    .build();
            HttpResponse<String> response = httpClient.send(tweetRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200)
                return OutputType.SUCCESS;

        } catch (IOException | InterruptedException e) {
            return OutputType.FAILURE;
        }
        return OutputType.FAILURE;
    }

    public OutputType quote(String content, int quoteId) {
        RequestTweet requestTweet = new RequestTweet();
        requestTweet.setContent(content);

        URI uri = null;
        try {
            uri = new URI("http://localhost:8000/tweet/quote/" + quoteId);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(gson.toJson(requestTweet));

        return sendTweetRequest(uri, bodyPublisher);
    }

    public OutputType retweet(int retweetId) {
        URI uri = null;
        try {
            uri = new URI("http://localhost:8000/tweet/retweet/" + retweetId);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.noBody();

        return sendTweetRequest(uri, bodyPublisher);
    }

    public OutputType regularTweet(RequestTweet requestTweet) {
        if (requestTweet.getImage() == null)
            requestTweet.setImage("");
        URI uri = null;
        try {
            uri = new URI("http://localhost:8000/tweet/regular_tweet");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(gson.toJson(requestTweet));

        return sendTweetRequest(uri, bodyPublisher);
    }
    public OutputType comment(String content, int tweet_id){
        RequestTweet requestTweet = new RequestTweet();
        requestTweet.setContent(content);
        URI uri = null;
        try {
            uri = new URI("http://localhost:8000/tweet/comment/"+tweet_id);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(gson.toJson(requestTweet));

        return sendTweetRequest(uri, bodyPublisher);
    }

    private OutputType sendLikeRequest(boolean like, int tweetId){
        try {
            HttpRequest.Builder likeRequestBuilder = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8000/tweet/like/"+tweetId))
                    .header("authorization", jwt);
            if(like)
                likeRequestBuilder.POST(HttpRequest.BodyPublishers.noBody());
            else
                likeRequestBuilder.DELETE();

            HttpResponse<String> response = httpClient.send(likeRequestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200)
                return OutputType.SUCCESS;

        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return OutputType.FAILURE;
    }
    public OutputType like(int tweetId){
        return sendLikeRequest(true, tweetId);
    }
    public OutputType unlike(int tweetId){
        return sendLikeRequest(false, tweetId);
    }

    public ArrayList<UserProfile> search(String keyword){
        try {
            HttpRequest searchRequest = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8000/search/"+keyword))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(searchRequest, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            Type type = new TypeToken<List<UserProfile>>(){}.getType();
            return ClientGson.getGson().fromJson(body, type);

        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<Tweet> getHashtagTweets(String keyword) {
        try {
            HttpRequest searchRequest = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8000/hashtag/" + keyword))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(searchRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404)
                return null;
            else {
                String body = response.body();
                Type type = new TypeToken<List<Tweet>>(){}.getType();
                return ClientGson.getTimelineGson().fromJson(body, type);
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
    public  OutputType editProfile(UserProfile userProfile){
        String jsonRequest = gson.toJson(userProfile);

        try {
            HttpRequest editRequest = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8000/edit"))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .header("authorization",jwt)
                    .build();
            HttpResponse<String> GETResponse = httpClient.send(editRequest, HttpResponse.BodyHandlers.ofString());
            if(GETResponse.statusCode()==200) {
                return OutputType.SUCCESS;
            }


        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return OutputType.INVALID;
    }

    public ArrayList<Direct> getAllDirects(){
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8000/direct/all"))
                    .header("authorization", jwt)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() == 200){
                String body = response.body();
                Type type = new TypeToken<List<Direct>>(){}.getType();
                return ClientGson.getGson().fromJson(body, type);
            } else {
                throw new IOException();
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
    public ArrayList<Message> getAllMessages(String directUsername){
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8000/direct/messages/"+directUsername))
                    .header("authorization", jwt)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() == 200){
                String body = response.body();
                Type type = new TypeToken<List<Message>>(){}.getType();
                return ClientGson.getGson().fromJson(body, type);
            } else {
                throw new IOException();
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
    public OutputType setSeen(int id){
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8000/direct/seen/"+id))
                    .header("authorization", jwt)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() != 200){
                throw new IOException();
            }
            return OutputType.SUCCESS;
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
            return OutputType.FAILURE;
        }
    }

    public OutputType sendMessage(String receiver, String content){
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8000/direct/send_message/"+receiver))
                    .header("authorization", jwt)
                    .POST(HttpRequest.BodyPublishers.ofString(content))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() != 200){
                throw new IOException();
            }
            return OutputType.SUCCESS;
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
            return OutputType.FAILURE;
        }
    }

    public OutputType answerPoll(int answerId){
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8000/poll/answer/"+answerId))
                    .header("authorization", jwt)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() != 200){
                throw new IOException();
            }
            return OutputType.SUCCESS;
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
            return OutputType.FAILURE;
        }
    }



}
