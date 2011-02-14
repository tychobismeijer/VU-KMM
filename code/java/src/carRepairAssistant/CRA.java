package carRepairAssistant;

import jess.JessException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CRA {
    private Model m;
    private Console c;
    private View view;

//Constructors
    public CRA() {
        //Define local objects
        List<Hypothesis> allHypothesis;
        Hypothesis currentHypothesis = new Hypothesis(m);

        //Construct global objects
        c = new Console();
        m = new Model();
        view = new View();
        
        try {
            m.setup();

            //Receive the complaint
            reportComplaint();

            //Generate all hypothesis
            allHypothesis = m.allHypothesis();

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
     * Gets the complaint from the user and asserts it to the model.
     *
     * @throws jess.JessException
     */
    private void reportComplaint() throws JessException{
        String choice;
        Observable complaint;

        //Print the start of the report complaint fase
        view.printReportComplaint();
        
        //Get likely complaints
        List<Observable> allComplaints = m.likelyComplaints();

        //Print complaints
        view.printObservableArray(allComplaints);

        //Receive input
        choice = c.readLine();
        choice = choice.trim();
        if (isNumber(choice)){
            int nrChoice = Integer.parseInt(choice);
            complaint = allComplaints.get(nrChoice);
        } else {
            complaint = new Observable(choice, choice);
        }

        //Assert complaint
        m.assertComplaint(complaint);
    }

    /**
     * Selects an hypothesis based on user input.
     * @param allHypothesis - All the hypothesis from which an hypothesis should be selected.
     * @return The selected hypothesis.
     * @throws jess.JessException
     */
    private Hypothesis selectHypothesis(List<Hypothesis> allHypothesis) throws JessException{
        List<Hypothesis> basicHypothesis;
        Hypothesis hypothesis = new Hypothesis(m);

        //Print the start of the Select hypothesis fase
        view.printSelectHypothesis();

        do {
            //Creates the list of basic hypothesis 
            basicHypothesis = filterAndSimplifyHypothesis(hypothesis, allHypothesis);

            //Sets our suggestion
            Hypothesis suggestion = basicHypothesis.get(basicHypothesis.size()-1);

            //Print the available hypothesis and our suggestion
            view.printHypothesisArray(basicHypothesis);
            view.printSuggestion(suggestion);

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
        } while(!hypothesis.directCause());

        return hypothesis;
    }

    /**
     * Specifies an observable for the supplied hypothesis, based on user input, and obtains the observation result.
     * @param hypothesis The hypothesis for which the observable should be specified.
     * @return Returns true if an observation was made; returns false otherwise.
     * @throws jess.JessException
     */
    private boolean specifyObservable(Hypothesis hypothesis) throws JessException {
        List<Observable> observables;
        String answer;

        //Print the start of the specify observable fase
        view.printNegotiateObservable();

        //Generate all possible observables that could falsify the hypothesis
        observables = m.observables(hypothesis);

        //Ask until there are no observables left or the answer is not 'no'
        answer = "no";
        while (observables.size() != 0 && answer.equals("no")) {
            //Suggest the first observable
            Observable observable = observables.remove(0);
            view.suggestObservable(observable);

            while (true) {
                answer = c.readLine();
                if (answer.equals("true")) {
                    m.assertFinding(observable, true);
                    return true;
                } else if (answer.equals("false")) {
                    m.assertFinding(observable, false);
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
    private List<Hypothesis> verifyHypothesis(List<Hypothesis> allHypothesis) throws JessException{
        List<Hypothesis> result = new ArrayList<Hypothesis>();

        for(int i=0; i<allHypothesis.size(); i++){
            //For each hypothesis do:
            //Rerun the reasoning process using the new data
            m.test(allHypothesis.get(i));
            if(!allHypothesis.get(i).contradiction()){
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
    private void reportResult(List<Hypothesis> allHypothesis) throws JessException{
        view.printResult(allHypothesis);
    }

//Secondary methods

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
    private List<Hypothesis> filterHypothesis(Hypothesis filterHypothesis, List<Hypothesis> hypothesisList){
        List<Hypothesis> result = new ArrayList<Hypothesis>();

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
    private List<Hypothesis> simplifyHypothesis(List<Hypothesis> hypothesisList){
        List<Hypothesis> result = new ArrayList<Hypothesis>();
        List<Hypothesis> temp;

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
    private List<Hypothesis> filterAndSimplifyHypothesis(Hypothesis filterHypothesis, List<Hypothesis> hypothesisList){
        List<Hypothesis> result = new ArrayList<Hypothesis>();
        List<Hypothesis> temp;

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
     * Indicates wheter there are any viable hypothesis left.
     * Counts only hypothesis that are not yet tested.
     * @param hypothesis The hypothesis to be evaluated
     * @return True if there is at least one viable hypothesis in the array;
     * False otherwise
     */
    private boolean hypothesisLeft(List<Hypothesis> hypothesis){
        for(int i=0; i<hypothesis.size(); i++){
            if(!hypothesis.get(i).tested){
                return true;
            }
        }

        return false;
    }

//Main
    public static void main(String[] arg) {
        new CRA();
    }
}
