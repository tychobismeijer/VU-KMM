package carRepairAssistant;

/**
 *
 * @author Joost
 */
public class State {
    private String name;
    private String id;

    State(String id, String name){
        this.id = id;
        this.name = name;
    }

    /**
     * Returns a nicely printable name of this state
     * @return The name of this state
     */
    public String name(){
        return name;
    }

    /**
     * Returns the id of this state as used in the jess enginge
     * @return The id of this state
     */
    public String id(){
        return id;
    }
}
