/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cra;

import java.io.IOException;

/**
 *
 * @author Joost
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        Car car = new Car();

        ElectricalComponent comp1 = car.getComponents()[0];
        ElectricalComponent comp2 = car.getComponents()[1];
        ElectricalComponent comp3 = car.getComponents()[2];

        System.out.println(comp1.getComponentID());
        System.out.println(comp2.getComponentID());
        System.out.println(comp3.getComponentID());
        System.out.println(car.connected(comp1, comp2));
    }

}
