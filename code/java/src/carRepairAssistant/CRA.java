package carRepairAssistant;

import jess.Rete;
import jess.JessException;
import jess.WorkingMemoryMarker;
import java.io.Console;
import java.util.List;
import java.util.ArrayList;

public class CRA {

    private Rete jess;
    private ConsoleCheat c;
    private String[] currentHypothesis = {"", ""};

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

    private void askAvailableHypothesis(List<String[]> allHypothesis) throws JessException{
        c.printf("Choose an hypothesis or push enter:\n");
        for(int i=0; i<allHypothesis.size(); i++){
            c.printf(i + " " + allHypothesis.get(i)[0] + " is " + allHypothesis.get(i)[1] + "\n");
        }
        String choice = c.readLine();

        choice = choice.trim();
        if (isNumber(choice)){
            int nrChoice = Integer.parseInt(choice);
            currentHypothesis = allHypothesis.remove(nrChoice);
        } else if(choice.contains(" is ")) {
            currentHypothesis[0] = choice.substring(0, choice.indexOf(" "));
            currentHypothesis[1] = choice.substring(choice.lastIndexOf(" ")+1, choice.length());
        } else {
            currentHypothesis = allHypothesis.remove(allHypothesis.size()-1);
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

    private List<String[]> allHypothesis() throws JessException{
        List<String[]> result = new ArrayList<String[]>();
        jess.QueryResult hypothesis = generateHypothesis();
        int i = 0;
        while (hypothesis.next()) {
            String[] h = new String[2];
            h[0] = hypothesis.getString("component");
            h[1] = hypothesis.getString("state");
            result.add(h);
        }
        return result;
    }

    private jess.QueryResult generateHypothesis() throws JessException {
        return jess.runQueryStar("search-hypothesis", new jess.ValueVector());
    }

    private boolean selectHypothesis(List<String[]> hypothesis) throws JessException {
        if(hypothesis.size() != 0) {
            currentHypothesis = hypothesis.get(0);
            return true;
        } else {
            return false;
        }

    }

    private void printHypothesis() {
        c.printf(
            "The hypothesis is that " +
            currentHypothesis[0] +
            " is " + 
            currentHypothesis[1] + "\n"
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

    private boolean negotiateObservable() throws JessException {
	WorkingMemoryMarker beforeHypothesis = jess.mark();
        c.printf("Trying hypothesis\n");
        jess.assertString(
            "(hypothesis " +
            currentHypothesis[0] + " " +
            currentHypothesis[1] + ")"
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
                    return true;
                } else if (answer.equals("false")) {
                    jess.resetToMark(beforeHypothesis);
                    jess.assertString(
                        "(observed " +
                        observable +
                        " FALSE)"
                    );
                    jess.run();
                    return true;
                } else if (answer.equals("no")) {
                    break;
                } else {
                    c.printf("try again: true/false/no\n");
                    continue;
                }
            }
	}
	c.printf("No observables for this hypothesis \n");
        return false;
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
            boolean found_hypothesis;
            do {
                List<String[]> hypothesis = allHypothesis();
                found_hypothesis = (hypothesis.size() > 0);
                boolean observed = false;
                while(!observed && hypothesis.size() > 0) {
                    askAvailableHypothesis(hypothesis);
                    printHypothesis();
                    observed = negotiateObservable();
                }
                if (!observed) break;
            } while (found_hypothesis);
        } catch (JessException ex) {
            System.err.println(ex);
        }
    }

    public static void main(String[] arg) {
        new CRA();
    }
}
