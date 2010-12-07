/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cra;

/**
 *
 * @author Joost
 */
public class SimpleSelector implements SelectorInterface{
    public Hypothesis[] select(Hypothesis[] hypothesis, Car car){
        Hypothesis[] result = new Hypothesis[1];
        result[0] = hypothesis[0];
        return result;
    }
}
