package carRepairAssistant;

import jess.Rete;
import jess.JessException;
import java.io.Console;

public class CRA {

    private Rete jess;
    private Console c;
    private String current_hypothesis;

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

    private void selectHypothesis() throws JessException {
        jess.QueryResult hypothesis =
            jess.runQueryStar("search-hypothesis", new jess.ValueVector());
        c.printf("It could be that:\n");
        while (hypothesis.next()) {
            c.printf(
                hypothesis.getString("component") + " " +
                hypothesis.getString("state") + "\n"
            );
        }

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
        } catch (JessException ex) {
            System.err.println(ex);
        }
        //printFacts();
    }
    
    public static void main(String[] arg) {
        new CRA();
    }
}
