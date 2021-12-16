package Server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.*;

//questo thread salva lo stato del social periodicamente
public class AutomaticSaving implements Runnable{
    private File postStatuss;
    private File usersStatus;
    private WinsomeSocial social;
    private final long timelapse;

    public AutomaticSaving(WinsomeSocial social, File usersStatus, File postStatuss, long timeLapse){
        this.social = social;
        this.postStatuss = postStatuss;
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

    private void backupPost() throws IOException {
        FileWriter jsonWriter = new FileWriter(postStatuss);
        ConcurrentHashMap<Integer, Post> post= social.getSocialPost();
        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gsonBuilder.toJson(post);
        //soluzione temporanea
        jsonWriter.write(jsonString);
        jsonWriter.close();
    }

    private void backupUsers() throws IOException {
        FileWriter jsonWriter = new FileWriter(usersStatus, true);
        ConcurrentHashMap<String, User> post= social.getSocialUsers();
        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gsonBuilder.toJson(post);
        //soluzione temporanea
        jsonWriter.write(jsonString);
        jsonWriter.close();
    }
}
