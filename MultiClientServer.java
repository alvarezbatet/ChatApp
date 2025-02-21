import java.net.*;
import java.io.*;
//import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

//import Globals.*;

public class MultiClientServer {

    private static final int PORT_NUMBER = 80;
    private ClientHandler arrClients[] = new ClientHandler[2];
    private int numclients = 0;
    Globals globals = new Globals();
    

    public void run() throws IOException {
        // Create a thread pool for handling client connections
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try (ServerSocket serverSocket = new ServerSocket(PORT_NUMBER)) {
            System.out.println("Server started on port: " + PORT_NUMBER);

            while (true) {
                for (int i=0; i < 2; i++) {
                    if ( arrClients[i] != null) {
                        if (arrClients[i].clientSocket.isClosed()) {
                            arrClients[i].close();
                            arrClients[i] = null;
                            numclients = 0;
                        }
                    }
                }
                try {
                    if (numclients < 2) { 
                        System.out.println("Waiting for client..." + numclients); 
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Client accepted: " + clientSocket);
                        ClientHandler clientHandler = new ClientHandler(clientSocket, numclients, globals);
                        executor.submit(clientHandler);
                        arrClients[numclients] = clientHandler;
                        numclients += 1;
                    }
                } catch (IOException e) {
                    System.out.println("Acceptance Error: " + e); 
                }
                if (numclients == 2) {
                    try {                      
                        TimeUnit.SECONDS.sleep(1); //TODO make future tests on this
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        if(globals.newMessage_0()){
                            arrClients[1].writer.writeUTF(globals.getmessage0());
                            globals.setNew0(false);
                        }
                        if(globals.newMessage_1()){
                            arrClients[0].writer.writeUTF(globals.getmessage1());
                            globals.setNew1(false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            arrClients[0].close();
            arrClients[1].close();
            executor.shutdown();
        }
    }
    public static void main(String args[]) {  
        try {
            MultiClientServer instance = new MultiClientServer();
            instance.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {

    public final Socket clientSocket;
    private DataInputStream reader;
    public DataOutputStream writer;
    private int id;
    public Globals globals;
    ExecutorService executorService;
    Future<?> future;
    

    public ClientHandler(Socket clientSocket, int ID, Globals globals) {
        this.clientSocket = clientSocket;
        this.id = ID;
        this.globals = globals;
        this.executorService = Executors.newSingleThreadExecutor();
        this.future = executorService.submit(this);
    }

    @Override
    public void run() {
        try {
            reader = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            writer = new DataOutputStream(clientSocket.getOutputStream());
            while (true) {
                String message = reader.readUTF();
                System.out.println(message); 
                if (id == 0) {
                    globals.setNew0(true);
                    globals.setMessage0(message);
                }else if (id == 1) {
                    globals.setNew1(true);
                    globals.setMessage1(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            close();
        } finally {
            close();
        }
    }
    void close() {
        try {
            reader.close();
            writer.close();
            future.cancel(true);
            executorService.shutdown();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}