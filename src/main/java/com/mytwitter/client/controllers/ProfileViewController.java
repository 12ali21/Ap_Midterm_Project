package com.mytwitter.client.controllers;

import com.mytwitter.client.Requester;
import com.mytwitter.client.TweetCell;
import com.mytwitter.tweet.Bio;
import com.mytwitter.tweet.Tweet;
import com.mytwitter.user.User;
import com.mytwitter.user.UserProfile;
import com.mytwitter.util.ImageBase64;
import com.mytwitter.util.ProfileImage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;


import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class ProfileViewController implements Initializable {
    @FXML
    private HBox actionBox;

    @FXML
    private ImageView avatarImageView;

    @FXML
    private Text bioText;

    @FXML
    private Label birthdate;

    @FXML
    private HBox birthdateBox;

    @FXML
    private Button blockButton;

    @FXML
    private Button messageButton;

    @FXML
    private Label countryLabel;

    @FXML
    private Button editProfileButton;

    @FXML
    private Button followButton;

    @FXML
    private Hyperlink followersLink;

    @FXML
    private Hyperlink followingLink;

    @FXML
    private Label fullnameLabel;

    @FXML
    private ImageView headerImageView;

    @FXML
    private Button homeButton;

    @FXML
    private Label joindate;

    @FXML
    private ListView<Tweet> tweetList;

    @FXML
    private Label usernameLabel;

    @FXML
    private HBox webAddressBox;

    @FXML
    private Hyperlink webAddressLink;

    private Stage currentStage;
    private Requester requester = Requester.getRequester();

    private String username;

    @FXML
    private AnchorPane root;


    private final String BLOCK = "Block";
    private final String UNBLOCK = "Unblock";
    private final String FOLLOW = "Follow";
    private final String UNFOLLOW = "Unfollow";

    public ProfileViewController(Stage currentStage, String username) {
        this.username = username;
        this.currentStage = currentStage;

        Scene profileScene = null;
        FXMLLoader profileLoader = null;
        try {
            profileLoader = new FXMLLoader(getClass().getResource("/fxml/profile-view.fxml"));
            profileLoader.setController(this);

            this.root = profileLoader.load();
            profileScene = new Scene(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
        currentStage.setScene(profileScene);
        currentStage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        homeButton.setOnAction(this::clickOnHomeButton);

        UserProfile profile = requester.getProfile(username);
        if(profile == null){
            root = (AnchorPane) homeButton.getParent();
            for(int i = 0; i<8;i++){
                root.getChildren().remove(0);
            }

            root.getChildren().add(homeButton);
            Label blocked = new Label("You are blocked by this user");
            root.getChildren().add(blocked);
            AnchorPane.setTopAnchor(blocked, 300.);
            AnchorPane.setLeftAnchor(blocked, 250.);
            AnchorPane.setRightAnchor(blocked, 250.);

            AnchorPane.setTopAnchor(homeButton, 350.);
            AnchorPane.setLeftAnchor(homeButton, 300.);
            AnchorPane.setRightAnchor(homeButton, 300.);
        } else {


            if (username.equals(Requester.getUsername())) {
                // showing profile for current user
                actionBox.setVisible(false);
                editProfileButton.setOnAction(event -> {
                    new EditViewController(currentStage, profile);
                });
            } else {
                //showing profile for someone else
                editProfileButton.setVisible(false);
                followButton.setOnAction(this::clickOnFollowOrUnFollow);
                blockButton.setOnAction(this::clickOnBlockOrUnblock);
                messageButton.setOnAction(event -> {
                    new MessagesViewController(currentStage, username);
                });
                setFollowBlock(profile.getFollowed(), profile.getBlocked());
            }


            followersLink.setOnAction(event -> {
                new UsersListController(profile.getFollowers());
            });
            followingLink.setOnAction(event -> {
                new UsersListController(profile.getFollowings());
            });

            setAvatar(profile.getAvatar());
            setHeader(profile.getHeader());

            setUserInfo(profile.getUser());
            setBioInfo(profile.getBio());
            setFollowCounts(profile.getCountFollowers(), profile.getCountFollowings());
            setTweets(profile.getTweets());
        }
    }

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }


    public void setFollowBlock(boolean isFollowed, boolean isBlocked){
        if(isFollowed)
            followButton.setText(UNFOLLOW);
        else
            followButton.setText(FOLLOW);

        if(isBlocked)
            blockButton.setText(UNBLOCK);
        else
            blockButton.setText(BLOCK);

    }

    @FXML
    public void clickOnHomeButton(ActionEvent event) {
        currentStage.hide();
        new HomeController(currentStage);
    }

    private void setAvatar(String image){
        avatarImageView.setImage(ProfileImage.getAvatarImage(image));
    }
    private void setHeader(String image){
        headerImageView.setImage(ProfileImage.getHeaderImage(image));
    }

    private String parseDate(String dateString) throws ParseException {
        SimpleDateFormat formatToDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatToString = new SimpleDateFormat("MMMM d yyyy");;

        return formatToString.format(formatToDate.parse(dateString));
    }

    private void setUserInfo(User user){
        fullnameLabel.setText(user.getFullName());
        usernameLabel.setText(user.getUserName());
        try {
            if(user.getBirthdate()!=null) {
                birthdate.setText(parseDate(user.getBirthdate()));
            }
        } catch (ParseException e) {
            birthdateBox.setVisible(false);
        }

        try {
            joindate.setText(parseDate(user.getJoinDate()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void setBioInfo(Bio bio){
        String text = bio.getText();
        String webAddress = bio.getWebAddress();
        String location = bio.getLocation();

        if(text != null)
            bioText.setText(bio.getText());
        else
            bioText.setVisible(false);

        if(webAddress != null)
            webAddressLink.setText(bio.getWebAddress());
        else
            webAddressBox.setVisible(false);

        if(location != null)
            countryLabel.setText(bio.getLocation());
        else
            countryLabel.setVisible(false);
    }

    private void setFollowCounts(int followersCount, int followingCount){
        followersLink.setText(""+followersCount+" Followers");
        followingLink.setText(""+followingCount+" Following");
    }

    private void setTweets(List<Tweet> tweets){
        ObservableList<Tweet> observableList = FXCollections.observableArrayList();
        observableList.setAll(tweets);
        tweetList.setItems(observableList);
        tweetList.setCellFactory(tweetListView -> new TweetCell(currentStage, requester));
    }


    public void setRequester(Requester requester) {
        this.requester = requester;
    }

    public void clickOnBlockOrUnblock(ActionEvent event) {
        if (BLOCK.equals(blockButton.getText())) {
            requester.block(username);
            blockButton.setText(UNBLOCK);
            followButton.setText(FOLLOW);
            followButton.setDisable(true);
        } else {
            requester.unblock(username);
            blockButton.setText(BLOCK);
            followButton.setDisable(false);
        }
    }

    public void clickOnFollowOrUnFollow(javafx.event.ActionEvent event) {
        if (FOLLOW.equals(followButton.getText())) {
            requester.follow(username);
            followButton.setText(UNFOLLOW);
        } else {
            requester.unfollow(username);
            followButton.setText(FOLLOW);
        }
    }
}
