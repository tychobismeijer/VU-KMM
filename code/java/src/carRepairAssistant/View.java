/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package carRepairAssistant;
import java.util.ArrayList;
import jess.JessException;
import jess.Rete;
/**
 *
 * @author Joost
 */
public class View {
    private ConsoleCheat c;

    View(){
        c = new ConsoleCheat();
    }

    private void printSpace(int number){
        for(int i=0; i<number; i++){
            c.printf(" ");
        }
    }

    private void printInt(int integer, int max){
        String number = Integer.toString(integer);
        String maxInt = Integer.toString(max);
        printSpace(maxInt.length()-number.length());
        c.printf(number);
    }

    private void printString(String string, int tab){
         c.printf(string);
         printSpace(tab - string.length());
    }

    public void printCurrentHypothesis(Hypothesis hypothesis, Rete jess)throws JessException{
        c.printf("The hypothesis is that ");
        printHypothesis(hypothesis, jess);
    }

    private void printHypothesis(Hypothesis hypothesis, Rete jess)throws JessException{
        int[] tabArray = new int[hypothesis.size()*2];
        printHypothesis(hypothesis, tabArray, jess);
    }

    private void printHypothesis(Hypothesis hypothesis, int[] tabArray, Rete jess) throws JessException{
        String name, state;

        for(int j=0; j<hypothesis.size(); j++){
            name = hypothesis.get(j).name();
            state = hypothesis.get(j).stateName();
            c.printf(" " + name);
            printSpace(tabArray[j*2] - name.length());
            c.printf(" is " + state);
            printSpace(tabArray[j*2+1] - state.length());

            if(j+1<hypothesis.size()){
                c.printf(" and");
            }
        }

        if(!hypothesis.directCause(jess)){
            c.printf(" and...");
        }
        c.printf("\n");
    }

    private int maxHypothesisSize(ArrayList<Hypothesis> hypothesisArray){
        int result = 0;
        for(int i=0; i<hypothesisArray.size(); i++){
            result = Math.max(result, hypothesisArray.get(i).size());
        }
        return result;
    }

    private int[] getTabArray(ArrayList<Hypothesis> hypothesisArray){
        int[] result = new int[maxHypothesisSize(hypothesisArray)*2];

        for(int i=0; i<hypothesisArray.size(); i++){
            for(int j=0; j<hypothesisArray.get(i).size(); j++){
                result[j*2] = Math.max(result[j*2], hypothesisArray.get(i).get(j).name().length());
                result[j*2+1] = Math.max(result[j*2+1], hypothesisArray.get(i).get(j).stateName().length());
            }
        }

        return result;
    }

    public void printSelectHypothesis(){
        c.printf("\n");
        c.printf("---------SELECT HYPOTHESIS---------\n");
    }

    public void printReportComplaint(){
        c.printf("\n");
        c.printf("---------REPORT COMPLAINT---------\n");
    }

    public void printHypothesisArray(ArrayList<Hypothesis> hypothesisArray, Rete jess) throws JessException{
        Hypothesis hypothesis;
        int[] tabArray = getTabArray(hypothesisArray);

        c.printf("Available hypothesis are:\n");

        for(int i=0; i<hypothesisArray.size(); i++){
            hypothesis = hypothesisArray.get(i);
            printInt(i, hypothesisArray.size());
            printHypothesis(hypothesis, tabArray, jess);
        }
    }

    public void printSuggestion(Hypothesis suggestion, Rete jess) throws JessException{
        c.printf("We suggest: ");
        printHypothesis(suggestion, jess);
        c.printf("Do you have an other suggestion (no/nr/id)?\n");
    }
}
