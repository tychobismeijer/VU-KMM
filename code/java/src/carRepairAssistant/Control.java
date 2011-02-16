package carRepairAssistant;

import jess.JessException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Tycho and Joost
 */
class Control {
    private Model m;
    private View view;

    Control(Model m, View view) {
        this.m = m;
        this.view = view;
        m.setControl(this);
        view.setControl(this);
    }

//Main methods
    /**
     * Gets the complaint from the user and asserts it to the model.
     *
     * @throws jess.JessException
     */

    public void start() {
        List<Hypothesis> allHypothesis;
        Hypothesis currentHypothesis = new Hypothesis(m);

        try {
            m.setup();

            reportComplaint();
            allHypothesis = m.allHypothesis();
            while (hypothesisLeft(allHypothesis)) {
                //Print the start of the Select hypothesis fase
                view.printSelectHypothesis();
                //Select an hypothesis
                currentHypothesis = view.askHypothesis(allHypothesis);
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

    public Hypothesis suggest(List<Hypothesis> hypothesis) {
        if (hypothesis
        Hypothesis result = hypothesis.get(0);
        for (Hypothesis h : hypothesis) {
            if (!result.containsWire()) {
                break;
            } else {
                resu
        }
        return result;
    }


    private void reportComplaint() throws JessException{
        Observable complaint;

        //Print the start of the report complaint fase
        view.printReportComplaint();
        
        //Get likely complaints
        List<Observable> allComplaints = m.likelyComplaints();
        
        complaint = view.askComplaint(allComplaints);

        //Assert complaint
        m.assertComplaint(complaint);
    }

    /**
     * Specifies an observable for the supplied hypothesis, based on user input, and obtains the observation result.
     * @param hypothesis The hypothesis for which the observable should be specified.
     * @return Returns true if an observation was made; returns false otherwise.
     * @throws jess.JessException
     */
    private boolean specifyObservable(Hypothesis hypothesis) throws JessException {
        List<Observable> observables;
        Finding finding;

        //Print the start of the specify observable fase
        view.printNegotiateObservable();

        //Generate all possible observables that could falsify the hypothesis
        observables = m.observables(hypothesis);
        
        // Negotiate an observation
        finding = view.askObservables(observables);
        if (finding != null) {
            m.assertFinding(finding);
            return true;
        } else {
            return false;
        }
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
        view.printReportResult();        
        view.reportResult(allHypothesis);
    }

//Secondary methods


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

// Factory methods

    Hypothesis newEmptyHypothesis() {
        return new Hypothesis(m);
    }

    
}

