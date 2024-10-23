package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.utils.VolumeControlUtil;

/** CluePhoneController is the controller class for the Clue Phone scene. */
public class CluePhoneController {

  // Static fields
  private static GameStateContext context = GameStateContext.getInstance();

  // Static methods
  // (None in this case)

  // Instance fields
  @FXML private AnchorPane rootPane;
  @FXML private ImageView phoneImageView;
  @FXML private ImageView imageView;
  @FXML private Button arrowButton;
  @FXML private Label label;
  @FXML private Label popUp;
  @FXML private Pane labelPane;
  @FXML private Label lbltimer;

  private Circle startCircleRed;
  private Circle startCircleBlue;
  private Circle startCircleGreen;

  private Circle endCircleRed;
  private Circle endCircleBlue;
  private Circle endCircleGreen;

  private Line redWire;
  private Line blueWire;
  private Line greenWire;
  private Line activeWire;

  private boolean isRedWireConnected = false;
  private boolean isBlueWireConnected = false;
  private boolean isGreenWireConnected = false;

  private Circle activeStartCircle = null;

  private TimerModel countdownTimer;
  private VolumeControlUtil volumeControlUtil;

  /**
   * Initializes the CluePhoneController after the associated FXML has been loaded.
   *
   * <p>This method is automatically called by the JavaFX framework when the FXML file for the
   * CluePhone view is loaded. It sets up the initial state of the CluePhone controller by
   * configuring UI components, binding properties, and initializing any necessary data structures
   * or event listeners required for the controller's functionality.
   */
  @FXML
  private void initialize() {
    // Use the helper method to create and add the timer pane
    createTimerPane();
    createBackground();

    if (context.isPhoneFound()) {
      ifphoneisalreadyFound();
    } else {
      ifphoneisnotFound();
    }

    labelPane.toFront();
    // Add the volume button to the label pane and show it
    // Add the volume button to the label pane and show it
    volumeControlUtil =
        new VolumeControlUtil(labelPane); // Initialize the VolumeControlUtil with the timerPane
    volumeControlUtil.showVolumeButton(); // Show the volume button

    // set up go back button
    setupGoBackButton();
  }

  private void setupGoBackButton() {
    Button goBackButton = new Button("Go Back");
    goBackButton.setStyle(
        "-fx-background-color: #c1b8b5; -fx-background-radius: 10px; -fx-border-radius: 10px;"
            + " -fx-border-color: #3f2218; -fx-border-width: 4px; -fx-text-fill:"
            + " black;-fx-font-size: 14px; -fx-background-insets: 0; -fx-border-insets: 0;");
    goBackButton.setPrefWidth(100);
    goBackButton.setPrefHeight(40);

    // Position the button at the bottom-right corner
    AnchorPane.setBottomAnchor(goBackButton, 10.0); // 10px from the bottom
    AnchorPane.setRightAnchor(goBackButton, 10.0); // 10px from the right

    goBackButton.getStyleClass().add("button"); // Apply the style class from CSS
    goBackButton.setOpacity(0.8); // Set opacity to 0.8
    // Add the button to the anchorPane
    rootPane.getChildren().add(goBackButton);

    // Set the action when the button is clicked
    goBackButton.setOnAction(
        event -> {
          try {
            App.setRoot("room");
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
    goBackButton.setOnMouseEntered(
        e -> {
          // Apply custom styles for hover
          goBackButton.setStyle(
              " -fx-background-color: #775E55; -fx-background-radius:"
                  + " 10px; -fx-border-radius: 10px; -fx-border-color: #3f2218;"
                  + " -fx-border-width: 4px;-fx-padding: 5; -fx-border-width: 3; -fx-cursor:"
                  + " hand; -fx-text-fill: #c1b8b5;"
                  + "-fx-font-size: 14px; "
                  + "-fx-background-insets: 0; "
                  + "-fx-border-insets: 0;");
        });
    goBackButton.setOnMouseExited(
        e -> {
          // Reset the button style when the mouse exits
          goBackButton.setStyle(
              "-fx-background-color: #c1b8b5; -fx-background-radius: 10px; -fx-border-radius: 10px;"
                  + " -fx-border-color: #3f2218; -fx-border-width: 4px; -fx-text-fill:"
                  + " black;-fx-font-size: 14px; -fx-background-insets: 0; -fx-border-insets:"
                  + " 0; -fx-padding: 5; -fx-border-width: 3; -fx-cursor: default;");
        });
  }

  private void ifphoneisnotFound() {
    Image phoneImage =
        new Image(
            CluePhoneController.class
                .getResource("/images/cluephoneimages/phoneinteraction.gif")
                .toString());
    phoneImageView = new ImageView(phoneImage);
    // Apply a scaling factor (e.g., 0.14 for 14% of the root node size)
    double scaleFactor = 1;
    phoneImageView.setFitWidth(rootPane.getWidth() * scaleFactor);
    phoneImageView.setFitHeight(rootPane.getHeight() * scaleFactor);

    // Make sure the background resizes with the window, but maintain the scaling
    phoneImageView.fitWidthProperty().bind(rootPane.widthProperty().multiply(scaleFactor));
    phoneImageView.fitHeightProperty().bind(rootPane.heightProperty().multiply(scaleFactor));

    // Use AnchorPane constraints to position the ImageView
    double rightMargin = 0.0; // Increase this value to move further from the right
    double verticalOffset = 50.0; // Move it slightly further down from the center

    AnchorPane.setRightAnchor(phoneImageView, rightMargin); // Move it 100px from the right
    AnchorPane.setTopAnchor(
        phoneImageView,
        (rootPane.getHeight() - phoneImageView.getFitHeight()) / 2
            + verticalOffset); // Vertically slightly lower

    // Bind TopAnchor to keep the image vertically responsive as the window resizes
    phoneImageView
        .fitHeightProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              AnchorPane.setTopAnchor(
                  phoneImageView,
                  (rootPane.getHeight() - newVal.doubleValue()) / 2 + verticalOffset);
            });

    // Enlarge image and change cursor on hover
    phoneImageView.setOnMouseEntered(
        e -> {
          phoneImageView.setScaleX(1.05); // Enlarge by 10%
          phoneImageView.setScaleY(1.05);
          phoneImageView.setCursor(Cursor.HAND); // Change to pointer cursor
        });

    phoneImageView.setOnMouseExited(
        e -> {
          phoneImageView.setScaleX(1.0); // Return to original size
          phoneImageView.setScaleY(1.0);
          phoneImageView.setCursor(Cursor.DEFAULT); // Reset cursor to default
        });

    phoneImageView.setOnMousePressed(
        e -> {
          // Set the initial position of the phone image
          handleArrowButtonClick();
        });

    rootPane.getChildren().add(phoneImageView);

    // Create arrow button on the left side of the phoneImage
    arrowButton = new Button("?"); // Unicode left arrow, you can also use an image
    arrowButton.setStyle(
        "-fx-background-color: #c1b8b5; -fx-background-radius: 50px; -fx-border-radius: 50px;"
            + " -fx-border-color: #3f2218; -fx-border-width: 4px; -fx-text-fill:"
            + " black;-fx-font-size: 14px;"); // Larger font and clear border for visibility
    arrowButton.setPrefWidth(30); // Set a fixed width
    arrowButton.setPrefHeight(30); // Set a fixed height

    // Position the button on the left side
    AnchorPane.setTopAnchor(arrowButton, 20.0); // Set 20px from the left
    AnchorPane.setRightAnchor(arrowButton, 20.0); // Center the button vertically

    arrowButton.setOnMouseEntered(
        e -> {
          // Apply custom styles for hover
          arrowButton.setStyle(
              " -fx-background-color: #775E55; -fx-background-radius:"
                  + " 50px; -fx-border-radius: 50px; -fx-border-color: #3f2218;"
                  + " -fx-border-width: 4px; -fx-cursor:"
                  + " hand; -fx-text-fill: #c1b8b5;"
                  + "-fx-font-size: 14px; ");

          // Pop up of a label to show the user what to do
          popUp = new Label("Click the button to turn the phone around");
          popUp.setStyle(
              "-fx-font-size: 14px; -fx-background-color: c1b8b5; -fx-background-radius: 10px;"
                  + " -fx-border-radius: 10px; -fx-padding: 10px; -fx-border-color: #3f2218;"
                  + " -fx-text-fill: #775E55; -fx-border-width: 3px;");
          popUp.setPrefWidth(266);
          popUp.setPrefHeight(50);
          // Bind the label to the rootPane
          AnchorPane.setBottomAnchor(popUp, 10.0);
          // Set center
          AnchorPane.setLeftAnchor(popUp, (rootPane.getWidth() - popUp.getPrefWidth()) / 2);
          popUp.setOpacity(0.8);
          // Add the label to the rootPane
          rootPane.getChildren().add(popUp);
        });

    // Reset styles on mouse exit
    arrowButton.setOnMouseExited(
        e -> {
          // Reset the button style when the mouse exits
          arrowButton.setStyle(
              "-fx-background-color: #c1b8b5; -fx-background-radius: 50px; -fx-border-radius:"
                  + " 50px; -fx-border-color: #3f2218; -fx-border-width: 4px; -fx-text-fill:"
                  + " black;-fx-font-size: 14px;"
                  + " -fx-cursor: default;");
          // Remove the pop up label
          rootPane.getChildren().remove(popUp);
        });

    arrowButton.getStyleClass().add("button"); // Apply the style class from CSS
    arrowButton.setOpacity(0.8);

    rootPane.getChildren().add(arrowButton); // Add the button to the rootPane
  }

  private void ifphoneisalreadyFound() {
    // Load the phone ringing image
    Image phoneRingingImage =
        new Image(
            CluePhoneController.class
                .getResource("/images/cluephoneimages/phonewvidimg.png")
                .toString());
    ImageView phoneRingingImageView = new ImageView(phoneRingingImage);

    // Bind the ImageView's fitWidth and fitHeight to the rootPane's width and height properties
    phoneRingingImageView.setFitWidth(rootPane.getWidth());
    phoneRingingImageView.setFitHeight(rootPane.getHeight());

    // Bind the ImageView's fitWidth to a percentage of the rootPane's width (e.g., 80% of the
    // rootPane width)
    double horizontalScaleFactor = 1; // Change this value to adjust the horizontal size
    phoneRingingImageView
        .fitWidthProperty()
        .bind(rootPane.widthProperty().multiply(horizontalScaleFactor));
    phoneRingingImageView.fitHeightProperty().bind(rootPane.heightProperty());

    // Use AnchorPane constraints to position the ImageView
    double rightMargin = 0.0; // Increase this value to move further from the right
    AnchorPane.setRightAnchor(phoneRingingImageView, rightMargin); // Move it 100px from the right

    rootPane.getChildren().add(phoneRingingImageView);

    // Load the media file path
    String videoPath =
        CluePhoneController.class
            .getResource("/images/cluephoneimages/clueaudiofile.mp4")
            .toExternalForm();
    Media media = new Media(videoPath);

    // Create a media player for the media file
    MediaPlayer mediaPlayer = new MediaPlayer(media);

    // Bind the volume property of the media player to the shared volume control setting
    mediaPlayer
        .volumeProperty()
        .bind(
            Bindings.createDoubleBinding(
                () -> SharedVolumeControl.getInstance().volumeSettingProperty().get() ? 1.0 : 0.0,
                SharedVolumeControl.getInstance().volumeSettingProperty()));

    // Create a MediaView to display the media content
    MediaView mediaView = new MediaView(mediaPlayer);

    // Set whether to preserve the aspect ratio (optional)
    mediaView.setPreserveRatio(false);

    // Bind the MediaView's fitWidth and fitHeight to a percentage of the rootPane's width and
    // height
    double mediaHorizontalScaleFactor = 0.2; // Adjust this value to control horizontal size
    double mediaVerticalScaleFactor = 0.75; // Adjust this value to control vertical size
    mediaView
        .fitWidthProperty()
        .bind(rootPane.widthProperty().multiply(mediaHorizontalScaleFactor));
    mediaView
        .fitHeightProperty()
        .bind(rootPane.heightProperty().multiply(mediaVerticalScaleFactor));

    // Center the MediaView horizontally and vertically
    // Center the MediaView when the screen loads
    double initialCenterX = (rootPane.getWidth() - mediaView.getFitWidth()) / 2 + 10;
    double initialCenterY = (rootPane.getHeight() - mediaView.getFitHeight()) / 2 - 10;
    AnchorPane.setLeftAnchor(mediaView, initialCenterX);
    AnchorPane.setTopAnchor(mediaView, initialCenterY);

    // Add listeners to ensure it stays centered when resized
    rootPane
        .widthProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              double centerX = (newVal.doubleValue() - mediaView.getFitWidth()) / 2 + 10;
              AnchorPane.setLeftAnchor(mediaView, centerX);
            });

    rootPane
        .heightProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              double centerY = (newVal.doubleValue() - mediaView.getFitHeight()) / 2 - 10;
              AnchorPane.setTopAnchor(mediaView, centerY);
            });

    // Add the MediaView to the rootPane
    rootPane.getChildren().add(mediaView);

    // Load the phone ringing image
    Image overlay =
        new Image(
            CluePhoneController.class
                .getResource("/images/cluephoneimages/overlay.png")
                .toString());
    ImageView overlayImageView = new ImageView(overlay);

    // Bind the ImageView's fitWidth and fitHeight to the rootPane's width and height properties
    overlayImageView.setFitWidth(rootPane.getWidth());
    overlayImageView.setFitHeight(rootPane.getHeight());

    // Bind the ImageView's fitWidth to a percentage of the rootPane's width (e.g., 80% of the
    // rootPane width)
    overlayImageView
        .fitWidthProperty()
        .bind(rootPane.widthProperty().multiply(horizontalScaleFactor));
    overlayImageView.fitHeightProperty().bind(rootPane.heightProperty());

    AnchorPane.setRightAnchor(overlayImageView, rightMargin); // Move it 100px from the right

    rootPane.getChildren().add(overlayImageView);

    // Create a circular play/pause button
    Button playPauseButton = new Button("▶");
    playPauseButton.setStyle(
        "-fx-background-radius: 200px; -fx-background-color: #FFFFFF; -fx-font-size: 18px;");
    playPauseButton.setPrefSize(50, 50); // Set size for circular button

    playPauseButton.setOpacity(0.8);

    // Position the button in the center of the media
    AnchorPane.setTopAnchor(playPauseButton, (rootPane.getHeight() - 50) / 2);
    AnchorPane.setLeftAnchor(playPauseButton, (rootPane.getWidth() - 50) / 2 + 10);

    // Make the button responsive to the rootPane size changes
    rootPane
        .heightProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              AnchorPane.setTopAnchor(playPauseButton, (newVal.doubleValue() - 50) / 2);
            });
    rootPane
        .widthProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              AnchorPane.setLeftAnchor(playPauseButton, (newVal.doubleValue() - 50) / 2 + 10);
            });
    mediaPlayer.seek(Duration.seconds(1));
    // Toggle between play and pause
    playPauseButton.setOnAction(
        e -> {
          if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            playPauseButton.setText("▶"); // Change button text to "Play"
          } else {
            mediaPlayer.play();
            playPauseButton.setText("⏸"); // Change button text to "Pause"
          }
        });

    // Initially show the button
    playPauseButton.setVisible(true);

    // Show the play/pause button when the video is paused
    mediaPlayer.setOnPaused(() -> playPauseButton.setVisible(true));
    mediaPlayer.setOnEndOfMedia(
        () -> {
          playPauseButton.setText("⟳"); // Change button text to replay symbol
          playPauseButton.setOnAction(
              e -> {
                mediaPlayer.seek(mediaPlayer.getStartTime()); // Reset to start of video
                mediaPlayer.play();
                playPauseButton.setText("⏸"); // Change button back to "Pause" during replay
              });
        });

    // Add the play/pause button to the rootPane
    rootPane.getChildren().add(playPauseButton);
  }

  private void createBackground() {
    // Load background
    Image backgroundImage =
        new Image(
            CluePhoneController.class
                .getResource("/images/cluephoneimages/Phonecluebg.png")
                .toString());

    createAndBindImageView(backgroundImage);
  }

  /**
   * Handles the action when the arrow button is clicked.
   *
   * <p>This method is invoked in response to a click event on the arrow button. It currently logs a
   * message to the console indicating that the button has been clicked. This method can be extended
   * to include additional functionality such as navigating to another screen or updating the user
   * interface based on the button's action.
   */
  private void handleArrowButtonClick() {
    System.out.println("Arrow button clicked!");

    disableArrowButton();

    // Set opacity of phoneImageView to 0 (assuming phoneImageView is defined earlier)
    phoneImageView.setOpacity(0);

    // Load the back of the phone image
    Image backofPhone =
        new Image(
            CluePhoneController.class
                .getResource("/images/cluephoneimages/backofphone.png")
                .toString());

    imageView = new ImageView(backofPhone);

    // Preserve the aspect ratio to avoid squashing
    imageView.setPreserveRatio(false);

    // Bind the ImageView's fitWidth and fitHeight to the rootPane's width and height properties
    imageView.setFitWidth(rootPane.getWidth());
    imageView.setFitHeight(rootPane.getHeight());

    // Bind the ImageView's fitWidth to a percentage of the rootPane's width (e.g., 80% of the
    // rootPane width)
    double horizontalScaleFactor = 0.6; // Change this value to adjust the horizontal size
    imageView.fitWidthProperty().bind(rootPane.widthProperty().multiply(horizontalScaleFactor));
    imageView.fitHeightProperty().bind(rootPane.heightProperty());

    // Use AnchorPane constraints to position the ImageView
    double rightMargin = 200.0; // Increase this value to move further from the right
    AnchorPane.setRightAnchor(imageView, rightMargin); // Move it 100px from the right

    // Add the imageView to the rootPane
    rootPane.getChildren().add(imageView);

    // Create a new label
    label = new Label("Connect the wires to unlock the phone! (From left to right)");
    label.setStyle(
        "-fx-font-size: 14px; -fx-background-color: c1b8b5; -fx-background-radius: 10px;"
            + " -fx-border-radius: 10px; -fx-padding: 10px; -fx-border-color: #3f2218;"
            + " -fx-text-fill: #775E55; -fx-border-width: 3px;");
    label.setPrefWidth(364);
    label.setPrefHeight(50);

    // Bind the label to the rootPane
    AnchorPane.setBottomAnchor(label, 10.0);
    // Set center
    AnchorPane.setLeftAnchor(label, (rootPane.getWidth() - label.getPrefWidth()) / 2);

    label.setOpacity(0.8);

    // Add the label to the rootPane
    rootPane.getChildren().add(label);

    // Create and setup the wiring game
    setupGame();
  }

  /**
   * Sets up the game by creating and positioning draggable start circles and fixed end circles.
   *
   * <p>This method initializes three start circles (red, blue, green) that are draggable and three
   * corresponding end circles that are fixed in position. The positions of the circles are bound to
   * the size and position of an associated image view, ensuring that they adjust dynamically.
   * Finally, all circles are added to the root pane for rendering.
   */
  private void setupGame() {
    // Create start circles (left side)
    startCircleRed = createDraggableCircle(0, 0, Color.RED);
    startCircleBlue = createDraggableCircle(0, 0, Color.BLUE);
    startCircleGreen = createDraggableCircle(0, 0, Color.GREEN);

    // Create end circles (right side)
    endCircleRed = createFixedCircle(0, 0, Color.RED);
    endCircleBlue = createFixedCircle(0, 0, Color.BLUE);
    endCircleGreen = createFixedCircle(0, 0, Color.GREEN);

    // Bind start circle positions to the imageView's size and position, using layoutXProperty and
    // fitWidthProperty
    startCircleRed
        .centerXProperty()
        .bind(imageView.layoutXProperty().add(imageView.fitWidthProperty().multiply(0.3)));
    startCircleRed
        .centerYProperty()
        .bind(imageView.layoutYProperty().add(imageView.fitHeightProperty().multiply(0.39)));

    startCircleBlue
        .centerXProperty()
        .bind(imageView.layoutXProperty().add(imageView.fitWidthProperty().multiply(0.3)));
    startCircleBlue
        .centerYProperty()
        .bind(imageView.layoutYProperty().add(imageView.fitHeightProperty().multiply(0.53)));

    startCircleGreen
        .centerXProperty()
        .bind(imageView.layoutXProperty().add(imageView.fitWidthProperty().multiply(0.3)));
    startCircleGreen
        .centerYProperty()
        .bind(imageView.layoutYProperty().add(imageView.fitHeightProperty().multiply(0.67)));

    // Bind end circle positions to the imageView's size and position
    endCircleRed
        .centerXProperty()
        .bind(imageView.layoutXProperty().add(imageView.fitWidthProperty().multiply(0.7).add(120)));
    endCircleRed
        .centerYProperty()
        .bind(imageView.layoutYProperty().add(imageView.fitHeightProperty().multiply(0.39)));

    endCircleBlue
        .centerXProperty()
        .bind(imageView.layoutXProperty().add(imageView.fitWidthProperty().multiply(0.7).add(120)));
    endCircleBlue
        .centerYProperty()
        .bind(imageView.layoutYProperty().add(imageView.fitHeightProperty().multiply(0.53)));

    endCircleGreen
        .centerXProperty()
        .bind(imageView.layoutXProperty().add(imageView.fitWidthProperty().multiply(0.7).add(120)));
    endCircleGreen
        .centerYProperty()
        .bind(imageView.layoutYProperty().add(imageView.fitHeightProperty().multiply(0.67)));

    // Add the circles to the pane
    rootPane
        .getChildren()
        .addAll(
            startCircleRed,
            startCircleBlue,
            startCircleGreen,
            endCircleRed,
            endCircleBlue,
            endCircleGreen);
  }

  /**
   * Creates a draggable circle at the specified coordinates with the given color.
   *
   * <p>This method initializes a {@code Circle} object with a specified position and color. It also
   * sets up mouse event handlers to make the circle draggable within the scene. The circle's size
   * is fixed, and it will respond to mouse actions for dragging.
   *
   * @param x the x-coordinate of the circle's center.
   * @param y the y-coordinate of the circle's center.
   * @param color the {@code Color} of the circle.
   * @return the created {@code Circle} object that can be dragged around the scene.
   */
  private Circle createDraggableCircle(double x, double y, Color color) {
    Circle circle = new Circle(x, y, 10, color);
    circle.setOnMousePressed(event -> onStartDrag(event, circle));
    circle.setOnMouseDragged(this::onDrag);
    circle.setOnMouseReleased(this::onEndDrag);
    return circle;
  }

  /**
   * Creates a fixed circle at the specified coordinates with the given color.
   *
   * <p>This method initializes a {@code Circle} object with a specified position and color. The
   * circle is fixed in place and does not respond to mouse events for dragging, making it suitable
   * for use as a static element in the scene.
   *
   * @param x the x-coordinate of the circle's center.
   * @param y the y-coordinate of the circle's center.
   * @param color the {@code Color} of the circle.
   * @return the created {@code Circle} object that is fixed in the scene.
   */
  private Circle createFixedCircle(double x, double y, Color color) {
    Circle circle = new Circle(x, y, 10, color);
    return circle;
  }

  /**
   * Called when dragging of the specified circle starts.
   *
   * <p>This method initializes the dragging process by setting the currently active circle to be
   * dragged. It creates a new {@code Line} object that visually represents the wire connected to
   * the circle while dragging. The line's properties, such as stroke width and color, are set
   * according to the circle's attributes. The line's starting point is set to the center of the
   * circle, and it is added to the root pane for visual representation.
   *
   * @param event the mouse event triggered when the dragging starts.
   * @param circle the {@code Circle} that is currently being dragged.
   */
  private void onStartDrag(MouseEvent event, Circle circle) {
    activeStartCircle = circle;

    // Create a new line to represent the wire while dragging
    activeWire = new Line();
    activeWire.setStrokeWidth(5);
    activeWire.setStrokeLineCap(StrokeLineCap.ROUND);
    activeWire.setStroke(circle.getFill());

    activeWire.setStartX(circle.getCenterX());
    activeWire.setStartY(circle.getCenterY());

    rootPane.getChildren().add(activeWire);
  }

  /**
   * Called while the dragging of the circle is in progress.
   *
   * <p>This method updates the endpoint of the active wire representation based on the current
   * mouse position. If an active wire exists, its endpoint is set to the coordinates of the mouse
   * event, visually connecting the wire to the position of the mouse cursor while dragging the
   * circle.
   *
   * @param event the mouse event triggered during the dragging action.
   */
  private void onDrag(MouseEvent event) {
    if (activeWire != null) {
      activeWire.setEndX(event.getX());
      activeWire.setEndY(event.getY());
    }
  }

  /**
   * Called when the dragging of the circle ends.
   *
   * <p>This method finalizes the dragging action by determining if the dragged circle was dropped
   * onto a valid end circle. If a valid end circle is found, the method snaps the active wire to
   * the center of that end circle and updates the connection flags for the corresponding wires. If
   * the drop location is invalid, the active wire is removed from the scene. The method also checks
   * if all wire connections are complete and resets the active wire and circle references.
   *
   * @param event the mouse event triggered when the dragging ends.
   */
  private void onEndDrag(MouseEvent event) {
    if (activeWire != null && activeStartCircle != null) {
      // Check if the user dropped on a valid end circle
      Circle endCircle = getMatchingEndCircle(activeStartCircle, event.getX(), event.getY());
      if (endCircle != null) {
        // Snap the wire to the correct position
        activeWire.setEndX(endCircle.getCenterX());
        activeWire.setEndY(endCircle.getCenterY());

        // Set the corresponding wire flag to true and assign the wire
        if (activeStartCircle == startCircleRed && endCircle == endCircleRed) {
          isRedWireConnected = true;
          redWire = activeWire;
        } else if (activeStartCircle == startCircleBlue && endCircle == endCircleBlue) {
          isBlueWireConnected = true;
          blueWire = activeWire;
        } else if (activeStartCircle == startCircleGreen && endCircle == endCircleGreen) {
          isGreenWireConnected = true;
          greenWire = activeWire;
        }

        // Check if all wires are connected
        checkAllConnections();
      } else {
        // If not on a valid end circle, remove the wire
        rootPane.getChildren().remove(activeWire);
      }
    }
    // Clear the current active wire
    activeWire = null;
    activeStartCircle = null;
  }

  // Check if all wires are connected
  private void checkAllConnections() {
    if (isRedWireConnected && isBlueWireConnected && isGreenWireConnected) {
      System.out.println("All wires are connected!");

      // Set a 0.5 second delay before the phone rings
      delayExecution(100);

      // Set the opacity of all wires and labels to 0
      setOpacityAll(0);

      // Load and display the phone ringing image
      ImageView phoneRingingImageView =
          createImageView("/images/cluephoneimages/phonewvidimg.png", 1, 1);
      rootPane.getChildren().add(phoneRingingImageView);

      // Load and play the video
      MediaPlayer mediaPlayer =
          createAndConfigureMediaPlayer("/images/cluephoneimages/clueaudiofile.mp4");
      MediaView mediaView = createMediaView(mediaPlayer, 0.2, 0.75);
      configureMediaViewAnchors(mediaView);
      rootPane.getChildren().add(mediaView);

      // Load and display the overlay image
      ImageView overlayImageView = createImageView("/images/cluephoneimages/overlay.png", 1, 1);
      rootPane.getChildren().add(overlayImageView);

      // Create and add the circular play/pause button
      Button playPauseButton = createPlayPauseButton(mediaPlayer, 50, 50);
      positionButton(playPauseButton, rootPane.getWidth(), rootPane.getHeight());
      configureButtonResponsiveness(playPauseButton, rootPane);
      rootPane.getChildren().add(playPauseButton);

      // Set opacity of all wires and circles to 0
      setOpacityAllCirclesAndWires(0);

      GameStateContext.getInstance().setPhoneFound(true); // Mark as found in the context
    }
  }

  // Helper method to set opacity of wires and label
  private void setOpacityAll(double opacity) {
    imageView.setOpacity(opacity);
    label.setOpacity(opacity);
  }

  // Helper method to set opacity of all wires and circles
  private void setOpacityAllCirclesAndWires(double opacity) {
    // setting the opacity of the wires
    redWire.setOpacity(opacity);
    blueWire.setOpacity(opacity);
    greenWire.setOpacity(opacity);

    // setting the opacity of the start and end circles
    startCircleRed.setOpacity(opacity);
    startCircleBlue.setOpacity(opacity);
    startCircleGreen.setOpacity(opacity);
    endCircleRed.setOpacity(opacity);
    endCircleBlue.setOpacity(opacity);
    endCircleGreen.setOpacity(opacity);
  }

  // Helper method to create and configure an ImageView
  private ImageView createImageView(
      String imagePath, double horizontalScaleFactor, double verticalScaleFactor) {
    Image image = new Image(CluePhoneController.class.getResource(imagePath).toString());
    ImageView imageView = new ImageView(image);
    imageView.fitWidthProperty().bind(rootPane.widthProperty().multiply(horizontalScaleFactor));
    imageView.fitHeightProperty().bind(rootPane.heightProperty().multiply(verticalScaleFactor));
    return imageView;
  }

  // Helper method to create and configure a MediaPlayer
  private MediaPlayer createAndConfigureMediaPlayer(String videoPath) {
    // Load the media file and bind the volume to the shared volume control setting
    Media media = new Media(CluePhoneController.class.getResource(videoPath).toExternalForm());
    MediaPlayer mediaPlayer = new MediaPlayer(media);
    BooleanProperty isVolumeOn = SharedVolumeControl.getInstance().volumeSettingProperty();
    mediaPlayer
        .volumeProperty()
        .bind(Bindings.createDoubleBinding(() -> isVolumeOn.get() ? 1.0 : 0.0, isVolumeOn));
    return mediaPlayer;
  }

  // Helper method to create and configure a MediaView
  private MediaView createMediaView(
      MediaPlayer mediaPlayer, double horizontalScaleFactor, double verticalScaleFactor) {
    MediaView mediaView = new MediaView(mediaPlayer);
    mediaView.setPreserveRatio(false);
    mediaView.fitWidthProperty().bind(rootPane.widthProperty().multiply(horizontalScaleFactor));
    mediaView.fitHeightProperty().bind(rootPane.heightProperty().multiply(verticalScaleFactor));
    return mediaView;
  }

  // Helper method to configure anchors for MediaView
  private void configureMediaViewAnchors(MediaView mediaView) {
    AnchorPane.setLeftAnchor(mediaView, (rootPane.getWidth() - mediaView.getFitWidth()) / 2 + 10);
    AnchorPane.setTopAnchor(mediaView, (rootPane.getHeight() - mediaView.getFitHeight()) / 2 - 10);

    // Add listeners to ensure the width stays centered when resized
    rootPane
        .widthProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              double centerX = (newVal.doubleValue() - mediaView.getFitWidth()) / 2 + 10;
              AnchorPane.setLeftAnchor(mediaView, centerX);
            });
    // Add listeners to ensure the height stays centered when resized
    rootPane
        .heightProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              double centerY = (newVal.doubleValue() - mediaView.getFitHeight()) / 2 - 10;
              AnchorPane.setTopAnchor(mediaView, centerY);
            });
  }

  // Helper method to create the circular play/pause button
  private Button createPlayPauseButton(MediaPlayer mediaPlayer, double width, double height) {
    Button playPauseButton = new Button("▶");
    playPauseButton.setStyle(
        "-fx-background-radius: 200px; -fx-background-color: #FFFFFF; -fx-font-size: 18px;");
    playPauseButton.setPrefSize(width, height);
    playPauseButton.setOpacity(0.8);

    playPauseButton.setOnAction(
        e -> {
          if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            playPauseButton.setText("▶"); // Change button text to "Play"
          } else {
            mediaPlayer.play();
            playPauseButton.setText("⏸"); // Change button text to "Pause"
          }
        });

    mediaPlayer.setOnPaused(() -> playPauseButton.setVisible(true));
    mediaPlayer.setOnEndOfMedia(
        () -> {
          playPauseButton.setText("⟳"); // Change button text to replay symbol
          playPauseButton.setOnAction(
              e -> {
                mediaPlayer.seek(mediaPlayer.getStartTime()); // Reset to start of video
                mediaPlayer.play();
                playPauseButton.setText("⏸"); // Change button back to "Pause" during replay
              });
        });

    return playPauseButton;
  }

  // Helper method to position the button in the center
  private void positionButton(Button button, double parentWidth, double parentHeight) {
    AnchorPane.setTopAnchor(button, (parentHeight - button.getPrefHeight()) / 2);
    AnchorPane.setLeftAnchor(button, (parentWidth - button.getPrefWidth()) / 2 + 10);
  }

  // Helper method to make the button responsive to rootPane size changes
  private void configureButtonResponsiveness(Button button, Pane rootPane) {
    // Making the height responsive
    rootPane
        .heightProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              AnchorPane.setTopAnchor(button, (newVal.doubleValue() - button.getPrefHeight()) / 2);
            });
    // making the width responsive
    rootPane
        .widthProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              AnchorPane.setLeftAnchor(
                  button, (newVal.doubleValue() - button.getPrefWidth()) / 2 + 10);
            });
  }

  // Helper method to add a delay
  private void delayExecution(int milliseconds) {
    try {
      Thread.sleep(milliseconds);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns the matching end circle based on the start circle and the given coordinates.
   *
   * @param startCircle the starting circle
   * @param x the x-coordinate to check
   * @param y the y-coordinate to check
   * @return the matching end circle, or null if no match is found
   */
  private Circle getMatchingEndCircle(Circle startCircle, double x, double y) {
    Circle targetEndCircle = null;

    // Check if the start circle and compare it to the end circle
    if (startCircle == startCircleRed && isInsideCircle(endCircleRed, x, y)) {
      targetEndCircle = endCircleRed;
    } else if (startCircle == startCircleBlue && isInsideCircle(endCircleBlue, x, y)) {
      targetEndCircle = endCircleBlue;
    } else if (startCircle == startCircleGreen && isInsideCircle(endCircleGreen, x, y)) {
      targetEndCircle = endCircleGreen;
    }

    return targetEndCircle;
  }

  /**
   * Checks if a given point is inside the specified circle.
   *
   * <p>This method calculates the distance between the center of the circle and the provided
   * coordinates (x, y). It determines if the point lies within the circle's radius, returning
   * {@code true} if it does and {@code false} otherwise.
   *
   * @param circle the {@code Circle} object used for the check.
   * @param x the x-coordinate of the point to check.
   * @param y the y-coordinate of the point to check.
   * @return {@code true} if the point is inside the circle; {@code false} otherwise.
   */
  private boolean isInsideCircle(Circle circle, double x, double y) {
    double dx = x - circle.getCenterX();
    double dy = y - circle.getCenterY();
    double distance = Math.sqrt(dx * dx + dy * dy);
    return distance <= circle.getRadius();
  }

  /**
   * Creates an ImageView for the specified image and binds its dimensions to the root pane.
   *
   * <p>This method initializes an {@code ImageView} with the provided image, setting its initial
   * width and height to match the dimensions of the root pane. It also establishes bindings for the
   * width and height properties of the image view to ensure that the image resizes responsively
   * when the root pane is resized. Finally, the image view is added to the root pane's children for
   * display.
   *
   * @param image the {@code Image} to be displayed in the ImageView.
   */
  private void createAndBindImageView(Image image) {
    ImageView imageView = new ImageView(image);
    imageView.setFitWidth(rootPane.getWidth());
    imageView.setFitHeight(rootPane.getHeight());

    imageView.fitWidthProperty().bind(rootPane.widthProperty());
    imageView.fitHeightProperty().bind(rootPane.heightProperty());

    rootPane.getChildren().add(imageView);
  }

  // Create a method to disable the "Turn phone around" button
  public void disableArrowButton() {
    arrowButton.setDisable(true);
    arrowButton.setOpacity(0.5); // lower the opacity to visually indicate it's disabled
  }

  /** Create a Pane to display the countdown timer. */
  private void createTimerPane() {
    // Bind the timerLabel to the countdown timer
    countdownTimer = SharedTimerModel.getInstance().getTimer();
    countdownTimer.start();

    // Bind the timer label to the countdown timer
    lbltimer.textProperty().bind(countdownTimer.timeStringProperty());
  }
}
