/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cra;

/**
 *
 * @author Joost
 */
public class ElectricalComponent implements ElectricalComponentInterface{
    private static Integer nextID;
    private int componentID;
    private int componentType;

    ElectricalComponent(){
        this(0);
    }

    ElectricalComponent(int componentType){
        if (nextID == null){
            nextID = 0;
        }
        componentID = nextID;
        nextID++;
        this.componentType = componentType;
    }

    public void setComponentType(int componentType){
        this.componentType = componentType;
    }

    public int getComponentType(){
        return componentType;
    }

    public int getComponentID(){
        return componentID;
    }

    public boolean equals(ElectricalComponent electricalComponent){
        return electricalComponent.getComponentID() == this.getComponentID();
    }
}
