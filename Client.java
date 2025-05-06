import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java Client <server_host> <server_port> <request_file>");
            System.exit(1);
        }
        String serverHost = args[0];
        int serverPort = Integer.parseInt(args[1]);
        String requestFile = args[2];

        List<String> requests = readRequests(requestFile);
        try (Socket socket = new Socket(serverHost, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            for (String request : requests) {
                String encodedRequest = encodeRequest(request);
                out.println(encodedRequest);
                String response = in.readLine();
                decodeResponse(response);
            }
        } catch (IOException e) {
            System.err.println("connect error: " + e.getMessage());
        }
    }


