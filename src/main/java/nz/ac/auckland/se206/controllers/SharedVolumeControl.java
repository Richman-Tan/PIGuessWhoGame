package nz.ac.auckland.se206.controllers;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Class to control the volume setting in the application.
 *
 * <p>This singleton class is responsible for managing the volume setting of the application. It
 * provides methods to get and set the volume status, allowing different components of the
 * application to respond to volume changes consistently.
 */
public class SharedVolumeControl {

  private static SharedVolumeControl instance;

  /**
   * Retrieves the singleton instance of the shared volume control.
   *
   * @return the instance of the shared volume control
   */
  public static SharedVolumeControl getInstance() {
    if (instance == null) {
      instance = new SharedVolumeControl();
    }
    return instance;
  }

  private final BooleanProperty volumeSetting = new SimpleBooleanProperty(true);

  /** Constructs a new shared volume control with the default volume setting. */
  private SharedVolumeControl() {}

  /**
   * Returns the property representing the volume setting.
   *
   * @return the BooleanProperty that indicates the current volume setting
   */
  public BooleanProperty volumeSettingProperty() {
    return volumeSetting;
  }

  /**
   * Retrieves the current volume setting.
   *
   * @return {@code true} if the volume is on; {@code false} otherwise
   */
  public boolean getVolumeSetting() {
    return volumeSetting.get();
  }

  /**
   * Sets the volume control to the specified state.
   *
   * @param volumeOn {@code true} to enable volume; {@code false} to mute it
   */
  public void setVolumeSetting(boolean volumeOn) {
    this.volumeSetting.set(volumeOn);
  }
}
