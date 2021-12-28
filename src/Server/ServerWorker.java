package Server;

import Utilities.Wallet;

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
            System.err.println("WORKER: chiusura connessione client");
            try{
                clientSocket.close();
            }catch(IOException ex){
                System.err.println("ERRORE WORKER: problemi con la chiusura del clientSocket");
            }
        }
    }

    private void executeRequest(String request, DataOutputStream writer) throws IOException, NullPointerException{
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
        else if(request.startsWith("deletepost")){
            if(social.deletePost(Long.parseLong(param[1]), clientUserName)){
                response = "SUCCESSO: Post eliminato";
                writer.writeUTF(response);
                writer.flush();
            }
            else{
                response = "ERRORE: errore durante la cancellazione del post";
                writer.writeUTF(response);
                writer.flush();
            }
        }
        else if(request.startsWith("showpost")){
            response = social.showPost(Long.parseLong(param[1]));
            if(response == null) {
                writer.writeUTF("ERRORE: post non presente nel social");
                writer.flush();
            }
            else{
                writer.writeUTF(response);
                writer.flush();
            }
        }
        else if(request.startsWith("getwalletinbitcoin")){
            try{
                Wallet wallet = social.getSocialUsers().get(clientUserName).getWallet();
                response = "PORTAFOGLIO(BITCOIN): " + social.toBitcoin(wallet.getTotale()) + wallet.toString();
            }catch(IOException e){
                response = "ERRORE: problema durante il calcolo del portafoglio, riprovare più tardi";
            }
            writer.writeUTF(response);
            writer.flush();
        }
        else if(request.startsWith("getwallet")){
            response = "PORTAFOGLIO(WINCOIN): " + (social.getSocialUsers().get(clientUserName).getWallet().getTotale());
            writer.writeUTF(response);
            writer.flush();
        }
        else if(request.startsWith("showfeed")){
            response = "FEED: " + social.showFeed(clientUserName);
            writer.writeUTF(response);
            writer.flush();
        }
        else if(request.startsWith("viewblog")){
            response = "BLOG: " + social.showBlog(clientUserName);
            writer.writeUTF(response);
            writer.flush();
        }
        else if(request.startsWith("listfollowing ")){
            response = social.listFollowed(clientUserName);
            writer.writeUTF(response);
            writer.flush();
        }
        else if(request.startsWith("listusers")){
            response = social.listUsers(social.getSocialUsers().get(clientUserName).getTags(), clientUserName);
            writer.writeUTF(response);
            writer.flush();
        }
        else if(request.startsWith("followuser")){
            if(!social.followUser(clientUserName, param[1])){
                response = "ERRORE: utente già seguito";
            }
            else{
                response = "SUCCESSO: ora segui" + param[1];
            }
            writer.writeUTF(response);
            writer.flush();
        }
        else if(request.startsWith("unfollowuser")){
            if(!social.unFollowUser(clientUserName, param[1])){
                response = "ERRORE: non segui questo utente :(";
            }
            else{
                response = "SUCCESSO: non segui più" + param[1];
            }
            writer.writeUTF(response);
            writer.flush();
        }

    }

}
