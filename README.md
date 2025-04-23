# Ap_Midterm_Project

A Java-based application that mimics the functionality of a social media platform, providing features like user authentication, timeline management, direct messaging, polls, and more.

## Features

- **User Authentication**: Includes signup and login functionality with JWT-based authentication.
- **Timeline Management**: Users can view their timeline with tweets, retweets, replies, and quotes.
- **Direct Messaging**: Users can send and receive private messages.
- **Polls**: Supports creating and answering polls.
- **Social Interactions**: Users can like, retweet, quote, follow, unfollow, block, and unblock other users.
- **Hashtag Support**: Users can search and interact with tweets containing specific hashtags.

## Project Structure

### Server-Side Components

- **`server/Config.java`**: Handles configuration properties such as decryption keys and database credentials.
- **`server/Server.java`**: The main entry point of the server application, sets up routes for various functionalities and manages the server lifecycle.
- **`server/JwtManager.java`**: Manages JWT creation and validation for secure authentication.
- **`server/database/Database.java`**: Handles all database interactions, including user management, tweet storage, and direct messages.
- **`server/contexthandlers/`**: Contains HTTP request handlers for various endpoints such as `/login`, `/signup`, `/edit`, and `/tweet`.

### Client-Side Components

- **`client/Client.java`**: The main entry point of the client application, which uses JavaFX for the UI.
- **`client/Requester.java`**: Handles HTTP requests to interact with the server for user authentication, timeline fetching, and social interactions.
- **`client/TweetCell.java`**: Manages the rendering of tweets, including retweets, replies, and polls, in the client UI.
- **`client/LikeEventHandler.java`**: Handles like/unlike actions for tweets.
- **`client/SeenChangeListener.java`**: Tracks changes in direct messages to update seen statuses.

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/12ali21/Ap_Midterm_Project.git
   ```
2. Open the project in your favorite Java IDE (e.g., IntelliJ IDEA or Eclipse).
3. Build and run the project.

## Usage

### Server
1. Navigate to the `server/` directory.
2. Run the `Server.java` file.
3. The server will start on `localhost:8000` and provide endpoints for user authentication, timeline management, and more.

### Client
1. Navigate to the `client/` directory.
2. Run the `Client.java` file.
3. Interact with the application through the JavaFX-based UI.

## Key Endpoints (Server)

- `/signup`: User registration.
- `/login`: User authentication.
- `/home`: Fetch user timeline.
- `/tweet`: Create, retweet, or reply to tweets.
- `/direct`: Manage direct messages.
- `/poll`: Interact with polls.
- `/search`: Search for hashtags or users.

## Technologies Used

- **Backend**: Java, HTTP server, JWT for authentication.
- **Frontend**: JavaFX for user interface.
- **Database**: Interacts with a relational database to store user and tweet data.
- **API Communication**: Java `HttpClient` for client-server communication.
