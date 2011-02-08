/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
    

    Hypothesis(){
        hypothesisList = new ArrayList<Component>();
    }

    Hypothesis(ArrayList<Component> hypothesisList){
        this.hypothesisList = hypothesisList;
    }

    Hypothesis(Hypothesis hypothesis){
        hypothesisList = new ArrayList<Component>();
        for(int i =0; i<hypothesis.size();i++){
            hypothesisList.add(hypothesis.get(i));
        }
    }

    Hypothesis(Component component){
        hypothesisList = new ArrayList<Component>();
        hypothesisList.add(component);
    }

    Hypothesis(String componentId, String componentName, String stateId, String stateName){
        hypothesisList = new ArrayList<Component>();
        hypothesisList.add(new Component(componentId, componentName, stateId, stateName));
    }

//    Hypothesis(String[] string){
//        this(string[0], string[1]);
//     }

    //public ArrayList<String[]> toArrayList(){
    //    return hypothesisList;
    //}

    @Override
    public boolean equals(Object otherObject){
        if(otherObject == null)
            return false;
        if(otherObject.getClass() != this.getClass())
            return false;

        Hypothesis otherHypothesis = (Hypothesis) otherObject;
        
        if(hypothesisList.size() != otherHypothesis.size())
            return false;

        for(int i=0; i<hypothesisList.size(); i++){
            if(!hypothesisList.get(i).equals(otherHypothesis.get(i)))
                return false;
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

    public int size(){
        return hypothesisList.size();
    }

    public Component get(int i){
        return hypothesisList.get(i);
    }

    public ArrayList<Hypothesis> split(){
        ArrayList<Hypothesis> result = new ArrayList<Hypothesis>();

         for(int i=0; i<hypothesisList.size(); i++){
             result.add(new Hypothesis(hypothesisList.get(i)));
         }

        return result;
    }

//    public String getFirstHypothesis(Rete jess) throws JessException{
//        String result;
//        result = hypothesisList.get(0).name() + " is " + hypothesisList.get(0).state();
//
//        if(!this.directCause(jess)){
//            result = result + " and... \n";
//        } else {
//            result = result + "\n";
//        }
//
//        return result;
//    }

//    public String getFullHypothesis(Rete jess) throws JessException{
//        String result;
//        result = hypothesisList.get(hypothesisList.size()-1).name() + " is " + hypothesisList.get(hypothesisList.size()-1).state();
//        for(int i=hypothesisList.size()-2; i>=0; i--){
//            result = result + " and " + hypothesisList.get(i).name() + " is " + hypothesisList.get(i).state();
//        }
//
//        if(!this.directCause(jess)){
//            result = result + " and... \n";
//        } else {
//            result = result + "\n";
//        }
//
//        return result;
//    }

    public boolean contains(Hypothesis otherHypothesis){
        for(int i=0; i<otherHypothesis.size(); i++){
            if(!hypothesisList.contains(otherHypothesis.get(i))){
                return false;
            }
        }
        
        return true;
    }

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

    public boolean contradiction(Rete jess) throws JessException{
        if (contradiction == null){
            test(jess);
        }
        return contradiction;
    }
    
    public boolean directCause(Rete jess) throws JessException{
        if (directCause == null){
            test(jess);
        }
        return directCause;
    }

    public boolean composed(Rete jess) throws JessException {
        if(hypothesisList.size() > 1){
            return true;
        }
        if(this.directCause(jess)){
            return false;
        }
        return true;
    }

    public int nrStateChanges(Rete jess) throws JessException{
        if (nrStateChanges == null){
            test(jess);
        }
        return nrStateChanges;
    }

    public jess.QueryResult observables(Rete jess) throws JessException{
        test(jess);
        return jess.runQueryStar("search-observable", new jess.ValueVector());
    }

    private void test(Rete jess) throws JessException{
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
