package carRepairAssistant;
/**
 * A finding resulting from an observation.
 *
 * @author Joost and Tycho
 */
class Finding {
    private Observable observation;
    private boolean result;

    /**
     * Construct a Finding from an Observable and observation result.
     */
    Finding(Observable observation, boolean result) {
        this.result = result;
        this.observation = observation;
    }

    public Observable observation() {
        return observation;
    }

    public boolean result() {
        return result;
    }
}
