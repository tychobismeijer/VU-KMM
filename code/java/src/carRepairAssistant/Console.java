package carRepairAssistant;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *  A Console to write on and read from. Replaces java.io.Console because of
 *  issues with NetBeans and that class.
 */
public class Console {
    BufferedReader br;
    java.io.Writer co;

    Console(){
        br = new BufferedReader(new InputStreamReader(System.in));
        co = new java.io.PrintWriter(System.out);
    }

    public void printf(String input){
        try {
            co.write(input);
            co.flush();
        } catch (IOException ex) {
            Logger.getLogger(Console.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public String readLine(){
        try {
            return br.readLine();
        } catch (IOException ex) {
            Logger.getLogger(Console.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
