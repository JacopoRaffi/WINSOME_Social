import Exceptions.IllegalRegisterException;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ServerWinsomeSocial extends RemoteObject implements ServerRegistryInterface {
    private ConcurrentHashMap<Integer, ServerPost> socialPost;
    private ConcurrentHashMap<String, ServerUser> socialUsers;
    private ConcurrentHashMap<String, ClientNotifyInterface> usersCallbacks; //associo ad uno username la sua Interface così so a chi inviare la notifica
    private volatile AtomicLong postID;

    public ServerWinsomeSocial(){
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
            ServerUser newUser = new ServerUser(username, password, tags, userAddress);
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

    public List<String> backUpFollowers(String username, String password) throws RemoteException{
        ServerUser user = socialUsers.get(username);
        try{
            if(!user.comparePassword(password)){
                return new LinkedList<>();
            }
        }catch(NoSuchAlgorithmException e){
            return null;
        }
        return socialUsers.get(username).getFollowers().stream().toList();
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
    protected ConcurrentHashMap<Integer, ServerPost> getSocialPost(){
        return new ConcurrentHashMap<>(socialPost);
    }

    protected ConcurrentHashMap<String, ServerUser> getSocialUsers(){
        return new ConcurrentHashMap<>(socialUsers);
    }

    //questi due metodi set sono utili per l'avvio del server(ripristinare tutto dal backup)
    protected void setSocialUsers(ConcurrentHashMap<String, ServerUser> mapUser){
        this.socialUsers = mapUser;
    }

    protected void setSocialPost(ConcurrentHashMap<Integer, ServerPost> mapPost){
        this.socialPost = mapPost;
    }

    public synchronized void registerForCallback (ClientNotifyInterface ClientInterface, String username, String password) throws RemoteException, NoSuchAlgorithmException{
        ServerUser user = socialUsers.get(username);
        if (!user.comparePassword(password))
            return;
        else
            usersCallbacks.putIfAbsent(username, ClientInterface);
        System.out.println(usersCallbacks.keySet());
    }

    public synchronized void unregisterForCallback (ClientNotifyInterface ClientInterface, String username, String password) throws RemoteException, NoSuchAlgorithmException{
        ServerUser user = socialUsers.get(username);
        if (!user.comparePassword(password))
            return;
        else
            usersCallbacks.remove(username, ClientInterface);
        System.out.println(usersCallbacks.keySet());
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
