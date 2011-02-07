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
    private final static int    COMPONENT =0,
                                STATE =1;

    private ArrayList<String[]> hypothesisList;
    private Boolean contradiction;
    private Boolean directCause;
    private Integer nrStateChanges;
    public Integer maxIndex;
    

    Hypothesis(){
        hypothesisList = new ArrayList<String[]>();
    }

    Hypothesis(ArrayList<String[]> hypothesisList){
        this.hypothesisList = hypothesisList;
    }

    Hypothesis(String component, String state){
        String[] h = new String[2];
        hypothesisList = new ArrayList<String[]>();
        h[COMPONENT] = component;
        h[STATE] = state;
        hypothesisList.add(h);
    }

    Hypothesis(String[] string){
        this(string[0], string[1]);
     }

    public ArrayList<String[]> toArrayList(){
        return hypothesisList;
    }

    @Override
    public boolean equals(Object otherHypothesis){
        if(otherHypothesis.getClass() != this.getClass())
            return false;
        
        return this.equals((Hypothesis) otherHypothesis);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.hypothesisList != null ? this.hypothesisList.hashCode() : 0);
        return hash;
    }

    public boolean equals(Hypothesis otherHypothesis){
        if(otherHypothesis == null)
            return false;

        ArrayList<String[]> otherHypothesisList = otherHypothesis.toArrayList();

        if(hypothesisList == null ^ otherHypothesisList == null)
            return false;
        if(hypothesisList == null && otherHypothesisList == null)
            return true;
        
        if(hypothesisList.size() != otherHypothesisList.size())
            return false;

        for(int i=0; i<hypothesisList.size(); i++){
            if(hypothesisList.get(i).length != 2 || otherHypothesisList.get(i).length != 2)
                return false;
            if(!hypothesisList.get(i)[0].equals(otherHypothesisList.get(i)[0]))
                return false;
            if(!hypothesisList.get(i)[1].equals(otherHypothesisList.get(i)[1]))
                return false;
        }

        return hypothesisList.equals(otherHypothesis.toArrayList());
    }

    public ArrayList<Hypothesis> split(){
        ArrayList<Hypothesis> result = new ArrayList<Hypothesis>();

         for(int i=0; i<hypothesisList.size(); i++){
             result.add(new Hypothesis(hypothesisList.get(i)));
         }

        return result;
    }

    public String getFirstHypothesis(Rete jess) throws JessException{
        String result;
        result = hypothesisList.get(0)[COMPONENT] + " is " + hypothesisList.get(0)[STATE];

        if(this.composed(jess)){
            result = result + " and... \n";
        } else {
            result = result + "\n";
        }

        return result;
    }

    public String getFullHypothesis(Rete jess) throws JessException{
        String result;
        result = hypothesisList.get(0)[COMPONENT] + " is " + hypothesisList.get(0)[STATE];
        for(int i=1; i<hypothesisList.size(); i++){
            result = result + " and " + hypothesisList.get(i)[COMPONENT] + " is " + hypothesisList.get(i)[STATE];
        }
        result = result + "\n";

        return result;
    }

    public boolean contains(Hypothesis otherHypothesis){
        ArrayList<String[]> otherHypothesisList = otherHypothesis.toArrayList();

        for(int i=0; i<otherHypothesisList.size(); i++){
            if(!hypothesisList.contains(otherHypothesisList.get(i))){
                return false;
            }
        }
        
        return true;
    }

    public void add(Hypothesis otherHypothesis){
        ArrayList<String[]> otherHypothesisList = otherHypothesis.toArrayList();

        for(int i=0; i<otherHypothesisList.size(); i++){
            hypothesisList.add(otherHypothesisList.get(i));
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

    private void test(Rete jess) throws JessException{
        WorkingMemoryMarker beforeHypothesis = jess.mark();
        reset(jess);

        for(int i=0; i<hypothesisList.size();i++){
            jess.assertString(
                "(hypothesis " +
                hypothesisList.get(i)[COMPONENT] + " " +
                hypothesisList.get(i)[STATE] + ")"
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
