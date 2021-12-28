package Server;

import java.io.*;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

//questo thread salva lo stato del social periodicamente
public class ServerBackup extends Thread {
    private File postStatus;
    private File usersStatus;
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
                Thread.sleep(timelapse);
                backupPost();
                backupUsers();
            }catch(InterruptedException e){
                break;
            }catch(IOException e){
                continue; //anche se fallisce una volta non voglio che si interrompa il thread
            }
        }
    }

    protected void backupPost() throws IOException {
        FileWriter writer = new FileWriter(postStatus);
        Gson builder = new GsonBuilder().setPrettyPrinting().create();
        Type typeOfMapPost = new TypeToken<ConcurrentHashMap<Long, ServerPost>>() {}.getType();
        writer.write(builder.toJson(social.getSocialPost(), typeOfMapPost));
        writer.close();
    }

    protected void backupUsers() throws IOException {
        FileWriter writer = new FileWriter(usersStatus);
        Gson builder = new GsonBuilder().setPrettyPrinting().create();
        Type typeOfMap = new TypeToken<ConcurrentHashMap<String, ServerUser>>() {}.getType();
        writer.write(builder.toJson(social.getSocialUsers(), typeOfMap));
        writer.close();
    }
}
