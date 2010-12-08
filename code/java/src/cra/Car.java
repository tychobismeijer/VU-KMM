/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cra;

/**
 *
 * @author Joost
 */
public class Car implements CarInterface{
    private int carType;
    private ElectricalComponent[] ElectricalComponents ;

    Car(){
        this(0);
    }

    Car(int carType){
        this.carType = carType;
        ElectricalComponents = getComponents(carType);
    }

    public int getType(){
        return carType;
    }

    public boolean connected(ElectricalComponent firstComponent, ElectricalComponent secondComponent){
        for(int i=0; i<ElectricalComponents.length;i++){
            if (ElectricalComponents[i].getClass() == Connection.class){
                Connection connection = (Connection)ElectricalComponents[i];
                if(connection.connects(firstComponent, secondComponent)){
                    return true;
                }
            }
        }
        return false;
    }

    public ElectricalComponent[] getComponents(){
        return ElectricalComponents;
    }

    private ElectricalComponent[] getComponents(int carType){
        ElectricalComponent[] result = new ElectricalComponent[3];
        result[0]= new ElectricalComponent();
        result[1]= new ElectricalComponent();
        result[2]= new Connection(result[0],result[1]);
        return result;
    }
}
