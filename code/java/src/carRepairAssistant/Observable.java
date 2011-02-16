package carRepairAssistant;
/**
 * An observable that can be observed and/or is a complaint.
 * An empty observable means that there was or is nothing to observe.
 *
 * @author Joost and Tycho
 */
class Observable {
    private String name;
    private String id;

    /**
     * Construct an Observable with id and name
     */
    Observable(String id, String name){
        this.id = id;
        this.name = name;
    }

    /**
     * Construct an empty Observable
     */
    Observable() {
        this.id = "";
        this.name = "";
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
    
    /**
     * Predicate about wether this Observable is empty or not
     */
    public boolean empty() {
        return id.equals("");
    }
}
