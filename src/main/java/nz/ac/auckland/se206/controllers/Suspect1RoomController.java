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

/** Controller for the room of Suspect 1 (the uncle). */
public class Suspect1RoomController extends BaseRoomController {

  private GameStateContext context = GameStateContext.getInstance();

  @Override
  protected String getInitialPrompt() {
    // This is the specific prompt for Suspect 1 (the uncle)
    return "prompts/uncle.txt";
  }

  @Override
  protected void recordVisit() {
    // Record the visit to suspect1's room if it hasn't been recorded already
    if (!context.getListOfVisitors().contains("suspect1")) {
      context.addVisitor("suspect1");
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
  private void onGrandmotherClick() throws IOException {
    // Your implementation to handle the click event
    App.setRoot("suspect2room"); // Or whatever action you want to perform
  }

  @FXML
  private void onGrandsonClick() throws IOException {
    // Your logic to switch to the grandson room or any other required action
    App.setRoot("suspect3room");
  }

  @FXML
  private void onGuessClick() throws IOException {
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
