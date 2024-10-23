package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameStateContext;

/** Controller for the room of Suspect 2 (the grandma). */
public class Suspect3RoomController extends BaseRoomController {

  private GameStateContext context = GameStateContext.getInstance();

  @Override
  protected String getInitialPrompt() {
    return "prompts/grandson.txt"; // Load the appropriate prompt for Suspect 3 (Grandson)
  }

  @Override
  protected void recordVisit() {
    if (!context.getListOfVisitors().contains("suspect3")) {
      context.addVisitor("suspect3");
    }
  }

  @Override
  protected void loadGptPrompt(String resourcePath)
      throws URISyntaxException, IOException, ApiProxyException {
    // Load the prompt file (resourcePath) and send it to GPT
    URI resourceUri = getClass().getClassLoader().getResource(resourcePath).toURI();

    // Load the prompt template
    String template = loadTemplate(resourceUri);

    // Send the initial prompt to GPT
    ChatMessage systemMessage = new ChatMessage("system", template);
    runGpt(systemMessage);
  }

  @FXML
  private void onUncleButtonClick(ActionEvent event) throws IOException {
    // Your implementation to handle the click event
    App.setRoot("suspect1room"); // Or whatever action you want to perform
  }

  @FXML
  private void onGrandmotherClick(ActionEvent event) throws IOException {
    // Your logic to switch to the grandson room or any other required action
    App.setRoot("suspect2room");
  }

  @FXML
  private void onGuessClick(ActionEvent event) throws IOException {
    // Implement the logic for what should happen when the "Guess" button is clicked.
    context.setGuessPressed(true);
    App.setRoot("guessingScene"); // Navigates to the guessing scene, for example
  }

  @FXML
  protected void onRoom(ActionEvent event) throws IOException {
    // Your implementation to handle the room event
    App.setRoot("room");
  }
}
