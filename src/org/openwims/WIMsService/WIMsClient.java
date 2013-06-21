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
import static org.openwims.WIMsService.WIMsService.DEFAULT_PORT;

/**
 * For testing the WIMsService
 * 
 * @author jesseenglish
 */
public class WIMsClient {
    
    public static void main(String[] args) throws Exception {
        //Identify the port
        int port = DEFAULT_PORT;
        for (String arg : args) {
            if (arg.startsWith("port=")) {
                port = Integer.parseInt(arg.replaceAll("port=", ""));
                break;
            }
        }
        
        String message = "{\"process-request\": \"parse\", \"ver\": \"1.0\", \"text\": \"The man hit the building.\"}";
        String response = message(port, message);
        System.out.println(response);
    }
    
    public static String message(int port, String message) throws Exception {
        Socket echoSocket = new Socket("localhost", port);
        PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));

        out.println(message);
        out.flush();

        String response = in.readLine();

        in.close();
        echoSocket.close();
        
        return response;
    }
    
}
