package carRepairAssistant;
import jess.JessException;
import jess.Rete;
/**
 * An observable that can be observed and/or is a complaint.
 *
 * @author Joost and Tycho
 */
public class Observable {
    private String name;
    private String id;

    Observable (String id, String name){
        this.id = id;
        this.name = name;
    }

    /**
     * A nicely formatted name for this observable.
     *
     * @return The name of this observable
     */
    public String name(){
        return name;
    }

    /**
     * The id of this observable as used in the jess enginge.
     *
     * @return The id of this observable
     */
    public String id(){
        return id;
    }
}
