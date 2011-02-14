package carRepairAssistant;
import java.util.ArrayList;
import java.util.List;
import jess.JessException;
/**
 *
 * @author Joost
 */
public class Hypothesis {
    Boolean contradiction;
    Boolean directCause;
    Integer nrStateChanges;
    List<Component> hypothesisList;
    private Model m;
    Integer maxIndex;
    boolean tested;
    
    /**
     * Construct an empty hypothesis
     */
    Hypothesis(Model m){
        hypothesisList = new ArrayList<Component>();
        tested = false;
        this.m = m;
    }

    Hypothesis(List<Component> hypothesisList, Model m){
        this.hypothesisList = hypothesisList;
        tested = false;
        this.m = m;
    }

    Hypothesis(Hypothesis hypothesis, Model m){
        hypothesisList = new ArrayList<Component>();
        for(int i =0; i<hypothesis.size();i++){
            hypothesisList.add(hypothesis.get(i));
        }
        tested = false;
        this.m = hypothesis.m;
    }

    Hypothesis(Component component, Model m){
        hypothesisList = new ArrayList<Component>();
        hypothesisList.add(component);
        tested = false;
        this.m = m;
    }

    Hypothesis(String componentId, String componentName, String stateId, String stateName, Model m){
        hypothesisList = new ArrayList<Component>();
        hypothesisList.add(new Component(componentId, componentName, stateId, stateName));
        tested = false;
        this.m = m;
    }

    @Override
    public boolean equals(Object otherObject){
        boolean temp;

        if(otherObject == null)
            return false;
        if(otherObject.getClass() != this.getClass())
            return false;

        Hypothesis otherHypothesis = (Hypothesis) otherObject;
        
        if(hypothesisList.size() != otherHypothesis.size())
            return false;

        for(int i=0; i<hypothesisList.size(); i++){
            temp = false;
            for(int j=0; j<otherHypothesis.size(); j++){
                if(hypothesisList.get(i).equals(otherHypothesis.get(j)))
                    temp = true;
            }

            if(!temp){
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.hypothesisList != null ? this.hypothesisList.hashCode() : 0);
        return hash;
    }
    
    /**
    * Returns the number of components in this hypothesis
    * @return the number of components
    */
    public int size(){
        return hypothesisList.size();
    }

    /**
     * Returns the component at index i within the hypothesis object
     * @param i The index of the component
     * @return The component at index i
     */
    public Component get(int i){
        return hypothesisList.get(i);
    }

    /**
     * Splits a composed hypothesis into basic hypothesis
     * When the current hypothesis is already a basic hypothesis it just returns the current hypothesis
     * @return
     */
    public List<Hypothesis> split(){
        List<Hypothesis> result = new ArrayList<Hypothesis>();

         for(int i=0; i<hypothesisList.size(); i++){
             result.add(new Hypothesis(hypothesisList.get(i), m));
         }

        return result;
    }

    /**
     * Indicates whether the other hypothesis is contained in the current hypothesis.
     * Note that an empty hypothesis is always contained in an other hypothesis.
     * @param otherHypothesis The hypothesis of which it is tested if it is contained whithin this hypothesis
     * @return Returns true if this hypothesis contains the other hypothesis
     */
    public boolean contains(Hypothesis otherHypothesis){
        for(int i=0; i<otherHypothesis.size(); i++){
            if(!hypothesisList.contains(otherHypothesis.get(i))){
                return false;
            }
        }
        
        return true;
    }

    /**
     * Adds an other hypothesis to this hypothesis.
     * @param otherHypothesis The hypothesis to be added to this hypothesis
     */
    public void add(Hypothesis otherHypothesis){
        for(int i=0; i<otherHypothesis.size(); i++){
            hypothesisList.add(otherHypothesis.get(i));
        }

        if(maxIndex != null && otherHypothesis.maxIndex != null){
            maxIndex = Math.max(maxIndex, otherHypothesis.maxIndex);
        }

        contradiction = null;
        directCause = null;
        nrStateChanges = null;

    }

    /**
     * Returns whether this hypothesis causes a contradiction whithin the supplied jess engine.
     * @param jess The supplied jess engine
     * @return Returns true if this hypothesis causes a contradiction
     * @throws jess.JessException
     */
    public boolean contradiction() throws JessException{
        if (contradiction == null){
            m.test(this);
        }
        return contradiction;
    }

    /**
     * Returns whether this hypothesis is direct cause for the complaint in the supplied jess engine.
     * A direct cause means that this hypothesis will lead to the initial complaint whitout the need for additional information.
     * @param jess The supplied jess engine
     * @return Returns true if this hypothesis is a direct cause
     * @throws jess.JessException
     */
    public boolean directCause() throws JessException{
        if (directCause == null){
            m.test(this);
        }
        return directCause;
    }

    /**
     * Returns the number of state changes whithin the supplied model that is caused by assuming this hypothesis.
     *
     * @return Returns the number of state changes
     * @throws jess.JessException
     */
    public int nrStateChanges() throws JessException {
        if (nrStateChanges == null){
            m.test(this);
        }
        return nrStateChanges;
    }

    public Hypothesis clone() {
        return new Hypothesis(this, m);
    }
}
