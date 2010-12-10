package carRepairAssistant;

import jess.Rete;
import jess.JessException;
import jess.WorkingMemoryMarker;
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

    private void negotiateObservable() throws JessException {
	WorkingMemoryMarker beforeHypothesis = jess.mark();
        c.printf("Trying hypothesis\n");
        jess.assertString(
            "(hypothesis " +
            currentHypothesisComponent + " " + 
            currentHypothesisState + ")"
        );
        jess.run();
        //printFacts();
        c.printf("Querying Observables\n");
	jess.QueryResult observables =
	    jess.runQueryStar("search-observable", new jess.ValueVector());
        String answer = "no";
	while (observables.next() && answer.equals("no")) {
            String observable = observables.getString("observable");
            c.printf(
                "You want to observe " +
                observable + "? true/false/no\n"
            );
            boolean noAnswer = true;
            while (noAnswer) {
                answer = c.readLine();
                if (answer.equals("true")) {
                    jess.resetToMark(beforeHypothesis);
                    jess.assertString(
                        "(observed " +
                        observable + 
                        " TRUE)"
                    );
                    jess.run();
                    return;
                } else if (answer.equals("false")) {
                    jess.resetToMark(beforeHypothesis);
                    jess.assertString(
                        "(observed " +
                        observable + 
                        " FALSE)"
                    );
                    jess.run();
                    return;
                } else if (answer.equals("no")) {
                    //
                } else {
                    c.printf("try again: true/false/no\n");
                    continue;
                }
                noAnswer = false;
            }
	}
	c.printf("No observables for this hypothesis \n");
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
            jess.batch("jess/test/negotiate-test.jess");
            jess.reset();
            //printFacts();
            askComplaint();
            //printFacts();
            jess.run();
            while(selectHypothesis()) {
                printHypothesis();
                negotiateObservable();
            }
        } catch (JessException ex) {
            System.err.println(ex);
        }
    }
    
    public static void main(String[] arg) {
        new CRA();
    }
}
