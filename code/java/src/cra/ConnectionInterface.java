/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cra;

/**
 *
 * @author Joost
 */
public interface ConnectionInterface extends ElectricalComponentInterface{
    public ElectricalComponent[] getConnectedComponents();
    public boolean connects(ElectricalComponent oneEnd, ElectricalComponent otherEnd);
}
