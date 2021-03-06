package test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import graph.GraphFactoryAEFD;
import graph.Model;
import graph.State;
import graph.StateMachine;
import graph.Transition;

import java.io.IOException;

import org.junit.Test;

import abstractGraph.verifiers.CoherentVariablesWriting;
import abstractGraph.verifiers.DeterminismChecker;
import abstractGraph.verifiers.InitializationProperties;
import abstractGraph.verifiers.NoUselessVariables;
import abstractGraph.verifiers.SingleWritingChecker;
import abstractGraph.verifiers.TautologyFromStateZero;
import abstractGraph.verifiers.Verifier;
import abstractGraph.verifiers.WrittenAtLeastOnceChecker;

/**
 * Test all verification units.
 */
public class Graph_Verifiers_PackagesTesting {

  private class MyVerifier extends Verifier<StateMachine, State, Transition> {

  }

  /**
   * Load a model from the resource/$thisClassName/
   * 
   * @param name
   *          The name of the file
   * @return The model build using a GraphFactoryAEFD.
   * @throws IOException
   */
  private Model loadFile(String name) throws IOException {

    String class_name = this.getClass().getSimpleName();

    GraphFactoryAEFD test = new GraphFactoryAEFD(null);
    Model model = test.buildModel(
        "src/test/resources/" + class_name + "/" + name,
        name);
    return model;
  }

  /**
   * Apply the given verifier to all the given files, expecting that the
   * verifier return the results.
   * 
   * @param verifier
   * @param files
   * @param results
   *          results[i] is the answer for files[i].
   */
  private void generalTest(MyVerifier verifier, String[] files,
      Boolean[] results) {
    assertTrue(files.length == results.length);

    int i = 0;
    try {
      for (i = 0; i < files.length; i++) {
        assertTrue("Error on " + files[i],
            verifier.check(loadFile(files[i]), true) == results[i]);
        assertTrue("Error on " + files[i],
            verifier.check(loadFile(files[i]), false) == results[i]);
      }
    } catch (IOException e) {
      e.printStackTrace();
      fail("Unexpected exception while loading " + files[i] + ":\n" + e);
    }
  }

  /**
   * Testing of {@link SingleWritingChecker}.
   * 
   * @details This test uses different files representing simple graphs:
   *          <ol>
   *          <li>
   *          Graph_with_no_variable.txt is not writing any variable.
   * 
   *          </li>
   *          <li>
   *          Graph_with_concurrent_writing.txt : 3 state machines, 2 variables.
   *          The 2 first states machines write the same variable, and the 3rd
   *          write the second variable.
   * 
   *          </li>
   *          <li>
   *          Graph_without_concurrent_writing.txt : 2 state machines, 2
   *          variables, each of them written by one state machine.
   * 
   *          </li>
   * 
   *          <li>
   *          Graph_with_not_written_variables.txt : Graph with a variable in a
   *          condition field but never written on.</li>
   * 
   *          </ol>
   */
  @Test
  public void SingleWritingChecker() {
    MyVerifier verifier = new MyVerifier();
    verifier
        .addVerification(new SingleWritingChecker<StateMachine, State, Transition>());

    String[] files = {
        "Graph_with_no_variable.txt",
        "Graph_without_concurrent_writing.txt",
        "Graph_with_concurrent_writing.txt"
    };

    Boolean[] results = {
        true,
        true,
        false
    };

    generalTest(verifier, files, results);
  }

  /**
   * Testing of {@link abstractGraph.verifiers.WrittenAtLeastOnceChecker}.
   * 
   * @details This test uses different files representing simple graphs:
   *          <ol>
   *          <li>
   *          Graph_with_no_variable.txt is not writing any variable.
   * 
   *          </li>
   *          <li>
   *          Graph_without_concurrent_writing.txt : 2 state machines, 2
   *          variables, each of them written by one state machine.
   * 
   *          </li>
   * 
   *          <li>
   *          Graph_with_not_written_variables.txt : Graph with a variable in a
   *          condition field but never written on.</li>
   * 
   *          </ol>
   */
  @Test
  public void WrittenAtLeastOnceChecker() {
    MyVerifier verifier = new MyVerifier();
    verifier
        .addVerification(new WrittenAtLeastOnceChecker<StateMachine, State, Transition>());

    String[] files = {
        "Graph_with_no_variable.txt",
        "Graph_without_concurrent_writing.txt",
        "Graph_with_not_written_variables.txt"
    };

    Boolean[] results = {
        true,
        true,
        false
    };

    generalTest(verifier, files, results);
  }

  /**
   * Testing of {@link DeterminismChecker}.
   * 
   * @details This test uses different files representing simple graphs:
   *          <ol>
   *          <li>
   *          Determinism_without_SAT_solving.txt : 1 state machines, 2 states.
   *          2 transitions from 0 to 1, labeled with a different event and
   *          without condition.
   * 
   *          </li>
   *          <li>
   *          Determinism_with_SAT_solving.txt : 2 states, 2 transitions from 0
   *          to 1,labeled with the same event and two simple exlusive
   *          conditions.</li>
   *          <li>
   *          Not_determinist_graph_1.txt : 2 identical transitions labeled with
   *          the same event and without condition.
   * 
   *          </li>
   *          <li>
   *          Not_determinist_graph_2.txt : same as the above, both the first
   *          transition is labeled with several events while the second
   *          transition is labeled only one 1 event (that is also labeling the
   *          first transition).
   * 
   *          </li>
   *          <li>
   *          Not_determinism_with_SAT_solving.txt : 2 transitions labeled with
   *          the same event, and with an incompatible condition.</li>
   *          <li>
   *          Determinism_two_identical_transitions.txt: two identical
   *          transitions that should not raise an error, but only a
   *          warning.</li>
   *          </ol>
   */
  @Test
  public void DeterminismChecker() {
    MyVerifier verifier = new MyVerifier();
    verifier
        .addVerification(new DeterminismChecker<StateMachine, State, Transition>());

    String[] files = {
        "Determinism_without_SAT_solving.txt",
        "Determinism_with_SAT_solving.txt",
        "Not_determinist_graph_1.txt",
        "Not_determinist_graph_2.txt",
        "Not_determinism_with_SAT_solving.txt",
        "Determinism_two_identical_transitions.txt",
        "Determinism_with_identically_labeled_transitions.txt"
    };

    Boolean[] results = {
        true,
        true,
        false,
        false,
        false,
        true,
        true
    };

    generalTest(verifier, files, results);
  }

  /**
   * Testing of {@link DeterminismChecker}.
   * 
   * @details This test uses different files representing simple graphs:
   *          <ol>
   *          <li>
   *          Incoherent_writting_1.txt: 2 incoherent writing into the same
   *          state.</li>
   *          <li>
   *          Incoherent_writting_2.txt: requires 1 level of propagation.</li>
   * 
   *          <li>
   *          Incoherent_writting_4.txt: a single transition writing A and Not
   *          A.</li>
   *          </ol>
   */
  @Test
  public void CoherentVariablesWriting() {
    MyVerifier verifier = new MyVerifier();
    verifier
        .addVerification(new CoherentVariablesWriting<StateMachine, State, Transition>());

    String[] files = {
        /* All files from the DeterminismChecker should be ok */
        "Determinism_without_SAT_solving.txt",
        "Determinism_with_SAT_solving.txt",
        "Not_determinist_graph_1.txt",
        "Not_determinist_graph_2.txt",
        "Not_determinism_with_SAT_solving.txt",
        "Determinism_two_identical_transitions.txt",
        "Determinism_with_identically_labeled_transitions.txt",
        /* All files from NoUselessVariablesChecker should be ok */
        "Graph_with_no_variable.txt",
        "Graph_without_useless_variables.txt",
        "Graph_with_not_used_variables_in_condition.txt",
        /* All files from SingleWrittingChecker should be ok */
        "Graph_with_no_variable.txt",
        "Graph_without_concurrent_writing.txt",
        "Graph_with_concurrent_writing.txt",
        "Graph_with_not_written_variables.txt",
        /* More specific tests */
        "Incoherent_writting_1.txt",
        "Incoherent_writting_2.txt",
        "Incoherent_writting_3.txt",
        "Incoherent_writting_4.txt"
    };

    Boolean[] results = {
        /* All files from the DeterminismChecker should be ok */
        true,
        true,
        true,
        true,
        true,
        true,
        true,
        /* All files from NoUselessVariablesChecker should be ok */
        true,
        true,
        true,
        /* All files from SingleWrittingChecker should be ok */
        true,
        true,
        true,
        true,
        /* More specific tests */
        false,
        false,
        false,
        false

    };

    generalTest(verifier, files, results);
  }

  /**
   * Testing of {@link NoUselessVariables}.
   * 
   * @details This test uses different files representing simple graphs:
   *          <ol>
   *          <li>
   *          Graph_with_no_variable.txt : Graph_with_no_variable.txt is not
   *          writing or reading any variable.</li>
   * 
   *          <li>
   *          Graph_without_useless_variables.txt : Graph writing and reading
   *          variables without any usless one.</li>
   * 
   *          <li>
   *          Graph_with_not_used_variables.txt :Graph with a variable in an
   *          action field but never used in a condition.</li>
   *          </ol>
   * 
   */
  @Test
  public void NoUselessVariablesChecker() {
    MyVerifier verifier = new MyVerifier();
    verifier
        .addVerification(new NoUselessVariables<StateMachine, State, Transition>());

    String[] files = {
        "Graph_with_no_variable.txt",
        "Graph_without_useless_variables.txt",
        "Graph_with_not_used_variables_in_condition.txt",
        "Graph_with_not_used_variables_in_event.txt",
        "Graph_with_used_variables_in_event.txt"

    };

    Boolean[] results = {
        true,
        true,
        false,
        false,
        true
    };

    generalTest(verifier, files, results);
  }

  @Test
  public void InitializationPropertyChecker() {
    MyVerifier verifier = new MyVerifier();
    verifier
        .addVerification(new InitializationProperties<StateMachine, State, Transition>());

    String[] files = {
        "Graph_without_initialisation_error.txt",
        "Graph_without_initial_state.txt",
        "Graph_with_variable_in_initialisation_transition.txt",
        "Graph_with_event_in_initialisation_transition.txt"
    };

    Boolean[] results = {
        true,
        false,
        false,
        false
    };

    generalTest(verifier, files, results);
  }

  @Test
  public void TautologyFromStateZeroChecker() {
    MyVerifier verifier = new MyVerifier();
    verifier
        .addVerification(new TautologyFromStateZero<StateMachine, State, Transition>());

    String[] files = {
        "Graph_without_tautology_at_state_zero1.txt",
        "Graph_without_tautology_at_state_zero2.txt",
        "Graph_with_tautology_at_state_zero1.txt",
        "Graph_with_tautology_at_state_zero2.txt",
        "Graph_with_tautology_at_state_zero3.txt"
    };

    Boolean[] results = {
        false,
        false,
        true,
        true,
        true
    };

    generalTest(verifier, files, results);
  }
}
