package Server;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import Utilities.FeedBack;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

import javax.xml.stream.events.Comment;

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
    protected synchronized void backupUser() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(usersStatus)));

        Iterator<String> it = social.getSocialUsers().keySet().iterator();
        writer.beginArray();
        while(it.hasNext()){
            ServerUser user = social.getSocialUsers().get(it.next());
            writeUser(writer, usersStatus, user, gson);
        }
        writer.endArray();
        writer.close();
    }

    protected synchronized void backupPost() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(postStatus)));

        Iterator<Long> it = social.getSocialPost().keySet().iterator();
        writer.beginArray();
        while(it.hasNext()){
            ServerPost post = social.getSocialPost().get(it.next());
            writePost(writer, postStatus, post, gson);
        }
        writer.endArray();
        writer.close();
    }

    private void writePost(JsonWriter writer, File postFile, ServerPost post, Gson gson) throws IOException{
        writer.beginObject();
        writer.name("idpost").value(post.getIdpost());
        writer.name("autore").value(post.getAutore());
        writer.name("titolo").value(post.getTitolo());
        writer.name("contenuto").value(post.getContenuto());
        writer.name("numIterazioni").value(post.getNumIterazioni());
        Type typeOfComments = new TypeToken<Hashtable<String, Comment>>() {}.getType();
        try {
            post.lock(1);
            writer.name("comments").value(gson.toJson(post.getComments(), typeOfComments));
        }finally{
            post.unlock(1);
        }
        Type typeOfLikes = new TypeToken<LinkedList<FeedBack>>() {}.getType();
        try {
            post.lock(0);
            writer.name("likes").value(gson.toJson(post.getLikes(), typeOfLikes));
        }finally{
            post.unlock(0);
        }
        writer.name("lastTimeReward").value(post.getLastTimeReward());
        writer.endObject();
    }

    private void writeUser(JsonWriter writer, File userFile, ServerUser user, Gson gson) throws IOException{
        writer.beginObject();
        writer.name("seed").value(user.getSeed());
        writer.name("tags").value(gson.toJson(user.getTags()));
        writer.name("username").value(user.getUsername());
        writer.name("hashedPassword").value(user.getHashedPassword());
        Type typeOfFollowers_ed = new TypeToken<LinkedHashSet<String>>() {}.getType();
        try {
            user.lock(1);
            writer.name("followers").value(gson.toJson(user.getFollowers(), typeOfFollowers_ed));
        }finally{
            user.lock(1);
        }
        writer.name("followed").value(gson.toJson(user.getFollowed(), typeOfFollowers_ed));
        Type typeOfMap = new TypeToken<ConcurrentHashMap<Long, ServerPost>>() {}.getType();
        try {
            user.lock(0);
            writer.name("feed").value(gson.toJson(user.getFeed(), typeOfMap));
        }finally{
            user.unlock(0);
        }
        try {
            user.lock(2);
            writer.name("blog").value(gson.toJson(user.getBlog(), typeOfMap));
        }finally{
            user.unlock(2);
        }
        writer.name("wallet").value(gson.toJson(user.getWallet()));
        writer.endObject();
    }
}
