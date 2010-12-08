/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cra;

/**
 *
 * @author Joost
 */
public class Connection extends ElectricalComponent implements ConnectionInterface{
    ElectricalComponent firstEnd;
    ElectricalComponent secondEnd;

    Connection(ElectricalComponent firstEnd, ElectricalComponent secondEnd){
        this.firstEnd = firstEnd;
        this.secondEnd = secondEnd;
    }
    
    public ElectricalComponent[] getConnectedComponents() {
        ElectricalComponent[] result = new ElectricalComponent[2];
        result[0] = firstEnd;
        result[1] = secondEnd;
        return result;
    }

    public boolean connects(ElectricalComponent oneEnd, ElectricalComponent otherEnd) {
        return (oneEnd.getComponentID() == firstEnd.getComponentID() &&
                otherEnd.getComponentID() == secondEnd.getComponentID()) ||
                (otherEnd.getComponentID() == firstEnd.getComponentID() &&
                oneEnd.getComponentID() == secondEnd.getComponentID());
    }

}
