package engine;

import genericLabeledGraph.Edge;
import genericLabeledGraph.Node;
import graphVizBinding.GraphViz;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import abstractGraph.AbstractModel;
import abstractGraph.AbstractState;
import abstractGraph.AbstractStateMachine;
import abstractGraph.AbstractTransition;
import abstractGraph.conditions.EnumeratedVariable;
import abstractGraph.events.Assignment;
import abstractGraph.events.EnumeratedVariableChange;
import abstractGraph.events.ModelCheckerEvent;
import abstractGraph.events.SingleEvent;
import abstractGraph.events.VariableChange;

/**
 * A class that allows the split of the graphs in small groups to optimize the
 * proof.
 */
public class BuildActivationGraph<M extends AbstractStateMachine<S, T>, S extends AbstractState<T>, T extends AbstractTransition<S>> {
  private AbstractModel<M, S, T> model;
  private AbstractModel<M, S, T> proof;
  private HashSet<M> P6_graphs = new HashSet<M>();

  public HashMap<M, MyNode> nodes = new HashMap<M, MyNode>();

  public BuildActivationGraph(GraphSimulatorInterface<?, M, S, T> simulator) {
    this(simulator.getModel(), simulator.getProof());
  }

  public BuildActivationGraph(AbstractModel<M, S, T> model,
      AbstractModel<M, S, T> proof) {
    this.model = model;
    this.proof = proof;
    createGraphOfGraphs();
  }

  /**
   * Create a graph of graphs, where every arc A -> B represents that graph A
   * modifies some variables or generate SYN's that are eaten by graph B.
   */
  private void createGraphOfGraphs() {
    HashMap<EnumeratedVariable, Collection<M>> writing_state_machine =
        model.getWritingStateMachines();
    @SuppressWarnings("unchecked")
    HashMap<EnumeratedVariable, Collection<M>> writing_state_machine_proof =
        (HashMap<EnumeratedVariable, Collection<M>>) proof
            .getWritingStateMachines()
            .clone();
    /*
     * The graphs of the proof model need to know the graphs that write on the
     * variables.
     */
    writing_state_machine_proof.putAll(writing_state_machine);

    /*
     * Store for every SynchronisationEvent (i.e. its name) the set of state
     * machine that have this event in an action field.
     */
    LinkedHashMap<String, LinkedHashSet<M>> syn_event_in_graphs =
        new LinkedHashMap<String, LinkedHashSet<M>>();

    /* We first discover which state machine write which SynchronizationEvents. */
    searchForEventWriting(syn_event_in_graphs, this.model);
    searchForEventWriting(syn_event_in_graphs, this.proof);

    // TODO: we also need to add the commands generated by the functional model

    /* We then build the Activation Graph */
    buildOutgoingLinksFromModel(writing_state_machine, syn_event_in_graphs,
        model);
    buildOutgoingLinksFromModel(writing_state_machine_proof,
        syn_event_in_graphs,
        proof);
  }

  /**
   * TODO: add a description for this function.
   * 
   * @param writing_state_machine
   * @param syn_event_in_graphs
   * @param current_model
   */
  private void buildOutgoingLinksFromModel(
      HashMap<EnumeratedVariable, Collection<M>> writing_state_machine,
      LinkedHashMap<String, LinkedHashSet<M>> syn_event_in_graphs,
      AbstractModel<M, S, T> current_model) {
    for (M state_machine : current_model) {

      for (S state : state_machine) {
        for (T transition : state) {
          for (SingleEvent event : transition.getEvents()) {
            if (event instanceof VariableChange) {
              addWritingStateMachineInGraph(writing_state_machine,
                  state_machine, ((VariableChange) event).getModifiedVariable());
            } else if (event instanceof Assignment) {
              addWritingStateMachineInGraph(writing_state_machine,
                  state_machine, ((Assignment) event).getVariable());
            } else if (event instanceof ModelCheckerEvent) {
            } else {
              LinkedHashSet<M> automata_using_this_event =
                  syn_event_in_graphs.get(event.getName());

              if (automata_using_this_event == null) {
                continue;
              }

              for (M writer : automata_using_this_event) {
                addInActivationGraph(writer, state_machine, event);
              }
            }
          }
          HashSet<EnumeratedVariable> list_variable = new HashSet<>();
          transition.getCondition().allVariables(list_variable);
          for (EnumeratedVariable variable : list_variable) {
            addWritingStateMachineInGraph(writing_state_machine, state_machine,
                variable);
          }
        }
      }
    }

  }

  /**
   * Create an arc in the activation graph. From the StateMachine writing the
   * variable to the StateMachine containing it.
   * 
   * @param writing_state_machine
   * @param state_machine
   * @param variable
   */
  private void addWritingStateMachineInGraph(
      HashMap<EnumeratedVariable, Collection<M>> writing_state_machine,
      M state_machine,
      EnumeratedVariable variable) {
    SingleEvent single_event = new EnumeratedVariableChange(variable);
    Collection<M> writers =
        writing_state_machine.get(variable);

    if (writers == null) {
      return;
    }
    if (writers.size() > 1) {
      throw new IllegalArgumentException("The variable " + variable
          + " is written by more than one graph.");
    } else {
      addInActivationGraph(writers.iterator().next(),
          state_machine, single_event);
    }
  }

  /**
   * Add in the syn_event_in_graphs structure, the automata that have that event
   * in the Action field.
   * 
   * @param syn_event_in_graphs
   *          The association :
   *          (Event name) -> (state_machines writing it).
   * @param current_model
   *          The model to process.
   */
  private void searchForEventWriting(
      LinkedHashMap<String, LinkedHashSet<M>> syn_event_in_graphs,
      AbstractModel<M, S, T> current_model) {
    for (M state_machine : current_model) {
      nodes.put(state_machine, new MyNode(state_machine));
      for (S state : state_machine) {
        for (T transition : state) {
          for (SingleEvent action : transition.getActions()) {

            if (action instanceof VariableChange) {
            } else if (action instanceof Assignment) {
            } else if (action instanceof ModelCheckerEvent) {
              if (action.getName().startsWith("P_6")) {
                P6_graphs.add(state_machine);
              }
            } else {
              LinkedHashSet<M> liste_state_machine =
                  syn_event_in_graphs.get(action.getName());

              if (liste_state_machine == null) {
                liste_state_machine = new LinkedHashSet<M>();
                syn_event_in_graphs
                    .put(action.getName(), liste_state_machine);
              }
              liste_state_machine.add(state_machine);
            }
          }
        }
      }
    }
  }

  private void addInActivationGraph(M source_state_machine,
      M destination_state_machine, SingleEvent label) {

    MyNode node = nodes.get(source_state_machine);
    assert (node != null);
    MyNode son = nodes.get(destination_state_machine);
    node.add(new MyEdge(node, son, label));
    son.addFather(node);
  }

  public void printToImage(String file_name) throws IOException {
    printToImage(file_name, null);
  }

  public void printToImage(String file_name,
      GraphSimulatorInterface<?, M, S, T> simulator) throws IOException {
    GraphViz gv = new GraphViz();
    gv.addln(gv.start_graph());

    for (MyNode node : nodes.values()) {
      M state_machine = node.data;
      String state_machine_name = "\"" + state_machine.getName() + "\"";

      boolean is_state_machine_selected =
          simulator != null &&
              (simulator.getModel().containsStateMachine(state_machine) ||
              simulator.getProof().containsStateMachine(state_machine));

      if (is_state_machine_selected) {
        gv.addln(state_machine_name + "[color=red];");
      }

      for (Edge<MyNode, SingleEvent> edge : node) {
        M target_machine = edge.to.data;
        String target_name = "\"" + target_machine.getName() + "\"";

        boolean is_target_machine_selected =
            simulator != null &&
                (simulator.getModel().containsStateMachine(target_machine) ||
                simulator.getProof().containsStateMachine(target_machine));

        if (is_state_machine_selected) {
          if (is_target_machine_selected) {
            gv.addln("edge [color=red];");
            gv.addln(target_name + "[color=red];");
          } else {
            gv.addln("edge [color=black];");
          }

          gv.addln(state_machine_name + " -> "
              + target_name + " [label=\""
              + edge.label + "\"];");

        } else {
          gv.addln("edge [color=black];");
          gv.addln(state_machine_name + " -> " + target_name
              + " [label=\""
              + edge.label + "\"];");
        }
      }
    }

    gv.add(gv.end_graph());

    String type = "png";
    gv.writeGraphToFile(file_name, type);

  }

  public HashSet<M> getP6_graphs() {
    return P6_graphs;
  }

  class MyNode extends Node<M, MyNode, SingleEvent> {
    public MyNode(M data) {
      super(data);
    }

    @Override
    public String toString() {
      return "MyNode [transitions=" + transitions + "]";
    }
  }

  class MyEdge extends Edge<MyNode, SingleEvent> {
    public MyEdge(MyNode from, MyNode to, SingleEvent label) {
      super(from, to, label);
    }
  }

}
