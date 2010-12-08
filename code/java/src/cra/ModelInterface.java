/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cra;

/**
 *
 * @author Joost
 */
public interface ModelInterface extends CausalModelInterface, ManifestationModelInterface{
    public Hypothesis[] cover(Complaint complaint, Car car);
    public Observable specify(Hypothesis[] hypothesis, Car car);
    public boolean verify(Hypothesis hypothesis, Finding finding, Car car);
}
