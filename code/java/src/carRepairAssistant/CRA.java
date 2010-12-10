package carRepairAssistant;

import jess.Rete;
import jess.JessException;
import java.io.Console;

public class CRA {

    private Rete jess;
    private Console c;
    private String
        currentHypothesisComponent,
        currentHypothesisState;

    private void askComplaint() {
        c.printf("Your complaint is?\n");
        String observable = c.readLine();
        String fact = "(complaint " + observable + " TRUE)";
        try {
            jess.assertString(fact);
        } catch (JessException ex) {
            System.err.println(ex);
        }
    }

    private boolean selectHypothesis() throws JessException {
        jess.QueryResult hypothesis =
            jess.runQueryStar("search-hypothesis", new jess.ValueVector());
        if (hypothesis.next()) {
            currentHypothesisComponent = hypothesis.getString("component");
            currentHypothesisState = hypothesis.getString("state");
            return true;
        } else { return false; }
    }

    private void printHypothesis() {
        c.printf(
            "The hypothesis is that " +
            currentHypothesisComponent +
            " is " + 
            currentHypothesisState + "\n"
        );
    }

    private void printFacts() {
        try {
            java.io.Writer co = new java.io.PrintWriter(System.out);
            co.write("-----------------------------------\n");
            jess.ppFacts(co);
            co.flush();
        } catch (java.io.IOException ex) {
            System.err.println(ex);
        }
    }

    public CRA() {
        jess = new Rete();
        c = System.console();
        try {
            jess.batch("jess/test/select-test.jess");
            jess.reset();
            //printFacts();
            askComplaint();
            //printFacts();
            jess.run();
            selectHypothesis();
            printHypothesis();
        } catch (JessException ex) {
            System.err.println(ex);
        }
        //printFacts();
    }
    
    public static void main(String[] arg) {
        new CRA();
    }
}
