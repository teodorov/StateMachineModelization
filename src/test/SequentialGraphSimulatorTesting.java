package test;

import static org.junit.Assert.assertTrue;
import engine.SequentialGraphSimulator;
import graph.GlobalState;
import graph.GraphFactoryAEFD;
import graph.Model;
import graph.State;
import graph.StateMachine;
import graph.Transition;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import org.junit.Test;

import utils.Pair;
import abstractGraph.AbstractGlobalState;
import abstractGraph.conditions.BooleanVariable;
import abstractGraph.conditions.EnumeratedVariable;
import abstractGraph.events.ExternalEvent;

public class SequentialGraphSimulatorTesting {

  private static final String class_name = SequentialGraphSimulatorTesting.class
      .getSimpleName();

  /**
   * Load a file and build the model associated to that file.
   * 
   * @param name
   *          the name of the file located in
   *          src/test/resources/SequentialGraphSimulatorTesting
   * @return a model built from the file.
   * @throws IOException
   */
  private Model loadFile(String name) throws IOException {
    GraphFactoryAEFD test = new GraphFactoryAEFD(null);
    Model model = test.buildModel("src/test/resources/" + class_name + "/"
        + name, "Testing model");
    return model;
  }

  /**
   * Test the graph simulator by simulating different graphs with external
   * events and ensure that the result is correct.
   * 
   * @throws IOException
   * 
   * @details This test uses different files representing simple graphs:
   *          <ol>
   *          <li>
   *          "Graph_with_no_external_events.txt" is a file containing a graph
   *          without external events.</li>
   *          <li>
   *          "Graph_testing_different_external_event.txt" is a file containing
   *          the different external events found in the model</li>
   *          <li>
   *          "Graph_with_propagation.txt" is a graph containing different
   *          internal events triggered by an external event</li>
   *          <li>
   *          "Graph_with_condition.txt" a graph containing a two transitions
   *          with the same event</li>
   *          <li>
   *          "Graph_testing_variables_value.txt" a graph with different change
   *          of the variable</li>
   *          <li>
   *          "Graph_P5.txt" a graph with a P5 in an action field</li>
   *          <li>
   *          "Graph_P6.txt" a graph with a P6 in an action field</li>
   *          <li>
   *          "Graph_P7.txt" a graph with a P7 in an action field</li>
   *          <li>
   *          "Graph_with_alarm.txt" a graph with an alarm set in the first
   *          transition (Action written in the form
   *          "[Liste of actions] / [Liste of Alarms] Action"</li>
   *          </ol>
   */
  @Test
  public void TraceTesting() throws IOException {

    LinkedList<ExternalEvent> events;
    String file_name;

    /* Test the file "Graph_with_no_external_events.txt" */
    file_name = "Graph_with_no_external_events.txt";
    Pair<GlobalState, SequentialGraphSimulator> pair = loadSimulator(file_name);
    GlobalState global_state = pair.getFirst();
    SequentialGraphSimulator simulator = pair.getSecond();
    Model model = simulator.getModel();

    events = convertToExternalEvent(new String[] { "CTL_1", "MSG_1" });
    global_state = simulator.executeAll(global_state, events);
    // Verify that the current state of the state machine is "0".
    assertInState("Error on " + file_name, global_state,
        model.getStateMachine("Graph_with_no_external_event"), "0");

    /* Test the file "Graph_testing_different_external_event.txt" */
    file_name = "Graph_testing_different_external_event.txt";
    pair = loadSimulator(file_name);
    global_state = pair.getFirst();
    simulator = pair.getSecond();
    model = simulator.getModel();
    events = convertToExternalEvent(new String[] { "CTL_1", "MSG_1", "ACT_1",
        "FTP_1" });

    global_state = simulator.executeAll(global_state, events);
    System.err.println(model);
    assertInState("Error on " + file_name, global_state, model
        .getStateMachine("Page 1"), "4");

    /* Test the file "Graph_with_propagation.txt" */
    file_name = "Graph_with_propagation.txt";
    pair = loadSimulator(file_name);
    global_state = pair.getFirst();
    simulator = pair.getSecond();
    model = simulator.getModel();
    events = convertToExternalEvent(new String[] { "CTL_1" });
    global_state = simulator.executeAll(global_state, events);
    assertInState("Error on " + file_name, global_state, model
        .getStateMachine("Page 1"), "1");
    assertInState("Error on " + file_name, global_state, model
        .getStateMachine("Page 2"), "1");
    assertInState("Error on " + file_name, global_state, model
        .getStateMachine("Page 3"), "1");

    /* Test the file "Graph_with_condition.txt" */
    file_name = "Graph_with_condition.txt";
    pair = loadSimulator(file_name);
    global_state = pair.getFirst();
    simulator = pair.getSecond();
    model = simulator.getModel();
    events = convertToExternalEvent(new String[] { "CTL_1" });
    global_state = simulator.executeAll(global_state, events);
    assertInState("Error on " + file_name, global_state, model
        .getStateMachine("Page 1"), "2");

    // Test the file "Graph_testing_variables_value.txt"
    file_name = "Graph_testing_variables_value.txt";
    pair = loadSimulator(file_name);
    global_state = pair.getFirst();
    simulator = pair.getSecond();
    model = simulator.getModel();
    events = convertToExternalEvent(new String[] { "CTL_1" });
    global_state = simulator.executeAll(global_state, events);
    assertInState("Error on " + file_name, global_state, model
        .getStateMachine("Page 1"), "1");
    assertInState("Error on " + file_name, global_state, model
        .getStateMachine("Page 2"), "1");
    assertInState("Error on " + file_name, global_state, model
        .getStateMachine("Page 3"), "1");

    // Verify that the value of the variables are corrects.
    // Retrieve the variable from the name
    Model model_tmp = simulator.getModel();
    assertTrue("Error on " + file_name, global_state
        .getVariableValue(model_tmp.getVariable("IND_A_Actif")) == false);
    assertTrue("Error on " + file_name, global_state
        .getVariableValue(model_tmp.getVariable("IND_B_Actif")) == false);
    assertTrue("Error on " + file_name, global_state
        .getVariableValue(model_tmp.getVariable("IND_C_Actif")) == true);

    /* Test the file with FCI */
    file_name = "Graph_with_FCI.txt";
    pair = loadSimulator(file_name);
    global_state = pair.getFirst();
    simulator = pair.getSecond();
    model = simulator.getModel();
    simulator.getModel().loadFCI("src/test/resources/" + class_name + "/"
        + "liste_fci.yaml");
    events = convertToExternalEvent(new String[] { "CTL_1_Actif" });
    global_state = simulator.executeAll(global_state, events);
    System.out.println("Global Sate under test " + global_state);
    assertInState("Error on " + file_name, global_state, model
        .getStateMachine("Page 1"), "1");
    assertInState("Error on " + file_name, global_state, model
        .getStateMachine("Page 2"), "1");
    assertInState("Error on " + file_name, global_state, model
        .getStateMachine("Page 3"), "1");

    /* Test the file "Graph_P5.txt" */
    file_name = "Graph_P5.txt";
    pair = loadSimulator(file_name);
    global_state = pair.getFirst();
    simulator = pair.getSecond();
    model = simulator.getModel();
    events = convertToExternalEvent(new String[] { "CTL_1" });
    global_state = simulator.executeAll(global_state, events);
    assertTrue("Error on " + file_name, !global_state.isSafe());

    /* Test the file "Graph_P6.txt" */
    file_name = "Graph_P6.txt";
    pair = loadSimulator(file_name);
    global_state = pair.getFirst();
    simulator = pair.getSecond();
    model = simulator.getModel();
    events = convertToExternalEvent(new String[] { "CTL_1" });
    global_state = simulator.executeAll(global_state, events);
    assertTrue("Error on " + file_name, !global_state.isLegal());

    /* Test the file "Graph_P7.txt" */
    file_name = "Graph_P7.txt";
    pair = loadSimulator(file_name);
    global_state = pair.getFirst();
    simulator = pair.getSecond();
    model = simulator.getModel();
    events = convertToExternalEvent(new String[] { "CTL_1" });
    global_state = simulator.executeAll(global_state, events);
    assertTrue("Error on " + file_name, !global_state.isNotP7());

    /* Test the file "Graph_with_alarm.txt" */
    file_name = "Graph_with_alarm.txt";
    pair = loadSimulator(file_name);
    global_state = pair.getFirst();
    simulator = pair.getSecond();
    model = simulator.getModel();
    events = convertToExternalEvent(new String[] { "CTL_1" });
    global_state = simulator.executeAll(global_state, events);
    assertInState("Error on " + file_name, global_state, model
        .getStateMachine("Page 1"), "0");
    assertInState("Error on " + file_name, global_state, model
        .getStateMachine("Page 2"), "0");
    assertInState("Error on " + file_name, global_state, model
        .getStateMachine("Page 3"), "1");

    /* Test the file Graph_with_safety_error.txt */
    file_name = "Graph_with_safety_error.txt";
    String proof_model_name = "AP_of_Graph_with_safety_error.txt";
    pair = loadSimulator(file_name, proof_model_name);
    global_state = pair.getFirst();
    simulator = pair.getSecond();
    model = simulator.getModel();
    events = convertToExternalEvent(new String[] { "CTL_1" });
    global_state = simulator.executeAll(global_state, events);
    assertTrue("Error on " + file_name,
        !global_state.isSafe());

  }

  private void assertInState(String error_message,
      GlobalState global_state,
      StateMachine sm, String targeted_state) {
    assert (sm != null);
    assert (targeted_state != null);
    assertTrue(error_message, global_state.getState(sm).getId().equals(
        targeted_state));

  }

  private LinkedList<ExternalEvent> convertToExternalEvent(String[] events) {
    LinkedList<ExternalEvent> result = new LinkedList<ExternalEvent>();
    for (int i = 0; i < events.length; i++) {
      result.add(new ExternalEvent(events[i]));
    }
    return result;
  }

  /**
   * Create a simulator from a file
   */
  private Pair<GlobalState, SequentialGraphSimulator> loadSimulator(
      String file_name) {
    Model model;
    try {
      model = loadFile(file_name);
    } catch (IOException e) {
      e.printStackTrace();
      throw new Error();
    }

    SequentialGraphSimulator simulator = new SequentialGraphSimulator(model);
    simulator.setVerbose(true);
    GlobalState global_state = simulator.emptyGlobalState();

    Iterator<EnumeratedVariable> variable_iterator = model
        .iteratorExistingVariables();
    while (variable_iterator.hasNext()) {
      global_state.setVariableValue((BooleanVariable) variable_iterator.next(),
          true);
    }

    for (StateMachine state_machine : model) {
      global_state.setState(state_machine, state_machine.getState("0"));
    }
    return new Pair<>(global_state, simulator);
  }

  /**
   * Create a simulator from the text files by generating the models and
   * initializing the global state. Create the simulator with proof model.
   * 
   * @throws IOException
   */
  private Pair<GlobalState, SequentialGraphSimulator> loadSimulator(
      String model_file_name,
      String proof_file_name) throws IOException {

    GraphFactoryAEFD factory = new GraphFactoryAEFD(null);

    Model model = factory.buildModel("src/test/resources/" + class_name + "/"
        + model_file_name, "Testing model");
    Model proof = factory.buildModel("src/test/resources/" + class_name + "/"
        + proof_file_name, "Testing proof model");

    SequentialGraphSimulator simulator = new SequentialGraphSimulator(model,
        proof);
    simulator.setVerbose(true);
    GlobalState global_state = simulator.emptyGlobalState();

    initGlobalState(model, global_state);
    initGlobalState(proof, global_state);

    return new Pair<>(global_state, simulator);
  }

  /**
   * Init the global state by putting all the current state of the state
   * machines of the model to "0" and all the variable to true.
   * 
   * @param model
   *          from where the global state will search for the state machines and
   *          the variables.
   * @param global_state
   */
  private void initGlobalState(Model model,
      AbstractGlobalState<StateMachine, State, Transition, ?> global_state) {
    for (EnumeratedVariable variable : model.getExistingVariables()) {
      global_state.setVariableValue((BooleanVariable) variable, true);
    }

    for (StateMachine state_machine : model) {
      global_state.setState(state_machine, state_machine.getState("0"));
    }
  }

}
