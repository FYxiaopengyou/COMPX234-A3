import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {
    public static void main(String[] args) {
    //check the parameter
        if (args.length != 3) {
            System.err.println("Usage: java Client <server_host> <server_port> <request_file>");
            System.exit(1);
        }
        String serverHost = args[0];
        int serverPort = Integer.parseInt(args[1]);
        String requestFile = args[2];

        //read the request
        List<String> requests = readRequests(requestFile);
        //connect to server using Socket
        try (Socket socket = new Socket(serverHost, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {


                //send request
            for (String request : requests) {
                String encodedRequest = encodeRequest(request);
                out.println(encodedRequest);
                String response = in.readLine();
                decodeResponse(response);
            }
 //error handing

        } catch (IOException e) {
            System.err.println("connect error: " + e.getMessage());
        }
    }

    private static List<String> readRequests(String file) {
        List<String> requests = new ArrayList<>();

        //read file use BufferedReader
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

 // check about format and size
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(" ");
                if (parts.length < 2) {
                    System.err.println("format is wrong '" + line + "'");
                    continue;
                }
                String command = parts[0];
                String key = parts[1];
                String value = parts.length > 2 ? parts[2] : null;
                int combinedSize = key.length() + (value != null ? value.length() : 0);
                if (combinedSize > 1024) {
                    System.err.println("size is not allow '" + line + "'");
                    continue;
                }
                requests.add(command + " " + key + (value != null ? " " + value : ""));
            }

//deal with some error if it have
        } catch (IOException e) {
            System.err.println("read error: " + e.getMessage());
        }
        return requests;
    }

    private static String encodeRequest(String request) {
        String lengthStr = String.format("%03d", request.length());
        return lengthStr + " " + request;
    }
//check format 
    private static void decodeResponse(String response) {
        if (response == null || response.length() < 3) {
            System.err.println("format is wrong");
            return;
        }

        try {
            int length = Integer.parseInt(response.substring(0, 3));
            String actualResponse = response.substring(4);
            if (actualResponse.length() != length) {
                System.err.println(" Invalid size");
                return;
            }
            String[] parts = actualResponse.split(" ");
            String status = parts[0];

//output proper message according to the status
            if ("OK".equals(status)) {
                if (parts.length > 1) {
                    System.out.println("OK " + parts[1]);
                } else {
                    System.out.println("OK");
                }
            } else if ("ERROR".equals(status)) {
                StringBuilder errorMsg = new StringBuilder();
                for (int i = 1; i < parts.length; i++) {
                    errorMsg.append(parts[i]).append(" ");
                }
                System.out.println("ERROR " + errorMsg.toString().trim());
            }
            
//check the error and outpput relavent message (if have error)
        } catch (NumberFormatException e) {
            System.err.println("invalid format");
        }
    }
}    