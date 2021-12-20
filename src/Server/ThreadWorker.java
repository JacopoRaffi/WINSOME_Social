package Server;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ThreadWorker implements Runnable{
    WinsomeSocial social;
    Socket clientSocket;
    String clientUserName;

    public ThreadWorker(Socket socket, WinsomeSocial social){
        clientSocket = socket;
        this.social = social;
    }

    public void run(){
        String request;
        String response;
        try {
            DataInputStream inReader = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream outWriter = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
            while(!Thread.currentThread().isInterrupted()){
                request = inReader.readUTF();
                executeRequest(request);
            }
        }catch(IOException e){
            System.err.println("ERRORE: problemi con la connessione...chiusura del clientSocket");
            try{
                clientSocket.close();
            }catch(IOException ex){
                System.err.println("ERRORE: problemi con la chiusura del clientSocket");
            }
        }
    }

    private void executeRequest(String request){}
}
