import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
//Author: Zhu Feiyu
//I enter a lot of comments to help me understand and review the code.
//Thanks for your teaching, professor.


public class Server {
// create a tuple space to store the relavent values. Also create a lock, only one thread can enter.
    private static final Map<String, String> tupleSpace = new HashMap<>();
    private static final Object lock = new Object();


    public static void main(String[] args) {
    //check if the parameter's number is 1.
        if (args.length != 1) {
            System.err.println("Usage: java Server <server_port>");
            System.exit(1);
        }
    //ensure the port is valid.
        int serverPort = Integer.parseInt(args[0]);
        if (serverPort < 1024 || serverPort > 65535) {
            System.err.println("Error: Server port must be between 1024 and 65535.");
            System.exit(1);
        }
    //start server, print port（端口）.
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("Server listening on port " + serverPort);
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(Server::printTupleSpaceStatus, 0, 10, TimeUnit.SECONDS);

        //wait for connection again and again.
        //and print IP of client
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting the server: " + e.getMessage());
        }
    }
//the note of handleClient:
/*
 * BufferedReader: is to read the request from Client
 * PrintWriter: is to send the response to Client
 * while: read every request line 
 * cut the request into pieces (command, key, value).
 * I write some error handing.
 */
    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.length() < 3) {
                    out.println("you Invalid input");
                    continue;
                }
                try {
                    int length = Integer.parseInt(inputLine.substring(0, 3));
                    String request = inputLine.substring(4);
                    if (request.length() != length) {
                        out.println("length invalid");
                        continue;
                    }
                    String[] parts = request.split(" ");
                    String command = parts[0];
                    String key = parts[1];
                    String value = parts.length > 2 ? parts[2] : null;
                    String response;
/*
* synchronized: only one thread can enter critical section.
* use switch to define different cases. and the actions professor required.
* i also wrote some error handing
*/
                    synchronized (lock) {
                        switch (command) {
                            case "PUT":
                                if (value == null) {
                                    response = "PUT command enter wrong";
                                } else {
                                    tupleSpace.put(key, value);
                                    response = "OK, right.";
                                }
                                break;
                            case "GET":
                                if (tupleSpace.containsKey(key)) {
                                    String val = tupleSpace.get(key);
                                    String responseStr = "OK " + val;
                                    response = String.format("%03d", responseStr.length()) + " " + responseStr;
                                } else {
                                    response = "key is wrong";
                                }
                                break;
                            case "REMOVE":
                                if (tupleSpace.containsKey(key)) {
                                    tupleSpace.remove(key);
                                    response = "002 OK";
                                } else {
                                    response = "key is wrong";
                                }
                                break;
                            default:
                                response = "you input it's wrong";
                        }
                    }
                    out.println(response);
                } catch (NumberFormatException e) {
                    out.println("you input format it's wrong");
                }
            }
        } catch (IOException e) {
            System.err.println("wrong " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("error about close " + e.getMessage());
            }
        }
    }
/*
 * use for loop to do :
 * output the messages of tuple space 
 * (as required in the specification)
 */
    private static void printTupleSpaceStatus() {
        int totalSize = 0;
        for (Map.Entry<String, String> entry : tupleSpace.entrySet()) {
            totalSize += entry.getKey().length() + entry.getValue().length();
        }
        int numTuples = tupleSpace.size();
        System.out.println("Tuple Space Status: Total Size = " + totalSize + " bytes, Number of Tuples = " + numTuples);
    }
}    