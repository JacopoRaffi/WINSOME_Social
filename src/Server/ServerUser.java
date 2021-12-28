package Server;

import Utilities.*;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerUser {
    final private String seed;
    final private String[] tags;
    final private String username;
    final private String hashedPassword;
    private final LinkedHashSet<String> followers; //la key è il nome dell'utente(username unico nel social)
    private final LinkedHashSet<String> followed;
    private final ConcurrentHashMap<Long, ServerPost> feed; //la key è l'idPost
    private final ConcurrentHashMap<Long, ServerPost> blog;
    private final Wallet wallet;
    private final Lock[] locks;

    public ServerUser(String username, String password, String tags) throws NoSuchAlgorithmException {
        byte[] arr = new byte[32];
        ThreadLocalRandom.current().nextBytes(arr);

        locks = new ReentrantLock[3];
        locks[0] = new ReentrantLock();
        locks[1] = new ReentrantLock();
        locks[2] = new ReentrantLock();
        this.seed = new String(arr, StandardCharsets.UTF_8);
        this.username = username;
        this.tags = tags.split(" ");
        hashedPassword = HashFunction.bytesToHex(HashFunction.sha256(password+seed));
        followers = new LinkedHashSet<>();
        followed = new LinkedHashSet<>();
        feed = new ConcurrentHashMap<>();
        blog = new ConcurrentHashMap<>();
        wallet = new Wallet();
    }

    public void lock(int index){
        //0->feed, 1->followers
        locks[index].lock();
    }

    public void unlock(int index){
        //0->feed, 1->followers
        locks[index].unlock();
    }

    public synchronized boolean addPostBlog(ServerPost post){
        return (blog.putIfAbsent(post.getIdpost(), post) == null);
    }

    public boolean addPostFeed(ServerPost post){
        return (feed.putIfAbsent(post.getIdpost(), post) == null);
    }

    public boolean removePostFeed(Long idpost){
        return (feed.remove(idpost) != null);
    }

    public synchronized boolean removePostBlog(Long idpost){
        return (blog.remove(idpost) != null);
    }

    public synchronized boolean login(String username, String password) throws NoSuchAlgorithmException{
        return hashedPassword.compareTo(HashFunction.bytesToHex(HashFunction.sha256(password + seed))) == 0 && (username.compareTo(this.username) == 0);
    }

    public boolean addFollower(String follower){
        return followers.add(follower);
    }

    public boolean addFollowed(String followedd){
        return followed.add(followedd);
    }

    public boolean removeFollower(String follower){
        return followers.add(follower);
    }

    public boolean removeFollowed(String followedd){
        return followed.remove(followedd);
    }

    public LinkedHashSet<String> getFollowed(){
        return followed;
    }

    public LinkedHashSet<String> getFollowers(){
        return followers;
    }

    public ConcurrentHashMap<Long, ServerPost> getFeed(){
        return feed;
    }

    public synchronized ConcurrentHashMap<Long, ServerPost> getBlog(){
        return blog;
    }

    public String[] getTags(){
        return tags;
    }

    public boolean comparePassword(String password) throws NoSuchAlgorithmException{
        return (hashedPassword.compareTo(HashFunction.bytesToHex(HashFunction.sha256(password+seed))) == 0);
    }

    public Wallet getWallet(){
        return wallet;
    }

    public String getUsername(){
        return username;
    }
    @Override
    public String toString(){
        return this.username;
    }
}
