package nz.ac.auckland.se206.states;

import java.io.IOException;

/**
 * Interface representing the state of the game. Defines methods to handle user interactions such as
 * clicking on a rectangle and making a guess.
 */
public interface GameState {

  /**
   * Handles the event when the guess button is clicked.
   *
   * @throws IOException if there is an I/O error
   */
  public void onGuessClick() throws IOException;
}
