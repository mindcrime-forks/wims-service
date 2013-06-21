/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.WIMsService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openwims.Objects.WIMFrame;
import org.openwims.Stanford.StanfordHelper;
import org.openwims.WIMGlobals;
import org.openwims.WIMProcessor;
import org.postgresql.jdbc2.optional.SimpleDataSource;

/**
 *
 * @author jesseenglish
 */
public class WIMsService {

    public static int DEFAULT_PORT = 9250;
    public static boolean STOP = false;
    public static ServerSocket SOCKET;
    public static File LOG;
    
    public static void main(String[] args) throws Exception {
        
        //Identify the port
        int port = DEFAULT_PORT;
        for (String arg : args) {
            if (arg.startsWith("port=")) {
                port = Integer.parseInt(arg.replaceAll("port=", ""));
                break;
            }
        }
        
        //Start or stop the service
        for (String arg : args) {
            if (arg.equals("start")) {
                System.out.println("Starting service on port " + port + ".");
                start(port);
                break;
            } else if (arg.equals("stop")) {
                System.out.println("Stopping service on port " + port + ".");
                stop(port);
                break;
            }
        }
        
        //default: assume default port and "start"
        if (args.length == 0) {
            start(port);
        }
    }
    
    private static void start(int port) throws Exception {
        try {
            String filename = "service_" + new SimpleDateFormat("dd-MM-yyy").format(new Date()) + ".log";
            System.out.println("Logging to " + filename + "...");
            LOG = new File(filename);
            if (!LOG.exists()) {
                LOG.createNewFile();
            }
            
            System.out.println("Initializing Ontology/Lexicon...");
            WIMGlobals.ontology();
            WIMGlobals.lexicon();
            
            System.out.println("Initializing Stanford...");
            StanfordHelper.annotate("The man hit the building.");
            
            System.out.println("Binding Socket...");
            SOCKET = new ServerSocket(port);

            System.out.println("...WIMsService is running!");
            while (true && !STOP) {
                Socket client = SOCKET.accept();
                ClientThread thread = new ClientThread(client);
                Thread t = new Thread(thread);
                t.start();
            }
        
        } catch (SocketException err) {
            if (err.getMessage().equalsIgnoreCase("Socket closed")) {
                System.out.println("System shutdown!");
            } else {
                err.printStackTrace();
            }
        }
    }
    
    private static void stop(int port) throws Exception {
        String response = WIMsClient.message(port, "{\"admin-request\": \"stop\"}");
        System.out.println(response);
    }
    
    private static void log(String request, String response) throws Exception {
        FileWriter fw = new FileWriter(LOG.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("REQUEST: " + request + " RESPONSE: " + response + "\n");
        bw.close();
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
                System.out.println("Received request: " + clientRequest);
                
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject)parser.parse(clientRequest);
                
                //JSON is valid in two forms, either a "process-request" or 
                //an "admin-request"; determine which we are dealing with
                //and then process the request
                
                String response;
                
                if (json.get("process-request") != null) {
                    response = process(json);
                } else if (json.get("admin-request") != null) {
                    response = admin(json);
                } else {
                    //invalid request
                    response = "{\"error\": \"bad request type\"}";
                }
                
                out.println(response);

                log(clientRequest, response);
                
                out.close();
                in.close();
                this.client.close();
                this.client.close();
            } catch (Exception err) {
                System.out.println("ERROR");
                err.printStackTrace();
            }
            
            if (STOP) {
                try {
                    SOCKET.close();
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        }
        
        private String process(JSONObject json) throws Exception {
            double ver = Double.parseDouble((String)json.get("ver"));
            String text = (String)json.get("text");

            LinkedList<WIMFrame> wims = WIMProcessor.WIMify(StanfordHelper.convert(StanfordHelper.annotate(text)));

            StringBuilder wimsjson = new StringBuilder();
            wimsjson.append("[");
            for (WIMFrame wim : wims) {
                wimsjson.append(wim.json());

                if (wim != wims.getLast()) {
                    wimsjson.append(", ");
                }
            }
            wimsjson.append("]");
            
            return wimsjson.toString();
        }
        
        private String admin(JSONObject json) throws Exception {
            String command = (String)json.get("admin-request");
            
            if (command.equalsIgnoreCase("stop")) {
                STOP = true;
            }
            
            return "{\"admin-response\": \"stopped\"}";
        }
        
    }
}
