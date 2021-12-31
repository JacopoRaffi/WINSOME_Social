package Server;

import java.io.*;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

//questo thread salva lo stato del social periodicamente
public class ServerBackup extends Thread {
    private final File postStatus;
    private final File usersStatus;
    private ServerWinsomeSocial social;
    private final long timelapse;

    public ServerBackup(ServerWinsomeSocial social, File usersStatus, File postStatuss, long timeLapse){
        this.social = social;
        this.postStatus = postStatuss;
        this.usersStatus = usersStatus;
        this.timelapse = timeLapse;
    }

    public void run(){
        while(!Thread.currentThread().isInterrupted()){
            try{
                Thread.sleep(timelapse * 60 * 1000); //*60 * 1000 serve a trasformare da minuti a millisecondi
                backupUser();
                backupPost();
            }catch(InterruptedException e){
                break;
            }catch(IOException e){
                continue; //anche se fallisce una volta non voglio che si interrompa il thread
            }
        }
    }

    //idea backup: fare la get di un utente alla volta e salvarlo nel file(costruire manualmente la tabella hash JSON)
    protected synchronized void backupUser() throws IOException {}

    protected synchronized void backupPost() throws IOException {}
}
