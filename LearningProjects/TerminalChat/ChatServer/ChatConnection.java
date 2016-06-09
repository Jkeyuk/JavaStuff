package chatserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatConnection implements Runnable {

    private final Socket CLIENT_SOCKET;
    private final BufferedReader IN;
    private final ConcurrentHashMap<String, Socket> CLIENT_LIST;

    public ChatConnection(Socket clientSocket, ConcurrentHashMap<String, Socket> clientList)
            throws IOException {
        this.CLIENT_SOCKET = clientSocket;
        this.IN = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        this.CLIENT_LIST = clientList;
    }

    @Override
    public void run() {
        System.out.println("Connection made to " + CLIENT_SOCKET.getInetAddress().toString());
        CLIENT_LIST.put(CLIENT_SOCKET.getInetAddress().toString(), CLIENT_SOCKET);
        startListening();
    }

    @SuppressWarnings("SleepWhileInLoop")
    private void startListening() {
        try {
            while (true) {
                String input;
                while ((input = IN.readLine()).length() != 0) {
                    broadcastMessage(CLIENT_SOCKET.getInetAddress().toString()
                            + ": " + input);
                }
                Thread.sleep(1000);
            }
        } catch (SocketException e) {
            CLIENT_LIST.remove(CLIENT_SOCKET.getInetAddress().toString());
        } catch (InterruptedException | IOException ex) {
            Logger.getLogger(ChatConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void broadcastMessage(String s) {
        for (Map.Entry<String, Socket> entry : CLIENT_LIST.entrySet()) {
            try {
                Socket sock = entry.getValue();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
                writer.write(s);
                writer.newLine();
                writer.flush();
                writer.write("");
                writer.newLine();
                writer.flush();
            } catch (IOException ex) {
                Logger.getLogger(ChatConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}