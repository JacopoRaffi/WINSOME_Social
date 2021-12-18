package Server;

import Utilities.*;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.concurrent.ThreadLocalRandom;

public class User {
    final private String seed;
    final private String[] tags;
    final private String username;
    final private String hashedPassword;
    private Hashtable<String, User> followers; //la key è il nome dell'utente(username unico nel social)
    private Hashtable<String, User> followed;
    private Hashtable<Integer, Post> feed; //la key è l'idPost
    private Hashtable<Integer, Post> blog;

    public User(String username, String password, String tags) throws NoSuchAlgorithmException {
        byte[] arr = new byte[32];
        ThreadLocalRandom.current().nextBytes(arr);
        this.seed = new String(arr, StandardCharsets.UTF_8);
        this.username = username;
        this.tags = tags.split(" ");
        hashedPassword = HashFunction.bytesToHex(HashFunction.sha256(password));
        followers = new Hashtable<>();
        followed = new Hashtable<>();
        feed = new Hashtable<>();
        blog = new Hashtable<>();
    }

    public boolean login(String username, String password) throws NoSuchAlgorithmException{
        return (hashedPassword.compareTo(HashFunction.bytesToHex(HashFunction.sha256(password))) == 0 && (username.compareTo(this.username) == 0));
    }

    public Hashtable<String, User> getFollower(){
        return (Hashtable<String, User>)followers.clone();
    }

    public Hashtable<String, User> getFollowed(){
        return (Hashtable<String, User>)followed.clone();
    }

    public Hashtable<String, User> getFeed(){
        return (Hashtable<String, User>)followers.clone();
    }

    public Hashtable<String, User> getBlog(){
        return (Hashtable<String, User>)followers.clone();
    }

    @Override
    public String toString(){
        return this.username;
    }

}
