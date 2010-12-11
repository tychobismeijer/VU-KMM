package carRepairAssistant;

import jess.Rete;
import jess.JessException;
import jess.WorkingMemoryMarker;
import java.io.Console;

public class CRA {

    private Rete jess;
    private ConsoleCheat c;
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

    private void askAvailableHypothesis() throws JessException{
        String[][] allHypothesis = allHypothesis();
        c.printf("Choose an hypothesis:\n");
        for(int i=0; i<allHypothesis.length; i++){
            c.printf(i + " " + allHypothesis[i][0] + " is " + allHypothesis[i][1] + "\n");
        }
        String choice = c.readLine();

        choice = choice.trim();
        if (isNumber(choice)){
            int nrChoice = Integer.parseInt(choice);
            currentHypothesisComponent = allHypothesis[nrChoice][0];
            currentHypothesisState = allHypothesis[nrChoice][1];
        } else if(choice.contains(" is ")){
            currentHypothesisComponent = choice.substring(0, choice.indexOf(" "));
            currentHypothesisState = choice.substring(choice.lastIndexOf(" ")+1,choice.length());
        }
    }

    private boolean isNumber(String string){
        try{
            Integer.parseInt(string);
            return true;
        }
        catch(NumberFormatException nfe){
            return false;
        }
    }

    private String[][] allHypothesis() throws JessException{
        int nrHypothesis = countHypothesis();
        String[][] result = new String[nrHypothesis][2];
        jess.QueryResult hypothesis = generateHypothesis();
        for(int i = 0; i<nrHypothesis; i++){
            selectHypothesis(hypothesis);
            result[i][0] = currentHypothesisComponent;
            result[i][1] = currentHypothesisState;
        }
        return result;
    }

    private int countHypothesis() throws JessException{
        int result = 0;
        jess.QueryResult hypothesis = generateHypothesis();
        while(selectHypothesis(hypothesis)){
            result++;
        }
        return result;
    }

    private jess.QueryResult generateHypothesis() throws JessException {
        return jess.runQueryStar("search-hypothesis", new jess.ValueVector());
    }

    private boolean selectHypothesis(jess.QueryResult hypothesis) throws JessException {
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

    public CRA() {
        jess = new Rete();
        c = new ConsoleCheat();
        try {
            jess.batch("jess/test/negotiate-test.jess");
            jess.reset();
            printFacts();
            askComplaint();
            //printFacts();
            jess.run();
            jess.QueryResult hypothesis = generateHypothesis();
            while(selectHypothesis(hypothesis)) {
                askAvailableHypothesis();
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
