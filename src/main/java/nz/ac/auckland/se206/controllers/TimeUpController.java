package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.states.GameStarted;

/** Controller for the Time Up scene. */
public class TimeUpController {

  // Static fields
  /** Path to the "times up" audio file. */
  private static final String timesUpMessage =
      GameStarted.class.getClassLoader().getResource("sounds/timesUp.mp3").toExternalForm();

  // Instance fields
  @FXML private AnchorPane anchorPane;
  private MediaPlayer mediaPlayer;
  private BooleanProperty volumeSettingProperty =
      SharedVolumeControl.getInstance().volumeSettingProperty();

  /**
   * Initializes the Time Up scene after the associated FXML has been loaded.
   *
   * <p>This method is automatically called by the JavaFX framework when the FXML file for the Time
   * Up scene is loaded. It sets up the initial state of the Time Up controller by configuring UI
   * components, binding properties, and initializing any necessary data structures or event
   * listeners required for the controller's functionality.
   */
  public void initialize() {
    // Play the "times up" audio
    Media sound = new Media(timesUpMessage);
    mediaPlayer = new MediaPlayer(sound);

    // Bind the volume property for the media player
    mediaPlayer
        .volumeProperty()
        .bind(
            Bindings.createDoubleBinding(
                () -> volumeSettingProperty.get() ? 1.0 : 0.0, volumeSettingProperty));

    // Start playing the audio
    mediaPlayer.play();

    // Schedule a timer to switch to the guessing scene after 2.5 seconds
    Timer timer = new Timer();
    timer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            try {
              // Switch to the guessing scene
              App.setRoot("guessingScene");
            } catch (IOException e) {
              // Handle the exception
              e.printStackTrace();
            }
          }
        },
        2200); // 2500 milliseconds = 2.5 seconds
  }
}
