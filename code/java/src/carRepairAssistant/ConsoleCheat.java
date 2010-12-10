/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package carRepairAssistant;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Joost Huizinga
 */
public class ConsoleCheat {
    BufferedReader br;
    java.io.Writer co;

    ConsoleCheat(){
        br = new BufferedReader(new InputStreamReader(System.in));
        co = new java.io.PrintWriter(System.out);
    }

    public void printf(String input){
        try {
            co.write(input);
            co.flush();
        } catch (IOException ex) {
            Logger.getLogger(ConsoleCheat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public String readLine(){
        try {
            return br.readLine();
        } catch (IOException ex) {
            Logger.getLogger(ConsoleCheat.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
