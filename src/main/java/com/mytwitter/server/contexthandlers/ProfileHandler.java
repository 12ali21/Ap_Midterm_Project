package com.mytwitter.server.contexthandlers;

import com.google.gson.Gson;
import com.mytwitter.server.database.Database;
import com.mytwitter.server.ServerGson;
import com.mytwitter.tweet.Tweet;
import com.mytwitter.user.User;
import com.mytwitter.user.UserProfile;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;


public class ProfileHandler implements HttpHandler {
    Database databaseManager = Database.getManager();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String usernameToRequest = (String) exchange.getAttribute("username");
        Gson gson = ServerGson.getGson();
        if (exchange.getRequestMethod().equals("GET")) {
            InputStream in = exchange.getRequestBody();

            String requestUri = exchange.getRequestURI().toString();
            String[] segments = requestUri.split("/");
            String usernameToView = segments[2];

            UserProfile userProfile = new UserProfile();
            if (segments.length == 3) {

                if(databaseManager.checkBlocked(usernameToRequest, usernameToView)){
                    exchange.sendResponseHeaders(403, 0);
                    exchange.close();
                    return;
                }

                User user = databaseManager.getUser(usernameToView);
                if (user != null) {
                    ArrayList<User> followers = databaseManager.getFollowers(usernameToView);
                    ArrayList<User> followings = databaseManager.getFollowings(usernameToView);
                    ArrayList<UserProfile> followersProfile = new ArrayList<>();
                    ArrayList<UserProfile> followingsProfile = new ArrayList<>();

                    for (User u : followers) {
                        UserProfile followerProfile = new UserProfile();
                        followerProfile.setUser(u);
                        followerProfile.setAvatar(databaseManager.getAvatar(u.getUserName()));
                        followerProfile.setBio(databaseManager.getBio(u.getUserName()));
                        followersProfile.add(followerProfile);
                    }
                    for (User u : followings) {
                        UserProfile followingProfile = new UserProfile();
                        followingProfile.setUser(u);
                        followingProfile.setAvatar(databaseManager.getAvatar(u.getUserName()));
                        followingProfile.setBio(databaseManager.getBio(u.getUserName()));
                        followingsProfile.add(followingProfile);
                    }

                    userProfile.setUser(user);
                    userProfile.setAvatar(databaseManager.getAvatar(usernameToView));
                    userProfile.setHeader(databaseManager.getHeader(usernameToView));
                    userProfile.setCountFollowers(followers.size());
                    userProfile.setCountFollowings(followings.size());
                    userProfile.setTweets((ArrayList<Tweet>) databaseManager.getTweetsForUser(usernameToView));
                    if (!usernameToView.equals(usernameToRequest)) {
                        userProfile.setBlocked(databaseManager.checkBlocked(usernameToView, usernameToRequest));
                        userProfile.setFollowed(databaseManager.checkFollowed(usernameToView, usernameToRequest));
                    }
                    userProfile.setFollowers(followersProfile);
                    userProfile.setFollowings(followingsProfile);
                    userProfile.setBio(databaseManager.getBio(usernameToView));

                    exchange.sendResponseHeaders(200, 0);
                } else {
                    exchange.sendResponseHeaders(404, 0);
                }

                OutputStream out = exchange.getResponseBody();
                out.write(gson.toJson(userProfile).getBytes());
                out.close();
            } else if (Objects.equals(segments[3], "avatar")) {
                try {
                    userProfile.setAvatar(databaseManager.getAvatar(usernameToView));
                } catch (Exception e){
                    e.printStackTrace();
                }
                exchange.sendResponseHeaders(200, 0);
                OutputStream out = exchange.getResponseBody();
                out.write(gson.toJson(userProfile).getBytes());
                out.close();
            }


            exchange.close();
        }
    }
}
