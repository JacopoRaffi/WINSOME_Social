import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;

public class WinsomeSocial implements ServerFunctions{
    private ConcurrentHashMap<Integer, Post> socialPost;
    private ConcurrentHashMap<String, User> socialUsers;
    private volatile AtomicLong postID;

    public WinsomeSocial(File usersStatus){
        socialPost = new ConcurrentHashMap<>();
        socialUsers = new ConcurrentHashMap<>();
        postID = new AtomicLong(0);
    }

    public boolean userRegister(String username, String password, String tags){
        if(socialUsers.containsKey(username)){
            return false; //utente già registrato
        }
        try{
            User newUser = new User(username, password, tags);
            socialUsers.put(username, newUser); //aggiungo l'utente registrato
            return true;
        }catch(NoSuchAlgorithmException e){
            return false;
        }

    }

    public boolean login(String username, String password){
        try {
            return (socialUsers.containsKey(username) && socialUsers.get(username).login(username, password));
        }catch(NoSuchAlgorithmException e){
            return false;
        }
    }

    public String listFollowed(String username){
        return socialUsers.get(username).getFollowed().toString();
    }

    public String showFeed(String username){
        return socialUsers.get(username).getFeed().toString();
    }

    public String showBlog(String username){
        return  socialUsers.get(username).getBlog().toString();
    }

    //questi due metodi get sono utili per il backup su file json
    public ConcurrentHashMap<Integer, Post> getSocialPost(){
        return new ConcurrentHashMap<>(socialPost);
    }

    public ConcurrentHashMap<String, User> getSocialUsers(){
        return new ConcurrentHashMap<>(socialUsers);
    }

}
