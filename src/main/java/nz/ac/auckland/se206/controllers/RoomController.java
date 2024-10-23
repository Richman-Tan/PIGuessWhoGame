package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.states.GameStarted;
import nz.ac.auckland.se206.utils.VolumeControlUtil;

/**
 * Controller class for the room view. Handles user interactions within the room where the user can
 * chat with customers and guess their profession.
 */
public class RoomController {

  private static final GameStateContext context = GameStateContext.getInstance();
  private static final String INITIAL_AUDIO =
      GameStarted.class.getClassLoader().getResource("sounds/initialaudio.mp3").toExternalForm();

  /**
   * Returns the initial audio file path.
   *
   * @return the initial audio file path
   */
  public static String getInitialaudio() {
    return INITIAL_AUDIO;
  }

  // 7. Instance Fields (Including FXML fields and other instance variables)
  // FXML fields (usually UI controls)
  @FXML private AnchorPane rootNode;
  @FXML private Button guessButton;
  @FXML private Label lbltimer;
  @FXML private ImageView clue1;
  @FXML private ImageView clue2;
  @FXML private ImageView clue3;
  @FXML private VBox viewBox;
  @FXML private ImageView basemapimg;
  @FXML private Label lblareastatus;
  @FXML private ImageView widowiconimg;
  @FXML private ImageView menuclosedimg;
  @FXML private ImageView brothericonimg;
  @FXML private ImageView grandsoniconimg;
  @FXML private ImageView topofmenubtn;
  @FXML private Pane timerpane;

  // Other instance fields
  private MediaPlayer mediaPlayer;
  private boolean isFirstTimeInit = context.isFirstTimeInit();
  protected TimerModel countdownTimer;
  private VolumeControlUtil volumeControlUtil;

  private final boolean isatleastoncecluefound =
      context.isGardenToolFound() || context.isPhoneFound() || context.isNoteFound();

  // Graphical components
  private Group drawGroup;

  /**
   * Initializes the room after the associated FXML has been loaded.
   *
   * <p>This method is automatically called by the JavaFX framework when the FXML file for the room
   * view is loaded. It sets up the initial state of the room controller by configuring UI
   * components, binding properties, and initializing any necessary data structures or event
   * listeners required for the controller's functionality.
   */
  @FXML // This method is called by the FXMLLoader when initialization is complete
  public void initialize() {

    lblareastatus.setText("You are in the: Crime Scene");

    // Set the initial opacity of clue1 to 0 (hidden)
    clue1.setOpacity(0);

    // set anchorpane of clue 1 to 10px to the right
    AnchorPane.setRightAnchor(clue1, 12.0);

    clue2.setOpacity(0);

    // set anchorpane of clue 2 to 10px to the right
    AnchorPane.setRightAnchor(clue2, 15.0);

    clue3.setOpacity(0);

    // set anchorpane of clue 3 to 10px to the right
    AnchorPane.setRightAnchor(clue3, 15.0);

    Media initial = new Media(INITIAL_AUDIO);
    mediaPlayer = new MediaPlayer(initial);
    BooleanProperty volumeSettingProperty =
        SharedVolumeControl.getInstance().volumeSettingProperty();

    // Bind the mediaPlayer's volume property using a DoubleBinding
    mediaPlayer
        .volumeProperty()
        .bind(
            Bindings.createDoubleBinding(
                () ->
                    volumeSettingProperty.get()
                        ? 1.0
                        : 0.0, // Use 1.0 for full volume if true, otherwise 0.0 for mute
                volumeSettingProperty));

    // Check if the guess button should be enabled
    checkGuessButton();

    // Check if the garden tool has been found
    if (context.isGardenToolFound()) {
      // If found, set the opacity of clue1 to 1 (visible)
      clue1.setOpacity(1);
    }

    // Check if the phone has been found
    if (context.isPhoneFound()) {
      // If found, set the opacity of clue2 to 1 (visible)
      clue2.setOpacity(1);
    }

    // Check if the note has been found
    if (context.isNoteFound()) {
      // If found, set the opacity of clue3 to 1 (visible)
      clue3.setOpacity(1);
    }

    // Add the volume button to the label pane and show it
    volumeControlUtil =
        new VolumeControlUtil(timerpane); // Initialize the VolumeControlUtil with the timerPane
    volumeControlUtil.showVolumeButton(); // Show the volume button

    // Load the images
    final Image image1 =
        new Image(RoomController.class.getResource("/images/drawframe1.PNG").toString());
    final Image image2 =
        new Image(RoomController.class.getResource("/images/drawframe2.PNG").toString());
    final Image image3 =
        new Image(RoomController.class.getResource("/images/drawframe3.PNG").toString());
    final Image image4 =
        new Image(RoomController.class.getResource("/images/drawframe4.PNG").toString());
    final Image image5 =
        new Image(RoomController.class.getResource("/images/drawframe5.PNG").toString());

    // Other initialization (fade transition, timer, label updates, etc.)
    Task<Void> task =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            if (isFirstTimeInit) {

              Platform.runLater(
                  () -> {
                    // Fade in the root node
                    rootNode.setOpacity(0);

                    // Create a fade transition to fade in the root node
                    FadeTransition fadeTransition =
                        new FadeTransition(Duration.millis(1000), rootNode);

                    // Set the opacity values
                    fadeTransition.setFromValue(0);
                    fadeTransition.setToValue(1);
                    fadeTransition.play();
                  });

              Platform.runLater(
                  () -> {
                    if (!SharedVolumeControl.getInstance().getVolumeSetting()) {
                      mediaPlayer.setVolume(0);
                    }
                    mediaPlayer.play();
                  });

              // Set the first time initialization to false
              context.setFirstTimeInit(false);
              isFirstTimeInit = context.isFirstTimeInit();
            }
            return null;
          }
        };

    // Create a new thread and start the task
    Thread thread = new Thread(task);
    thread.setDaemon(true); // Allows the application to exit even if the thread is running
    thread.start();

    // Load background image
    Image backgroundImage =
        new Image(RoomController.class.getResource("/images/Office.jpg").toString());

    // Create the background ImageView and set it to fill the entire pane
    ImageView backgroundImageView = new ImageView(backgroundImage);
    backgroundImageView.setFitWidth(rootNode.getWidth());
    backgroundImageView.setFitHeight(rootNode.getHeight());

    // Make sure the background resizes with the window
    backgroundImageView.fitWidthProperty().bind(rootNode.widthProperty());
    backgroundImageView.fitHeightProperty().bind(rootNode.heightProperty());

    basemapimg.fitWidthProperty().bind(rootNode.widthProperty());
    basemapimg.fitHeightProperty().bind(rootNode.heightProperty());

    widowiconimg.setOnMouseEntered(
        eh -> {
          if (rootNode.getScene() != null) {
            System.out.println("Drag entered");
            widowiconimg.setScaleX(1.1);
            widowiconimg.setScaleY(1.1);
            rootNode.getScene().setCursor(javafx.scene.Cursor.HAND);
            lblareastatus.setText("Go to Widow's Garden?");
          }
        });

    widowiconimg.setOnMouseExited(
        eh -> {
          if (rootNode.getScene() != null) {
            System.out.println("Drag exited");
            widowiconimg.setScaleX(1);
            widowiconimg.setScaleY(1);
            rootNode.getScene().setCursor(javafx.scene.Cursor.DEFAULT);
            lblareastatus.setText("You are in the: Crime Scene");
          }
        });
    brothericonimg.setOnMouseEntered(
        eh -> {
          if (rootNode.getScene() != null) {
            System.out.println("Drag entered");
            // set a little grow
            brothericonimg.setScaleX(1.1);
            brothericonimg.setScaleY(1.1);

            // change cursor
            rootNode.getScene().setCursor(javafx.scene.Cursor.HAND);
            lblareastatus.setText("Go to Brother's Room?");
          }
        });

    brothericonimg.setOnMouseExited(
        eh -> {
          if (rootNode.getScene() != null) {
            System.out.println("Drag exited");
            // set a little grow
            brothericonimg.setScaleX(1);
            brothericonimg.setScaleY(1);

            // change cursor
            rootNode.getScene().setCursor(javafx.scene.Cursor.DEFAULT);
            lblareastatus.setText("You are in the: Crime Scene");
          }
        });

    grandsoniconimg.setOnMouseEntered(
        eh -> {
          if (rootNode.getScene() != null) {
            System.out.println("Drag entered");
            // set a little grow
            grandsoniconimg.setScaleX(1.1);
            grandsoniconimg.setScaleY(1.1);

            // change cursor
            rootNode.getScene().setCursor(javafx.scene.Cursor.HAND);
            lblareastatus.setText("Go to Grandson's Room?");
          }
        });

    grandsoniconimg.setOnMouseExited(
        eh -> {
          if (rootNode.getScene() != null) {
            System.out.println("Drag exited");
            // set a little grow
            grandsoniconimg.setScaleX(1);
            grandsoniconimg.setScaleY(1);

            // change cursor
            rootNode.getScene().setCursor(javafx.scene.Cursor.DEFAULT);
            lblareastatus.setText("You are in the: Crime Scene");
          }
        });

    topofmenubtn.setOnMouseEntered(
        eh -> {
          // change cursor
          rootNode.getScene().setCursor(javafx.scene.Cursor.HAND);
          lblareastatus.setText("Close Menu?");
        });

    topofmenubtn.setOnMouseExited(
        eh -> {
          // change cursor
          rootNode.getScene().setCursor(javafx.scene.Cursor.DEFAULT);
          lblareastatus.setText("You are in the: Crime Scene");
        });

    topofmenubtn.setOnMouseClicked(
        eh -> {
          topofmenubtn.toBack();
          basemapimg.toBack();
          lblareastatus.toBack();
          widowiconimg.toBack();
          grandsoniconimg.toBack();
          brothericonimg.toBack();
          menuclosedimg.toFront();
        });

    // Create a DropShadow effect
    DropShadow dropShadow = new DropShadow();
    dropShadow.setOffsetX(5); // Smaller offset
    dropShadow.setOffsetY(5);
    dropShadow.setRadius(6); // Reduce blur radius
    dropShadow.setSpread(0.07); // Lower spread for less intensity
    dropShadow.setColor(Color.color(0, 0, 0, 0.4)); // Less opacity for lighter shadow

    // Load the images
    final ImageView file1 = createAndBindImageView(image1); // Displayed initially
    final ImageView file2 = createAndBindImageView(image2);
    final ImageView file3 = createAndBindImageView(image3);
    final ImageView file4 = createAndBindImageView(image4);
    final ImageView file5 = createAndBindImageView(image5);

    // Apply the drop shadow effect to the images
    applyDropShadow(dropShadow, file1, file2, file3, file4, file5);

    // Instantiate an object called drawGroup and add the first image
    drawGroup = new Group(file1);

    // Position the image at the bottom-right corner of the AnchorPane
    AnchorPane.setRightAnchor(drawGroup, -10.0); // 10px from the right
    AnchorPane.setBottomAnchor(drawGroup, -10.0); // 10px from the bottom

    // Add the drawGroup to the AnchorPane (rootNode)
    rootNode.getChildren().addAll(backgroundImageView, drawGroup);

    // Set the drawGroup to the back
    drawGroup.toBack();

    // Set the background image to the back
    backgroundImageView.toBack();

    // Set hover effect
    addHoverEffect(drawGroup);

    // Set mouse click behavior to trigger the animation
    drawGroup.setOnMouseClicked(e -> playAnimation(file1, file2, file3, file4, file5));

    // Apply the orange drop shadow effect to the drawGroup
    applyOrangeDropShadow(drawGroup);

    backgroundImageView.toBack();

    // Timer and other UI-related updates
    countdownTimer = SharedTimerModel.getInstance().getTimer();
    countdownTimer.start();

    // Bind the timer label to the countdown timer
    lbltimer.textProperty().bind(countdownTimer.timeStringProperty());

    countdownTimer
        .timeStringProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              checkiftimeris4minleft();
            });

    // Load background image
    Image phoneClueImage =
        new Image(RoomController.class.getResource("/images/phoneclue.png").toString());

    // Create the background ImageView and set an initial size (scale)
    ImageView phoneClueImageView = new ImageView(phoneClueImage);

    // Set whether to preserve the aspect ratio (optional)
    phoneClueImageView.setPreserveRatio(false);

    // Bind the MediaView's fitWidth and fitHeight to a percentage of the rootPane's width and
    // height
    double mediaHorizontalScaleFactor = 0.1; // Adjust this value to control horizontal size
    double mediaVerticalScaleFactor = 0.1; // Adjust this value to control vertical size
    phoneClueImageView
        .fitWidthProperty()
        .bind(rootNode.widthProperty().multiply(mediaHorizontalScaleFactor));
    phoneClueImageView
        .fitHeightProperty()
        .bind(rootNode.heightProperty().multiply(mediaVerticalScaleFactor));

    // Center the MediaView horizontally and vertically
    // Center the MediaView when the screen loads
    double initialCenterX = (rootNode.getWidth() - phoneClueImageView.getFitWidth()) / 2 + 170;
    double initialCenterY = (rootNode.getHeight() - phoneClueImageView.getFitHeight()) / 2 + 80;
    AnchorPane.setLeftAnchor(phoneClueImageView, initialCenterX);
    AnchorPane.setTopAnchor(phoneClueImageView, initialCenterY);

    applyOrangeDropShadow(phoneClueImageView);

    // Add listeners to ensure it stays centered when resized
    rootNode
        .widthProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              double centerX = (newVal.doubleValue() - phoneClueImageView.getFitWidth()) / 2 + 170;
              AnchorPane.setLeftAnchor(phoneClueImageView, centerX);
            });

    rootNode
        .heightProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              double centerY = (newVal.doubleValue() - phoneClueImageView.getFitHeight()) / 2 + 80;
              AnchorPane.setTopAnchor(phoneClueImageView, centerY);
            });

    DropShadow hoverShadow = new DropShadow();
    hoverShadow.setColor(Color.CORNFLOWERBLUE); // Customize the hover effect color
    hoverShadow.setRadius(10); // Customize the shadow effect

    // Apply hover effect
    phoneClueImageView.setOnMouseEntered(
        e -> {
          phoneClueImageView.setEffect(hoverShadow); // Apply hover effect when mouse enters
        });
    phoneClueImageView.setOnMouseExited(
        e -> {
          applyOrangeDropShadow(phoneClueImageView); // Remove effect when mouse exits
        });
    phoneClueImageView.setOnMouseClicked(
        e -> {
          try {
            // Handle the phone clue click event
            App.setRoot("cluephone");
          } catch (IOException e1) {
            System.err.println("Error loading the phone clue scene");
          }
        });

    // Add the ImageView to the root node
    rootNode.getChildren().addAll(phoneClueImageView);

    // Load background image
    Image paintingImage =
        new Image(RoomController.class.getResource("/images/cluepainting.png").toString());

    // Create the background ImageView and set it to fill the entire pane
    ImageView paintingImageView = new ImageView(paintingImage);
    paintingImageView.setFitWidth(rootNode.getWidth());
    paintingImageView.setFitHeight(rootNode.getHeight());

    // Make sure the background resizes with the window
    paintingImageView.fitWidthProperty().bind(rootNode.widthProperty());
    paintingImageView.fitHeightProperty().bind(rootNode.heightProperty());

    // Create a DropShadow effect
    DropShadow dropShadowPainting = new DropShadow();
    dropShadowPainting.setOffsetX(5); // Smaller offset
    dropShadowPainting.setOffsetY(5);
    dropShadowPainting.setRadius(5); // Reduce blur radius
    dropShadowPainting.setSpread(2); // Lower spread for less intensity
    dropShadowPainting.setColor(Color.color(0, 0, 0, 0.5)); // Less opacity for lighter shadow

    // Apply drop shadow effect to the painting image
    applyOrangeDropShadow(paintingImageView);

    // Apply hover effect
    DropShadow hoverShadowPainting = new DropShadow();
    hoverShadowPainting.setColor(Color.CORNFLOWERBLUE); // Customize the hover effect color
    hoverShadowPainting.setRadius(10); // Customize the shadow effect

    paintingImageView.setOnMouseEntered(
        e -> {
          paintingImageView.setEffect(hoverShadowPainting); // Apply hover effect when mouse enters
        });

    paintingImageView.setOnMouseExited(
        e -> {
          applyOrangeDropShadow(paintingImageView);
        });

    paintingImageView.setOnMouseClicked(
        e -> {
          try {
            handleSafeClick();
          } catch (IOException e1) {
            System.err.println("Error loading the safe clue scene");
          }
        });

    // Add the ImageView to the root node
    rootNode.getChildren().addAll(paintingImageView);

    menuclosedimg.setOnMouseEntered(
        eh -> {
          // change cursor
          rootNode.getScene().setCursor(javafx.scene.Cursor.HAND);
          lblareastatus.setText("Open Menu?");

          // expand
          menuclosedimg.setScaleX(1.1);
          menuclosedimg.setScaleY(1.1);
        });

    menuclosedimg.setOnMouseExited(
        eh -> {
          // change cursor
          if (rootNode.getScene() != null) {
            rootNode.getScene().setCursor(javafx.scene.Cursor.DEFAULT);
          }
          lblareastatus.setText("You are in the: Crime Scene");

          // shrink
          menuclosedimg.setScaleX(1);
          menuclosedimg.setScaleY(1);
        });

    menuclosedimg.setOnMouseClicked(
        eh -> {
          basemapimg.toFront();
          topofmenubtn.toFront();
          lblareastatus.toFront();
          widowiconimg.toFront();
          grandsoniconimg.toFront();
          brothericonimg.toFront();
          menuclosedimg.toBack();
          phoneClueImageView.toFront();
          paintingImageView.toFront();
        });

    // Load background image
    menuclosedimg.toFront();
    // Set the menu to hidden
    basemapimg.toBack();
    lblareastatus.toBack();
    widowiconimg.toBack();
    grandsoniconimg.toBack();
    brothericonimg.toBack();
    topofmenubtn.toBack();

    viewBox.toFront();
    clue1.toFront();
    clue2.toFront();
    clue3.toFront();
  }

  /**
   * Adds a hover effect to the specified group.
   *
   * <p>This method applies a drop shadow effect with a cornflower blue color to the given {@code
   * Group} when the mouse pointer enters its area. When the mouse exits, it reverts the effect by
   * applying an orange drop shadow instead. This creates a visual cue for user interaction and
   * enhances the overall user experience.
   *
   * @param group the {@code Group} to which the hover effect will be applied.
   */
  private void addHoverEffect(Group group) {
    DropShadow hoverShadow = new DropShadow();
    hoverShadow.setColor(Color.CORNFLOWERBLUE); // Customize the hover effect color
    hoverShadow.setRadius(10); // Customize the shadow effect

    group.setOnMouseEntered(
        e -> {
          group.setEffect(hoverShadow); // Apply hover effect when mouse enters
        });

    group.setOnMouseExited(
        e -> {
          applyOrangeDropShadow(group); // Remove effect when mouse exits
        });
  }

  /**
   * Plays the animation of the drawing.
   *
   * @param file1 the first image to display
   * @param file2 the second image to display
   * @param file3 the third image to display
   * @param file4 the fourth image to display
   * @param file5 the fifth image to display
   */
  private void playAnimation(
      ImageView file1, ImageView file2, ImageView file3, ImageView file4, ImageView file5) {
    Timeline timeline = new Timeline();

    // KeyFrames for each image in sequence
    timeline
        .getKeyFrames()
        .add(new KeyFrame(Duration.seconds(0.2), event -> showNextImage(drawGroup, file2)));
    timeline
        .getKeyFrames()
        .add(new KeyFrame(Duration.seconds(0.4), event -> showNextImage(drawGroup, file3)));
    timeline
        .getKeyFrames()
        .add(new KeyFrame(Duration.seconds(0.6), event -> showNextImage(drawGroup, file4)));
    timeline
        .getKeyFrames()
        .add(new KeyFrame(Duration.seconds(0.8), event -> showNextImage(drawGroup, file5)));

    timeline.setCycleCount(1); // Play only once
    timeline.play();

    // 1 sec delay and then on animation finish
    timeline.setOnFinished(
        e -> {
          try {
            // 1 second delay
            Thread.sleep(10);
            App.setRoot("cluetornphotograph");
          } catch (IOException | InterruptedException e1) {
            System.err.println("Error loading the torn photograph clue scene");
          }
        });
  }

  /**
   * Creates an ImageView and binds its width and height to the width and height of the AnchorPane.
   *
   * @param image the image to display in the ImageView
   * @return the ImageView with the image and bound width and height
   */
  private ImageView createAndBindImageView(Image image) {
    ImageView imageView = new ImageView(image);
    imageView
        .fitWidthProperty()
        .bind(rootNode.widthProperty().multiply(1)); // 100% of AnchorPane width
    imageView
        .fitHeightProperty()
        .bind(rootNode.heightProperty().multiply(1)); // 100% of AnchorPane height
    return imageView;
  }

  /**
   * Replaces the current image in the file with the next image.
   *
   * @param file the file to replace the image in
   * @param nextImage the next image to display
   */
  private void showNextImage(Group file, ImageView nextImage) {
    file.getChildren().setAll(nextImage);
  }

  /**
   * Applies a drop shadow effect to a list of ImageViews.
   *
   * @param dropShadow the drop shadow effect to apply
   * @param imageViews the list of ImageViews to apply the effect to
   */
  private void applyDropShadow(DropShadow dropShadow, ImageView... imageViews) {
    for (ImageView imageView : imageViews) {
      imageView.setEffect(dropShadow);
    }
  }

  /** Checks if the timer is at 4 minutes left and starts the flashing animation. */
  private void checkiftimeris4minleft() {
    if (countdownTimer
        .timeStringProperty()
        .get()
        .equals("01:00")) { // When the time reaches 1 minute left
      startFlashingAnimation(timerpane);
    }
  }

  /**
   * Starts the flashing animation on a pane.
   *
   * @param pane the pane to flash
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
   * Applies an orange drop shadow effect to the specified node.
   *
   * <p>This method creates a {@code DropShadow} effect with an orange color and applies it to the
   * given {@code Node}. The shadow's offset and radius can be adjusted to customize its appearance.
   * This effect enhances the visual presentation of the node, making it stand out in the user
   * interface.
   *
   * @param node the {@code Node} to which the drop shadow effect will be applied.
   */
  private void applyOrangeDropShadow(Node node) {
    DropShadow orangedropShadow = new DropShadow();
    orangedropShadow.setOffsetX(0);
    orangedropShadow.setOffsetY(0);
    orangedropShadow.setRadius(15); // Adjust the radius for desired shadow spread
    orangedropShadow.setColor(Color.GOLD);
    node.setEffect(orangedropShadow);
  }

  /**
   * Handles the key pressed event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyPressed(KeyEvent event) {
    System.out.println("Key " + event.getCode() + " pressed");
  }

  /**
   * Handles the key released event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyReleased(KeyEvent event) {
    System.out.println("Key " + event.getCode() + " released");
  }

  /**
   * Handles the guess button click event.
   *
   * @param event the action event triggered by clicking the guess button
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onGuessClick(ActionEvent event) throws IOException {
    context.setGuessPressed(true);
    App.setRoot("guessingScene");
    context.onGuessClick();
  }

  /**
   * Handles the phone clue click event.
   *
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleSafeClick() throws IOException {
    if (context.isNoteFound() || context.isSafeOpen()) {
      App.setRoot("cluesafeopened");
    } else {
      App.setRoot("cluesafe");
    }
  }

  /**
   * Handles the volume off event.
   *
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onUncleButtonClick() throws IOException {
    App.setRoot("suspect1room");
  }

  /**
   * Handles the volume off event.
   *
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onGrandmotherClick() throws IOException {
    context.setMenuVisible(true); // Toggle the visibility in the context
    App.setRoot("suspect2room");
  }

  /**
   * Handles the volume off event.
   *
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onGrandsonClick() throws IOException {
    App.setRoot("suspect3room");
  }

  /**
   * Handles the volume off event.
   *
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void checkGuessButton() {
    if (context.getListOfVisitors().contains("suspect1")
        && context.getListOfVisitors().contains("suspect2")
        && context.getListOfVisitors().contains("suspect3")
        && isatleastoncecluefound) {
      // Enable the guess button
      guessButton.setOpacity(0.8);
      guessButton.setDisable(false);
    } else {
      // Disable the guess button
      guessButton.setOpacity(0.3);
      guessButton.setDisable(true);
    }
  }
}
