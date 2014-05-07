package abstractGraph.Conditions;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.gui.TreeViewer;

import abstractGraph.Conditions.parser.BooleanExpressionLexer;
import abstractGraph.Conditions.parser.BooleanExpressionParser;
import abstractGraph.Conditions.parser.GenerateFormula;

/**
 * Boolean expression formula
 * 
 */
public abstract class Formula implements AbstractCondition {

  public static final String AND = "ET";
  public static final String OR = "OU";
  public static final String NOT = "NON";

  static public Formula newFormula(String expression) {
    Formula result = parse(expression);
    return result;
  }

  @Override
  public abstract String toString();

  /**
   * Parse literally a string expression into a formula.
   * The formula accepts AND, &&, &, ET as the "and" operator ;
   * OR, ||, |, OU as the "or" operator".
   * 
   * @param expression
   *          The string to parse
   * @param view_tree
   *          Set to "debug" mode and display the parsed formula as a tree
   * @return The exact formula parsed without any modification.
   */
  static public Formula parse(String expression, boolean view_tree) {
    ANTLRInputStream input = new ANTLRInputStream(expression);

    /* Create a lexer that feeds off of input CharStream */
    BooleanExpressionLexer lexer = new BooleanExpressionLexer(input);

    /* Create a buffer of tokens pulled from the lexer */
    CommonTokenStream tokens = new CommonTokenStream(lexer);

    /* Create a parser that feeds off the tokens buffer */
    BooleanExpressionParser parser = new BooleanExpressionParser(tokens);
    /* begin parsing at booleanExpression rule */
    ParseTree tree = parser.booleanExpression();

    if (view_tree) {
      TreeViewer viewer = new TreeViewer(null, tree);
      viewer.open();
    }
    GenerateFormula generation_of_formula = new GenerateFormula();
    Formula f = generation_of_formula.visit(tree);
    return f;
  }

  /**
   * {@inheritDoc #parse(String, boolean)}
   * 
   * @details Parse a formula using parse(expresssion, false).
   * @see #parse(String, boolean)
   */
  static public Formula parse(String expression) {
    return parse(expression, false);
  }
}
