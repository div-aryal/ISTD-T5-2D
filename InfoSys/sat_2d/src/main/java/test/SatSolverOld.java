package test;


import test.immutable.EmptyImList;
import test.immutable.ImList;
import test.env.Environment;
import test.env.Variable;
import test.formula.Clause;
import test.formula.Formula;
import test.formula.Literal;
import test.formula.PosLiteral;

/**
 * A simple DPLL SAT solver. See http://en.wikipedia.org/wiki/DPLL_algorithm
 */
public class SatSolverOld {
    /**
     * Solve the problem using a simple version of DPLL with backtracking and
     * unit propagation. The returned environment binds literals of class
     * bool.Variable rather than the special literals used in clausification of
     * class clausal.Literal, so that clients can more readily use it.
     *
     * @return an environment for which the problem evaluates to Bool.TRUE, or
     * null if no such environment exists.
     */
    public static Environment solve(Formula formula) {
        ImList<Clause> clauses = formula.getClauses();
        Environment newEnv = new Environment();
        return solve(clauses, newEnv);
    }

    /**
     * Takes a partial assignment of variables to values, and recursively
     * searches for a complete satisfying assignment.
     *
     * @param clauses formula in conjunctive normal form
     * @param env     assignment of some or all variables in clauses to true or
     *                false values.
     * @return an environment for which all the clauses evaluate to Bool.TRUE,
     * or null if no such environment exists.
     */
    private static Environment solve(ImList<Clause> clauses, Environment env) {
        // trivially satisfiable if clauses is empty
        if (clauses.isEmpty()) {
            return env;
        }

        // get smallest clause or first unit clause
        Clause smallestClause = new Clause();
        int minClauseSize = Integer.MAX_VALUE;
        for (Clause clause : clauses) {
            int clauseSize = clause.size();

            // not satisfiable if clause is empty
            if (clauseSize == 0) {
                return null;
            }

            // get the smaller clause of the two
            if (clauseSize < minClauseSize) {
                minClauseSize = clauseSize;
                smallestClause = clause;
            }
        }

        Literal lit = smallestClause.chooseLiteral();
        Variable var = lit.getVariable();
        ImList<Clause> tempClauses;
        Environment tempEnv;

        // if unit clause, set the literal such that the clause is satisfied
        if (minClauseSize == 1) {
            tempClauses = substitute(clauses, lit);
            tempEnv = (lit instanceof PosLiteral) ?
                    env.putTrue(var) : env.putFalse(var);
            return solve(tempClauses, tempEnv);
        }

        Literal tempLiteral = PosLiteral.make(var);
        Environment solutionEnv;

        // if not unit clause
        // try setting first literal to True
        tempEnv = env.putTrue(var);
        tempClauses = substitute(clauses, tempLiteral);
        solutionEnv = solve(tempClauses, tempEnv);

        // if the above fails, set the literal to False
        if (solutionEnv == null) {
            tempEnv = env.putFalse(var);
            tempLiteral = tempLiteral.getNegation();
            tempClauses = substitute(clauses, tempLiteral);
            return solve(tempClauses, tempEnv);
        }

        return solutionEnv;
    }

    /**
     * given a clause list and literal, produce a new list resulting from
     * setting that literal to true
     *
     * @param clauses , a list of clauses
     * @param literal       , a literal to set to true
     * @return a new list of clauses resulting from setting l to true
     */
    private static ImList<Clause> substitute(ImList<Clause> clauses, Literal literal) {
        if (clauses.isEmpty()) return clauses;

        ImList<Clause> subClauses = new EmptyImList<>();
        for (Clause clause : clauses) {
            Clause reducedClause = clause.reduce(literal);
            if (reducedClause != null) subClauses = subClauses.add(reducedClause);
        }

        return subClauses;
    }

}