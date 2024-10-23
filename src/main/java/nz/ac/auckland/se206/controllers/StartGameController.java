package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.Random;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.states.GameStarted;

/**
 * Controller class for the start view.
 *
 * <p>This class is responsible for handling user input and updating the view for the start scene.
 * It is used to manage the user interface components and respond to user interactions in the start
 * scene.
 */
public class StartGameController {

  // Static fields
  private static final String door =
      GameStarted.class.getClassLoader().getResource("sounds/doorOpen.mp3").toExternalForm();
  private static final String backgroundMusic =
      GameStarted.class.getClassLoader().getResource("sounds/start.mp3").toExternalForm();

  static final Image image1 =
      new Image(BackstoryController.class.getResource("/images/initialDoor.jpg").toString());
  static final Image image2 =
      new Image(BackstoryController.class.getResource("/images/doorframe2.PNG").toString());
  static final Image image3 =
      new Image(BackstoryController.class.getResource("/images/doorframe3.PNG").toString());
  static final Image image4 =
      new Image(BackstoryController.class.getResource("/images/doorframe4.PNG").toString());
  static final Image image5 =
      new Image(BackstoryController.class.getResource("/images/doorframe5.PNG").toString());
  static final Image image6 =
      new Image(BackstoryController.class.getResource("/images/doorframe6.PNG").toString());
  static final Image image7 =
      new Image(BackstoryController.class.getResource("/images/doorframe7.PNG").toString());
  static final Image image8 =
      new Image(BackstoryController.class.getResource("/images/doorframe8.PNG").toString());
  static final Image image9 =
      new Image(BackstoryController.class.getResource("/images/doorframe9.PNG").toString());

  // Instance fields
  @FXML private AnchorPane rootPane;
  private MediaPlayer mediaPlayer;
  private ImageView imageView;
  private MediaPlayer backgroundPlayer;

  // Constructors

  // Instance methods
  /**
   * Initializes the start after the associated FXML has been loaded.
   *
   * <p>This method is automatically called by the JavaFX framework when the FXML file for the start
   * view is loaded. It sets up the initial state of the start controller by configuring UI
   * components, binding properties, and initializing any necessary data structures or event
   * listeners required for the controller's functionality.
   */
  @FXML
  public void initialize() {
    // Initialize the ImageView with image1 and set it to take up the whole screen
    imageView = new ImageView(image1);
    imageView.setFitWidth(rootPane.getWidth());
    imageView.setFitHeight(rootPane.getHeight());
    imageView.fitWidthProperty().bind(rootPane.widthProperty());
    imageView.fitHeightProperty().bind(rootPane.heightProperty());

    // Add the imageView to the rootPane
    rootPane.getChildren().add(imageView);

    // Load and play the background music
    Media backgroundSound = new Media(backgroundMusic);
    backgroundPlayer = new MediaPlayer(backgroundSound);
    backgroundPlayer.setVolume(0.4); // Set volume to 50%
    BooleanProperty volumeSettingProperty =
        SharedVolumeControl.getInstance().volumeSettingProperty();
    backgroundPlayer
        .volumeProperty()
        .bind(
            Bindings.createDoubleBinding(
                () ->
                    volumeSettingProperty.get() ? 0.4 : 0.0, // Full volume or mute based on setting
                volumeSettingProperty));
    backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop the background music
    backgroundPlayer.play();

    // Set up imageView as a button
    setupImageViewAsButton();

    // Create and animate the snow falling effect
    createSnowFallingEffect();
  }

  /** Sets up the ImageView to act as a button. */
  private void setupImageViewAsButton() {
    // Set hover effects to change the cursor to a pointer (hand)
    imageView.setOnMouseEntered(
        e -> imageView.setStyle("-fx-cursor: hand;")); // Change to pointer on hover
    imageView.setOnMouseExited(
        e -> imageView.setStyle("-fx-cursor: default;")); // Restore to default on exit

    // Load the audio file
    Media sound = new Media(door);
    mediaPlayer = new MediaPlayer(sound);
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

    // Set click action
    imageView.setOnMouseClicked(
        (var e) -> {
          try {
            mediaPlayer.seek(Duration.seconds(1)); // Seek to start of door sound
            if (!SharedVolumeControl.getInstance().getVolumeSetting()) {
              mediaPlayer.setVolume(0);
            }
            mediaPlayer.play();
            backgroundPlayer.stop(); // Stop background music when door sound plays
            onPlay();
          } catch (IOException | ApiProxyException ex) {
            ex.printStackTrace();
          }
        });
  }

  /**
   * Creates an enhanced snow falling effect by generating snowflake nodes and animating them with
   * various properties such as size, speed, direction, and rotation.
   */
  private void createSnowFallingEffect() {
    Random random = new Random();

    // Create a Timeline for generating snowflakes
    Timeline snowTimeline =
        new Timeline(
            new KeyFrame(
                Duration.millis(300),
                event -> {
                  // Create a snowflake with a random size between 2 and 7
                  double radius = random.nextDouble() * 5 + 2;
                  Circle snowflake = new Circle(radius);

                  // Randomly choose a color for the snowflake (white, light blue, or light gray)
                  snowflake.setFill(
                      Color.rgb(
                          255 - random.nextInt(20), // Light variations of white
                          255 - random.nextInt(20),
                          255 - random.nextInt(20),
                          random.nextDouble() * 0.7 + 0.3)); // Opacity between 0.3 and 1.0

                  snowflake.setLayoutX(
                      random.nextDouble() * rootPane.getWidth()); // Set random x-position
                  snowflake.setLayoutY(0); // Start at the top of the screen

                  // Animate the snowflake falling down with random speed and slight left/right
                  // motion
                  TranslateTransition fallTransition =
                      new TranslateTransition(
                          Duration.seconds(5 + random.nextDouble() * 5), snowflake);
                  fallTransition.setByY(rootPane.getHeight());
                  fallTransition.setByX(
                      random.nextDouble() * 40 - 20); // Drift slightly left or right

                  // Rotate the snowflake as it falls
                  RotateTransition rotateTransition =
                      new RotateTransition(
                          Duration.seconds(5 + random.nextDouble() * 5), snowflake);
                  rotateTransition.setByAngle(360);
                  rotateTransition.setInterpolator(Interpolator.LINEAR);
                  rotateTransition.setCycleCount(1);

                  // Combine both transitions
                  ParallelTransition parallelTransition =
                      new ParallelTransition(fallTransition, rotateTransition);
                  parallelTransition.setOnFinished(
                      e -> rootPane.getChildren().remove(snowflake)); // Remove when done

                  rootPane.getChildren().add(snowflake);
                  parallelTransition.play();
                }));

    snowTimeline.setCycleCount(Timeline.INDEFINITE); // Generate snowflakes continuously
    snowTimeline.play();
  }

  /*
   * View with image rotation.
   */
  @FXML
  private void onPlay() throws IOException, ApiProxyException {
    // Create a timeline to rotate through the images
    Timeline timeline = new Timeline();
    timeline
        .getKeyFrames()
        .add(new KeyFrame(Duration.seconds(0.06), e -> imageView.setImage(image3)));

    timeline
        .getKeyFrames()
        .add(new KeyFrame(Duration.seconds(0.12), e -> imageView.setImage(image4)));

    timeline
        .getKeyFrames()
        .add(new KeyFrame(Duration.seconds(0.18), e -> imageView.setImage(image5)));

    timeline
        .getKeyFrames()
        .add(new KeyFrame(Duration.seconds(0.24), e -> imageView.setImage(image6)));

    timeline
        .getKeyFrames()
        .add(new KeyFrame(Duration.seconds(0.3), e -> imageView.setImage(image7)));

    timeline
        .getKeyFrames()
        .add(new KeyFrame(Duration.seconds(0.36), e -> imageView.setImage(image8)));

    timeline
        .getKeyFrames()
        .add(new KeyFrame(Duration.seconds(0.42), e -> imageView.setImage(image9)));

    // After the timeline, add the fade transition to switch to the backstory view
    timeline.setOnFinished(
        e -> {
          FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000));
          fadeTransition.setNode(rootPane);
          fadeTransition.setFromValue(1);
          fadeTransition.setToValue(0);
          fadeTransition.setOnFinished(
              event -> {
                try {
                  App.setRoot("cutscene");
                } catch (IOException ex) {
                  ex.printStackTrace();
                }
              });
          fadeTransition.play();
        });

    // Start the timeline
    timeline.play();
  }
}
