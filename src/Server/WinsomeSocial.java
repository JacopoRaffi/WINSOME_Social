package Server;

import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class WinsomeSocial implements ServerRegistry {
    private ConcurrentHashMap<Integer, Post> socialPost;
    private ConcurrentHashMap<String, User> socialUsers;
    private volatile AtomicLong postID;

    public WinsomeSocial(){
        socialPost = new ConcurrentHashMap<>();
        socialUsers = new ConcurrentHashMap<>();
        postID = new AtomicLong(0);
    }

    public boolean userRegister(String username, String password, String tags, String userAddress) throws RemoteException {
        try{
            User newUser = new User(username, password, tags, userAddress);
            if(socialUsers.putIfAbsent(username, newUser) == null){ //aggiungo l'utente registrato
                System.out.println("NUOVO UTENTE REGISTRATO: " + username);
                return true;
            }else{
                return false;
            }
        }catch(NoSuchAlgorithmException e){
            return false;
        }

    }

    public boolean login(String username, String password, String userAddress){
        try {
            return (socialUsers.containsKey(username) && socialUsers.get(username).login(username, password, userAddress));
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
    protected ConcurrentHashMap<Integer, Post> getSocialPost(){
        return new ConcurrentHashMap<>(socialPost);
    }

    protected ConcurrentHashMap<String, User> getSocialUsers(){
        return new ConcurrentHashMap<>(socialUsers);
    }

    //questi due metodi set sono utili per l'avvio del server(ripristinare tutto dal backup)
    protected void setSocialUsers(ConcurrentHashMap<String, User> mapUser){
        this.socialUsers = mapUser;
    }

    protected void setSocialPost(ConcurrentHashMap<Integer, Post> mapPost){
        this.socialPost = mapPost;
    }

}
