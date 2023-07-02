package com.mytwitter.client.controllers;

import com.mytwitter.client.Requester;
import com.mytwitter.client.TweetCell;
import com.mytwitter.direct.Direct;
import com.mytwitter.direct.Message;
import com.mytwitter.user.User;
import com.mytwitter.user.UserProfile;
import com.mytwitter.util.ProfileImage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DirectViewController implements Initializable {

    @FXML
    ListView<Direct> listView;

    @FXML
    Button homeButton;

    @FXML
    Button refreshButton;

    private Stage currentStage;

    public DirectViewController(Stage currentStage) {
        this.currentStage = currentStage;

        Scene scene = null;
        FXMLLoader loader = null;
        try {
            loader = new FXMLLoader(getClass().getResource("/fxml/direct-view.fxml"));
            loader.setController(this);
            scene = new Scene(loader.load());

        } catch (IOException e) {
            e.printStackTrace();
        }
        currentStage.setScene(scene);
        currentStage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Requester requester = Requester.getRequester();

        ObservableList<Direct> observableList = FXCollections.observableArrayList();
        observableList.setAll(requester.getAllDirects());
        listView.setItems(observableList);

        homeButton.setOnAction(event -> {
            new HomeController(currentStage);
        });

        refreshButton.setOnAction(event -> {
            observableList.setAll(requester.getAllDirects());
        });




        listView.setCellFactory(directListView -> {
            ListCell<Direct> listCell = new ListCell<>() {

                ImageView avatarImageView = new ImageView();
                Hyperlink fullnameLink = new Hyperlink();
                Label usernameLabel = new Label();
                Label lastMessageLabel = new Label();

                @Override
                protected void updateItem(Direct direct, boolean b) {
                    super.updateItem(direct, b);
                    if (!b && direct != null) {

                        UserProfile directProfile = requester.getProfile(direct.getUsername());
                        if(directProfile==null){
                            setGraphic(new Label("BLOCKED!"));
                            return;
                        }

                        TweetCell.setProfileAvatar(avatarImageView, ProfileImage.getAvatarImage(directProfile.getAvatar()), 25);

                        User user = directProfile.getUser();
                        fullnameLink.setText(user.getFullName());
                        fullnameLink.setOnAction(event -> {
                            new ProfileViewController(currentStage, user.getUserName());
                        });

                        usernameLabel.setText("@" + user.getUserName());

                        Message lastMessage = direct.getLastMessage();
                        if(lastMessage.getSender().equals(Requester.getUsername())){
                            if(lastMessage.getSeen())
                                lastMessageLabel.setText(lastMessage.getContent()+ " ✔✔");
                            else
                                lastMessageLabel.setText("You: "+lastMessage.getContent() + " ✔");



                        } else {
                            if(lastMessage.getSeen())
                                lastMessageLabel.setText(lastMessage.getContent());
                            else
                                lastMessageLabel.setText("🔴 New Message: "+lastMessage.getContent());


                        }

                        VBox infoBox = new VBox(fullnameLink, usernameLabel, lastMessageLabel);
                        infoBox.setSpacing(7);

                        HBox directProfileBox = new HBox(avatarImageView, infoBox);
                        directProfileBox.setSpacing(10);

                        directProfileBox.setAlignment(Pos.TOP_LEFT);

                        setGraphic(directProfileBox);
                    }

                }
            };

            listCell.setOnMouseClicked(mouseEvent -> {
                String directUsername =  listCell.getItem().getUsername();
                if(requester.getUserAvatar(directUsername) == null)
                    return;
                new MessagesViewController(currentStage, directUsername);
            });
            return listCell;
        });


    }
}
