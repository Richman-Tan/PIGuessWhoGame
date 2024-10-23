package nz.ac.auckland.se206;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import nz.ac.auckland.se206.controllers.SharedTimerModel;
import nz.ac.auckland.se206.controllers.TimerModel;
import nz.ac.auckland.se206.states.GameOver;
import nz.ac.auckland.se206.states.GameStarted;
import nz.ac.auckland.se206.states.GameState;
import nz.ac.auckland.se206.states.Guessing;

/**
 * Context class for managing the state of the game. Handles transitions between different game
 * states and maintains game data such as the professions and rectangle IDs.
 */
public class GameStateContext {

  private static GameStateContext instance;

  /**
   * Retrieves the singleton instance of the {@code GameStateContext} class.
   *
   * <p>This static method provides a global point of access to the single instance of {@code
   * GameStateContext}. If the instance has not yet been created, it initializes a new instance
   * before returning it. This ensures that only one instance of the {@code GameStateContext} exists
   * throughout the application, promoting efficient resource use and consistent state management.
   *
   * @return the singleton instance of {@code GameStateContext}.
   */
  public static GameStateContext getInstance() {
    if (instance == null) {
      instance = new GameStateContext();
    }
    return instance;
  }

  private final GameStarted gameStartedState;
  private final Guessing guessingState;
  private final GameOver gameOverState;

  private GameState gameState;
  private List<String> listOfVisitors;
  private boolean firstTimeInit;
  private boolean isGardenToolFound;
  private boolean isGuessPressed;
  private boolean isMenuVisible;
  private boolean isNoteFound;
  private boolean isPhoneFound;
  private boolean isSafeOpen;
  private TimerModel countdownTimer;

  /** Constructs a new GameStateContext and initializes the game states. */
  public GameStateContext() {
    gameStartedState = new GameStarted(this);
    guessingState = new Guessing(this);
    gameOverState = new GameOver(this);
    listOfVisitors = new ArrayList<>();

    gameState = gameStartedState; // Initial state

    this.firstTimeInit = true;
    this.isMenuVisible = false;
    this.isGardenToolFound = false;
    this.isNoteFound = false;
    this.isSafeOpen = false;
    this.isPhoneFound = false;
    this.isGuessPressed = false;
  }

  /**
   * Sets the current state of the game.
   *
   * @param state the new state to set
   */
  public void setState(GameState state) {
    this.gameState = state;
  }

  /**
   * Gets the initial game started state.
   *
   * @return the game started state
   */
  public GameState getGameStartedState() {
    return gameStartedState;
  }

  /**
   * Gets the guessing state.
   *
   * @return the guessing state
   */
  public GameState getGuessingState() {
    return guessingState;
  }

  /**
   * Gets the game over state.
   *
   * @return the game over state
   */
  public GameState getGameOverState() {
    return gameOverState;
  }

  /**
   * Handles the event when the guess button is clicked.
   *
   * @throws IOException if there is an I/O error
   */
  public void onGuessClick() throws IOException {
    gameState.onGuessClick();
  }

  /**
   * Retrieves the visibility status of the menu.
   *
   * <p>This method checks and returns the current state of the {@code isMenuVisible} variable,
   * which indicates whether the menu is currently visible to the user. This can be useful for
   * determining how to update the user interface or manage interactions based on the menu's
   * visibility.
   *
   * @return {@code true} if the menu is visible; {@code false} otherwise.
   */
  public boolean isMenuVisible() {
    return isMenuVisible;
  }

  /**
   * Sets the visibility state of the menu.
   *
   * <p>This method updates the {@code isMenuVisible} variable to reflect the desired visibility
   * state of the menu. A value of {@code true} indicates that the menu should be visible, while
   * {@code false} hides the menu.
   *
   * @param menuVisible {@code true} to make the menu visible; {@code false} to hide it.
   */
  public void setMenuVisible(boolean menuVisible) {
    this.isMenuVisible = menuVisible;
  }

  /** Toggles the visibility of the menu. */
  public void toggleMenuVisibility() {
    this.isMenuVisible = !this.isMenuVisible;
  }

  // Getter for garden tool state
  public boolean isGardenToolFound() {
    return isGardenToolFound;
  }

  // Setter for garden tool state
  public void setGardenToolFound(boolean found) {
    this.isGardenToolFound = found;
  }

  // Getter for safe state
  public boolean isSafeOpen() {
    return isSafeOpen;
  }

  // Setter for safe state
  public void setSafeOpen(boolean open) {
    this.isSafeOpen = open;
  }

  // Getter for note state
  public boolean isNoteFound() {
    return isNoteFound;
  }

  // Setter for note state
  public void setNoteFound(boolean found) {
    this.isNoteFound = found;
  }

  public void addVisitor(String visitor) {
    // Add visitor to the list of visitors
    listOfVisitors.add(visitor);
  }

  @SuppressWarnings("rawtypes")
  public List getListOfVisitors() {
    // Return the list of visitors
    return listOfVisitors;
  }

  // Getter for guess pressed state
  public boolean isGuessPressed() {
    return isGuessPressed;
  }

  // Setter for guess pressed state
  public void setGuessPressed(boolean pressed) {
    this.isGuessPressed = pressed;
  }

  public boolean isPhoneFound() {
    return isPhoneFound;
  }

  public void setPhoneFound(boolean b) {
    this.isPhoneFound = b;
  }

  public boolean isFirstTimeInit() {
    return firstTimeInit;
  }

  public void setFirstTimeInit(boolean firstTimeInit) {
    this.firstTimeInit = firstTimeInit;
  }

  /**
   * Resets the game state to the initial game started state.
   *
   * @throws IOException if there is an I/O error
   */
  public void reset() throws IOException {
    // Reset instance
    isMenuVisible = false;
    isGardenToolFound = false;
    isPhoneFound = false;
    isNoteFound = false;
    isSafeOpen = false;
    isGuessPressed = false;
    firstTimeInit = true;
    gameState = gameStartedState;
    listOfVisitors.clear();

    countdownTimer = SharedTimerModel.getInstance().getTimer();

    countdownTimer.resetI();
    countdownTimer.stop();
    SharedTimerModel.getInstance().resetTimer();
  }

  public boolean isAtLeastOneClueFound() {
    return isGardenToolFound || isPhoneFound || isNoteFound;
  }
}
