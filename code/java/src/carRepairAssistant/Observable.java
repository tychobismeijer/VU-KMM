package carRepairAssistant;
import jess.JessException;
import jess.Rete;
/**
 *
 * @author Joost
 */
public class Observable {
    private String name;
    private String id;

    Observable (String id, String name){
        this.id = id;
        this.name = name;
    }

    /**
     * Returns a nicely printable name of this observable
     * @return The name of this observable
     */
    public String name(){
        return name;
    }

    /**
     * Returns the id of this observable as used in the jess enginge
     * @return The id of this observable
     */
    public String id(){
        return id;
    }

    /**
     * Asserts this observable as a complaint in the supplied jess engine
     * @param jess - The supplied jess engine
     */
    public void assertAsComplaint(Rete jess){
        String fact = "(complaint " + id + " TRUE)";
        try {
            jess.assertString(fact);
        } catch (JessException ex) {
            System.err.println(ex);
        }
    }

    /**
     * Asserts this observable as an observation to the supplied jess engine.
     * @param truth Indicates whether the observable was observed to be true or false.
     * @param jess The supplied jess engine
     * @throws jess.JessException
     */
    public void assertAsObservation(boolean truth, Rete jess) throws JessException{
        String assertion = "(observed " + id;

        if(truth){
            assertion = assertion + " TRUE)";
        } else {
            assertion = assertion + " FALSE)";
        }

        jess.assertString(assertion);
        jess.run();
    }
}
