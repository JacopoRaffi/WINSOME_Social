import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerWorker implements Runnable{
    ServerWinsomeSocial social;
    Socket clientSocket;
    String clientUserName;

    public ServerWorker(Socket socket, ServerWinsomeSocial social){
        clientSocket = socket;
        this.social = social;
    }

    public void run(){
        String request;
        try {
            DataInputStream inReader = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream outWriter = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
            while(!Thread.currentThread().isInterrupted()){
                request = inReader.readUTF();
                executeRequest(request, outWriter);
            }
        }catch(IOException e){
            System.err.println("ERRORE WORKER: problemi con la connessione...chiusura del clientSocket");
            try{
                clientSocket.close();
            }catch(IOException ex){
                System.err.println("ERRORE WORKER: problemi con la chiusura del clientSocket");
            }
        }
    }

    private void executeRequest(String request, DataOutputStream writer) throws IOException{
        String response = "";
        String[] param = request.split(" ");
        if(request.startsWith("login")){
            if(social.login(param[1], param[2])) { //username e password
                response = "SUCCESSO: Login effettuato con successo";
                clientUserName = param[1];
                writer.writeUTF(response);
                writer.flush();
            }
            else{
                response = "ERRORE: username o password errati";
                writer.writeUTF(response);
                writer.flush();
            }
        }
        else if(request.startsWith("createpost")){
            if(social.createPost(clientUserName, param[1], param[2])){
                response = "SUCCESSO: Post creato";
                writer.writeUTF(response);
                writer.flush();
            }
            else{
                response = "ERRORE: errore durante la creazione del post";
                writer.writeUTF(response);
                writer.flush();
            }
        }
    }

}
