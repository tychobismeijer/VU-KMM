package carRepairAssistant;

import jess.Rete;
import jess.JessException;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

public class CRAmisluktproject {
    private final static int    COMPLAINT = 0,
                                NAME =1;

    private Rete jess;
    private Console c;
    private View view;
    //private List<String[]> currentHypothesis;

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
        try {
            jess.assertString(fact);
        } catch (JessException ex) {
            System.err.println(ex);
        }
    }

    private ArrayList<Hypothesis> removeContradictions(ArrayList<Hypothesis> allHypothesis) throws JessException{
        ArrayList<Hypothesis> result = new ArrayList<Hypothesis>();
        ArrayList<Hypothesis> basicHypothesis = new ArrayList<Hypothesis>();
        LinkedList<Hypothesis> candidateHypothesis = new LinkedList<Hypothesis>();
        Hypothesis currentHypothesis;
        Hypothesis newHypothesis;

        //Check each individual hypothesis and create candidate hypothesis that might be part of a composed hypothesis
        for(int i=0; i<allHypothesis.size(); i++){
            currentHypothesis = allHypothesis.get(i);
            if(!currentHypothesis.contradiction(jess)){ //if this hypothesis causes a contradiction then skip it
                if(currentHypothesis.directCause(jess)){ //if this hypothesis is a direct cause, add to result and skip it
                    result.add(allHypothesis.get(i));
                } else { //if this hypothesis might create a composed hypothesis
                    currentHypothesis.maxIndex = basicHypothesis.size(); //stores the index this hypothesis gets in the basic hypothesis set
                    basicHypothesis.add(currentHypothesis); //store the hypothesis
                    candidateHypothesis.add(currentHypothesis); //store the hypothesis
                }
            }
        }

        //Check wether the candidate hypothesis are actually composed hypothesis
        while(candidateHypothesis.peek() != null){
            currentHypothesis = candidateHypothesis.pop();
            for(int j=currentHypothesis.maxIndex; j<basicHypothesis.size(); j++){
                newHypothesis = currentHypothesis.clone();
                newHypothesis.add(basicHypothesis.get(j)); //create a combined hypothesis

                if(!newHypothesis.contradiction(jess)){ //if this hypothesis causes a contradiction then skip it
                    if(newHypothesis.directCause(jess)){ //if this hypothesis is a direct cause, add to result and skip it
                        result.add(newHypothesis);
                    } else { //if this hypothesis might create a composed hypothesis
                        if((currentHypothesis.nrStateChanges(jess) +basicHypothesis.get(j).nrStateChanges(jess) < newHypothesis.nrStateChanges(jess))){ //check if the new nr of state changes is greater then the sum of its parts
                            candidateHypothesis.add(newHypothesis);
                        }
                    }
                }
            }
        }

        return result;
    }

    private ArrayList<Hypothesis> listBasicHypothesis(Hypothesis currentHypothesis, ArrayList<Hypothesis> allHypothesis){
        ArrayList<Hypothesis> filteredHypothesis = new ArrayList<Hypothesis>();
        ArrayList<Hypothesis> basicHypothesis = new ArrayList<Hypothesis>();
        ArrayList<Hypothesis> temp;

        //find all hypothesis that contain the current hypothesis
        if(currentHypothesis == null){
            filteredHypothesis = allHypothesis;
        } else {
            for(int i=0; i<allHypothesis.size(); i++){
                if(allHypothesis.get(i).contains(currentHypothesis)){
                    filteredHypothesis.add(allHypothesis.get(i));
                }
            }
        }

        //create an array of all basic hypothesis that are in the filtered hypothesis and that does not contain duplicates
        for(int i=0; i<filteredHypothesis.size(); i++){
            temp = filteredHypothesis.get(i).split();
            for(int j=0; j<temp.size(); j++){
                if(!basicHypothesis.contains(temp.get(j))){
                    basicHypothesis.add(temp.get(j));
                }
            }
        }

        //Remove the current hypothesis
        temp = currentHypothesis.split();
        for(int i=0; i<temp.size();i++){
            basicHypothesis.remove(temp.get(i));
        }

        //Add current hypothesis to all basic hypothesis
        for(int i=0; i<basicHypothesis.size();i++){
            basicHypothesis.get(i).add(currentHypothesis);
        }
        

        return basicHypothesis;
    }

    private Hypothesis askAvailableHypothesis(ArrayList<Hypothesis> allHypothesis) throws JessException{
        ArrayList<Hypothesis> basicHypothesis;
        Hypothesis hypothesis = new Hypothesis();

        if(allHypothesis.isEmpty()){
            return hypothesis;
        }

        allHypothesis = removeContradictions(allHypothesis);
        //our sugestion
        do {
            basicHypothesis = listBasicHypothesis(hypothesis, allHypothesis);
            Hypothesis suggestion = basicHypothesis.get(basicHypothesis.size()-1);

            //Print
            view.printHypothesisArray(basicHypothesis, jess);
            view.printSuggestion(suggestion, jess);
            
//            c.printf("Available hypothesis are:\n");
//            for(int i=0; i<basicHypothesis.size(); i++){
//                c.printf(i + " " + basicHypothesis.get(i).getFullHypothesis(jess));
//            }
//
//            c.printf("We suggest: " + suggestion.getFullHypothesis(jess));
//            c.printf("Do you have an other suggestion (no/nr/id)?\n");

            //Receive input
            String choice = c.readLine();
            choice = choice.trim();
            if (isNumber(choice)){
                int nrChoice = Integer.parseInt(choice);
                hypothesis = (basicHypothesis.get(nrChoice));
            }
//            else if(choice.contains(" is ")) {
//                hypothesis.add(new Hypothesis(choice.substring(0, choice.indexOf(" ")), choice.substring(choice.lastIndexOf(" ")+1, choice.length())));
//            } 
            else {
                hypothesis = suggestion;
            }

        }while(!hypothesis.directCause(jess));

        return hypothesis;
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

    private ArrayList<Hypothesis> allHypothesis() throws JessException{
        ArrayList<Hypothesis> result = new ArrayList<Hypothesis>();
        jess.QueryResult hypothesis = jess.runQueryStar("search-hypothesis", new jess.ValueVector());

        while (hypothesis.next()) {
            result.add(new Hypothesis(hypothesis.getString("component"),
                    hypothesis.getString("name"),
                    hypothesis.getString("state"),
                    hypothesis.getString("stateName")));
        }
        return result;
    }

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

//    private jess.QueryResult generateHypothesis() throws JessException {
//        return jess.runQueryStar("search-hypothesis", new jess.ValueVector());
//    }

//    private void printHypothesis(Hypothesis hypothesis) throws JessException {
//        c.printf("The hypothesis is that ");
//        view.printHypothesis(hypothesis, jess);
//    }

//    private void printFacts() {
//        try {
//            java.io.Writer co = new java.io.PrintWriter(System.out);
//            co.write("-----------------------------------\n");
//            jess.ppFacts(co);
//            co.flush();
//        } catch (java.io.IOException ex) {
//            System.err.println(ex);
//        }
//    }

    private boolean negotiateObservable(Hypothesis hypothesis) throws JessException {
        c.printf("Trying hypothesis\n");
        c.printf("Querying Observables\n");
	jess.QueryResult observables = hypothesis.observables(jess);
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
                    //printFacts();
                    //c.readLine();
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

    public CRAmisluktproject() {
        jess = new Rete();
        c = new Console();
        view = new View();
        Hypothesis currentHypothesis = new Hypothesis();
        try {
            jess.batch("jess/test/test-multi-cover.jess");
            jess.reset();
            //printFacts();
            askLikelyComplaint();
            jess.run();
            //printFacts();
            boolean found_hypothesis;
            do {
                //printAllComponents();
                ArrayList<Hypothesis> allHypothesis = allHypothesis();
                found_hypothesis = (allHypothesis.size() > 0);
                boolean observed = false;
                while(!observed && allHypothesis.size() > 0) {
                    currentHypothesis = askAvailableHypothesis(allHypothesis);
                    view.printCurrentHypothesis(currentHypothesis, jess);
                    observed = negotiateObservable(currentHypothesis);
                }
                if (!observed) break;
            } while (found_hypothesis);
        } catch (JessException ex) {
            System.err.println(ex);
        }
    }

    public static void main(String[] arg) {
        new CRAmisluktproject();
    }
}
