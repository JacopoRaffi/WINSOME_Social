import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

//writeUTF, readUTF string len limit 65535 bytes
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
            System.err.println("WORKER: chiusura connessione client...");
            try{
                social.getSocialUsers().get(clientUserName).logout();
                clientSocket.close();
            }catch(IOException ex){
                System.err.println("ERRORE WORKER: problemi con la chiusura del clientSocket");
            }catch(NullPointerException exex){
                System.err.println("ERRORE: l'utente non aveva fatto login, impossibile fare logout");
            }
        }
    }

    private void executeRequest(String request, DataOutputStream writer) throws IOException, NullPointerException{
        String response = "";
        String[] param = request.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if(request.startsWith("login")){
            ServerUser user = social.getSocialUsers().get(param[1]);
            if((user != null)){
                if(!user.isLogged()){
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
                }else{
                    writer.writeUTF("ERRORE: hai già fatto il login");
                    writer.flush();
                }
            }else{
                writer.writeUTF("ERRORE: non esiste alcun utente con questo username");
                writer.flush();
            }
        }
        else if(request.startsWith("createpost")){
            long id;
            if((id=social.createPost(clientUserName, param[1], param[2])) > 0){
                response = "SUCCESSO: Post creato(id=" + id + ")";
            }
            else{
                response = "ERRORE: errore durante la creazione del post";
            }
            writer.writeUTF(response);
            writer.flush();
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
        else if(request.startsWith("rewinpost")){
            if(!social.rewinPost(Long.parseLong(param[1]), clientUserName)) {
                writer.writeUTF("ERRORE: post non presente nel social oppure utente non seguito");
            }
            else{
                writer.writeUTF("SUCCESSO: post condiviso nel tuo blog");
            }
            writer.flush();
        }
        else if(request.startsWith("ratepost")){
            response = social.ratePost(clientUserName, Long.parseLong(param[1]) ,Integer.parseInt(param[2]));
            writer.writeUTF(response);
            writer.flush();
        }
        else if(request.startsWith("addcomment")){
            response = social.addComment(clientUserName, Long.parseLong(param[1]) , param[2]);
            writer.writeUTF(response);
            writer.flush();
        }
        else if(request.startsWith("getwalletinbitcoin")){
            try{
                ServerWallet wallet = social.getSocialUsers().get(clientUserName).getWallet();
                response = "PORTAFOGLIO(BITCOIN): " + social.toBitcoin(wallet.getTotale());
            }catch(IOException e){
                response = "ERRORE: problema durante il calcolo del portafoglio, riprovare più tardi";
            }
            writer.writeUTF(response);
            writer.flush();
        }
        else if(request.startsWith("getwallet")){
            ServerWallet wallet = social.getSocialUsers().get(clientUserName).getWallet();
            response = "PORTAFOGLIO(WINCOIN): " + wallet.getTotale();
            writer.writeUTF(response);
            writer.flush();
            Integer dim = wallet.getTransazioni().size();
            writer.writeUTF(dim.toString());
            writer.flush();
            List<String> transazioni = wallet.getTransazioni();
            Iterator<String> it = transazioni.iterator();
            int i = 1;
            while(it.hasNext()){
                response = it.next();
                writer.writeUTF("Transazione " + i + " " + response);
                writer.flush();
                i++;
            }
        }
        else if(request.startsWith("showfeed")){
            response = social.showFeed(clientUserName);
            String[] posts = response.split("~");
            Integer dim = posts.length;
            writer.writeUTF(dim.toString());
            writer.flush();

            for(int i = 0; i < dim; i++){
                response = posts[i];
                writer.writeUTF(response);
                writer.flush();
            }
        }
        else if(request.startsWith("viewblog")){
            response = social.showBlog(clientUserName);
            String[] posts = response.split("~");
            Integer dim = posts.length;
            writer.writeUTF(dim.toString());
            writer.flush();

            for(int i = 0; i < dim; i++){
                response = posts[i];
                writer.writeUTF(response);
                writer.flush();
            }
        }
        else if(request.startsWith("listfollowing")){
            LinkedHashSet<String> aux = social.listFollowed(clientUserName);
            Integer dim = aux.size();
            writer.writeUTF(dim.toString());
            writer.flush();
            Iterator<String> it = aux.iterator();
            while(it.hasNext()) {
                response = it.next();
                writer.writeUTF("user: " + response);
                writer.flush();
            }
        }
        else if(request.startsWith("listusers")){
            Set<String> aux = social.listUsers(social.getSocialUsers().get(clientUserName).getTags(), clientUserName);
            Integer dim = aux.size();
            Iterator<String> it = aux.iterator();
            writer.writeUTF(dim.toString());
            writer.flush();
            while(it.hasNext()){
                String user = it.next();
                String[] tags = social.getSocialUsers().get(user).getTags();
                response = user + ", TAGS: ";
                for(int i = 0; i < tags.length; i++){
                    response += tags[i] + "-";
                }
                writer.writeUTF(response);
                writer.flush();
            }
        }
        else if(request.startsWith("follow")){
            if(social.getSocialUsers().get(param[1]) == null)
                response = "Utente inesistente";
            else {
                if (!social.followUser(clientUserName, param[1])) {
                    response = "ERRORE: utente già seguito";
                } else {
                    response = "SUCCESSO: ora segui " + param[1];
                }
            }
            writer.writeUTF(response);
            writer.flush();
        }
        else if(request.startsWith("unfollow")){
            if(!social.unFollowUser(clientUserName, param[1])){
                response = "ERRORE: non segui questo utente ";
            }
            else{
                response = "SUCCESSO: non segui più" + param[1];
            }
            writer.writeUTF(response);
            writer.flush();
        }
        else if(request.startsWith("logout")){
            social.getSocialUsers().get(clientUserName).logout();
        }

    }

}
