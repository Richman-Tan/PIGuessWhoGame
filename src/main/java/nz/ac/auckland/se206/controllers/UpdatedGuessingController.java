package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.chat.openai.Choice;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.prompts.PromptEngineering;
import nz.ac.auckland.se206.states.GameStarted;

/**
 * Controller class for the updated guessing scene.
 *
 * <p>This class is responsible for handling user input and updating the view for the updated
 * guessing scene. It manages the user interface components and responds to user interactions in the
 * updated guessing scene, including the selection of the culprit and the provision of an
 * explanation for the guess.
 */
public class UpdatedGuessingController {

  private static final String confirmed =
      GameStarted.class.getClassLoader().getResource("sounds/confirmed.mp3").toExternalForm();
  private static final String gameOver =
      GameStarted.class.getClassLoader().getResource("sounds/gameover.mp3").toExternalForm();
  private static final String culprit =
      GameStarted.class.getClassLoader().getResource("sounds/clickOnCulprit.mp3").toExternalForm();
  private static final String explanation =
      GameStarted.class
          .getClassLoader()
          .getResource("sounds/provideExplanation.mp3")
          .toExternalForm();
  private static final String correctGuess =
      GameStarted.class.getClassLoader().getResource("sounds/guessCorrectly.mp3").toExternalForm();
  private static final String incorrectGuess =
      GameStarted.class
          .getClassLoader()
          .getResource("sounds/guessIncorrectly.mp3")
          .toExternalForm();

  // Static methods
  private static String loadTemplate(URI filePath) throws IOException {
    return new String(Files.readAllBytes(Paths.get(filePath)));
  }

  // Instance fields
  @FXML private AnchorPane rootPane;
  @FXML private Pane guessPhotoPane;
  @FXML private Pane verifyCulpritPane;
  @FXML private Rectangle recSuspect1;
  @FXML private Rectangle recSuspect2;
  @FXML private Rectangle recSuspect3;
  @FXML private ImageView confirmedSuspect1;
  @FXML private ImageView confirmedSuspect2;
  @FXML private ImageView confirmedSuspect3;
  @FXML private ImageView clue1foundimg; // gardening tool
  @FXML private ImageView clue2foundimg; // phone
  @FXML private ImageView clue3foundimg; // note
  @FXML private Label culpritLabel;
  @FXML private Button confirmCulpritButton;
  @FXML private ImageView staticlayer;
  @FXML private TextArea userExplanation;
  @FXML private Button btnReplay;
  @FXML private ProgressIndicator progressIndicator;
  @FXML private ImageView staticImage;
  @FXML private ImageView background;
  @FXML private Pane gameOverPane;
  @FXML private Rectangle gameOverRectangle;
  @FXML private Label correctGuessLbl;
  @FXML private Label incorrectGuessLbl;
  @FXML private Label lbltimer;
  @FXML private Label gameOverTxt;
  @FXML private Label reviewLbl;
  @FXML private TextArea feedbackField;
  @FXML private Label lblStory;
  @FXML private Pane labelPane;
  @FXML private Button confirmExplanationButton;
  @FXML private Rectangle recSus1;
  @FXML private Rectangle recSus2;
  @FXML private Rectangle recSus3;
  @FXML private Label incorrectGuessLbl2;
  @FXML protected SVGPath volumeOff;
  @FXML protected SVGPath volumeUp;
  @FXML protected SVGPath volumeUpStroke;

  private boolean playedConfirmCulprit = false;
  private boolean playedConfirmEx = false;
  private String text = "Who is the culprit . . .";
  private ImageView staticimg1;
  private Timeline timeline;
  private int textIndex = 0;
  private int visibleNodeIndex = 0;
  private ArrayList<Object> list;
  private boolean guess = false;
  private MediaPlayer mediaPlayer;
  private MediaPlayer gameOverPlayer;
  private MediaPlayer culpritPlayer;
  private MediaPlayer explanationPlayer;
  private MediaPlayer guessPlayer;
  private GameStateContext context = GameStateContext.getInstance();
  private TimerModel countdownTimer;
  private ChatCompletionRequest chatCompletionRequest;
  private String guessedsuspect;
  private BooleanProperty volumeSettingProperty =
      SharedVolumeControl.getInstance().volumeSettingProperty();

  /**
   * Initializes the guessing view when the controller is loaded.
   *
   * <p>This method sets up the initial state of the chat interface, including disabling the confirm
   * culprit button. It also checks for specific conditions in the game state, such as whether a
   * garden tool has been found, and updates the visibility of relevant UI elements accordingly.
   * This method is called automatically by the JavaFX framework when the associated FXML file is
   * loaded.
   *
   * @throws URISyntaxException if the URI syntax is incorrect during initialization.
   * @throws IOException if there is an error loading resources or files.
   * @throws ApiProxyException if there is an error communicating with the API proxy.
   */
  @FXML
  public void initialize() {
    confirmCulpritButton.setDisable(true);

    if (context.isGardenToolFound()) {
      clue1foundimg.setVisible(true);
    }
    if (context.isPhoneFound()) {
      clue2foundimg.setVisible(true);
    }
    if (context.isNoteFound()) {
      clue3foundimg.setVisible(true);
    }
    try {
      checkVolumeIcon();
    } catch (IOException e) {
      e.printStackTrace();
    }

    countdownTimer = SharedTimerModel.getInstance().getTimer();
    countdownTimer.reset(61);
    countdownTimer.start();
    lbltimer.textProperty().bind(countdownTimer.timeStringProperty());

    // add listener for the label when it shows "over"
    lbltimer
        .textProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              if (newValue.equals("Over!")) {
                System.out.println("Game Over");
                countdownTimer.stop();
                confirmCulpritButton.setDisable(true);
                confirmCulpritButton.setOpacity(0.7);
                confirmExplanationButton.setDisable(true);
                confirmExplanationButton.setOpacity(0.7);
                verifyCulpritPane.setVisible(false);
                staticImage.setVisible(false);
                guessPhotoPane.setVisible(false);
                gameOverRectangle.setVisible(true);
                gameOverPane.setVisible(true);
                showGameOver();
              }
            });

    // play the audio
    Media sound = new Media(culprit);
    culpritPlayer = new MediaPlayer(sound);
    // Bind the volume property for culpritPlayer
    culpritPlayer
        .volumeProperty()
        .bind(
            Bindings.createDoubleBinding(
                () -> volumeSettingProperty.get() ? 1.0 : 0.0, volumeSettingProperty));
    culpritPlayer.play();
    warpText(); // Start the text animation
    createImageView(); // Create the ImageView and add it to the scene

    Media confirmedSound = new Media(confirmed);
    mediaPlayer = new MediaPlayer(confirmedSound);
    // Use a DoubleBinding to bind the mediaPlayer volume property
    mediaPlayer
        .volumeProperty()
        .bind(
            Bindings.createDoubleBinding(
                () -> volumeSettingProperty.get() ? 1.0 : 0.0, volumeSettingProperty));
  }

  /**
   * Method to handle when the hover over the image of the suspect.
   *
   * @param event the mouse event.
   * @throws IOException if there is an error loading the FXML file.
   */
  @FXML
  private void hoverImageGma(MouseEvent event) throws IOException {
    recSuspect2.setVisible(true);
    recSuspect2.setMouseTransparent(true);
  }

  /**
   * Method to handle when the hover over the image of the suspect.
   *
   * @param event the mouse event.
   * @throws IOException if there is an error loading the FXML file.
   */
  @FXML
  private void hoverImageUncle(MouseEvent event) throws IOException {
    recSuspect1.setVisible(true);
    recSuspect1.setMouseTransparent(true);
  }

  /**
   * Method to handle when the hover over the image of the suspect.
   *
   * @param event the mouse event.
   * @throws IOException if there is an error loading the FXML file.
   */
  @FXML
  private void hoverImageSon(MouseEvent event) throws IOException {
    recSuspect3.setVisible(true);
    recSuspect3.setMouseTransparent(true);
  }

  /**
   * Method to handle when the hover over the image of the suspect is off.
   *
   * @param event the mouse event.
   * @throws IOException if there is an error loading the FXML file.
   */
  @FXML
  private void offHoverImageSon(MouseEvent event) {
    recSuspect3.setVisible(false);
  }

  /**
   * Method to handle when the hover over the image of the suspect is off.
   *
   * @param event the mouse event.
   * @throws IOException if there is an error loading the FXML file.
   */
  @FXML
  private void offHoverImageGma(MouseEvent event) {
    recSuspect2.setVisible(false);
  }

  /**
   * Method to handle when the hover over the image of the suspect is off.
   *
   * @param event the mouse event.
   * @throws IOException if there is an error loading the FXML file.
   */
  @FXML
  private void offHoverImageUncle(MouseEvent event) {
    recSuspect1.setVisible(false);
  }

  /**
   * Method to handle when the image of the suspect is clicked.
   *
   * @param event the mouse event.
   * @throws IOException if there is an error loading the FXML file.
   */
  @FXML
  private void clickedImageUncle(MouseEvent event) throws IOException {
    guessedsuspect = "Uncle";
    // set the red rectangle to visible but the other culprits to invisible
    recSus1.setVisible(true);
    recSus2.setVisible(false);
    recSus3.setVisible(false);
    confirmCulpritButton.setDisable(false);
    confirmCulpritButton.setOpacity(1);
  }

  /**
   * Method to handle when the image of the suspect is clicked.
   *
   * @param event the mouse event.
   * @throws IOException if there is an error loading the FXML file.
   */
  @FXML
  private void clickedImageSon(MouseEvent event) throws IOException {
    guessedsuspect = "Grandson";
    // set the red rectangle to visible but the other culprits to invisible
    recSus3.setVisible(true);
    recSus2.setVisible(false);
    recSus1.setVisible(false);
    confirmCulpritButton.setDisable(false);
    confirmCulpritButton.setOpacity(1);
  }

  /**
   * Method to handle when the image of the suspect is clicked.
   *
   * @param event the mouse event.
   * @throws IOException if there is an error loading the FXML file.
   */
  @FXML
  private void clickedImageGma(MouseEvent event) throws IOException {
    guessedsuspect = "Grandma";
    // set the red rectangle to visible but the other culprits to invisible
    recSus2.setVisible(true);
    recSus3.setVisible(false);
    recSus1.setVisible(false);
    confirmCulpritButton.setDisable(false);
    confirmCulpritButton.setOpacity(1);
  }

  /**
   * Handles the action when the confirm culprit button is clicked.
   *
   * <p>This method is triggered by a mouse event and initiates the confirmation process for the
   * selected culprit. It plays a confirmation sound and performs any necessary updates or
   * transitions related to the culprit confirmation. Additionally, it manages the volume settings
   * for the audio playback.
   *
   * @param event the mouse event that triggered the confirmation action.
   * @throws IOException if there is an error loading the FXML file or other resources required for
   *     the confirmation process.
   */
  @FXML
  private void confirmCulprit(MouseEvent event) throws IOException {
    Media confirmedSound = new Media(confirmed);
    mediaPlayer = new MediaPlayer(confirmedSound);
    // Use a DoubleBinding to bind the volume property
    mediaPlayer
        .volumeProperty()
        .bind(
            Bindings.createDoubleBinding(
                () ->
                    volumeSettingProperty.get()
                        ? 1.0
                        : 0.0, // Use 1.0 for full volume and 0.0 for mute
                volumeSettingProperty));

    Media sound2 = new Media(explanation);
    explanationPlayer = new MediaPlayer(sound2);
    // Create a DoubleBinding and bind it to the explanationPlayer's volume property
    explanationPlayer
        .volumeProperty()
        .bind(
            Bindings.createDoubleBinding(
                () ->
                    volumeSettingProperty.get() ? 1.0 : 0.0, // Full volume if true, otherwise mute
                volumeSettingProperty));
    if (!playedConfirmCulprit) {
      mediaPlayer.play();
      playedConfirmCulprit = true;
      mediaPlayer.setOnEndOfMedia(
          () -> {
            // open new pane to confirm culprit
            guessPhotoPane.setVisible(false);
            verifyCulpritPane.setVisible(true);
            // play gif
            staticImage.setVisible(false);
            playgif();
            explanationPlayer.play();
          });
    }

    switch (guessedsuspect) {
      case "Uncle":
        confirmedSuspect1.setVisible(true);
        culpritLabel.setText("The Brother");
        break;
      case "Grandma":
        guess = true;
        confirmedSuspect2.setVisible(true);
        culpritLabel.setText("The Widow");
        break;
      case "Grandson":
        confirmedSuspect3.setVisible(true);
        culpritLabel.setText("The Grandson");
        break;
    }
  }

  /**
   * Method to handle when the volume is turned off.
   *
   * @throws IOException if there is an error loading the FXML file.
   */
  @FXML
  protected void turnVolumeOff() throws IOException {
    volumeOff.setVisible(true);
    volumeUp.setVisible(false);
    volumeUpStroke.setVisible(false);
    SharedVolumeControl.getInstance().setVolumeSetting(false);
  }

  /**
   * Handles the action of turning the volume on.
   *
   * <p>This method updates the visibility of the volume control icons to reflect that the volume
   * has been turned on. It hides the volume-off icon and shows the volume-on icon, ensuring that
   * the user's settings are properly reflected.
   *
   * @throws IOException if there is an error loading the FXML file associated with the volume
   *     control.
   */
  @FXML
  protected void turnVolumeOn() throws IOException {
    volumeOff.setVisible(false);
    volumeUp.setVisible(true);
    volumeUpStroke.setVisible(true);
    SharedVolumeControl.getInstance().setVolumeSetting(true);
  }

  /**
   * Method to adjust the position of the volume button.
   *
   * @throws IOException if there is an error loading the FXML file.
   */
  private void checkVolumeIcon() throws IOException {
    if (SharedVolumeControl.getInstance().getVolumeSetting()) {
      turnVolumeOn();
    } else {
      turnVolumeOff();
    }
  }

  /**
   * Method to handle when the confirm explanation button is clicked.
   *
   * @param event the mouse event.
   * @throws IOException if there is an error loading the FXML file.
   */
  @FXML
  private void confirmExplanation(MouseEvent event) throws IOException {
    // play sound
    Media confirmedSound = new Media(confirmed);
    mediaPlayer = new MediaPlayer(confirmedSound);
    // Create a DoubleBinding and bind it to the mediaPlayer's volume property
    mediaPlayer
        .volumeProperty()
        .bind(
            Bindings.createDoubleBinding(
                () ->
                    volumeSettingProperty.get() ? 1.0 : 0.0, // Full volume if true, otherwise mute
                volumeSettingProperty));

    if (!playedConfirmEx) {
      // if (SharedVolumeControl.getInstance().getVolumeSetting()) {
      mediaPlayer.play();
      playedConfirmEx = true;
      mediaPlayer.setOnEndOfMedia(
          () -> {
            // open new pane to confirm explanation
            verifyCulpritPane.setVisible(false);
            staticimg1.setVisible(false);
            gameOverRectangle.setVisible(true);
            gameOverPane.setVisible(true);
            showGameOver();
            // remove the timer from the screen if user has been moved to game over state
            labelPane.setVisible(false);
            adjustVolumeButtonPosition();

            // stop the timer
            countdownTimer.stop();
          });
    }
  }

  /**
   * Method to handle when the game over pane is shown.
   *
   * @param event the mouse event.
   * @throws IOException if there is an error loading the FXML file.
   */
  @FXML
  private void showGameOver() {
    list = new ArrayList<>();
    if (guess) {
      list.add(correctGuessLbl);
      list.add(reviewLbl);
      list.add(feedbackField);
      Media sound = new Media(correctGuess);
      guessPlayer = new MediaPlayer(sound);
    } else {
      list.add(incorrectGuessLbl);
      list.add(incorrectGuessLbl2);
      list.add(btnReplay);
      System.out.println("List:" + list);

      Media sound = new Media(incorrectGuess);
      guessPlayer = new MediaPlayer(sound);
    }
    // Bind the volume property for guessPlayer
    guessPlayer
        .volumeProperty()
        .bind(
            Bindings.createDoubleBinding(
                () -> volumeSettingProperty.get() ? 1.0 : 0.0, volumeSettingProperty));

    // Run a seperate thread to play the sound
    new Thread(
            () -> {
              // 0.5 sec delay before playing the sound
              try {
                Thread.sleep(1500);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              onSendMessage();
            })
        .start();

    Media sound = new Media(gameOver);
    gameOverPlayer = new MediaPlayer(sound);

    // Use a boolean binding to control the volume based on the shared volume setting
    BooleanProperty isVolumeOn = SharedVolumeControl.getInstance().volumeSettingProperty();
    gameOverPlayer
        .volumeProperty()
        .bind(Bindings.createDoubleBinding(() -> isVolumeOn.get() ? 1.0 : 0.0, isVolumeOn));

    // Play the game over sound
    gameOverPlayer.play();
    gameOverTxt.setVisible(true);

    // Set up an event handler for when the media ends
    gameOverPlayer.setOnEndOfMedia(
        () -> {
          // Play another sound once the game over sound ends
          guessPlayer.play();

          // Create a timeline to control the visibility of nodes in the list
          timeline =
              new Timeline(
                  new KeyFrame(
                      Duration.seconds(0.7),
                      event -> {
                        if (visibleNodeIndex < list.size()) {
                          Object obj = list.get(visibleNodeIndex);
                          if (obj instanceof Node) {
                            ((Node) obj).setVisible(true); // Make the current element visible
                          }
                          visibleNodeIndex++;
                        } else {
                          timeline.stop(); // Stop the timeline when all elements are shown
                        }
                      }));

          // Set the timeline to run indefinitely until manually stopped
          timeline.setCycleCount(Timeline.INDEFINITE);
          timeline.play(); // Start the animation
        });
  }

  /**
   * Animates the display of text by revealing it one character at a time.
   *
   * <p>This method creates a timeline that progressively updates the text displayed in the {@code
   * lblStory} label. Each character is revealed at a specified interval, and once the full text is
   * displayed, the animation stops and begins to flash the last dot.
   *
   * @throws IOException if there is an error loading related resources (if applicable).
   */
  private void warpText() {
    timeline =
        new Timeline(
            new KeyFrame(
                Duration.seconds(0.2),
                event -> {
                  if (textIndex < text.length()) {
                    lblStory.setText(text.substring(0, textIndex + 1));
                    textIndex++;
                  } else {
                    timeline.stop();
                    flashLastDot(); // Start flashing the last dot
                  }
                }));

    timeline.setCycleCount(Timeline.INDEFINITE); // Loop until all text is shown
    timeline.play(); // Start the animation
  }

  /**
   * Method to handle when the replay button is clicked.
   *
   * @param event the mouse event.
   * @throws IOException if there is an error loading the FXML file.
   */
  private void flashLastDot() {
    // Create a new Timeline for flashing the last '.'
    Timeline flashTimeline =
        new Timeline(
            new KeyFrame(
                Duration.seconds(0.5),
                event -> {
                  // Get the current text and toggle the last dot's visibility
                  String currentText = lblStory.getText();
                  if (currentText.endsWith(".")) {
                    lblStory.setText(
                        currentText.substring(0, currentText.length() - 1)); // Hide dot
                  } else {
                    lblStory.setText(currentText + "."); // Show dot
                  }
                }));

    flashTimeline.setCycleCount(Timeline.INDEFINITE); // Keep flashing indefinitely
    flashTimeline.play(); // Start the flashing animation
  }

  /**
   * Creates and configures an ImageView to be displayed in the root pane.
   *
   * <p>This method programmatically initializes an {@code ImageView} instance, sets its dimensions
   * and position, and adds it to the specified {@code rootPane}. The size and layout properties are
   * set to ensure that the image view is displayed correctly within the application interface.
   */
  private void createImageView() {
    // Create the ImageView programmatically
    staticimg1 = new ImageView();

    // Set the initial size and position for the ImageView
    staticimg1.setFitWidth(700);
    staticimg1.setFitHeight(900);
    staticimg1.setLayoutX(200); // Set X position
    staticimg1.setLayoutY(0); // Set Y position

    // Add the ImageView to the rootPane (or any other container)
    rootPane.getChildren().add(staticimg1);
  }

  /**
   * Method to play the gif.
   *
   * @param event the mouse event.
   * @throws IOException if there is an error loading the FXML file.
   */
  private void playgif() {
    // Load the GIF image once
    Image gifImage =
        new Image(
            UpdatedGuessingController.class
                .getResource("/images/guessingimages/static.gif")
                .toString(),
            true // Enable background loading for smoother performance
            );

    // Set the GIF to the ImageView
    staticimg1.setImage(gifImage);
    staticimg1.toBack();

    // Make the ImageView visible and set opacity
    staticimg1.setVisible(true);
    staticimg1.setOpacity(0.75); // Adjust opacity as needed
  }

  /**
   * Method to handle when the replay button is clicked.
   *
   * @param event the mouse event.
   * @throws IOException if there is an error loading the FXML file.
   */
  private void appendChatMessage(ChatMessage msg) {
    // Clear the text area before showing the new message
    feedbackField.clear();

    // Get the message content as a string
    String content = msg.getContent();

    // Create a new StringBuilder to hold the text progressively
    StringBuilder displayedText = new StringBuilder();

    // Create a new Timeline to append the text one letter at a time
    Timeline timeline = new Timeline();

    // Loop through each character of the message and create keyframes to append the characters
    for (int i = 0; i < content.length(); i++) {
      final int index = i;
      KeyFrame keyFrame =
          new KeyFrame(
              Duration.millis(50 * (index + 1)), // Delay based on character position
              event -> {
                // Append the next character to the StringBuilder
                displayedText.append(content.charAt(index));
                // Update the TextArea with the current text
                feedbackField.setText(displayedText.toString());
              });
      timeline.getKeyFrames().add(keyFrame);
    }
    timeline.setOnFinished(
        e -> {
          btnReplay.setVisible(true); // Show the replay button when the timeline finishes
        });

    // Play the timeline animation
    timeline.play();
  }

  /**
   * Method to run the GPT model.
   *
   * @param msg the chat message.
   * @return the chat message.
   * @throws ApiProxyException if there is an error communicating with the API proxy.
   */
  private ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {

    // Add the message to the request
    chatCompletionRequest.addMessage(msg);
    try {
      // Execute the request and get the result
      ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();

      // Get the first choice from the result
      Choice result = chatCompletionResult.getChoices().iterator().next();

      // Add the result message to the request
      chatCompletionRequest.addMessage(result.getChatMessage());

      // Append the result message to the chat
      appendChatMessage(result.getChatMessage());

      // Return the result message
      return result.getChatMessage();
    } catch (ApiProxyException e) {
      appendChatMessage(new ChatMessage("system", "Error during GPT call: " + e.getMessage()));
      e.printStackTrace();
      return null;
    }
  }

  /** Method to handle when the send message button is clicked. */
  private void onSendMessage() {
    // Get the user's explanation message
    String message = userExplanation.getText().trim();

    // Print the message to the console
    System.out.println("Message: " + message);
    if (message.isEmpty()) {
      // Default feedback
      appendChatMessage(
          new ChatMessage(
              "system",
              "You did not provide an explanation, next time rely on clues rather than a hunch"));
      return;
    }

    // Clear the text area after sending the message
    userExplanation.clear();

    // Create a background task for the GPT request
    Task<ChatMessage> task =
        new Task<ChatMessage>() {
          @Override
          protected ChatMessage call() throws Exception {
            // Initialize the API configuration and ChatCompletionRequest
            ApiProxyConfig config = ApiProxyConfig.readConfig();
            chatCompletionRequest =
                new ChatCompletionRequest(config)
                    .setN(1)
                    .setTemperature(0.2)
                    .setTopP(0.5)
                    .setMaxTokens(100);

            // Load the template from the resource file
            URL resourceUrl =
                PromptEngineering.class.getClassLoader().getResource("prompts/guessing.txt");
            String template = loadTemplate(resourceUrl.toURI());

            // Append the user's message to the end of the template
            String combinedMessage = template + "\n" + message;

            // Create a system message with the combined template and user input
            ChatMessage systemMessage = new ChatMessage("system", combinedMessage);

            // Run GPT and get the response
            return runGpt(systemMessage);
          }
        };

    // On success, update the UI (run on the JavaFX Application Thread)
    task.setOnSucceeded(
        workerStateEvent -> {
          ChatMessage response = task.getValue();
          Platform.runLater(
              () -> {
                appendChatMessage(response); // Append the GPT response to the chat
              });
        });

    // On failure, handle the exception (you can also update the UI with an error message)
    task.setOnFailed(
        workerStateEvent -> {
          Throwable throwable = task.getException();
          Platform.runLater(
              () -> {
                // Append an error message to the chat
                appendChatMessage(new ChatMessage("system", "Error: " + throwable.getMessage()));
              });
          throwable.printStackTrace();
        });

    // Start the task in a new thread
    Thread thread = new Thread(task);
    thread.setDaemon(true);
    thread.start();
  }

  /**
   * Reinitializes the scene when the Replay button is clicked.
   *
   * <p>This method is called when the user clicks the Replay button. It resets the game state
   * context and transitions the application to the initial scene, allowing the player to start the
   * game anew. Any necessary cleanup or preparation for a new game session should be handled within
   * this method.
   *
   * @param event the action event triggered by the Replay button click
   * @throws IOException if there is an error loading the initial scene
   */
  @FXML
  private void onReplay(ActionEvent event) throws IOException {

    // Re initalise the context
    GameStateContext.getInstance().reset();

    // Fade out transition
    FadeTransition fadeOutTransition = new FadeTransition(Duration.millis(1000), rootPane);
    fadeOutTransition.setFromValue(1.0);
    fadeOutTransition.setToValue(0.0);
    fadeOutTransition.play();
    SharedVolumeControl.getInstance().setVolumeSetting(true);

    App.setRoot("initialScene");
  }

  /** Method to adjust the position of the volume button. */
  private void adjustVolumeButtonPosition() {
    volumeUp.setLayoutX(15);
    volumeUp.setLayoutY(8);
    volumeUpStroke.setLayoutY(8);
    volumeUpStroke.setLayoutX(22);
    volumeOff.setLayoutX(15);
    volumeOff.setLayoutY(8);
  }
}
