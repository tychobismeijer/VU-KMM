package carRepairAssistant;
import java.util.ArrayList;
import java.util.Iterator;
import jess.Rete;
import jess.JessException;
import jess.WorkingMemoryMarker;
import jess.RU;
/**
 *
 * @author Joost
 */
public class Hypothesis {
    private ArrayList<Component> hypothesisList;
    private Boolean contradiction;
    private Boolean directCause;
    private Integer nrStateChanges;
    public Integer maxIndex;
    public boolean tested;
    

    Hypothesis(){
        hypothesisList = new ArrayList<Component>();
        tested = false;
    }

    Hypothesis(ArrayList<Component> hypothesisList){
        this.hypothesisList = hypothesisList;
        tested = false;
    }

    Hypothesis(Hypothesis hypothesis){
        hypothesisList = new ArrayList<Component>();
        for(int i =0; i<hypothesis.size();i++){
            hypothesisList.add(hypothesis.get(i));
        }
        tested = false;
    }

    Hypothesis(Component component){
        hypothesisList = new ArrayList<Component>();
        hypothesisList.add(component);
        tested = false;
    }

    Hypothesis(String componentId, String componentName, String stateId, String stateName){
        hypothesisList = new ArrayList<Component>();
        hypothesisList.add(new Component(componentId, componentName, stateId, stateName));
        tested = false;
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
    
    @Override
    public Hypothesis clone(){
        return new Hypothesis(this);
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
    public ArrayList<Hypothesis> split(){
        ArrayList<Hypothesis> result = new ArrayList<Hypothesis>();

         for(int i=0; i<hypothesisList.size(); i++){
             result.add(new Hypothesis(hypothesisList.get(i)));
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
    public boolean contradiction(Rete jess) throws JessException{
        if (contradiction == null){
            test(jess);
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
    public boolean directCause(Rete jess) throws JessException{
        if (directCause == null){
            test(jess);
        }
        return directCause;
    }

    /**
     * Returns the number of state changes whithin the supplied jess engine that is caused by assuming this hypothesis
     * @param jess The supplied jess engine
     * @return Returns the number of state changes
     * @throws jess.JessException
     */
    public int nrStateChanges(Rete jess) throws JessException{
        if (nrStateChanges == null){
            test(jess);
        }
        return nrStateChanges;
    }

    /**
     * Returns all observables with which this hypothesis could be falsified whithin the supplied jess engine
     * @param jess The supplied jess engine
     * @return Returns an array list of observables that could falsify this hypothesis
     * @throws jess.JessException
     */
    public ArrayList<Observable> observables(Rete jess) throws JessException{
        ArrayList<Observable> result = new ArrayList<Observable>();

        test(jess);

        jess.QueryResult observables = jess.runQueryStar("search-observable", new jess.ValueVector());

        while (observables.next()){
            result.add(new Observable(observables.getString("observable"), observables.getString("name")));
        }
        
        return result;
    }

    /**
     * Asserts this hypothesis in the supplied jess engine and internally stores the results
     * @param jess The supplied jess engine
     * @throws jess.JessException
     */
    public void test(Rete jess) throws JessException{
        WorkingMemoryMarker beforeHypothesis = jess.mark();
        reset(jess);

        for(int i=0; i<hypothesisList.size();i++){
            jess.assertString(
                "(hypothesis " +
                hypothesisList.get(i).id() + " " +
                hypothesisList.get(i).stateId() + ")"
            );
        }
        jess.run();

        contradiction = !(jess.findFactByFact(new jess.Fact("contradiction", jess)) ==null);
        directCause = !(jess.findFactByFact(new jess.Fact("direct-cause", jess)) ==null);
        nrStateChanges = 0;
        jess.QueryResult components = jess.runQueryStar("components-in-state", new jess.ValueVector());

        while (components.next()) {
            nrStateChanges++;
        }
        
        jess.resetToMark(beforeHypothesis);
    }

    /**
     * Retracts all hypothesis and all conclusions that relate to these hypothesis from the supplied jess enginge.
     * Facts related to hypothesis are contradictions and direct causes.
     * Also reverts all state changes.
     * @param jess The supplied jess engine.
     * @throws jess.JessException
     */
    private void reset(Rete jess) throws JessException{
        jess.Value nil = new jess.Value("nil", RU.SYMBOL);
        jess.Fact fact;

        Iterator<jess.Fact> facts = jess.listFacts();
        while(facts.hasNext()){
            fact = facts.next();
            //Reset all states to nill before trying a new hypothesis
            if (fact.getDeftemplate().getSlotIndex("state")>0){
                jess.modify(fact, "state", nil);
            }
            //Reset all could-be-observed to nil before trying a new hypothesis
            if (fact.getDeftemplate().getSlotIndex("could-be-observed")>0){
                jess.modify(fact, "could-be-observed", nil);
            }
            //Retract all old hypothesis
            if (fact.getName().equals("MAIN::hypothesis")){
                jess.retract(fact);
            }
            //Retract all old contradictions
            if (fact.getName().equals("MAIN::contradiction")){
                jess.retract(fact);
            }
            //Retract all old direct-causes
            if (fact.getName().equals("MAIN::direct-cause")){
                jess.retract(fact);
            }
        }
    }
}
