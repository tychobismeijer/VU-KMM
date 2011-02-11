package carRepairAssistant;

import jess.Rete;
import jess.JessException;
import java.util.ArrayList;
import java.util.LinkedList;

public class CRAmisluktproject {
    private Rete jess;
    private Console c;
    private View view;

//Constructors
    public CRAmisluktproject() {
        //Define local objects
        ArrayList<Hypothesis> allHypothesis;
        Hypothesis currentHypothesis = new Hypothesis();

        //Construct global objects
        jess = new Rete();
        c = new Console();
        view = new View();
        
        try {
            //Initialize jess
            jess.batch("jess/test/test-multi-cover.jess");
            jess.reset();

            //Receive the complaint
            reportComplaint();

            //Generate all hypothesis
            allHypothesis = coverComplaints();

            while (hypothesisLeft(allHypothesis)) {
                //While there are still viable hypothesis left
                //Select an hypothesis
                currentHypothesis = selectHypothesis(allHypothesis);

                //Specify and obtain an observable for this hypothesis
                if(!specifyObservable(currentHypothesis)){
                    //If no observation was obtained, mark this hypothesis as tested
                    allHypothesis.get(allHypothesis.indexOf(currentHypothesis)).tested = true;
                } else {
                    //If an observation was obtained, check all hypothesis against this new observation
                    allHypothesis = verifyHypothesis(allHypothesis);
                }
            }

            reportResult(allHypothesis);
        } catch (JessException ex) {
            System.err.println(ex);
        }
    }

//Main methods
    /**
     * Gets the complaint from the user and asserts it to the jess rules engine
     * @throws jess.JessException
     */
    private void reportComplaint() throws JessException{
        String choice;
        Observable observable;

        //Print the start of the report complaint fase
        view.printReportComplaint();
        
        //Get possible complaints
        ArrayList<Observable> allComplaints = allComplaints();

        //Print complaints
        view.printObservableArray(allComplaints);

        //Receive input
        choice = c.readLine();
        choice = choice.trim();
        if (isNumber(choice)){
            int nrChoice = Integer.parseInt(choice);
            observable = allComplaints.get(nrChoice);
        } else {
            observable = new Observable(choice, choice);
        }

        //Assert complaint
        observable.assertAsComplaint(jess);
    }

    /**
     * Generates all possible hypothesis that could cause the reported complaint
     * @return All possible hypothesis
     * @throws jess.JessException
     */
    private ArrayList<Hypothesis> coverComplaints() throws JessException{
        ArrayList<Hypothesis> result = new ArrayList<Hypothesis>();

        //Runs the jess engine and queries for all basic hypothesis
        jess.run();
        jess.QueryResult hypothesis = jess.runQueryStar("search-hypothesis", new jess.ValueVector());

        //Adds the basic hypothesis to the result array
        while (hypothesis.next()) {
            result.add(new Hypothesis(hypothesis.getString("component"),
                    hypothesis.getString("name"),
                    hypothesis.getString("state"),
                    hypothesis.getString("stateName")));
        }
        
        //Expands the basic hypothesis to composed hypothesis where necessary
        result = expandHypothesis(result);

        return result;
    }

    /**
     * Selects an hypothesis based on user input.
     * @param allHypothesis - All the hypothesis from which an hypothesis should be selected.
     * @return The selected hypothesis.
     * @throws jess.JessException
     */
    private Hypothesis selectHypothesis(ArrayList<Hypothesis> allHypothesis) throws JessException{
        ArrayList<Hypothesis> basicHypothesis;
        Hypothesis hypothesis = new Hypothesis();

        //Print the start of the Select hypothesis fase
        view.printSelectHypothesis();

        do {
            //Creates the list of basic hypothesis 
            basicHypothesis = filterAndSimplifyHypothesis(hypothesis, allHypothesis);

            //Sets our suggestion
            Hypothesis suggestion = basicHypothesis.get(basicHypothesis.size()-1);

            //Print the available hypothesis and our suggestion
            view.printHypothesisArray(basicHypothesis, jess);
            view.printSuggestion(suggestion, jess);

            //Receive user input
            String choice = c.readLine();
            choice = choice.trim();

            if (isNumber(choice)){
                //If the user made a choice from the list then that choice becomes the selected hypothesis
                int nrChoice = Integer.parseInt(choice);
                hypothesis = (basicHypothesis.get(nrChoice));
            }
            else {
                //If the user did not make a choice then our suggestion becomes the selected hypothesis
                hypothesis = suggestion;
            }

            //Repeat until the hypothesis is a direct cause for the complaint
        }while(!hypothesis.directCause(jess));

        return hypothesis;
    }

    /**
     * Specifies an observable for the supplied hypothesis, based on user input, and obtains the observation result.
     * @param hypothesis The hypothesis for which the observable should be specified.
     * @return Returns true if an observation was made; returns false otherwise.
     * @throws jess.JessException
     */
    private boolean specifyObservable(Hypothesis hypothesis) throws JessException {
        ArrayList<Observable> observables;
        String answer;

        //Print the start of the specify observable fase
        view.printNegotiateObservable();

        //Generate all possible observables that could falsify the hypothesis
        observables = hypothesis.observables(jess);

        //Ask until there are no observables left or the answer is not 'no'
        answer = "no";
        while (observables.size() != 0 && answer.equals("no")) {
            //Suggest the first observable
            Observable observable = observables.remove(0);
            view.suggestObservable(observable);

            while (true) {
                answer = c.readLine();
                if (answer.equals("true")) {
                    observable.assertAsObservation(true, jess);
                    return true;
                } else if (answer.equals("false")) {
                    observable.assertAsObservation(false, jess);
                    return true;
                } else if (answer.equals("no")) {
                    break;
                } else {
                    view.printTryAgain();
                    continue;
                }
            }
        }
        view.printNoObservablesLeft();
        return false;
    }

    /**
     * Removes all hypothesis that cause a contradiction according to the current knowledge.
     * @param allHypothesis The hypothesis out of which the contradicting hypothesis should be removed.
     * @return A list of hypothesis that does not contain any contradictions
     * @throws jess.JessException
     */
    private ArrayList<Hypothesis> verifyHypothesis(ArrayList<Hypothesis> allHypothesis) throws JessException{
        ArrayList<Hypothesis> result = new ArrayList<Hypothesis>();

        for(int i=0; i<allHypothesis.size(); i++){
            //For each hypothesis do:
            //Rerun the reasoning process using the new data
            allHypothesis.get(i).test(jess);
            if(!allHypothesis.get(i).contradiction(jess)){
                //If the hypothesis does NOT contain a contradiction
                //Add it to the result
                result.add(allHypothesis.get(i));
            }
        }

        return result;
    }

    /**
     * Prints the results.
     * @param allHypothesis The hypothesis on which the results will be based.
     * @throws jess.JessException
     */
    private void reportResult(ArrayList<Hypothesis> allHypothesis) throws JessException{
        view.printResult(allHypothesis, jess);
    }

//Secondary methods
    /**
     * Expands a list of basic hypothesis into composed hypothesis where necessary.
     * A basic hypothesis is an hypothesis which consists of only one component and its state.
     * A composed hypothesis is an hypothesis consisting of multiple components and their states.
     * It is necessary to expand a basic hypothesis when the hypothesis is not a direct cause.
     * 
     * E.G. 'Wire 16 is broken' is a basic hypothesis for 'left head light gives no light',
     * but assuming that ONLY 'wire 16 is broken' does not lead to 'left head light gives no light'
     * (it is not a direct cause).
     * Therefore 'wire 16 is broken' has to be expanded to, for example
     * 'wire 16 is broken and light switch is broken'.
     *
     * Note that most basic hypothesis are expanded into multiple composed hypothesis,
     * thus the returned list is often bigger then the inital list.
     * @param allHypothesis A list of basic hypothesis which should be expanded.
     * @return A list of expanded hypothesis.
     * @throws jess.JessException
     */
    private ArrayList<Hypothesis> expandHypothesis(ArrayList<Hypothesis> allHypothesis) throws JessException{
        ArrayList<Hypothesis> result = new ArrayList<Hypothesis>();
        ArrayList<Hypothesis> basicHypothesis = new ArrayList<Hypothesis>();
        LinkedList<Hypothesis> candidateHypothesis = new LinkedList<Hypothesis>();
        Hypothesis currentHypothesis;
        Hypothesis newHypothesis;

        //Check each hypothesis and create a list of hypothesis should be expanded
        for(int i=0; i<allHypothesis.size(); i++){
            currentHypothesis = allHypothesis.get(i);

            if(currentHypothesis.directCause(jess)){
                //If the current hypothesis is a direct cause it does not need expanding
                //add it to result
                result.add(allHypothesis.get(i));
            } else {
                //If this hypothesis is not a direct cause then it needs expanding
                //Store the index of this hypothesis for optimalization purpose
                currentHypothesis.maxIndex = basicHypothesis.size();
                //Add the hypothesis to the list of basic hypothesis that will be used for expanding
                basicHypothesis.add(currentHypothesis);
                //Add the hypothesis to the queue of candidates for expanding
                candidateHypothesis.add(currentHypothesis); 
            }
        }

        //Expand the candidate hypothesis
        while(candidateHypothesis.peek() != null){
            //Get the first hypothesis
            currentHypothesis = candidateHypothesis.pop();
            for(int j=currentHypothesis.maxIndex; j<basicHypothesis.size(); j++){
                //For each basic hypothesis that we have not tried with this combination
                //Create a new composed hypothesis by expanding the candidate with the basic hypothesis
                newHypothesis = currentHypothesis.clone();
                newHypothesis.add(basicHypothesis.get(j));

                if(newHypothesis.directCause(jess)){
                    //If the new hypothesis is a direct cause then a new composed hypothesis has been found
                    //Add it to the result
                    result.add(newHypothesis);
                } else {
                    //If the new hypothesis is not a direct cause then it might need further expanding.
                    //Check whether new information was gained with the composed hypothesis by
                    //checking whether the new hypothesis causes more state changes then
                    //the two previous hypothesis combined
                    if((currentHypothesis.nrStateChanges(jess) + basicHypothesis.get(j).nrStateChanges(jess) < newHypothesis.nrStateChanges(jess))){
                        //If new information was gained then the new hypothesis
                        //should get further expanded
                        candidateHypothesis.add(newHypothesis);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Takes a filter hypothesis and a list of hypothesis. It then returns a list
     * of hypothesis that all contain the filter hypothesis and that does not
     * contain any hypothesis that was already tested.
     *
     * Note, the filter hypothesis might be an empty hypothesis in which case
     * this function only removes those hypothesis that are tested
     * @param filterHypothesis The hypothesis used to filter the hypothesis list
     * @param hypothesisList The hypothesis list that is being filtered
     * @return A filtered hypothesis list
     */
    private ArrayList<Hypothesis> filterHypothesis(Hypothesis filterHypothesis, ArrayList<Hypothesis> hypothesisList){
        ArrayList<Hypothesis> result = new ArrayList<Hypothesis>();

        //find all hypothesis that contain the current hypothesis and are not yet tested
        for(int i=0; i<hypothesisList.size(); i++){
            if(hypothesisList.get(i).contains(filterHypothesis) && !hypothesisList.get(i).tested){
                //If this hypothesis contains the filter hypothesis and is not yet tested
                //Add it to the result
                result.add(hypothesisList.get(i));
            }
        }

        return result;
    }

    /**
     * Takes a list of composed and basic hypothesis and returns a list of all
     * different basic hypothesis that are contained in these hypothesis.
     * @param hypothesisList The list of hypothesis to be simplified
     * @return A list of basic hypothesis
     */
    private ArrayList<Hypothesis> simplifyHypothesis(ArrayList<Hypothesis> hypothesisList){
        ArrayList<Hypothesis> result = new ArrayList<Hypothesis>();
        ArrayList<Hypothesis> temp;

        //create an array of all basic hypothesis that are in the filtered hypothesis and that does not contain duplicates
        for(int i=0; i<hypothesisList.size(); i++){
            //Split the hypothesis into its basic hypothesis
            temp = hypothesisList.get(i).split();
            for(int j=0; j<temp.size(); j++){
                if(!result.contains(temp.get(j))){
                    //If the basic hypothesis is not yet contained in the result
                    //Add it to the result
                    result.add(temp.get(j));
                }
            }
        }

        return result;
    }

    /**
     * Takes a list of hypothesis and a filter hypothesis.
     * It then returns a list of hypothesis that contains the filter hypothesis
     * and that extends the filter hypothesis with at most one basic hypothesis.
     *
     * If the filter hypothesis is an empty hypothesis then the result will be
     * a list of basic hypothesis.
     *
     * This function is used to generate the list of hypothesis that is shown to
     * the user.
     * @param filterHypothesis The hypothesis used to filter the list
     * @param hypothesisList The list being filtered and simplified
     * @return
     */
    private ArrayList<Hypothesis> filterAndSimplifyHypothesis(Hypothesis filterHypothesis, ArrayList<Hypothesis> hypothesisList){
        ArrayList<Hypothesis> result = new ArrayList<Hypothesis>();
        ArrayList<Hypothesis> temp;

        result = filterHypothesis(filterHypothesis, hypothesisList);
        result = simplifyHypothesis(result);

        //Remove the current hypothesis
        temp = filterHypothesis.split();
        for(int i=0; i<temp.size();i++){
            result.remove(temp.get(i));
        }

        //Add current hypothesis to all basic hypothesis
        for(int i=0; i<result.size();i++){
            result.get(i).add(filterHypothesis);
        }

        return result;
    }

    /**
     * Returns whether the input string is parseble as an integer
     * @param string The string to be tested
     * @return True if the string can be parsed as an integer; False otherwise
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

    /**
     * Returns all observables that are likely as a complaint
     * @return Observables that are likely as complaints
     * @throws jess.JessException
     */
    private ArrayList<Observable> allComplaints() throws JessException {
        ArrayList<Observable> result = new ArrayList<Observable>();

        //Run the query
        jess.QueryResult likely_complaints = jess.runQueryStar("search-likely-complaints", new jess.ValueVector());

        //Put the results in an array list
        while (likely_complaints.next()){
            result.add(new Observable(likely_complaints.getString("observable"), likely_complaints.getString("name")));
        }

        return result;
    }

    /**
     * Indicates wheter there are any viable hypothesis left.
     * Counts only hypothesis that are not yet tested.
     * @param hypothesis The hypothesis to be evaluated
     * @return True if there is at least one viable hypothesis in the array;
     * False otherwise
     */
    private boolean hypothesisLeft(ArrayList<Hypothesis> hypothesis){
        for(int i=0; i<hypothesis.size(); i++){
            if(!hypothesis.get(i).tested){
                return true;
            }
        }

        return false;
    }

//Main
    public static void main(String[] arg) {
        new CRAmisluktproject();
    }
}
