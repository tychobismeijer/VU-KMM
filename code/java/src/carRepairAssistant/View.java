package carRepairAssistant;
import java.util.ArrayList;
import java.util.List;
import jess.JessException;

/**
 *
 * @author Joost and Tycho
 */
class View {
    private Console c;
    private Control control;

    View(){
        c = new Console();
    }

    void setControl(Control control) {
        this.control = control;
    }

    public Observable askComplaint(List<Observable> allComplaints) {
        Observable result;
        String choice;

        //Print complaints
        printObservableArray(allComplaints);

        //Receive input
        choice = c.readLine();
        choice = choice.trim();
        if (isNumber(choice)){
            int nrChoice = Integer.parseInt(choice);
            result = allComplaints.get(nrChoice);
        } else {
            result = new Observable(choice, choice);
        }

        return result;
    }

    public Hypothesis askHypothesis(List<Hypothesis> allHypothesis) throws JessException {
        List<Hypothesis> basicHypothesis;
        Hypothesis hypothesis = control.newEmptyHypothesis();

        //Print the start of the Select hypothesis fase
        printSelectHypothesis();

        do {
            //Creates the list of basic hypothesis 
            basicHypothesis = hypothesis.filterSingleExtensions(allHypothesis);

            //Sets our suggestion
            Hypothesis suggestion = basicHypothesis.get(basicHypothesis.size()-1);

            //Print the available hypothesis and our suggestion
            printHypothesisArray(basicHypothesis);
            printSuggestion(suggestion);

            //Receive user input
            String choice = c.readLine();
            choice = choice.trim();

            if (isNumber(choice)){
                //If the user made a choice from the list then that choice becomes the selected hypothesis
                int nrChoice = Integer.parseInt(choice);
                hypothesis = (basicHypothesis.get(nrChoice));
            }
            else {
                //If the user did not make a choice then our suggestion becomes the selected hypothesis
                hypothesis = suggestion;
            }

            //Repeat until the hypothesis is a direct cause for the complaint
        } while(!hypothesis.directCause());

        return hypothesis;
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

    public void printCurrentHypothesis(Hypothesis hypothesis) throws JessException{
        c.printf("The hypothesis is that ");
        printHypothesis(hypothesis);
    }

    private void printHypothesis(Hypothesis hypothesis) throws JessException{
        int[] tabArray = new int[hypothesis.size()*2];
        printHypothesis(hypothesis, tabArray);
    }

    private void printHypothesis(Hypothesis hypothesis, int[] tabArray) throws JessException{
        String name, state;

        for(int j=hypothesis.size()-1; j>=0; j--){
            name = hypothesis.get(j).name();
            state = hypothesis.get(j).stateName();
            c.printf(" " + name);
            printSpace(tabArray[j*2] - name.length());
            c.printf(" is " + state);
            printSpace(tabArray[j*2+1] - state.length());

            if(j>0){
                c.printf(" and");
            }
        }

        if(!hypothesis.directCause()){
            c.printf(" and...");
        }
        c.printf("\n");
    }

    private int maxHypothesisSize(List<Hypothesis> hypothesisArray){
        int result = 0;
        for(int i=0; i<hypothesisArray.size(); i++){
            result = Math.max(result, hypothesisArray.get(i).size());
        }
        return result;
    }

    private int[] getTabArray(List<Hypothesis> hypothesisArray){
        int[] result = new int[maxHypothesisSize(hypothesisArray)*2];

        for(int i=0; i<hypothesisArray.size(); i++){
            for(int j=0; j<hypothesisArray.get(i).size(); j++){
                result[j*2] = Math.max(result[j*2], hypothesisArray.get(i).get(j).name().length());
                result[j*2+1] = Math.max(result[j*2+1], hypothesisArray.get(i).get(j).stateName().length());
            }
        }

        return result;
    }

    public void suggestObservable(Observable observable){
        c.printf("Do you want to observe if " + observable.name() + "? true/false/no\n");
    }

    public void printTryAgain(){
        c.printf("try again: true/false/no\n");
    }

    public void printNoObservablesLeft(){
        c.printf("No observables for this hypothesis \n");
    }

    public void printSelectHypothesis(){
        c.printf("\n");
        c.printf("-----------SELECT HYPOTHESIS----------\n");
    }

    public void printReportComplaint(){
        c.printf("\n");
        c.printf("-----------REPORT COMPLAINT-----------\n");
    }

    public void printNegotiateObservable(){
        c.printf("\n");
        c.printf("---------NEGOTIATE OBSERVABLE---------\n");
    }

    public void printReportResult(){
        c.printf("\n");
        c.printf("------------REPORT RESULT-------------\n");
    }

    public void printHypothesisArray(List<Hypothesis> hypothesisArray) throws JessException{
        Hypothesis hypothesis;
        int[] tabArray = getTabArray(hypothesisArray);

        for(int i=0; i<hypothesisArray.size(); i++){
            hypothesis = hypothesisArray.get(i);
            printInt(i, hypothesisArray.size());
            printHypothesis(hypothesis, tabArray);
        }
    }

    public void printObservableArray(List<Observable> observableArray){
        c.printf("Likely complaints are:\n");
        for(int i=0; i<observableArray.size(); i++){
            c.printf(i + ": " + observableArray.get(i).name() + " (id: " + observableArray.get(i).id() + ")\n");
        }
        c.printf("Your complaint is?\n");
    }

    public void printSuggestion(Hypothesis suggestion) throws JessException{
        c.printf("We suggest: ");
        printHypothesis(suggestion);
        c.printf("Do you have an other suggestion (no/nr/id)?\n");
    }


    public void printResult(List<Hypothesis> hypothesis) throws JessException{
        printReportResult();
        if(hypothesis.size()==0){
            c.printf("There are no possible hypothesis left.\n");
        } else if(hypothesis.size()==1){
            c.printf("The cause is probably that: ");
            printHypothesis(hypothesis.get(0));
            c.printf("Try to fix this and, if the problem remains, run the program again.\n");
        } else {
            c.printf("The cause is probably one of the following: \n");
            printHypothesisArray(hypothesis);
            c.printf("Try to fix these and, if the problem remains, run the program again.\n");
        }
    }

    /**
     * Returns whether the input string is parseble as an integer
     * @param string The string to be tested
     * @return True if the string can be parsed as an integer; False otherwise
     */
    private boolean isNumber(String string){
        try{
            Integer.parseInt(string);
            return true;
        }
        catch(NumberFormatException nfe){
            return false;
        }
    }
}
