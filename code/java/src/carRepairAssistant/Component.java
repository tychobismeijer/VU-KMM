/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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

    String name(){
        return name;
    }

    String id(){
        return id;
    }

    String stateId(){
        return state.id();
    }

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
