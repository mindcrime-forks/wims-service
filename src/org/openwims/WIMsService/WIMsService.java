/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.WIMsService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openwims.Objects.WIMFrame;
import org.openwims.Stanford.StanfordHelper;
import org.openwims.WIMProcessor;

/**
 *
 * @author jesseenglish
 */
public class WIMsService {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
//        if (args.length != 1) {
//            throw new Exception("First argument must be the desired port!");
//        }
//        
//        int port = Integer.parseInt(args[0]);
        
        //TODO: initialize stanford here, rather than lazy load it later
        
        int port = 9250;
        ServerSocket serverSocket = new ServerSocket(port);
        
        
        try {
            while (true) {
                Socket client = serverSocket.accept();
                ClientThread thread = new ClientThread(client);
                Thread t = new Thread(thread);
                t.start();
            }
        }  catch (IOException e) {
            System.out.println("Accept failed: " + port);
            System.exit(-1);
        }
    }
    
    private static class ClientThread implements Runnable {
        
        private Socket client;

        public ClientThread(Socket client) {
            super();
            this.client = client;
        }

        @Override
        public void run() {
            try {
                PrintWriter out = new PrintWriter(this.client.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
                
                StringBuilder builder = new StringBuilder();
                while (true) {
                    int ch = in.read();
                    if ((ch < 0) || (ch == '\n')) {
                        break;
                    }
                    builder.append((char) ch);
                }
                
                String clientRequest = builder.toString();
                
                
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject)parser.parse(clientRequest);
                double ver = Double.parseDouble((String)json.get("ver"));
                String text = (String)json.get("text");
                
                LinkedList<WIMFrame> wims = WIMProcessor.WIMify(StanfordHelper.convert(StanfordHelper.annotate(text)));
                out.println(wims);

                out.close();
                in.close();
                this.client.close();
                this.client.close();
            } catch (Exception err) {
                System.out.println("ERROR");
                err.printStackTrace();
            }
        }
        
    }
}
