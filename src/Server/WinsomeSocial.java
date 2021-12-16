package Server;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import Server.Post;
import Server.ServerFunctions;
import Server.User;

public class WinsomeSocial implements ServerFunctions {
    private ConcurrentHashMap<Integer, Post> socialPost;
    private ConcurrentHashMap<String, User> socialUsers;
    private volatile AtomicLong postID;

    public WinsomeSocial(File usersStatus){
        socialPost = new ConcurrentHashMap<>();
        socialUsers = new ConcurrentHashMap<>();
        postID = new AtomicLong(0);
    }

    public boolean userRegister(String username, String password, String tags){
        try{
            User newUser = new User(username, password, tags);
            if(socialUsers.putIfAbsent(username, newUser) == null){ //aggiungo l'utente registrato
                return true;
            }else{
                return false;
            }
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