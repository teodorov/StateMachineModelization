package abstractGraph.conditions;

import java.util.HashSet;

import abstractGraph.conditions.valuation.AbstractValuation;

/**
 * @brief
 *        A disjunction of 2 formulas.
 * 
 */
public class OrFormula extends Formula {

  Formula p, q;

  /**
   * Create p OR q.
   * 
   * @param p
   *          The left formula.
   * @param q
   *          The right formula.
   */
  public OrFormula(Formula p, Formula q) {
    assert (p != null && q != null);
    this.p = p;
    this.q = q;
  }

  @Override
  public boolean eval(AbstractValuation valuation) {
    return p.eval(valuation) || q.eval(valuation);
  }

  /**
   * An OR formula represents : p OR q. This function returns p.
   * 
   * @return The first formula p of this OR
   */
  public Formula getFirst() {
    return p;
  }

  /**
   * An OR formula represents : p OR q. This function returns q.
   * 
   * @return The second formula q of this OR
   */
  public Formula getSecond() {
    return q;
  }

  @Override
  public HashSet<EnumeratedVariable> allVariables(
      HashSet<EnumeratedVariable> vars) {
    p.allVariables(vars);
    q.allVariables(vars);
    return vars;
  }

  @Override
  public String toString() {
    return parenthesis(p) + " " + Formula.OR + " " + parenthesis(q);
  }

  private String parenthesis(Formula f) {
    return f.toString();
  }
}
