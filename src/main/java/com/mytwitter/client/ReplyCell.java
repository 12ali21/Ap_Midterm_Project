package com.mytwitter.client;

import com.mytwitter.client.controllers.CommentsViewController;
import com.mytwitter.client.controllers.ProfileController;
import com.mytwitter.tweet.Quote;
import com.mytwitter.tweet.Reply;
import com.mytwitter.tweet.Retweet;
import com.mytwitter.tweet.Tweet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Set;

import static com.mytwitter.client.TweetCell.setProfile;

public class ReplyCell extends ListCell<Reply> {

    private Stage currentStage;
    private Requester requester;
    private StackPane stackPane;

    public ReplyCell(Stage currentStage, Requester requester, StackPane stackPane) {
        this.currentStage = currentStage;
        this.requester = requester;
        this.stackPane = stackPane;
    }

    private Button usernameLabel = new Button();
    private Label typeLabel = new Label();
    private Label contentLabel = new Label();
    private Button likesButton = new Button();
    private Button repliesButton = new Button();
    private Button retweetsButton = new Button();
    private ImageView profileImg = new ImageView();

    @Override
    protected void updateItem(Reply reply, boolean empty) {
        super.updateItem(reply, empty);
        if (!empty && reply != null) {
            //TODO: get profile image

            setProfile(profileImg, "file:profiles/king.jpg");

            TweetCell.setUsername(usernameLabel, reply, currentStage);

            //TODO: show the replied to tweet
            Set<String> replyToUsernames = reply.getRepliedToUsernames();
            //TODO: hyperlink for each username
            String usernames = "";
            for (String name : replyToUsernames) {
                usernames += name + " ";
            }
            typeLabel.setText("Reply to " + usernames);
            contentLabel.setText(reply.getContent());

            contentLabel.setStyle("-fx-padding: 3 15 5 15");


            TweetCell.setLikeButton(likesButton, reply, requester);


            repliesButton.setText("💬" + reply.getReplyCount());
            repliesButton.setPrefWidth(40);
            repliesButton.setOnAction(event -> {
                CommentsViewController.createCommentsView(currentStage, requester, stackPane, reply.getTweetId());
            });

            TweetCell.setRetweetButton(retweetsButton, reply);


            HBox hBox = new HBox(likesButton, repliesButton, retweetsButton);
            hBox.setSpacing(20);
            setGraphic(new VBox(new HBox(profileImg, usernameLabel), typeLabel, contentLabel, hBox));

            getStyleClass().add("fx-cell-size: 50px;");
//                            CardController card = new CardController();
//                            card.updateDetails(item.getName(), item.getContent());
//                            setGraphic(card.getRootVBox());
//
            setStyle("-fx-border-color: black;");
        } else {
            setGraphic(null);
        }
    }
}
