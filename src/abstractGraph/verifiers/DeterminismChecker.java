package abstractGraph.verifiers;

import java.util.Iterator;
import java.util.LinkedList;

import org.sat4j.specs.TimeoutException;

import solver.SAT4JSolver;
import abstractGraph.AbstractModel;
import abstractGraph.AbstractState;
import abstractGraph.AbstractStateMachine;
import abstractGraph.AbstractTransition;
import abstractGraph.conditions.AndFormula;
import abstractGraph.conditions.Formula;
import abstractGraph.conditions.cnf.CNFFormula;
import abstractGraph.events.Events;

/**
 * This verifier checks that for all states, all transitions leaving from that
 * state and having a common event have an exclusive Condition field.
 * 
 * It will not consider 2 not exclusive transitions going to the same state and
 * labeled with the same action as an error.
 */
public class DeterminismChecker<M extends AbstractStateMachine<S, T>, S extends AbstractState<T>, T extends AbstractTransition<S>>
    extends AbstractVerificationUnit<M, S, T> {

  /*
   * The solver used to solve the SAT instances. It can be used multiple times
   * for different formulas
   */
  private static SAT4JSolver solver = new SAT4JSolver();

  /*
   * The 2 transitions that offer a counter example if needed (i.e. that are not
   * exclusive while beginning at the same state and being activated by a common
   * Event).
   * Then the machines where the counter example if found.
   * Because we use a single solver, we need to save the solution.
   */
  private LinkedList<T>
      list_counter_example_t1 = new LinkedList<>(),
      list_counter_example_t2 = new LinkedList<>();
  private LinkedList<M> list_counter_example_machine =
      new LinkedList<>();
  private LinkedList<String> solution_details =
      new LinkedList<>();

  private boolean identicalActionFields(T t1,
      T t2) {
    return t1.getActions().equals(t2.getActions());
  }

  private boolean identicalTarget(T t1,
      T t2) {
    return t1.getDestination().equals(t2.getDestination());
  }

  /**
   * Serves for both the {@link #check(AbstractModel, boolean)} and
   * {@link #checkAll(AbstractModel, boolean)} functions.
   */
  private boolean checkFunction(AbstractModel<M, S, T> m, boolean verbose,
      boolean check_all) {

    boolean result = true;

    list_counter_example_t1.clear();
    list_counter_example_t2.clear();
    list_counter_example_machine.clear();
    solution_details.clear();

    /* For all StateMachines */
    Iterator<M> it = m.iterator();
    while (it.hasNext()) {
      M machine = it.next();

      /* For all states within the given state machine */
      Iterator<S> it_states = machine.iterator();
      while (it_states.hasNext()) {
        S state = it_states.next();
        T[] transitions = state.toArray();
        if (transitions == null) {
          continue;
        }
        /* For all couple of different transitions */
        for (int i = 0; i < transitions.length; i++) {
          T t1 = transitions[i];
          Formula t1_formula = t1.getCondition();
          Events t1_events = t1.getEvents();

          for (int j = i + 1; j < transitions.length; j++) {
            T t2 = transitions[j];
            Formula t2_formula = t2.getCondition();
            Events t2_events = t2.getEvents();

            /* If both transitions are labeled with a common event */
            if (t1_events.notEmptyIntersection(t2_events)) {

              CNFFormula formula = CNFFormula.ConvertToCNF(
                  new AndFormula(t1_formula, t2_formula));

              try {
                if (solver.isSatisfiable(formula)) {
                  if (identicalActionFields(t1, t2) &&
                      identicalTarget(t1, t2)) {
                    continue;
                  }
                  list_counter_example_machine.add(machine);
                  list_counter_example_t1.add(t1);
                  list_counter_example_t2.add(t2);
                  solution_details.add(solver.solution());

                  result = false;
                  if (!check_all) {
                    if (verbose) {
                      System.out.println("[FAILURE] Transitions exclusion not"
                          + " verified. Execution stopped at the first error");
                      System.out.println(errorMessage(machine, t1, t2,
                          solver.solution()));
                    }
                    return false;
                  }
                }
              } catch (TimeoutException e) {
                e.printStackTrace();
                throw new RuntimeException(
                    "[FAILURE]TIMEOUT during a SAT solving.");
              }
            }
          }
        }
      }
    }

    if (verbose && result) {
      System.out.println(successMessage());
    } else if (verbose && !result) {
      System.out.println(errorMessage());
    }
    return result;
  }

  @Override
  public boolean check(AbstractModel<M, S, T> m, boolean verbose) {
    return checkFunction(m, verbose, false);
  }

  @Override
  public boolean checkAll(AbstractModel<M, S, T> m, boolean verbose) {
    return checkFunction(m, verbose, true);
  }

  private String errorMessage(
      M counter_example_machine,
      T counter_example_t1,
      T counter_example_t2,
      String solution_details) {
    return "In the state machine "
        + counter_example_machine.getName() +
        ", the transitions \n" +
        counter_example_t1 + " \n" +
        "and \n" +
        counter_example_t2 + "\n" +
        " are not exclusive. \n" +
        " Here is the details of the not exlusivity: " +
        solution_details;
  }

  @Override
  public String errorMessage() {
    StringBuffer result = new StringBuffer();
    int n = list_counter_example_machine.size();

    result.append("[FAILURE] Transitions exclusion not verified:"
        + n + " errors have been found.\n");
    for (int i = 0; i < n; i++) {
      result.append(errorMessage(list_counter_example_machine.get(i),
          list_counter_example_t1.get(i), list_counter_example_t2.get(i),
          solution_details.get(i)) + "\n");
    }

    return result.toString();
  }

  @Override
  public String successMessage() {
    if (list_counter_example_machine.isEmpty()) {
      return "[SUCCESS] Checking that transitions are exlusives, ensuring determinism...OK";
    } else {
      throw new IllegalArgumentException(
          "The last call to check returned with errors."
              + "You should not be calling sucessMessage().");
    }
  }

}
