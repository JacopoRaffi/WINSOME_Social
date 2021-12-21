import Utilities.*;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class ServerUser {
    final private String seed;
    final private String[] tags;
    final private String username;
    final private String hashedPassword;
    private boolean logged;
    private LinkedHashSet<String> followers; //la key è il nome dell'utente(username unico nel social)
    private LinkedHashSet<String> followed;
    private ConcurrentHashMap<Integer, ServerPost> feed; //la key è l'idPost
    private ConcurrentHashMap<Integer, ServerPost> blog;

    public ServerUser(String username, String password, String tags, String userAddress) throws NoSuchAlgorithmException {
        byte[] arr = new byte[32];
        ThreadLocalRandom.current().nextBytes(arr);
        this.logged = false;
        this.seed = new String(arr, StandardCharsets.UTF_8);
        this.username = username;
        this.tags = tags.split(" ");
        hashedPassword = HashFunction.bytesToHex(HashFunction.sha256(password));
        followers = new LinkedHashSet<>();
        followed = new LinkedHashSet<>();
        feed = new ConcurrentHashMap<>();
        blog = new ConcurrentHashMap<>();
    }

    public boolean login(String username, String password) throws NoSuchAlgorithmException{
        if (hashedPassword.compareTo(HashFunction.bytesToHex(HashFunction.sha256(password))) == 0 && (username.compareTo(this.username) == 0)) {
            logged = true;
            return true;
        }
        return false;
    }

    public boolean isLogged(){
        return logged;
    }

    public LinkedHashSet<String> getFollowed(){
        return (LinkedHashSet<String>) followed.clone();
    }

    public ConcurrentHashMap<Integer, ServerPost> getFeed(){
        return (ConcurrentHashMap<Integer, ServerPost>)followers.clone();
    }

    public ConcurrentHashMap<Integer, ServerPost> getBlog(){
        return (ConcurrentHashMap<Integer, ServerPost>)followers.clone();
    }

    @Override
    public String toString(){
        return this.username;
    }

}
