package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.utils.VolumeControlUtil;

/**
 * Base class for room controllers that provides common functionality and UI elements.
 *
 * <p>This class serves as the base class for all room controllers in the application. It provides
 * common functionality and UI elements that are shared across different rooms, such as chat boxes,
 * buttons, and icons. The base room controller also handles the initialization of the GPT model,
 * countdown timer, and volume control. Subclasses can extend this base class to implement specific
 * functionality for each room while leveraging the shared components and features provided by the
 * base controller.
 */
public abstract class BaseRoomController {

  protected ChatCompletionRequest chatCompletionRequest;
  protected GameStateContext context = GameStateContext.getInstance();
  protected boolean firstTime = true;
  protected TimerModel countdownTimer;
  protected SharedVolumeControl sharedVolumeControl;
  private VolumeControlUtil volumeControlUtil;

  @FXML protected TextArea userChatBox;
  @FXML protected TextArea suspectChatBox;
  @FXML protected Circle sendButton;
  @FXML protected Button guessButton;
  @FXML protected AnchorPane rootNode;
  @FXML protected ImageView backgroundimg;
  @FXML protected Label lbltimer;
  @FXML protected SVGPath volumeOff;
  @FXML protected SVGPath volumeUp;
  @FXML protected SVGPath volumeUpStroke;

  @FXML private ImageView basemapimg;
  @FXML private Label lblareastatus;
  @FXML private ImageView widowiconimg;
  @FXML private ImageView menuclosedimg;
  @FXML private ImageView brothericonimg;
  @FXML private ImageView grandsoniconimg;
  @FXML private ImageView topofmenubtn;
  @FXML private ImageView crimesceneiconimg;
  @FXML private Pane timerpane;

  /**
   * Initializes the controller after the FXML has been loaded.
   *
   * <p>This method is automatically called by the JavaFX framework when the associated FXML file is
   * loaded. It sets up the initial state of the controller, including configuring UI components,
   * binding properties, and initializing data necessary for the view's functionality. Any event
   * listeners or default settings can also be set within this method.
   */
  @FXML
  public void initialize() {
    updateMenuVisibility();
    bindTimerToLabel();
    initializeGptModel();
    setResponsiveBackground(backgroundimg, rootNode);
    checkGuessButton();

    // Set up the volume control
    // Add the volume button to the label pane and show it
    volumeControlUtil =
        new VolumeControlUtil(timerpane); // Initialize the VolumeControlUtil with the timerPane
    volumeControlUtil.showVolumeButton(); // Show the volume button

    // Set initial position of basemapimg off the screen (below)
    basemapimg.setTranslateY(rootNode.getHeight()); // Set translateY to move it out of view

    countdownTimer
        .timeStringProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              checkiftimeris4minleft();
            });

    // Add key event handler for detecting Enter key
    userChatBox.addEventFilter(
        KeyEvent.KEY_PRESSED,
        event -> {
          if (event.getCode() == KeyCode.ENTER) {
            event.consume();
            // Handle the Enter key press action
            ActionEvent actionEvent = new ActionEvent(event.getSource(), event.getTarget());
            try {
              onEnterKey(actionEvent);
            } catch (IOException | ApiProxyException e) {
              e.printStackTrace();
            }
            // Optionally, consume the event to prevent a new line from being added
            // event.consume();
          }
        });

    basemapimg.fitWidthProperty().bind(rootNode.widthProperty());
    basemapimg.fitHeightProperty().bind(rootNode.heightProperty());

    // Add mouse event handlers for icon ImageViews
    widowiconimg.setOnMouseEntered(this::onMouseEnteredIcon);
    widowiconimg.setOnMouseExited(this::onMouseExitedIcon);
    widowiconimg.setOnMouseClicked(this::onIconClicked);

    brothericonimg.setOnMouseEntered(this::onMouseEnteredIcon);
    brothericonimg.setOnMouseExited(this::onMouseExitedIcon);
    brothericonimg.setOnMouseClicked(this::onIconClicked);

    grandsoniconimg.setOnMouseEntered(this::onMouseEnteredIcon);
    grandsoniconimg.setOnMouseExited(this::onMouseExitedIcon);
    grandsoniconimg.setOnMouseClicked(this::onIconClicked);

    // Add mouse event handlers for the closed menu button
    menuclosedimg.setOnMouseEntered(this::onMouseEnteredMenuClosed);
    menuclosedimg.setOnMouseExited(this::onMouseExitedMenuClosed);
    menuclosedimg.setOnMouseClicked(this::onToggleMenu);

    // Add mouse event handlers for the top of menu button
    topofmenubtn.setOnMouseEntered(this::onMouseEnteredTopOfMenu);
    topofmenubtn.setOnMouseExited(this::onMouseExitedTopOfMenu);
    topofmenubtn.setOnMouseClicked(this::onToggleMenuOff);

    // Add mouse event handlers for the crime scene icon
    crimesceneiconimg.setOnMouseEntered(this::onMouseEnteredIcon);
    crimesceneiconimg.setOnMouseExited(this::onMouseExitedIcon);
    crimesceneiconimg.setOnMouseClicked(
        event -> {
          try {
            onRoom(event);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
  }

  /**
   * Checks if the timer has reached one minute remaining.
   *
   * <p>This method is called to monitor the countdown timer's remaining time. When the timer
   * displays "01:00" (indicating one minute left), it triggers a flashing animation on the
   * specified timer pane to alert the user. This visual cue is intended to emphasize the urgency as
   * the time limit approaches.
   */
  private void checkiftimeris4minleft() {
    if (countdownTimer
        .timeStringProperty()
        .get()
        .equals("01:00")) { // When the time reaches 1 minute left
      startFlashingAnimation(timerpane);
    }
  }

  /**
   * This method starts the flashing animation for a pane.
   *
   * @param pane the pane to flash.
   */
  private void startFlashingAnimation(Pane pane) {
    // Store the existing style to restore it later after flashing
    String originalStyle = pane.getStyle();

    // Create a Timeline to flash the pane between styles
    Timeline flashTimeline = new Timeline();

    // Define the CSS styles to use during the animation
    String flashOnStyle =
        "-fx-background-color: #FF0000; -fx-background-radius: 10px; -fx-border-radius: 10px;"
            + " -fx-border-width: 4px; -fx-border-color: #FF0000;";
    String flashOffStyle =
        "-fx-background-color: #ADD8E6; -fx-background-radius: 10px; -fx-border-radius: 10px;"
            + " -fx-border-width: 4px; -fx-border-color: #ADD8E6;";

    // Alternate between red and light blue with border and background radius
    KeyFrame flashOn =
        new KeyFrame(
            Duration.seconds(0),
            new KeyValue(pane.styleProperty(), flashOnStyle) // Set the style to flash on
            );
    KeyFrame flashOff =
        new KeyFrame(
            Duration.seconds(0.5),
            new KeyValue(pane.styleProperty(), flashOffStyle) // Set the style to flash off
            );
    KeyFrame flashOnAgain =
        new KeyFrame(
            Duration.seconds(1),
            new KeyValue(pane.styleProperty(), flashOnStyle) // Set the style to flash on again
            );
    KeyFrame flashOffAgain =
        new KeyFrame(
            Duration.seconds(1.5),
            new KeyValue(pane.styleProperty(), flashOffStyle) // Set the style to flash off again
            );

    // Add keyframes to the timeline
    flashTimeline.getKeyFrames().addAll(flashOn, flashOff, flashOnAgain, flashOffAgain);

    // Repeat the flash sequence for 3 cycles
    flashTimeline.setCycleCount(2);

    // Create a Timeline for the shaking effect
    Timeline shakeTimeline = new Timeline();

    // Create shake keyframes for horizontal movement
    KeyFrame moveRight =
        new KeyFrame(Duration.millis(50), new KeyValue(pane.translateXProperty(), 10));
    KeyFrame moveLeft =
        new KeyFrame(Duration.millis(100), new KeyValue(pane.translateXProperty(), -10));
    KeyFrame moveCenter =
        new KeyFrame(Duration.millis(150), new KeyValue(pane.translateXProperty(), 0));

    // Add keyframes to the shake timeline and set it to repeat during the flashing duration
    shakeTimeline.getKeyFrames().addAll(moveRight, moveLeft, moveCenter);
    shakeTimeline.setCycleCount(Timeline.INDEFINITE); // Repeat indefinitely

    // Create a DropShadow effect for the glowing red border
    DropShadow redGlow = new DropShadow();
    redGlow.setColor(Color.RED);
    redGlow.setRadius(30); // Set the glow radius
    redGlow.setSpread(0.7); // How much the color spreads out

    // Add the glowing effect before starting the animation
    pane.setEffect(redGlow);
    rootNode.setEffect(redGlow);

    // Play the shake timeline in parallel with the flash timeline
    flashTimeline.setOnFinished(
        event -> {
          pane.setStyle(originalStyle); // Restore the original pane style
          pane.setEffect(null); // Remove the glow effect
          rootNode.setEffect(null); // Remove the glow effect
          shakeTimeline.stop(); // Stop shaking after the flash ends
          pane.setTranslateX(0); // Reset the pane's position
        });

    // Start the shake and flash animations
    shakeTimeline.play();
    flashTimeline.play();
  }

  /**
   * Handles the mouse entered event when the cursor is over the top of the menu button.
   *
   * <p>This method changes the cursor to a hand symbol to indicate that the menu button is
   * interactive. It also updates the status label to prompt the user with a message asking if they
   * want to close the menu. This provides visual feedback to enhance user experience.
   *
   * @param event the mouse event triggered when the cursor enters the top of the menu button.
   */
  @FXML
  protected void onMouseEnteredTopOfMenu(MouseEvent event) {
    // change cursor
    rootNode.getScene().setCursor(javafx.scene.Cursor.HAND);
    lblareastatus.setText("Close Menu?");
  }

  /**
   * Handles the mouse exited event when the cursor leaves the top of the menu button.
   *
   * <p>This method resets the cursor back to the default cursor, indicating that the menu button is
   * no longer interactive. It also updates the status label to reflect the current room based on
   * the controller type. This provides context to the user about their current location within the
   * application.
   *
   * @param event the mouse event triggered when the cursor exits the top of the menu button.
   */
  @FXML
  protected void onMouseExitedTopOfMenu(MouseEvent event) {
    // change cursor
    rootNode.getScene().setCursor(javafx.scene.Cursor.DEFAULT);

    // if its in the suspect1room controller set the status to the uncle
    if (this instanceof Suspect1RoomController) {
      lblareastatus.setText("You are in the: Uncle's Room");
    } else if (this instanceof Suspect2RoomController) {
      lblareastatus.setText("You are in the: Widow's Room");
    } else if (this instanceof Suspect3RoomController) {
      lblareastatus.setText("You are in the: Grandson's Room");
    }
  }

  /**
   * Handles the mouse click event on the top of the menu button to toggle its visibility.
   *
   * <p>This method is called when the user clicks on the menu button. It invokes the {@code
   * toggleMenuVisibility} method of the context to switch the menu's visibility state. After
   * toggling, it updates the menu display to reflect the current visibility status.
   *
   * @param event the mouse event triggered by the click on the menu button.
   */
  @FXML
  protected void onToggleMenuOff(MouseEvent event) {
    context.toggleMenuVisibility();
    updateMenuVisibility();
  }

  /**
   * Handles the mouse entered event when the cursor is over the closed menu icon.
   *
   * <p>This method is triggered when the mouse hovers over the closed menu icon. It changes the
   * cursor to a hand symbol, indicating interactivity, and updates the status label to prompt the
   * user with a message asking if they want to open the menu. Additionally, the method scales the
   * closed menu icon slightly larger to create a visual effect that indicates it can be clicked.
   *
   * @param event the mouse event triggered when the cursor enters the closed menu icon.
   */
  @FXML
  protected void onMouseEnteredMenuClosed(MouseEvent event) {
    // change cursor
    rootNode.getScene().setCursor(javafx.scene.Cursor.HAND);
    lblareastatus.setText("Open Menu?");

    // expand
    menuclosedimg.setScaleX(1.1);
    menuclosedimg.setScaleY(1.1);
  }

  /**
   * Handles the mouse exited event when the cursor leaves the closed menu icon.
   *
   * <p>This method is triggered when the mouse pointer exits the closed menu icon. It resets the
   * cursor back to the default cursor, indicating that the icon is no longer interactive. The
   * method also updates the status label to reflect the current room based on the controller type,
   * providing contextual information to the user. Additionally, it shrinks the icon back to its
   * original size, giving visual feedback that the icon is no longer hovered over.
   *
   * @param event the mouse event triggered when the cursor exits the closed menu icon.
   */
  @FXML
  protected void onMouseExitedMenuClosed(MouseEvent event) {
    // change cursor
    rootNode.getScene().setCursor(javafx.scene.Cursor.DEFAULT);

    if (this instanceof Suspect1RoomController) {
      lblareastatus.setText("You are in the: Uncle's Room");
    } else if (this instanceof Suspect2RoomController) {
      lblareastatus.setText("You are in the: Widow's Room");
    } else if (this instanceof Suspect3RoomController) {
      lblareastatus.setText("You are in the: Grandson's Room");
    }

    // shrink
    menuclosedimg.setScaleX(1);
    menuclosedimg.setScaleY(1);
  }

  /**
   * Handles the mouse click event on the closed menu icon to toggle its visibility.
   *
   * <p>This method is called when the user clicks on the closed menu icon. It invokes the {@code
   * toggleMenuVisibility} method from the context to switch the visibility of the menu. After
   * toggling, the method updates the display of various UI elements by bringing them to the front
   * or sending them to the back, ensuring that the user interface reflects the current state
   * appropriately. The opacity of the icons is also adjusted based on the controller context.
   *
   * @param event the mouse event triggered by the click on the closed menu icon.
   */
  @FXML
  protected void onToggleMenu(MouseEvent event) {
    // Toggle the menu visibility
    context.toggleMenuVisibility();

    // Update the visibility of the menu
    basemapimg.toFront();
    topofmenubtn.toFront();
    lblareastatus.toFront();
    widowiconimg.toFront();
    grandsoniconimg.toFront();
    brothericonimg.toFront();

    // Hide the closed menu icon behind everything else
    menuclosedimg.toBack();

    // Set the opacity of the icons based on the controller
    userChatBox.toFront();
    suspectChatBox.toFront();
    guessButton.toFront();
    sendButton.toFront();
    lbltimer.toFront();
    crimesceneiconimg.toFront();
  }

  /**
   * This method is called when an action event occurs on the closed menu icon. It toggles the menu
   * visibility and updates its state.
   *
   * @param event the action event.
   */
  @FXML
  protected void onToggleMenu(ActionEvent event) {
    context.toggleMenuVisibility();
    updateMenuVisibility();
  }

  /**
   * Handles the mouse entered event when the cursor hovers over an icon.
   *
   * <p>This method is triggered when the mouse pointer enters an icon. It changes the opacity of
   * the icon to 0.7 to provide visual feedback indicating that the icon is interactive.
   * Additionally, it updates the status label based on the specific icon being hovered over,
   * prompting the user with the appropriate action related to that icon (e.g., visiting a specific
   * character).
   *
   * @param event the mouse event triggered when the cursor enters the icon.
   */
  @FXML
  protected void onMouseEnteredIcon(MouseEvent event) {
    ImageView icon = (ImageView) event.getSource();
    icon.setOpacity(0.7); // Change opacity on hover

    if (icon.getId().equals("widowiconimg")) {
      lblareastatus.setText("Visit the Widow");
    } else if (icon.getId().equals("brothericonimg")) {
      lblareastatus.setText("Visit the Uncle");
    } else if (icon.getId().equals("grandsoniconimg")) {
      lblareastatus.setText("Visit the Grandson");
    }
  }

  /**
   * Handles the mouse exited event when the cursor leaves an icon.
   *
   * <p>This method is triggered when the mouse pointer exits an icon. It restores the opacity of
   * the icon back to 1.0, indicating that it is no longer interactive. Additionally, the method
   * updates the status label to reflect the current room based on the controller type, providing
   * contextual information to the user about their location within the application.
   *
   * @param event the mouse event triggered when the cursor exits the icon.
   */
  @FXML
  protected void onMouseExitedIcon(MouseEvent event) {
    ImageView icon = (ImageView) event.getSource();
    icon.setOpacity(1.0); // Restore opacity

    if (this instanceof Suspect1RoomController) {
      lblareastatus.setText("You are in the: Uncle's Room");
    } else if (this instanceof Suspect2RoomController) {
      lblareastatus.setText("You are in the: Widow's Room");
    } else if (this instanceof Suspect3RoomController) {
      lblareastatus.setText("You are in the: Grandson's Room");
    }
  }

  /**
   * Handles the mouse click event on an icon.
   *
   * <p>This method is called when the user clicks on an icon. It retrieves the {@code ImageView}
   * that was clicked and prints its ID to the console for debugging purposes. Based on the ID of
   * the clicked icon, it navigates to the corresponding room associated with that icon. If the
   * clicked icon is not recognized, an error message is printed to the console.
   *
   * @param event the mouse event triggered by clicking the icon.
   */
  @FXML
  protected void onIconClicked(MouseEvent event) {
    // Get the icon that was clicked
    ImageView icon = (ImageView) event.getSource();
    System.out.println("Clicked on: " + icon.getId()); // Replace with desired action

    // Navigate to the corresponding room based on the icon clicked
    switch (icon.getId()) {
      case "widowiconimg" -> {
        try {
          App.setRoot("suspect2room");
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      case "brothericonimg" -> {
        try {
          App.setRoot("suspect1room");
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      case "grandsoniconimg" -> {
        try {
          App.setRoot("suspect3room");
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      default -> {
        System.out.println("Invalid icon clicked");
      }
    }
  }

  /**
   * Initializes the GPT model and loads the initial prompt.
   *
   * <p>This method sets up a new task to initialize the GPT model in a separate thread, allowing
   * the application to remain responsive during the initialization process. It handles the loading
   * of the initial prompt necessary for the model's operation. Any exceptions that occur during the
   * initialization process, such as {@code IOException} or {@code URISyntaxException}, are thrown
   * to be handled by the calling code.
   *
   * @throws IOException if there is an error during input or output operations while initializing
   *     the model.
   * @throws URISyntaxException if the URI for the prompt file is not valid.
   */
  protected void initializeGptModel() {
    // Create a new task to initialize the GPT model
    Task<Void> task =
        new Task<>() {
          @Override
          protected Void call() throws IOException, URISyntaxException {
            try {
              setupGptRequest();
              // Load the initial prompt
              loadGptPrompt(getInitialPrompt());
              // Set the user chat box prompt with the initial prompt
              if (firstTime) {
                setUserChatBoxPrompt("Begin interrogating...");
                firstTime = false;
              }
              // Error handling
            } catch (ApiProxyException | IOException | URISyntaxException e) {
              appendChatMessage(new ChatMessage("system", "Error: " + e.getMessage()));
            }
            return null;
          }
        };

    new Thread(task).start();
  }

  /**
   * Sets up the GPT request with the specified parameters.
   *
   * <p>This method initializes the chat completion request by reading the API configuration and
   * setting the required parameters for the GPT request, such as the number of responses,
   * temperature, top-p value, and maximum tokens.
   *
   * @throws IOException if there is an error reading the API configuration file.
   * @throws ApiProxyException if there is an error communicating with the API proxy.
   */
  protected void setupGptRequest() throws IOException, ApiProxyException {
    // Set up the GPT request
    ApiProxyConfig config = ApiProxyConfig.readConfig();
    chatCompletionRequest =
        // Set the parameters for the GPT request
        new ChatCompletionRequest(config)
            .setN(1)
            .setTemperature(0.2)
            .setTopP(0.5)
            .setMaxTokens(100);
  }

  /**
   * Binds the countdown timer to the label for display.
   *
   * <p>This method retrieves the countdown timer instance from the shared timer model and binds its
   * time string property to the {@code lbltimer} label, ensuring that the label updates
   * automatically as the timer changes. Additionally, it attaches a listener to the timer's time
   * string property to perform actions when the timer's value changes, specifically checking if
   * there are four minutes left.
   */
  protected void bindTimerToLabel() {
    countdownTimer = SharedTimerModel.getInstance().getTimer();
    System.out.println(
        "Countdown Timer initialized: "
            + (countdownTimer != null)); // Check if countdownTimer is null

    lbltimer
        .textProperty()
        .bind(countdownTimer.timeStringProperty()); // Ensure this binding is correct

    // Attach listener
    countdownTimer
        .timeStringProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              checkiftimeris4minleft();
            });
  }

  /**
   * Retrieves the initial prompt for the GPT model.
   *
   * <p>This method is responsible for loading the initial prompt that the GPT model will use for
   * generating responses. The implementation of this method will define how and where the prompt is
   * retrieved from. It returns the prompt as a {@code String}. This method is abstract, requiring
   * subclasses to provide a specific implementation.
   *
   * @return the initial prompt as a {@code String} to be used by the GPT model.
   * @throws URISyntaxException if the resource path for the prompt is invalid.
   * @throws IOException if there is an error during input or output operations while retrieving the
   *     prompt.
   * @throws ApiProxyException if there is an error communicating with the API proxy to fetch the
   *     prompt.
   */
  protected abstract String getInitialPrompt();

  /**
   * Loads the initial prompt for the GPT model from a specified resource file.
   *
   * <p>This abstract method is intended to be implemented by subclasses to define how the initial
   * prompt is loaded into the GPT model. It takes the path to the resource file containing the
   * prompt as an input. The implementation must handle the loading of the prompt while managing
   * potential errors that may arise during the process.
   *
   * @param resourcePath the path to the resource file containing the initial prompt. This path
   *     should be accessible and valid to ensure successful loading.
   * @throws URISyntaxException if the provided resource path cannot be converted into a URI.
   * @throws IOException if there is an error reading from the resource file.
   * @throws ApiProxyException if there is an error communicating with the API proxy or if the
   *     loaded prompt parameters are invalid.
   */
  protected abstract void loadGptPrompt(String resourcePath)
      throws URISyntaxException, IOException, ApiProxyException;

  /**
   * Sets the prompt text for the user chat box.
   *
   * <p>This method updates the prompt displayed in the user chat box to provide guidance or
   * instructions to the user. The update is performed on the JavaFX Application Thread using {@code
   * Platform.runLater}, ensuring that UI changes are made safely and without threading issues.
   *
   * @param text the text to be set as the prompt for the user chat box. This should provide clear
   *     guidance to the user about what to enter.
   */
  protected void setUserChatBoxPrompt(String text) {
    Platform.runLater(() -> userChatBox.setPromptText(text));
  }

  /**
   * Handles the mouse event to transition to the room scene.
   *
   * <p>This method is triggered when a mouse event occurs. It changes the current scene to the
   * "room" view, allowing the user to interact with the room interface. The method uses the App
   * class to set the root of the application to the specified scene.
   *
   * @param event the mouse event that triggered the scene change.
   * @throws IOException if there is an error loading the FXML file for the room scene.
   */
  @FXML
  protected void onRoom(MouseEvent event) throws IOException {
    App.setRoot("room");
  }

  /**
   * Handles the action of sending a chat message when the send event occurs.
   *
   * <p>This method is triggered by a mouse event (e.g., a button click) to send the current chat
   * message. It calls the {@code handleSendMessage} method to process the sending of the message to
   * the GPT model. Any exceptions that occur during the sending process will be propagated to the
   * caller.
   *
   * @param event the mouse event that triggered the send action.
   * @throws ApiProxyException if there is an error communicating with the API proxy or if the
   *     request parameters are invalid.
   * @throws IOException if there is an error during input or output operations.
   */
  @FXML
  protected void onSend(MouseEvent event) throws ApiProxyException, IOException {
    handleSendMessage();
  }

  /**
   * Handles the action event when the send button is clicked.
   *
   * <p>This method is called when the user clicks the send button. It invokes the {@code
   * handleSendMessage} method to process the user's message and send it to the appropriate
   * recipient. This allows for the interaction with the chat system, facilitating communication
   * within the application.
   *
   * @param event the action event triggered by clicking the send button.
   * @throws ApiProxyException if there is an error communicating with the API proxy.
   * @throws IOException if there is an error during input or output operations.
   */
  @FXML
  protected void onSend(ActionEvent event) throws ApiProxyException, IOException {
    handleSendMessage();
  }

  /**
   * Handles the process of sending a message to the GPT model.
   *
   * <p>This method orchestrates the actions required to send a message to the GPT model, including
   * executing the message sending logic, recording the visit for tracking purposes, and updating
   * the state of the guess button based on the current context. It ensures that the necessary
   * procedures are followed for effective communication with the model.
   *
   * @throws ApiProxyException if there is an error communicating with the API proxy or if the
   *     request parameters are invalid.
   * @throws IOException if there is an error during input or output operations, such as reading
   *     from or writing to a file.
   */
  protected void handleSendMessage() throws ApiProxyException, IOException {
    sendMessageCode();
    recordVisit();
    checkGuessButton();
  }

  /**
   * Sends a message to the GPT model for processing.
   *
   * <p>This method retrieves the message from the user chat box, ensuring that empty messages are
   * not sent. If the message is valid, it clears the user input, creates a {@code ChatMessage}
   * instance with the user's message, and initiates a task to send the message to the GPT model.
   * The task is executed in a new thread to prevent blocking the UI.
   *
   * @throws ApiProxyException if there is an error communicating with the API proxy or if the
   *     request parameters are invalid.
   * @throws IOException if there is an error during input or output operations.
   */
  protected void sendMessageCode() throws ApiProxyException, IOException {
    // Get the message from the user chat box
    String message = userChatBox.getText().trim();

    // Check if the message is empy as to not send empty messages
    if (message.isEmpty()) {
      return;
    }

    // Clear the user input
    clearUserInput();

    // Append the message to the chat box
    ChatMessage msg = new ChatMessage("user", message);
    Task<Void> task = createGptTask(msg);

    // Start the task in a new thread
    new Thread(task).start();
  }

  /**
   * Creates a new task to run the GPT model with the specified chat message.
   *
   * <p>This method initializes a new {@code Task<Void>} that encapsulates the operation of running
   * the GPT model. It accepts a {@code ChatMessage} as input, which is used to interact with the
   * model. The task executes in the background and updates the user chat box prompt upon
   * completion. This allows for non-blocking interaction within the application.
   *
   * @param msg the {@code ChatMessage} containing the user's query to be sent to the GPT model.
   * @return a {@code Task<Void>} that can be executed to run the GPT model asynchronously.
   */
  protected Task<Void> createGptTask(ChatMessage msg) {
    // Create a new task to run the GPT model
    return new Task<>() {
      @Override
      protected Void call() {
        // Run the GPT model and alter the user chat box prompt
        try {
          runGpt(msg);
          setUserChatBoxPrompt("Ask another question...");
        } catch (ApiProxyException e) {
          appendChatMessage(new ChatMessage("system", "Error: " + e.getMessage()));
        }
        return null;
      }
    };
  }

  /**
   * Clears the user input fields in the chat interface.
   *
   * <p>This method removes any text from the user chat box and the suspect chat box, resetting them
   * to their initial state. Additionally, it sets the prompt text of the user chat box to indicate
   * that the system is waiting for a response.
   */
  protected void clearUserInput() {
    userChatBox.clear();
    suspectChatBox.clear();
    userChatBox.setPromptText("Waiting for response...");
  }

  /**
   * Runs the GPT model with the provided chat message.
   *
   * <p>This method takes a {@code ChatMessage} as input, sends it to the GPT model, and retrieves
   * the model's response. It manages the message flow by appending the user's message and the
   * model's response to the chat. If an error occurs during the communication with the API, an
   * error message is appended to the chat box to inform the user. The method also enables and
   * disables the send button to prevent multiple submissions while the request is processed.
   *
   * @param msg the {@code ChatMessage} containing the user's message to be sent to the GPT model.
   * @return the {@code ChatMessage} response from the GPT model, or {@code null} if an error
   *     occurs.
   * @throws ApiProxyException if there is an error communicating with the API proxy during the
   *     request.
   */
  protected ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    chatCompletionRequest.addMessage(msg);
    disableSendButton(true);

    // Send the message to GPT
    try {
      ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
      ChatMessage response = chatCompletionResult.getChoices().iterator().next().getChatMessage();
      chatCompletionRequest.addMessage(response);
      // Append the response to the chat box
      appendChatMessage(response);

      return response;
    } catch (ApiProxyException e) {
      // Append an error message to the chat box if an exception occurs
      appendChatMessage(new ChatMessage("system", "Error during GPT call: " + e.getMessage()));
      return null;
    } finally {
      disableSendButton(false);
    }
  }

  /**
   * This method appends a chat message to the suspect chat box.
   *
   * @param msg the chat message to append.
   */
  @FXML
  protected void appendChatMessage(ChatMessage msg) {
    suspectChatBox.clear();
    suspectChatBox.appendText(msg.getContent() + "\n\n");
  }

  /**
   * This method disables the send button.
   *
   * @param disable whether to disable the send button.
   */
  protected void disableSendButton(boolean disable) {
    sendButton.setDisable(disable);
  }

  /** This method records the visit to the room. */
  protected void recordVisit() {
    // Implement specific record-keeping logic in subclasses
  }

  /**
   * This method checks if the guess button should be enabled.
   *
   * <p>The guess button is enabled if all suspects have been visited and at least one clue has been
   * found.
   */
  @FXML
  protected void checkGuessButton() {
    // Enable the guess button if all suspects have been visited and at least one clue has been
    // found
    boolean canGuess =
        context.getListOfVisitors().contains("suspect1")
            && context.getListOfVisitors().contains("suspect2")
            && context.getListOfVisitors().contains("suspect3")
            && context.isAtLeastOneClueFound();

    // Set the opacity and disable state of the guess button
    guessButton.setOpacity(canGuess ? 0.8 : 0.3);
    guessButton.setDisable(!canGuess);
  }

  /**
   * Sets the background image to be responsive within the specified pane.
   *
   * <p>This method binds the width and height properties of the given {@code ImageView} to the
   * width and height of the specified {@code AnchorPane}. This ensures that the background image
   * scales dynamically as the size of the pane changes, maintaining its aspect ratio and fit within
   * the pane's dimensions.
   *
   * @param imageView the {@code ImageView} containing the background image that needs to be made
   *     responsive.
   * @param pane the {@code AnchorPane} in which the background image will be displayed
   *     responsively.
   */
  protected void setResponsiveBackground(ImageView imageView, AnchorPane pane) {
    // Set the background image to be responsive
    imageView.fitWidthProperty().bind(pane.widthProperty());
    imageView.fitHeightProperty().bind(pane.heightProperty());
  }

  /** This method updates the visibility of the menu. */
  protected void updateMenuVisibility() {
    // If the menu is not visible, bring everything to the back except the closed menu icon
    basemapimg.toBack();
    lblareastatus.toBack();
    widowiconimg.toBack();
    brothericonimg.toBack();
    grandsoniconimg.toBack();
    topofmenubtn.toBack();
    crimesceneiconimg.toBack();

    // Bring the closed menu icon to the front
    menuclosedimg.toFront();
  }

  /**
   * Sets the visibility and managed properties of a button.
   *
   * <p>This method sets the visibility and managed properties of a button based on the specified
   * visibility flag. If the flag is {@code true}, the button is set to be visible and managed;
   * otherwise, it is set to be invisible and unmanaged.
   *
   * @param button the button to set the visibility and managed properties for.
   * @param isVisible the flag indicating whether the button should be visible and managed.
   */
  protected void setVisibleAndManaged(Button button, boolean isVisible) {
    button.setVisible(isVisible);
    button.setManaged(isVisible);
  }

  /**
   * Handles the key press event for the application.
   *
   * <p>This method is triggered when a key is pressed while the application has focus. If the Enter
   * key is pressed, it invokes the {@code handleSendMessage} method to process and send the user's
   * message. Any exceptions that occur during this process are caught and printed to the console
   * for debugging purposes.
   *
   * @param event the key event triggered by pressing a key on the keyboard.
   */
  @FXML
  protected void onKeyPressed(KeyEvent event) {
    if (event.getCode() == KeyCode.ENTER) {
      try {
        handleSendMessage(); // Handle the action when the Enter key is pressed
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Loads a template from a specified file.
   *
   * <p>This method reads the contents of a file located at the given URI and returns it as a {@code
   * String}. The file is expected to contain a template that can be used in the application. If the
   * file cannot be accessed or read, an {@code IOException} will be thrown.
   *
   * @param filePath the URI of the file containing the template to be loaded.
   * @return the contents of the template file as a {@code String}.
   * @throws IOException if there is an error during reading from the file.
   */
  protected String loadTemplate(URI filePath) throws IOException {
    return new String(Files.readAllBytes(Paths.get(filePath)));
  }

  /**
   * Handles the action event when the Enter key is pressed.
   *
   * <p>This method is called when the user presses the Enter key. It invokes the {@code
   * handleSendMessage} method to process and send the user's message. This allows for seamless
   * interaction with the chat system, enabling users to submit their queries without needing to
   * click a send button.
   *
   * @param event the action event triggered by pressing the Enter key.
   * @throws IOException if there is an error during input or output operations while sending the
   *     message.
   * @throws ApiProxyException if there is an error communicating with the API proxy.
   */
  @FXML
  protected void onEnterKey(ActionEvent event) throws IOException, ApiProxyException {
    handleSendMessage();
  }
}
