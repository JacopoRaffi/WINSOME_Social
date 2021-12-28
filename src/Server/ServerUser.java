package Server;

import Utilities.*;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServerUser {
    final private String seed;
    final private String[] tags;
    final private String username;
    final private String hashedPassword;
    private boolean logged;
    private final LinkedHashSet<String> followers; //la key è il nome dell'utente(username unico nel social)
    private final LinkedHashSet<String> followed;
    private final ConcurrentHashMap<Long, ServerPost> feed; //la key è l'idPost
    private final ConcurrentHashMap<Long, ServerPost> blog;
    private final Wallet wallet;
    private final ReentrantReadWriteLock[] locks; //lock per feed, blog, followers, followed(messe in un array per avere codice pulito)
    private final Lock[][] RWlocks;

    public ServerUser(String username, String password, String tags) throws NoSuchAlgorithmException {
        byte[] arr = new byte[32];
        ThreadLocalRandom.current().nextBytes(arr);
        this.logged = false;
        locks = new ReentrantReadWriteLock[4];
        RWlocks = new Lock[2][4];
        //righe: 0->readLock, 1->writeLock
        //colonne: 0->feed, 1->blog, 2->followers, 3->followed
        RWlocks[0][0] = locks[0].readLock(); //feed read
        RWlocks[1][0] = locks[0].writeLock();//feed write

        RWlocks[0][1] = locks[0].readLock(); //blog read
        RWlocks[1][1] = locks[0].writeLock();//blog write

        RWlocks[0][2] = locks[0].readLock(); //followers read
        RWlocks[1][2] = locks[0].writeLock();//followers write

        RWlocks[0][3] = locks[0].readLock(); //followed read
        RWlocks[1][3] = locks[0].writeLock();//followed write
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

    public void lock(int riga, int colonna){
        //righe: 0->readLock, 1->writeLock
        //colonne: 0->feed, 1->blog, 2->followers, 3->followed
        RWlocks[riga][colonna].lock();
    }

    public void unlock(int riga, int colonna){
        //righe: 0->readLock, 1->writeLock
        //colonne: 0->feed, 1->blog, 2->followers, 3->followed
        RWlocks[riga][colonna].unlock();
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
        if (hashedPassword.compareTo(HashFunction.bytesToHex(HashFunction.sha256(password+seed))) == 0 && (username.compareTo(this.username) == 0)) {
            logged = true;
            return true;
        }
        return false;
    }

    public boolean isLogged(){
        return logged;
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

    public ConcurrentHashMap<Long, ServerPost> getBlog(){
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
