/** This code is copyright Articulate Software (c) 2003.
This software is released under the GNU Public License
<http://www.gnu.org/copyleft/gpl.html>.  Users of this code also consent,
by use of this code, to credit Articulate Software and Teknowledge in any
writings, briefings, publications, presentations, or other representations
of any software which incorporates, builds on, or uses this code.  Please
cite the following article in any publication with references:

Pease, A., (2003). The Sigma Ontology Development Environment, in Working
Notes of the IJCAI-2003 Workshop on Ontology and Distributed Systems,
August 9, Acapulco, Mexico. see also 
http://sigmakee.sourceforge.net 
*/

/*************************************************************************************************/
package com.articulate.sigma.trans;

import tptp_parser.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

import com.articulate.sigma.*;

public class TPTP2SUMO {

    public static boolean debug = false;

  /** ***************************************************************
   * Convenience routine that calls the main convert() method below
   */
  public static String convert (String tptp, ArrayList<Binding> answer, 
                                boolean instantiated) throws Exception {

      return convert(new BufferedReader(new StringReader(tptp)), answer, instantiated);
  }

  /** ***************************************************************
   * Convenience routine that calls the main convert() method below
   */
  public static String convert (Reader in, ArrayList<Binding> answer, 
                                boolean instantiated) throws Exception {

      return convert(new BufferedReader(in), answer, instantiated);
  }

  /** ***************************************************************
  * Convert a TPTP file to SUMO

  public static String convertFile(String filename, boolean instantiated) throws Exception {

      ArrayList<Binding> answer = null;
      TPTPVisitor v = new TPTPVisitor();
      v.parseFile(filename);
       //----Start output
      StringBuffer result = new StringBuffer();
      for (String name : v.result.keySet()) {
          TPTPFormula formula = v.result.get(name);
          String sumo = convertBareTPTPFormula(formula, v.result, instantiated).toString();
          Formula sumoFormula = new Formula(sumo);
          result.append(sumoFormula.textFormat(sumo) + "\n\n");
      }
      result.append(result);
      return result.toString();
  }
*/
    /** ***************************************************************
     * Convert a TPTP formula to SUMO

    public static String convertString(String tptp, boolean instantiated) throws Exception {

        ArrayList<Binding> answer = null;
        HashMap<String,TPTPFormula> forms = TPTPVisitor.parseString(tptp);
        if (forms.values().size() > 1)
            System.out.println("Error in convertString(): expected one formula but found " + forms);
        StringBuffer result = new StringBuffer();
        TPTPFormula f = forms.values().iterator().next();
        String sumo = convertBareTPTPFormula(f, TPTPVisitor.result, instantiated).toString();
        Formula sumoFormula = new Formula(sumo);
        result.append(sumoFormula.textFormat(sumo) + "\n\n");
        return result.toString();
    }
*/
    /** ***************************************************************
     * Convert a TPTP statement to SUMO

    private static StringBuffer convertBareTPTPFormula (TPTPFormula formula,
                                                    HashMap<String,TPTPFormula> ftable,
                                                    boolean instantiated) {

        StringBuffer result = new StringBuffer();
        int indent = 12;
        int indented = 0;
        if (debug) System.out.println("convertBareTPTPFormula(): " + formula);
        //----Add parents info as "premises"
        for (TPTPFormula parent : formula.parent)
            result.append(convertBareType(parent, 0, indented,true));
        result.append(convertBareType(formula, 0, indented,true));
        result.append("\n\n");
        if (debug) System.out.println("convertBareTPTPFormula(): result: " + result);
        return result;
    }
*/
  /** ***************************************************************
   * Remove binary cascading or's and and's and consolidate as single
   * connectives with more arguments.  For example
   * (and (and A B) C) becomes (and A B C)
   */
  public static Formula collapseConnectives(Formula form) {

      if (!form.isBalancedList())
          return form;
      if (debug) System.out.println("collapseConnectives(): input: " + form);
      ArrayList<Formula> args = form.complexArgumentsToArrayList(1);
      if (args == null)
          return form;
      StringBuffer sb = new StringBuffer();
      String pred = form.car();
      sb.append("(" + pred + " ");
      ArrayList<Formula> newargs = new ArrayList<>();
      if (debug) System.out.println("collapseConnectives(): args: " + args);
      for (Formula f : args)
          newargs.add(collapseConnectives(f));
      if (debug) System.out.println("collapseConnectives(): newargs: " + newargs);
      if (pred.equals("or") || pred.equals("and")) {
          for (Formula f : newargs) {
              if (f.car().equals(pred)) {
                  if (debug) System.out.println("collapseConnectives(): matching connectives in " + f);
                  ArrayList<Formula> subargs = f.complexArgumentsToArrayList(1);
                  if (debug) System.out.println("collapseConnectives(): subargs " + subargs);
                  for (Formula f2 : subargs)
                      sb.append(f2.toString() + " ");
                  if (debug) System.out.println("collapseConnectives(): after adding to " + f + " result is " + sb);
              }
              else {
                  if (debug) System.out.println("collapseConnectives(): not matching connective in " + f);
                  if (debug) System.out.println("collapseConnectives(): adding to " + sb);
                  sb.append(f.toString() + " ");
              }
          }
      }
      else {
          for (Formula f : newargs)
            sb.append(f.toString() + " ");
      }
      sb.deleteCharAt(sb.length()-1);
      sb.append(")");
      Formula newForm = new Formula(sb.toString());
      if (debug) System.out.println("collapseConnectives(): result: " + newForm);
      return newForm;
  }
  
  /** ***************************************************************
   * Convert a single annotated TPTP clause to a single SUMO formula, possibly XML-wrapped.
   * This is the main entry point for this class.

  public static StringBuffer convertType (TPTPFormula formula, int indent, int indented) {

      StringBuffer result = new StringBuffer();
      String type = "";
      String id = formula.name;
      TptpParser.Annotated_formulaContext item = formula.parsedFormula;
      if (item.getKind() == SimpleTptpParserOutput.TopLevelItem.Kind.Formula) {
          SimpleTptpParserOutput.AnnotatedFormula AF = (SimpleTptpParserOutput.AnnotatedFormula) item;
          type = "formula";
          String form = convertFormula(AF.getFormula(),indent,indented).toString();
          form = collapseConnectives(new Formula(form)).toString();
          result.append(form);
      } 
      else if (item.getKind() == SimpleTptpParserOutput.TopLevelItem.Kind.Clause) {
          SimpleTptpParserOutput.AnnotatedClause AC = (SimpleTptpParserOutput.AnnotatedClause) item;
          type = "clause";
          String form = convertClause(AC.getClause(),indent,indented).toString();
          form = collapseConnectives(new Formula(form)).toString();
          result.append(form);
      } 
      else 
          result.append("Error: TPTP Formula syntax unknown for converting");      
      return result;
  }
*/
    /** ***************************************************************
     * Convert a single annotated TPTP clause to a single SUMO formula, possibly XML-wrapped.
     * This is the main entry point for this class.

    public static StringBuffer convertBareType (TPTPFormula formula, int indent, int indented, boolean noXML) {

        StringBuffer result = new StringBuffer();
        String type = "";
        int id = formula.id;
        SimpleTptpParserOutput.TopLevelItem item = formula.item;
        if (item.getKind() == SimpleTptpParserOutput.TopLevelItem.Kind.Formula) {
            SimpleTptpParserOutput.AnnotatedFormula AF = (SimpleTptpParserOutput.AnnotatedFormula) item;
            type = "formula";
            result.append(convertFormula(AF.getFormula(),indent,indented));
        }
        else if (item.getKind() == SimpleTptpParserOutput.TopLevelItem.Kind.Clause) {
            SimpleTptpParserOutput.AnnotatedClause AC = (SimpleTptpParserOutput.AnnotatedClause) item;
            type = "clause";
            result.append(convertClause(AC.getClause(),indent,indented));
        }
        else
            result.append("Error: TPTP Formula syntax unknown for converting");
        return result;
    }
*/

  /** ***************************************************************
   */
  private static StringBuffer convertConnective (SimpleTptpParserOutput.BinaryConnective connective) {

      StringBuffer result = new StringBuffer();
      switch (connective) {
      case And:
          result.append("and");
          break;
      case Or:
          result.append("or");
          break;
//      case Equal:
//         result.append("equal");
//          break;
      case Equivalence: 
          result.append("<=>");
          break;
      case Implication:
          result.append("=>");
          break;
      case ReverseImplication:
          result.append("<=");
          break;
      case Disequivalence:
          result.append("not <=>");
          break;
      case NotOr:
          result.append("not or");
          break;
      case NotAnd:
          result.append("not and");
          break;
      default:
          result.append("Not a connective");
          break;
      }
      return result;
  }

  /** ***************************************************************
   */
  private static String convertQuantifier (SimpleTptpParserOutput.Quantifier quantifier) {
  
      switch (quantifier) {
      case ForAll:
          return "forall";
      case Exists:
          return "exists";
      default:
          return "Not a quantifier";
      }
  }

  /** ***************************************************************
   */
  private static boolean kifAssociative (SimpleTptpParserOutput.BinaryConnective connective) {

      switch (connective) {
      case And:
      case Or:
          return true;
      default:
          return false;
      }
  }

  /** ***************************************************************
   */
  private static String addIndent (int indent, int indented) {

      String res = "";
      for (int i = indented+1; i <= indent; i++) 
          res += " ";      
      return res;    
  }

  /** ***************************************************************
   * remove dollar sign, for special tptp terms such as $false and $true
   */
  private static String removeDollarSign (String argument) {

      if (argument.length() > 0) {
          if (argument.charAt(0) == '$') 
              return argument.substring(1,argument.length());
          else
              return argument;          
      }
      return "";
  }
  
  /** ***************************************************************
   * remove termVariablePrefix
   */
  private static String transformVariable (String variable) {

      return variable.replace(Formula.termVariablePrefix, "");
  } 

  /** ***************************************************************
   * remove termSymbolPrefix and termMentionSuffix
   */
  public static String transformTerm (String term) {

      term = term.replaceFirst(Formula.termSymbolPrefix, "");
      term = term.replace(Formula.termMentionSuffix, "");
      if (term.matches(".*__\\d"))
          term = term.substring(0,term.length()-3);
      return term;
  }

  /** ***************************************************************
   * A term is a variable or function symbol with term arguments.
   * Variables are symbols with a question mark in SUO-KIF or capitalized symbols
   * in TPTP.  Constants are functions with arity 0.
   */
  private static String convertTerm (SimpleTptpParserOutput.Formula.Atomic atom) {

	//  System.out.println("INFO in TPTP2SUMO.convertTerm(): " + atom);
      String res = "";
      LinkedList<SimpleTptpParserOutput.Term> arguments = (LinkedList)atom.getArguments();
      if (arguments != null) 
          res += "(";
      // "esk" comes up in the function SigGetNewSkolemCode in E's source code, with the form esk<count>_<ar>, standing for Skolemization
      if (!atom.getPredicate().startsWith("esk"))
          res += transformTerm(removeDollarSign(atom.getPredicate()));
      if (arguments != null) {
          for (int n = 0; n < arguments.size();  n++) {
              if (((SimpleTptpParserOutput.Term)arguments.get(n)).getTopSymbol().isVariable()) 
                  res += " " + "?" + transformVariable(arguments.get(n).toString());
              else if (arguments.get(n).toString().indexOf("(") != -1)
            	  res += " " + convertTerm(arguments.get(n).toAtom());
              else
                  res += " " + transformTerm(removeDollarSign(arguments.get(n).toString()));              
          }
      }
      if (arguments != null) 
          res += ")";      
      return res;
  }

  /** ***************************************************************
   * A formula is a literal or compound formula.
   * A compound formula consists of  (logical operator formula [formula]*)
   * (but conforming to arity of logical operators).
   */
  private static StringBuffer convertFormula (SimpleTptpParserOutput.Formula formula, int indent, int indented) {

	//  System.out.println("INFO in TPTP2SUMO.convertFormula(): " + formula);
      StringBuffer result = new StringBuffer();
      switch(formula.getKind()) {
      case Atomic:
          result.append(addIndent(indent,indented));
          result.append(convertTerm((SimpleTptpParserOutput.Formula.Atomic)formula));
          break;
      case Negation:
          result.append(addIndent(indent,indented));
          result.append("(" + "not" + " ");
          result.append(convertFormula(((SimpleTptpParserOutput.Formula.Negation)formula).getArgument(),indent+4,indent+4));
          result.append(")");
          break;
      case Binary:
          result.append(addIndent(indent,indented));
          result.append("(");
          result.append(convertConnective(((SimpleTptpParserOutput.Formula.Binary)formula).getConnective()));
          result.append(" ");
          result.append(convertFormula(((SimpleTptpParserOutput.Formula.Binary)formula).getLhs(),indent+4,indent+4));
          result.append("\n");
          while (kifAssociative(((SimpleTptpParserOutput.Formula.Binary)formula).getConnective()) &&
                 ((SimpleTptpParserOutput.Formula.Binary)formula).getRhs().getKind() == SimpleTptpParserOutput.Formula.Kind.Binary &&
                 ((SimpleTptpParserOutput.Formula.Binary)formula).getConnective() == 
                   ((SimpleTptpParserOutput.Formula.Binary)((SimpleTptpParserOutput.Formula.Binary)formula).getRhs()).getConnective()) {
              formula = ((SimpleTptpParserOutput.Formula.Binary)formula).getRhs();
              result.append(convertFormula(((SimpleTptpParserOutput.Formula.Binary)formula).getLhs(),indent+4,0));
              result.append("\n");
          }
          result.append(convertFormula(((SimpleTptpParserOutput.Formula.Binary)formula).getRhs(),indent+4,0));
          result.append(")");
          break;
      case Quantified:
          result.append(addIndent(indent,indented));
          result.append("(");
          result.append(convertQuantifier(((SimpleTptpParserOutput.Formula.Quantified)formula).getQuantifier()));
          result.append(" (");
          result.append("?" + transformVariable(((SimpleTptpParserOutput.Formula.Quantified)formula).getVariable()));
          while (((SimpleTptpParserOutput.Formula.Quantified)formula).getKind() == SimpleTptpParserOutput.Formula.Kind.Quantified &&
                 ((SimpleTptpParserOutput.Formula.Quantified)formula).getMatrix().getKind() == SimpleTptpParserOutput.Formula.Kind.Quantified &&
                 ((SimpleTptpParserOutput.Formula.Quantified)formula).getQuantifier() == 
                   ((SimpleTptpParserOutput.Formula.Quantified)((SimpleTptpParserOutput.Formula.Quantified)formula).getMatrix()).getQuantifier()) {
              formula = ((SimpleTptpParserOutput.Formula.Quantified)formula).getMatrix();
              result.append(" ?" + transformVariable(((SimpleTptpParserOutput.Formula.Quantified)formula).getVariable()));
          }
          result.append(") ");
          if (((SimpleTptpParserOutput.Formula.Quantified)formula).getMatrix().getKind() == SimpleTptpParserOutput.Formula.Kind.Negation ||
              ((SimpleTptpParserOutput.Formula.Quantified)formula).getMatrix().getKind() == SimpleTptpParserOutput.Formula.Kind.Atomic) {
              result.append(convertFormula(((SimpleTptpParserOutput.Formula.Quantified)formula).getMatrix(),indent,indent));
          } 
          else {
              result.append("\n");
              result.append(convertFormula(((SimpleTptpParserOutput.Formula.Quantified)formula).getMatrix(),indent + 4,0));
          }
          result.append(")");
          break;
      default:
          result.append("Error in TPTP2SUMO.convertFormula(): TPTP Formula syntax unkown for converting");
          break;
      }
      return result;
  }

  /** ***************************************************************
   */
  private static StringBuffer convertClause (SimpleTptpParserOutput.Clause clause, int indent, int indented) {

	//  System.out.println("INFO in TPTP2SUMO.convertClause(): " + clause);
      StringBuffer result = new StringBuffer();
      LinkedList<SimpleTptpParserOutput.Literal> literals = (LinkedList) clause.getLiterals();
      result.append(addIndent(indent,indented));
      if (literals == null) {
          result.append("false\n");
          return result;
      }   
      assert !literals.isEmpty();
      if (literals.size() == 1)
          result.append(convertLiteral(literals.get(0),indent,indent));
      else {
          result.append("(");
          result.append(convertConnective(SimpleTptpParserOutput.BinaryConnective.Or));
          result.append(" ");
          result.append(convertLiteral(literals.get(0),indent,indent));
          for (int i = 1; i < literals.size(); i++) {
              result.append("\n");
              result.append(convertLiteral(literals.get(i),indent+4,0));
          }
          result.append(")");
      }
      return result;
  }

  /** ***************************************************************
   * An atom is a predicate symbol with term arguments.
   * A literal is an atom or negated atom.
   */
  private static StringBuffer convertLiteral (SimpleTptpParserOutput.Literal literal, int indent, int indented) {
  
	//  System.out.println("INFO in TPTP2SUMO.convertLiteral(): " + literal);
      StringBuffer result = new StringBuffer();
      result.append(addIndent(indent,indented));
      if (literal.isPositive()) 
          result.append(convertTerm((SimpleTptpParserOutput.Formula.Atomic)literal.getAtom()));
      else {
          result.append("(not ");
          result.append(convertTerm((SimpleTptpParserOutput.Formula.Atomic)literal.getAtom()));
          result.append(")");
      }
      return result;
  }

    /** ***************************************************************
     */
    public static void showHelp() {

        System.out.println("KB class");
        System.out.println("  options:");
        System.out.println("  -h - show this help screen");
        System.out.println("  -t - run test");
        System.out.println("  -c <fname> - convert file to SUO-KIF");
    }

  /** ***************************************************************
   */
  public static void main (String args[]) {

      System.out.println("INFO in TPTP2SUMO.main()");
      if (args != null && args.length > 0 && args[0].equals("-h"))
          showHelp();
      else {
          //String formula = "fof(1,axiom,(    s_holds_2__(s_p,s_a) ),    file('/tmp/SystemOnTPTP11002/Simple2965.tptp',kb_Simple_1)).fof(2,conjecture,(    s_holds_2__(s_p,s_a) ),    file('/tmp/SystemOnTPTP11002/Simple2965.tptp',prove_from_Simple)).fof(3,negated_conjecture,(    ~ s_holds_2__(s_p,s_a) ),    inference(assume_negation,[status(cth)],[2])).fof(4,negated_conjecture,(    ~ s_holds_2__(s_p,s_a) ),    inference(fof_simplification,[status(thm)],[3,theory(equality)])).cnf(5,plain,    ( s_holds_2__(s_p,s_a) ),    inference(split_conjunct,[status(thm)],[1])).cnf(6,negated_conjecture,    ( ~ s_holds_2__(s_p,s_a) ),    inference(split_conjunct,[status(thm)],[4])).cnf(7,negated_conjecture,    ( $false ),    inference(rw,[status(thm)],[6,5,theory(equality)])).cnf(8,negated_conjecture,    ( $false ),    inference(cn,[status(thm)],[7,theory(equality)])).cnf(9,negated_conjecture,    ( $false ),    8,    [proof]).";
          //String formula = "fof(pel55,conjecture,(killed(X,Z) )). cnf(1,plain,( agatha = butler| hates(agatha,agatha) ),inference(subst,[[X,$fot(X0)]],[pel55])). cnf(6,plain,( a ) , inference(subst,[[X0,$fot(skolemFOFtoCNF_X)],[Z,$fot(a)]],[1])).";
          //String formula = "fof(pel55_1,axiom,(? [X] : (lives(X) & killed(X,agatha) ) )).fof(pel55,conjecture,(? [X] : killed(X,agatha) )).cnf(0,plain,(killed(skolemFOFtoCNF_X,agatha)), inference(fof_to_cnf,[],[pel55_1])).cnf(1,plain,(~killed(X,agatha)),inference(fof_to_cnf,[],[pel55])).cnf(2,plain,(~killed(skolemFOFtoCNF_X,agatha)),inference(subst,[[X,$fot(skolemFOFtoCNF_X)]],[1])).cnf(3,theorem,($false),inference(resolve,[$cnf(killed(skolemFOFtoCNF_X,agatha))],[0,2])).";
          //String formula = "fof(ax1,axiom,(! [X0] : (~s__irreflexiveOn(s__relatedInternalConcept__m,X0) | ! [X1] : (~s__instance(X0,s__Class) | ~s__instance(X1,X0) | ~s__relatedInternalConcept(X1,X1))))).";
          //String clause  = "cnf(ax1,axiom,(~s__irreflexiveOn(s__relatedInternalConcept__m,X0) | ~s__instance(X0,s__Class) | ~s__instance(X1,X0) | ~s__relatedInternalConcept(X1,X1))).";
          String clause = "cnf(c_0_10,negated_conjecture,($answer(esk1_1(X1))|~s__subclass(X1,s__Object)), c_0_8).";
          String inFile;
          FileReader file;
          String kif = "";
          try {
              if (args.length > 1 && args[0].equals("-c")) {
                  inFile = args[1];
                  file = new FileReader(inFile);
                  //kif = TPTP2SUMO.convertFile(args[1], false);
                  System.out.println(kif);
              }
              if (args.length > 0 && args[0].equals("-t")) {
                  StringReader reader = new StringReader(clause);
                  // kif = TPTP2SUMO.convert(reader, false);
                  TPTPParser tptpP = TPTPParser.parse(clause);
                  System.out.println(tptpP.result);
                  for (String id : tptpP.result.keySet()) {
                      TPTPFormula tptpF = tptpP.result.get(id);
                      //System.out.println(TPTP2SUMO.convertType(tptpF, 0, 0));
                  }
              }
          }
          catch (Exception e) {
              System.out.println("e: " + e);
          }
      }
  }
}
 
