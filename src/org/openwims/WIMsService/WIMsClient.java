/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.WIMsService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * For testing the WIMsService
 * 
 * @author jesseenglish
 */
public class WIMsClient {
    
    public static void main(String[] args) throws Exception {
//        int port = Integer.parseInt(args[0]);
        int port = 9250;
        
        Socket echoSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            echoSocket = new Socket("localhost", port);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));

            out.println("{\"ver\": \"1.0\", \"text\": \"The man hit the building.\"}");
            out.flush();
            
            System.out.println("here");
            
            System.out.println(in.readLine());
            
            
            in.close();
            echoSocket.close();
            
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: localhost.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: localhost.");
            System.exit(1);
        }
        
        
        
        

//	BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
//	String userInput;
//
//	while ((userInput = stdIn.readLine()) != null) {
//	    out.println(userInput);
//	    System.out.println("echo: " + in.readLine());
//	}

	
    }
    
}
