package com.example.chat2;


import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;



class ClientThread implements Runnable {
    private Socket socket;
    private String SERVER_IP;
    private int SERVER_PORT;
    public String serverMessage = null;
    public boolean newMessage = false;
    DataInputStream streamIn = null;
    private static final String TAG = "MyActivity";
    public ClientThread(String SERVER_IP, int SERVER_PORT) {
        this.SERVER_IP = SERVER_IP;
        this.SERVER_PORT = SERVER_PORT;
    }
    public void ChangeIPnPort(String SERVER_IP, int SERVER_PORT) {
        this.SERVER_IP = SERVER_IP;
        this.SERVER_PORT = SERVER_PORT;
    }


    @Override
    public void run() {

        try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            socket = new Socket(serverAddr, SERVER_PORT);
            while (!Thread.currentThread().isInterrupted()) {
                streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                String message = streamIn.readUTF();

                if (null == message || "Disconnect".contentEquals(message)) {
                    boolean interrupted = Thread.interrupted();
                    serverMessage = "Server Disconnected: " + interrupted;
                }
                if (!message.equals(serverMessage)) {
                    serverMessage = message;
                    newMessage = true;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void sendMessage(final String message) {
        new Thread(() -> {
            try {
                if (null != socket) {
                    DataOutputStream streamOut = new DataOutputStream(socket.getOutputStream());
                    streamOut.writeUTF(message);
                    streamOut.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}
