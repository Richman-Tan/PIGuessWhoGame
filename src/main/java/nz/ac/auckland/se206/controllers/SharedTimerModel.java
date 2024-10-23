package nz.ac.auckland.se206.controllers;

/**
 * Singleton class to manage a shared timer model for the application.
 *
 * <p>This class provides access to a single instance of a timer model, which can be used across
 * different components of the application. It initializes the timer with a default time and allows
 * retrieval and resetting of the timer as needed.
 */
public class SharedTimerModel {

  private static SharedTimerModel instance;

  /**
   * Retrieves the singleton instance of the shared timer model.
   *
   * @return the instance of the shared timer model
   */
  public static SharedTimerModel getInstance() {
    if (instance == null) {
      instance = new SharedTimerModel();
    }
    return instance;
  }

  private TimerModel countdownTimer;

  /**
   * Constructs a new shared timer model and initializes the countdown timer with a default time of
   * 301 seconds.
   */
  private SharedTimerModel() {
    countdownTimer = new TimerModel(301); // Set the initial time
  }

  /**
   * Retrieves the current timer model.
   *
   * @return the timer model representing the countdown timer
   */
  public TimerModel getTimer() {
    return countdownTimer;
  }

  /** Resets the countdown timer to its initial value of 301 seconds. */
  public void resetTimer() {
    countdownTimer = new TimerModel(301);
  }
}
