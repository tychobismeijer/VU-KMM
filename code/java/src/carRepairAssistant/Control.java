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

    /**
     * Start the diagnosis process.
     */
    public void start() {
        List<Hypothesis> allHypothesis;
        Hypothesis currentHypothesis = new Hypothesis(m);

        try {
            m.setup();

            reportComplaint();
            allHypothesis = m.allHypothesis();
            while (hypothesisLeft(allHypothesis)) {
                view.startSelectHypothesis();
                // Select an hypothesis
                currentHypothesis = view.askHypothesis(allHypothesis);
                // Specify and obtain an observable for this hypothesis
                if (!specifyObservable(currentHypothesis)) {
                    // If no observation was obtained, mark this having no
                    // observations
                    allHypothesis.get(allHypothesis.indexOf(currentHypothesis)
                        ).setNoObservationsLeft();
                } else {
                    // If an observation was obtained, check all hypothesis
                    // against this new observation
                    allHypothesis = m.verify(allHypothesis);
                }
            }
            reportResult(allHypothesis);
        } catch (JessException ex) {
            System.err.println(ex);
        }
    }
    
    /*
     **************************************************************************
     * Public methods as documented in the report.
     */

    /**
     * Suggest an hypothesis. This could call the Model for help, but doesn't.
     * We only filter on the ComponentStates in the hypothesis.
     *
     * @param hypothesis The list of hypothesis to pick the suggestion from.
     */
    Hypothesis suggest(List<Hypothesis> hypothesis) {
        if (hypothesis.size() <= 0) {
            return newEmptyHypothesis();
        }
        Hypothesis result = hypothesis.get(0);
        for (Hypothesis h : hypothesis) {
            if (!result.containsWire()) {
                break;
            } else {
                result = h;
            }
        }
        return result;
    }

    /*
     **************************************************************************
     * Factory methods
     */

    Hypothesis newEmptyHypothesis() {
        return new Hypothesis(m);
    }

    /*
     **************************************************************************
     * Private
     */

    /**
     * Ask for a complaint and assert it to the model.
     */
    private void reportComplaint() throws JessException{
        Observable complaint;

        view.startReportComplaint();
        List<Observable> allComplaints = m.likelyComplaints();
        complaint = view.askComplaint(allComplaints);
        m.assertComplaint(complaint);
    }

    /**
     * Specifies an observable for the supplied hypothesis, based on user
     * input, and obtains the observation result.
     *
     * @param hypothesis The hypothesis for which the observable should be
     *      specified.
     * @return Returns true if an observation was made; returns false
     *      otherwise.
     */
    private boolean specifyObservable(Hypothesis hypothesis)
            throws JessException {
        List<Observable> observables;
        Finding finding;

        view.startNegotiateObservable();
        // Generate all possible observables that could falsify the hypothesis.
        observables = m.observables(hypothesis);
        // Try to make an observation.
        finding = view.askObservables(observables);
        if (finding != null) {
            m.assertFinding(finding);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Report the results of the process.
     *
     * @param allHypothesis The hypothesis on which the results will be based.
     * @throws jess.JessException
     */
    private void reportResult(List<Hypothesis> allHypothesis) throws JessException{
        view.startReportResult();        
        view.reportResult(allHypothesis);
    }

    /**
     * Indicates wheter there are any viable hypothesis left.
     * Counts only hypothesis that possibly have observations left.
     *
     * @param hypothesis The hypothesis to be evaluated
     * @return <code>true</code> if there is at least one viable hypothesis in
     *      the list
     *      <code>false</code> otherwise
     */
    private boolean hypothesisLeft(List<Hypothesis> hypothesis){
        for (Hypothesis h : hypothesis) {
            if (h.observationsLeft()) {
                return true;
            }
        }
        return false;
    }
}

