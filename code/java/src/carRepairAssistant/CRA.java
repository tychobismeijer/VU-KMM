package carRepairAssistant;

import jess.Rete;
import jess.JessException;
import jess.WorkingMemoryMarker;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import jess.RU;

public class CRA {
    private final static int
        COMPONENT =0,
        STATE =1,
        COMPLAINT = 0,
        NAME =1;

    private Rete jess;
    private Console c;
    private String[] currentHypothesis = {"", ""};

    /* CONTROL */

    /**
     * Asks for a complaint. Prints a list of likely complaint and offers the
     * user a choice between them. Also asserts the fact in the model in Jess.
     *
     * Transaction: Report Complaint
     */
    private void askLikelyComplaint() throws JessException{
        String observable, choice;
        List<String[]> allComplaints = allComplaints();
        //Print
        c.printf("Likely complaints are:\n");
        for(int i=0; i<allComplaints.size(); i++){
            c.printf(i + ": " + allComplaints.get(i)[NAME] + " (id: " + allComplaints.get(i)[COMPLAINT] + ")\n");
        }
        c.printf("Your complaint is?\n");

        //Receive input
        choice = c.readLine();
        choice = choice.trim();
        if (isNumber(choice)){
            int nrChoice = Integer.parseInt(choice);
            observable = allComplaints.get(nrChoice)[COMPLAINT];
        } else {
            observable = choice;
        }

        //Assert complaint
        String fact = "(complaint " + observable + " TRUE)";
        jess.assertString(fact);
    }

    /**
     * Selects all possible hypothesis, and offers the user a choice to select one from them
     * or selects one itself.
     *
     * Transaction: Propose Hypothesis
     */
    private void askAvailableHypothesis(List<String[]> allHypothesis) throws JessException{
        //our sugestion
        currentHypothesis = allHypothesis.get(allHypothesis.size()-1);
        //Print
        c.printf("Available hypothesis are:\n");
        for(int i=0; i<allHypothesis.size(); i++){
            c.printf(i + " " + allHypothesis.get(i)[COMPONENT] + " is " + allHypothesis.get(i)[STATE] + "\n");
        }
		c.printf("We suggest: " + currentHypothesis[COMPONENT] + " is " + currentHypothesis[STATE] + "\n");
        c.printf("Do you have an other suggestion (no/nr/id)?\n");

		//Receive input
        String choice = c.readLine();
        choice = choice.trim();
        if (isNumber(choice)){
            int nrChoice = Integer.parseInt(choice);
            currentHypothesis = allHypothesis.remove(nrChoice);
        } else if(choice.contains(" is ")) {
            currentHypothesis[COMPONENT] = choice.substring(0, choice.indexOf(" "));
            currentHypothesis[STATE] = choice.substring(choice.lastIndexOf(" ")+1, choice.length());
        } else {
            currentHypothesis = allHypothesis.remove(allHypothesis.size()-1);
        }
    }

    /**
     * Negotiates an observable with the user. Asks the user wether he wants to
     * observe an observable, one observable after another, until he/she reports
     * an observation. Asserts that observation in the model in Jess.
     *
     * Transactions: Negotiate Observable, Report Observable
     */
    private boolean negotiateObservable() throws JessException {
        reset();
        c.printf("Trying hypothesis\n");
        jess.assertString(
            "(hypothesis " +
            currentHypothesis[COMPONENT] + " " +
            currentHypothesis[STATE] + ")"
        );
        jess.run();
        c.printf("Querying Observables\n");
	jess.QueryResult observables =
	jess.runQueryStar("search-observable", new jess.ValueVector());
        String answer = "no";
        while (observables.next() && answer.equals("no")) {
            String observable = observables.getString("observable");
            c.printf(
                "Do you want to observe " +
                observable + "? true/false/no\n"
            );
            boolean noAnswer = true;
            while (noAnswer) {
                answer = c.readLine();
                if (answer.equals("true")) {
                    jess.assertString(
                        "(observed " +
                        observable +
                        " TRUE)"
                    );
                    jess.run();
                    return true;
                } else if (answer.equals("false")) {
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

    /**
     * Reports all possible hypothesis at the moment.
     *
     * Transaction: Report Hypothesis
     */
    private void reportHypothesis() throws JessException {
        List<String[]> hypothesis = allHypothesis();
        c.printf("It could be that:\n");
        for (String[] h : hypothesis) {
            c.printf(h[0] + " is " + h[1] + "\n");
        }
    }

    /* MODEL */

    /**
     * Makes a list of all possible hypothesis.
     */
    private List<String[]> allHypothesis() throws JessException{
        List<String[]> result = new ArrayList<String[]>();
        jess.QueryResult hypothesis = jess.runQueryStar("search-hypothesis", new jess.ValueVector());

        while (hypothesis.next()) {
            String[] h = new String[2];
            h[COMPONENT] = hypothesis.getString("component");
            h[STATE] = hypothesis.getString("state");
            result.add(h);
        }
        return result;
    }
    
    /**
     * Makes a list of all likely complaints.
     */
    private List<String[]> allComplaints() throws JessException {
        List<String[]> result = new ArrayList<String[]>();
        jess.QueryResult likely_complaints = jess.runQueryStar("search-likely-complaints", new jess.ValueVector());

        while (likely_complaints.next()){
            String[] h = new String[2];
            h[COMPLAINT] = likely_complaints.getString("observable");
            h[NAME] = likely_complaints.getString("name");
            result.add(h);
        }

        return result;
    }
    
    /*
     * Selects one hypothesis from the list of hypothesis without user
     * interaction
     */
    private boolean selectHypothesis(List<String[]> hypothesis) throws JessException {
        if(hypothesis.size() != 0) {
            currentHypothesis = hypothesis.get(0);
            return true;
        } else {
            return false;
        }
    }

    

    /**
     * Removes all hypothosized states of components.
     */
    private void reset() throws JessException{
        jess.Value nil = new jess.Value("nil", RU.SYMBOL);
        jess.Fact fact;
        Iterator<jess.Fact> facts = jess.listFacts();
        while(facts.hasNext()){
            fact = facts.next();
            //Reset all states to nill before trying a new hypothesis
            if (fact.getDeftemplate().getSlotIndex("state")>0){
                jess.modify(fact, "state", nil);
            }
            //Reset all could-be-observed to nil before trying a new hypothesis
            if (fact.getDeftemplate().getSlotIndex("could-be-observed")>0){
                jess.modify(fact, "could-be-observed", nil);
            }
            //Retract all old hypothesis
            if (fact.getName().equals("MAIN::hypothesis")){
                jess.retract(fact);
            }
        }
    }
    
    /**
     * Tests wether a String can be interpreted as a String
     */
    private boolean isNumber(String string){
        try{
            Integer.parseInt(string);
            return true;
        }
        catch(NumberFormatException nfe){
            return false;
        }
    }

    

    /* SUPPORT */

    /**
     * Prints the current hypothesis to the console.
     *
     */
    private void printCurrentHypothesis() {
        c.printf(
            "The hypothesis is that " +
            currentHypothesis[COMPONENT] +
            " is " + 
            currentHypothesis[STATE] + "\n"
        );
    }
    

    /**
     * Print all components.
     * DEBUG method
     * requires test jess file
     */
    private void printAllComponents() throws JessException {
        jess.QueryResult components = jess.runQueryStar("search-components", new jess.ValueVector());

        while (components.next()){
            c.printf(components.getString("component") + "\t state: ");
            c.printf(components.get("state") + "\t impossible-states: ");
            c.printf(components.get("impossible-states").toString() + "\n");
        }
    }

    /**
     * Prints the current facts in the model in Jess to the console.
     * DEBUG Method
     */
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
        c = new Console();
        try {
            jess.batch("engine/run-from-java.jess");
            jess.reset();
            askLikelyComplaint();
            jess.run();
            boolean found_hypothesis;
            do {
                // Keep an updated list of possible hypothesis
                List<String[]> hypothesis = allHypothesis();
                found_hypothesis = (hypothesis.size() > 0);
                boolean observed = false;
                while(!observed && hypothesis.size() > 0) {
                    // Keep asking questions about hypothesis, until we receive an
                    // answer.
                    askAvailableHypothesis(hypothesis);
                    printCurrentHypothesis();
                    observed = negotiateObservable();
                }
                if (!observed) break;
            } while (found_hypothesis);
            reportHypothesis();
        } catch (JessException ex) {
            System.err.println(ex);
        }
    }

    public static void main(String[] arg) {
        new CRA();
    }
}
