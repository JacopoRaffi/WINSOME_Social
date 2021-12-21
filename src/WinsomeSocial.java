import Exceptions.IllegalRegisterException;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class WinsomeSocial extends RemoteObject implements ServerRegistryInterface {
    private ConcurrentHashMap<Integer, Post> socialPost;
    private ConcurrentHashMap<String, User> socialUsers;
    private ConcurrentHashMap<String, ClientNotifyInterface> usersCallbacks; //associo ad uno username la sua Interface così so a chi inviare la notifica
    private volatile AtomicLong postID;

    public WinsomeSocial(){
        super();
        usersCallbacks = new ConcurrentHashMap<>();
        socialPost = new ConcurrentHashMap<>();
        socialUsers = new ConcurrentHashMap<>();
        postID = new AtomicLong(0);
    }

    public boolean userRegister(String username, String password, String tags, String userAddress) throws RemoteException {
        if(password.length() > 16 || password.length() < 8){
            throw new IllegalRegisterException();
        }
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

    public synchronized void registerForCallback (ClientNotifyInterface ClientInterface, String username) throws RemoteException{
        usersCallbacks.putIfAbsent(username, ClientInterface);
        System.out.println(usersCallbacks.keySet());
    }

    public synchronized void unregisterForCallback (ClientNotifyInterface ClientInterface, String username) throws RemoteException{
        usersCallbacks.remove(username, ClientInterface);
    }

    public synchronized boolean doCallbackFollow(String usernameFollowed){
        ClientNotifyInterface client = usersCallbacks.get(usernameFollowed);
        try{
            client.notifyNewFollow(usernameFollowed);
            return true;
        }catch(RemoteException e){
            return false;
        }
    }

    public boolean doCallbackUnfollow(String usernameUnfollowed){
        ClientNotifyInterface client = usersCallbacks.get(usernameUnfollowed);
        try{
            client.notifyNewUnfollow(usernameUnfollowed);
            return true;
        }catch(RemoteException e){
            return false;
        }
    }

}
