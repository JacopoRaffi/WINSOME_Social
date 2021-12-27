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
    private ConcurrentHashMap<Long, ServerPost> feed; //la key è l'idPost
    private ConcurrentHashMap<Long, ServerPost> blog;
    private final Wallet wallet;

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
        wallet = new Wallet();
    }

    public boolean addPostBlog(ServerPost post){
        return (blog.putIfAbsent(post.getIdpost(), post) == null);
    }

    public boolean addPostFeed(ServerPost post){
        return (feed.putIfAbsent(post.getIdpost(), post) == null);
    }

    public boolean removePostFeed(Long idpost){
        return (feed.remove(idpost) != null);
    }

    public boolean removePostBlog(Long idpost){
        return (blog.remove(idpost) != null);
    }

    public synchronized boolean login(String username, String password) throws NoSuchAlgorithmException{
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

    public LinkedHashSet<String> getFollowers(){
        return (LinkedHashSet<String>) followers.clone();
    }

    public ConcurrentHashMap<Integer, ServerPost> getFeed(){
        return (ConcurrentHashMap<Integer, ServerPost>)followers.clone();
    }

    public ConcurrentHashMap<Integer, ServerPost> getBlog(){
        return (ConcurrentHashMap<Integer, ServerPost>)followers.clone();
    }

    public boolean comparePassword(String password) throws NoSuchAlgorithmException{
        return (hashedPassword.compareTo(HashFunction.bytesToHex(HashFunction.sha256(password))) == 0);
    }

    public Wallet getWallet(){
        return wallet;
    }
    @Override
    public String toString(){
        return this.username;
    }

}
