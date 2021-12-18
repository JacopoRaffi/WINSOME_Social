package Server;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

//questo thread salva lo stato del social periodicamente
public class AutomaticSaving implements Runnable{
    private File postStatus;
    private File usersStatus;
    private WinsomeSocial social;
    private final long timelapse;

    public AutomaticSaving(WinsomeSocial social, File usersStatus, File postStatuss, long timeLapse){
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

    private void backupPost() throws IOException {
        FileWriter writer = new FileWriter(postStatus);
        Gson builder = new GsonBuilder().setPrettyPrinting().create();
        writer.write(builder.toJson(social.getSocialPost()));
        writer.close();
    }

    private void backupUsers() throws IOException {
        FileWriter writer = new FileWriter(usersStatus);
        Gson builder = new GsonBuilder().setPrettyPrinting().create();
        writer.write(builder.toJson(social.getSocialUsers()));
        writer.close();
    }
}
