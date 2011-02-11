package carRepairAssistant;

/**
 *
 * @author Joost
 */
public class Component {
    private String name;
    private String id;
    private State state;

    Component(String id, String name, String stateId, String stateName){
        this.id = id;
        this.name = name;
        this.state = new State(stateId, stateName);
    }

    /**
     * Returns a nicely printable name of this component
     * @return The name of this component
     */
    String name(){
        return name;
    }

    /**
     * Returns the id of this component as used in the jess enginge
     * @return The id of this component
     */
    String id(){
        return id;
    }

    /**
     * Returns a nicely printable name of the state of this component
     * @return The name of this state
     */
    String stateId(){
        return state.id();
    }

    /**
     * Returns the id of the state of this component as used in the jess enginge
     * @return The id of the state of this component
     */
    String stateName(){
        return state.name();
    }

     @Override
    public boolean equals(Object otherObject){
         if(otherObject == null)
            return false;
         if(otherObject.getClass() != this.getClass())
            return false;
         Component otherComponent = (Component) otherObject;

         if(!id.equals(otherComponent.id()))
            return false;
         if(!state.id().equals(otherComponent.stateId()))
            return false;
         return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 17 * hash + (this.state != null ? this.state.hashCode() : 0);
        return hash;
    }
}
