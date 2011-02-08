/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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

    public String name(){
        return name;
    }

    public String id(){
        return id;
    }
}
