package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.states.GameStarted;
import nz.ac.auckland.se206.utils.VolumeControlUtil;

/**
 * Controller class for the backstory view.
 *
 * <p>This class is responsible for managing the user interface and logic of the backstory view. It
 * displays the backstory of the game through a series of images and transitions to the next scene
 * after a set period. The controller also handles user interactions, such as zooming in on images
 * and managing the volume setting.
 */
public class BackstoryController {

  // Static fields
  private static final String sound =
      GameStarted.class.getClassLoader().getResource("sounds/woosh.mp3").toExternalForm();
  static final Image image1 =
      new Image(BackstoryController.class.getResource("/images/fileframe1.PNG").toString());
  static final Image image2 =
      new Image(BackstoryController.class.getResource("/images/fileframe2.PNG").toString());
  static final Image image3 =
      new Image(BackstoryController.class.getResource("/images/fileframe3.PNG").toString());
  static final Image image4 =
      new Image(BackstoryController.class.getResource("/images/fileframe4.PNG").toString());
  static final Image image5 =
      new Image(BackstoryController.class.getResource("/images/fileframe5.PNG").toString());
  static final Image image6 =
      new Image(BackstoryController.class.getResource("/images/fileframe6.PNG").toString());
  static final Image image7 =
      new Image(BackstoryController.class.getResource("/images/fileframe7.PNG").toString());
  static final Image image8 =
      new Image(BackstoryController.class.getResource("/images/fileframe8.PNG").toString());
  static final Image image9 =
      new Image(BackstoryController.class.getResource("/images/fileframe9.PNG").toString());
  static final Image image10 =
      new Image(BackstoryController.class.getResource("/images/fileframe10.PNG").toString());

  // Instance fields
  @FXML private AnchorPane anchorPane;
  @FXML private Label lbltimer;
  @FXML private Pane labelPane;
  private MediaPlayer mediaPlayer;
  private TimerModel countdownTimer;
  private Group file;

  private VolumeControlUtil volumeControlUtil; // Reference to VolumeControlUtil

  // Constructors (if any)

  // Instance methods
  /**
   * Initializes the backstory view when the FXML component is loaded.
   *
   * <p>This method is automatically called by the JavaFX framework after the FXML file has been
   * loaded. It sets up the initial state of the backstory view, including any necessary UI
   * components, event listeners, and data bindings required for the view's functionality.
   */
  @FXML
  public void initialize() {

    // Fade in the anchor pane
    Platform.runLater(
        () -> {

          // Set the opacity of the anchor pane to 0
          anchorPane.setOpacity(0);

          // Create a fade transition
          FadeTransition fadeTransition = new FadeTransition();
          fadeTransition.setDuration(Duration.millis(1000));
          fadeTransition.setNode(anchorPane);
          fadeTransition.setFromValue(0);
          fadeTransition.setToValue(1);
          fadeTransition.play();
        });

    // Load the background image
    Image backgroundImage =
        new Image(
            BackstoryController.class.getResource("/images/blurredcrimescene.png").toString());

    // Create an image view for the background image
    ImageView backgroundImageView = new ImageView(backgroundImage);
    backgroundImageView.setFitWidth(anchorPane.getWidth());
    backgroundImageView.setFitHeight(anchorPane.getHeight());
    backgroundImageView.fitWidthProperty().bind(anchorPane.widthProperty());
    backgroundImageView.fitHeightProperty().bind(anchorPane.heightProperty());

    // Create the drop shadow effect
    DropShadow dropShadow = new DropShadow();
    dropShadow.setOffsetX(5);
    dropShadow.setOffsetY(5);
    dropShadow.setRadius(6);
    dropShadow.setSpread(0.07);
    dropShadow.setColor(Color.color(0, 0, 0, 0.4));

    // Create the file
    final ImageView file1 = createAndBindImageView(image1);
    final ImageView file2 = createAndBindImageView(image2);
    final ImageView file3 = createAndBindImageView(image3);
    final ImageView file4 = createAndBindImageView(image4);
    final ImageView file5 = createAndBindImageView(image5);
    final ImageView file6 = createAndBindImageView(image6);
    final ImageView file7 = createAndBindImageView(image7);
    final ImageView file8 = createAndBindImageView(image8);
    final ImageView file9 = createAndBindImageView(image9);
    final ImageView file10 = createAndBindImageView(image10);

    // Apply the drop shadow effect to the file
    applyDropShadow(
        dropShadow, file1, file2, file3, file4, file5, file6, file7, file8, file9, file10);

    // Create the file
    file = new Group(file1);
    centerImage(file1);

    // Create the timeline
    Timeline timeline = new Timeline();

    // Add the key frames to the timeline
    timeline
        .getKeyFrames()
        .add(new KeyFrame(Duration.seconds(1.4), e -> showNextImage(file, file2)));
    timeline
        .getKeyFrames()
        .add(new KeyFrame(Duration.seconds(1.49), e -> showNextImage(file, file3)));
    timeline
        .getKeyFrames()
        .add(new KeyFrame(Duration.seconds(1.58), e -> showNextImage(file, file4)));
    timeline
        .getKeyFrames()
        .add(new KeyFrame(Duration.seconds(1.67), e -> showNextImage(file, file5)));
    timeline
        .getKeyFrames()
        .add(new KeyFrame(Duration.seconds(1.76), e -> showNextImage(file, file6)));
    timeline
        .getKeyFrames()
        .add(new KeyFrame(Duration.seconds(1.85), e -> showNextImage(file, file7)));
    timeline
        .getKeyFrames()
        .add(new KeyFrame(Duration.seconds(1.94), e -> showNextImage(file, file8)));
    timeline
        .getKeyFrames()
        .add(new KeyFrame(Duration.seconds(2.03), e -> showNextImage(file, file9)));
    timeline
        .getKeyFrames()
        .add(new KeyFrame(Duration.seconds(2.12), e -> showNextImage(file, file10)));
    timeline.setCycleCount(1);
    timeline.play();

    // Add the background image and file to the anchor pane
    anchorPane.getChildren().addAll(backgroundImageView, file);
    anchorPane.widthProperty().addListener((obs, oldVal, newVal) -> recenterCurrentImage(file));
    anchorPane.heightProperty().addListener((obs, oldVal, newVal) -> recenterCurrentImage(file));

    // Create the additional and enlarged image views
    Image additionalImage =
        new Image(
            BackstoryController.class.getResource("/images/magnifyingglassbtn.png").toString());

    // Create the additional and enlarged image views
    ImageView additionalImageView = new ImageView(additionalImage);
    additionalImageView.fitWidthProperty().bind(anchorPane.widthProperty().multiply(0.32));
    additionalImageView.fitHeightProperty().bind(anchorPane.heightProperty().multiply(0.32));
    additionalImageView.setOpacity(0);
    additionalImageView.setDisable(true);

    // Create the additional and enlarged image views
    ImageView enlargedImageView = new ImageView(additionalImage);
    enlargedImageView.fitWidthProperty().bind(anchorPane.widthProperty().multiply(0.36));
    enlargedImageView.fitHeightProperty().bind(anchorPane.heightProperty().multiply(0.36));
    enlargedImageView.setOpacity(0);
    enlargedImageView.setDisable(true);

    // Add the additional and enlarged image views to the anchor pane
    anchorPane.getChildren().addAll(additionalImageView, enlargedImageView);

    // Update the position of the additional and enlarged image views
    anchorPane
        .widthProperty()
        .addListener(
            (obs, oldVal, newVal) -> updateImagePosition(additionalImageView, enlargedImageView));
    anchorPane
        .heightProperty()
        .addListener(
            (obs, oldVal, newVal) -> updateImagePosition(additionalImageView, enlargedImageView));

    // Create the fade in transition
    FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), additionalImageView);
    fadeIn.setFromValue(0);
    fadeIn.setToValue(1);
    timeline.setOnFinished(e -> fadeIn.play());

    // Create the media player
    Media woosh = new Media(sound);
    mediaPlayer = new MediaPlayer(woosh);
    // Play the sound effect
    mediaPlayer.seek(Duration.seconds(1));
    BooleanProperty volumeSettingProperty =
        SharedVolumeControl.getInstance().volumeSettingProperty();

    // Create a DoubleBinding to bind the volume property of mediaPlayer
    mediaPlayer
        .volumeProperty()
        .bind(
            Bindings.createDoubleBinding(
                () ->
                    volumeSettingProperty.get() ? 1.0 : 0.0, // Full volume if true, otherwise mute
                volumeSettingProperty));

    // Set the on finished event handler
    fadeIn.setOnFinished(
        event -> {
          additionalImageView.setDisable(false);
          enlargedImageView.setDisable(false);
        });

    // Set the hover handlers
    setHoverHandlers(additionalImageView, enlargedImageView);

    // Set the on mouse clicked event handler
    additionalImageView.setOnMouseClicked(e -> zoomIn(additionalImageView, "room"));
    enlargedImageView.setOnMouseClicked(e -> zoomIn(enlargedImageView, "room"));

    // Create the timer pane
    createTimerPane();

    // Add the volume button to the label pane and show it
    volumeControlUtil =
        new VolumeControlUtil(labelPane); // Initialize the VolumeControlUtil with the timerPane
    volumeControlUtil.showVolumeButton(); // Show the volume button
  }

  // Inner classes (if any)

  // Static methods (if any)

  /**
   * Creates a timer pane and binds the countdown timer to the display label.
   *
   * <p>This method initializes the countdown timer by retrieving it from the {@code
   * SharedTimerModel}, starting the timer, and binding its time string property to the {@code
   * lbltimer} label for display. It ensures that the timer is displayed in the user interface and
   * brings the label pane to the front of the view hierarchy for visibility.
   */
  private void createTimerPane() {
    // Create the countdown timer
    countdownTimer = SharedTimerModel.getInstance().getTimer();
    countdownTimer.start();
    lbltimer.textProperty().bind(countdownTimer.timeStringProperty());

    labelPane.toFront();
  }

  /**
   * Creates an {@code ImageView} for the specified image and binds its width and height to the
   * dimensions of the provided anchor pane.
   *
   * <p>The method initializes an {@code ImageView} instance with the given image, sets its
   * dimensions to be responsive by binding them to the width and height of the {@code anchorPane},
   * scaling the image accordingly. The method returns the created {@code ImageView} instance.
   *
   * @param image the {@code Image} to be displayed in the {@code ImageView}.
   * @return the newly created {@code ImageView} bound to the anchor pane dimensions.
   */
  private ImageView createAndBindImageView(Image image) {
    // Create an ImageView
    ImageView imageView = new ImageView(image);

    // Bind the width and height of the ImageView to the width and height of the anchorPane
    imageView.fitWidthProperty().bind(anchorPane.widthProperty().multiply(1.2));
    imageView.fitHeightProperty().bind(anchorPane.heightProperty().multiply(1.2));

    // Set the properties of the ImageView
    imageView.setPreserveRatio(true);
    imageView.setCache(true);

    // Set the cache hint of the ImageView
    imageView.setCacheHint(CacheHint.SPEED);
    return imageView;
  }

  /**
   * Applies a drop shadow effect to the specified {@code ImageView} instances.
   *
   * <p>This method iterates through the provided array of {@code ImageView} objects and applies the
   * given {@code DropShadow} effect to each one. This is useful for enhancing the visual appearance
   * of images by adding depth through shadows.
   *
   * @param dropShadow the {@code DropShadow} effect to be applied to the image views.
   * @param imageViews the {@code ImageView} instances to which the drop shadow effect will be
   *     applied.
   */
  private void applyDropShadow(DropShadow dropShadow, ImageView... imageViews) {
    // Apply the drop shadow effect to the image views
    for (ImageView imageView : imageViews) {
      imageView.setEffect(dropShadow);
    }
  }

  /**
   * Displays the next image in the specified group.
   *
   * <p>This method updates the provided {@code Group} by replacing its current children with the
   * specified {@code ImageView} representing the next image. After setting the next image, it also
   * centers the image within the view to ensure it is properly positioned for display.
   *
   * @param file the {@code Group} that contains the images to be displayed.
   * @param nextImage the {@code ImageView} representing the next image to show.
   */
  private void showNextImage(Group file, ImageView nextImage) {
    // Set the next image in the file
    file.getChildren().setAll(nextImage);
    centerImage(nextImage);
  }

  /**
   * Recenters the current image within the specified group.
   *
   * <p>This method checks if the first child of the provided {@code Group} is an {@code ImageView}.
   * If it is, the method calls the {@code centerImage} function to recenter the image in the view.
   * This is useful for ensuring that the image is displayed correctly within its parent container.
   *
   * @param file the {@code Group} containing the image to be recentered.
   */
  private void recenterCurrentImage(Group file) {
    // Recenter the current image in the file
    if (file.getChildren().get(0) instanceof ImageView) {
      centerImage((ImageView) file.getChildren().get(0));
    }
  }

  /**
   * Updates the position of the additional and enlarged image views within the anchor pane.
   *
   * <p>This method calculates and sets the layout positions of the provided {@code ImageView}
   * instances for the additional and enlarged images. The positions are adjusted based on the width
   * and height of the {@code anchorPane}, ensuring that the images are displayed correctly with
   * specified margins.
   *
   * @param additionalImageView the {@code ImageView} representing the additional image.
   * @param enlargedImageView the {@code ImageView} representing the enlarged image.
   */
  private void updateImagePosition(ImageView additionalImageView, ImageView enlargedImageView) {
    // Update the position of the additional and enlarged image views
    additionalImageView.setLayoutX(anchorPane.getWidth() - additionalImageView.getFitWidth() + 40);
    enlargedImageView.setLayoutX(anchorPane.getWidth() - enlargedImageView.getFitWidth() + 40);

    // Update the position of the additional and enlarged image views
    additionalImageView.setLayoutY(
        anchorPane.getHeight() - additionalImageView.getFitHeight() + 10);
    enlargedImageView.setLayoutY(anchorPane.getHeight() - enlargedImageView.getFitHeight() + 10);
  }

  /**
   * Sets the hover handlers for the specified additional and enlarged image views.
   *
   * <p>This method configures mouse hover behavior for the {@code additionalImageView} and {@code
   * enlargedImageView}. When the mouse enters the additional image view, its opacity is set to 0
   * (making it invisible), and the enlarged image view's opacity is set to 1 (making it visible).
   * Conversely, when the mouse exits the additional image view, the opacity of the additional image
   * view is restored to 1 (visible), and the enlarged image view's opacity is set back to 0
   * (invisible).
   *
   * @param additionalImageView the image view that will become invisible on hover.
   * @param enlargedImageView the image view that will become visible on hover.
   */
  private void setHoverHandlers(ImageView additionalImageView, ImageView enlargedImageView) {

    // Set the hover handlers for the additional and enlarged image views
    additionalImageView.setOnMouseEntered(
        e -> {
          additionalImageView.setOpacity(0);
          enlargedImageView.setOpacity(1);
          additionalImageView.setCursor(javafx.scene.Cursor.HAND);
        });

    // Set the hover handlers for the additional and enlarged image views
    additionalImageView.setOnMouseExited(
        e -> {
          // Set the hover handlers for the additional and enlarged image views
          additionalImageView.setOpacity(1);
          enlargedImageView.setOpacity(0);
          additionalImageView.setCursor(javafx.scene.Cursor.DEFAULT);
        });

    //  Set the hover handlers for the additional and enlarged image views
    enlargedImageView.setOnMouseEntered(
        e -> {
          // Set the hover handlers for the additional and enlarged image views
          additionalImageView.setOpacity(0);
          enlargedImageView.setOpacity(1);
          enlargedImageView.setCursor(javafx.scene.Cursor.HAND);
        });

    // Set the hover handlers for the additional and enlarged image views
    enlargedImageView.setOnMouseExited(
        e -> {
          // Set the hover handlers for the additional and enlarged image views
          additionalImageView.setOpacity(1);
          enlargedImageView.setOpacity(0);
          enlargedImageView.setCursor(javafx.scene.Cursor.DEFAULT);
        });
  }

  /**
   * Centers the specified {@code ImageView} within the anchor pane.
   *
   * <p>This method calculates the appropriate X and Y coordinates to position the given {@code
   * ImageView} in the center of the {@code anchorPane}. The image view's position is adjusted based
   * on its width and height to ensure it appears centered visually within the pane.
   *
   * @param imageView the {@code ImageView} to be centered in the anchor pane.
   */
  private void centerImage(ImageView imageView) {
    // Center the image view in the anchor pane
    imageView.setX(((anchorPane.getWidth() - imageView.getFitWidth()) / 2) - 50);
    imageView.setY((anchorPane.getHeight() - imageView.getFitHeight()) / 2);
  }

  /**
   * Zooms in on the specified image view and transitions to the next scene.
   *
   * <p>This method animates a zoom-in effect on the provided {@code ImageView} and then transitions
   * to the specified next scene. The zoom effect enhances the visual experience, drawing the user's
   * attention to the image before navigating to a different part of the application.
   *
   * @param imageView the {@code ImageView} to zoom in on, which will be visually enlarged during
   *     the transition.
   * @param nextScene the identifier of the next scene to transition to after the zoom effect is
   *     complete.
   */
  private void zoomIn(ImageView imageView, String nextScene) {

    // Play the media player
    mediaPlayer.seek(Duration.seconds(1));
    mediaPlayer.play();

    // Zoom in on the image view
    new Thread(
            () -> {
              Platform.runLater(
                  () -> {

                    // Zoom in on the image view
                    ScaleTransition zoomInTransition =
                        new ScaleTransition(Duration.seconds(1), imageView);

                    // Set the zoom in transition properties
                    zoomInTransition.setToX(6.0);
                    zoomInTransition.setToY(6.0);
                    zoomInTransition.setCycleCount(1);
                    zoomInTransition.setAutoReverse(false);

                    // Get the bounds of the image view
                    Bounds bounds = imageView.localToScene(imageView.getBoundsInLocal());

                    // Calculate the center of the scene and the center of the image view
                    double sceneCenterX = anchorPane.getWidth() / 2;
                    double sceneCenterY = anchorPane.getHeight() / 2;

                    // Calculate the translation values
                    double imageCenterX = bounds.getMinX() + bounds.getWidth() / 2;
                    double imageCenterY = bounds.getMinY() + bounds.getHeight() / 2;

                    // Calculate the translation values
                    double translateX = sceneCenterX - imageCenterX + 250;
                    double translateY = sceneCenterY - imageCenterY + 300;

                    // Move the image view to the center of the scene
                    TranslateTransition moveToCenterTransition =
                        new TranslateTransition(Duration.seconds(0.5), imageView);
                    moveToCenterTransition.setByX(translateX);
                    moveToCenterTransition.setByY(translateY);
                    moveToCenterTransition.setCycleCount(1);
                    moveToCenterTransition.setAutoReverse(false);

                    // Rotate the image view
                    RotateTransition rotateTransition =
                        new RotateTransition(Duration.seconds(1), imageView);
                    rotateTransition.setByAngle(20);
                    rotateTransition.setCycleCount(1);
                    rotateTransition.setAutoReverse(false);

                    // Create a parallel transition
                    ParallelTransition parallelTransition =
                        new ParallelTransition(
                            zoomInTransition, moveToCenterTransition, rotateTransition);

                    // Play the parallel transition
                    parallelTransition.play();
                  });
            })
        // Start the next scene transition
        .start();

    // Start the next scene transition
    new Thread(
            () -> {
              // Wait for the zoom in transition to finish
              Platform.runLater(
                  () -> {
                    // Fade out the anchor pane
                    FadeTransition fadeOutTransition =
                        new FadeTransition(Duration.seconds(0.5), anchorPane);
                    fadeOutTransition.setFromValue(1.0);
                    fadeOutTransition.setToValue(0.0);
                    fadeOutTransition.setCycleCount(1);
                    fadeOutTransition.setAutoReverse(false);

                    // Pause for a short duration
                    PauseTransition pauseTransition = new PauseTransition(Duration.seconds(0.5));

                    // Create a sequential transition
                    SequentialTransition sequentialTransition =
                        new SequentialTransition(pauseTransition, fadeOutTransition);

                    // Set the on finished event handler
                    fadeOutTransition.setOnFinished(
                        event -> {
                          try {
                            App.setRoot(nextScene);
                          } catch (IOException e) {
                            e.printStackTrace();
                          }
                        });

                    // Play the sequential transition
                    sequentialTransition.play();
                  });
            })
        .start();
  }
}
