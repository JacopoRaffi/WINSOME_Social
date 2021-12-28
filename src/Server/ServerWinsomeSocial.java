package Server;

import Exceptions.IllegalRegisterException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ServerWinsomeSocial extends RemoteObject implements ServerRegistryInterface {
    private ConcurrentHashMap<Long, ServerPost> socialPost;
    private ConcurrentHashMap<String, ServerUser> socialUsers;
    private final ConcurrentHashMap<String, ClientNotifyInterface> usersCallbacks; //associo ad uno username la sua Interface così so a chi inviare la notifica
    private volatile AtomicLong postID;

    public ServerWinsomeSocial(){
        super();
        usersCallbacks = new ConcurrentHashMap<>();
        socialPost = new ConcurrentHashMap<>();
        socialUsers = new ConcurrentHashMap<>();
        postID = new AtomicLong(0);
    }

    public boolean userRegister(String username, String password, String tags) throws RemoteException {
        if(password.length() > 16 || password.length() < 8){
            throw new IllegalRegisterException();
        }
        try{
            ServerUser newUser = new ServerUser(username, password, tags);
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
        List<String> auxList;
        try{
            if(!user.comparePassword(password)){
                return new LinkedList<>();
            }
        }catch(NoSuchAlgorithmException e){
            return null;
        }
        try {
            user.lock(1);
            auxList = new LinkedList<>(user.getFollowers());
        }finally{
            user.unlock(1);
        }
        return auxList;
    }

    public boolean login(String username, String password){
        try {
            return (socialUsers.containsKey(username) && socialUsers.get(username).login(username, password));
        }catch(NoSuchAlgorithmException e){
            return false;
        }
    }

    public String listFollowed(String username){
        String aux = "";
        ServerUser user = socialUsers.get(username);
        aux = user.getFollowed().toString();
        return aux;
    }

    public String listUsers(String[] tags, String username){
        Set<String> commonTagsUsers = new TreeSet<>();
        for (ServerUser user : socialUsers.values()) {
            if(username.compareTo(user.getUsername()) != 0) {
                Iterator<String> it = Arrays.stream(tags).iterator();
                while (it.hasNext()) {
                    if (Arrays.asList(user.getTags()).contains(it.next())) {
                        commonTagsUsers.add(user.getUsername());
                    }
                }
            }
        }
        return commonTagsUsers.toString();
    }

    public String showFeed(String username){
        ServerUser user = socialUsers.get(username);
        String aux = "";
        try{
            user.lock(0);
            aux = user.getFeed().toString();
        }finally{
            user.unlock(0);
        }
        return aux;
    }

    public String showBlog(String username){
        ServerUser user = socialUsers.get(username);
        return user.getBlog().toString();
    }

    public ConcurrentHashMap<String, ServerUser> getSocialUsers(){
        return new ConcurrentHashMap<>(socialUsers); //restituisco una copia per il calcolo delle ricompense
    }

    //metodi utili per il backup(passo il riferimento e non la copia dell'oggetto)
    public ConcurrentHashMap<String, ServerUser> getBackupUsers(){
        return socialUsers;
    }

    public ConcurrentHashMap<Long, ServerPost> getBackupPost(){
        return socialPost;
    }

    //questi due metodi set sono utili per l'avvio del server(ripristinare tutto dal backup)
    public void setSocialUsers(ConcurrentHashMap<String, ServerUser> mapUser){
        this.socialUsers = mapUser;
    }

    public void setSocialPost(ConcurrentHashMap<Long, ServerPost> mapPost){
        this.socialPost = mapPost;
        if(!socialPost.keySet().isEmpty())
            this.postID = new AtomicLong(Collections.max(socialPost.keySet()));
    }

    public synchronized void registerForCallback (ClientNotifyInterface ClientInterface, String username, String password) throws RemoteException, NoSuchAlgorithmException{
        ServerUser user = socialUsers.get(username);
        if (user.comparePassword(password))
            usersCallbacks.putIfAbsent(username, ClientInterface);
    }

    public synchronized void unregisterForCallback (ClientNotifyInterface ClientInterface, String username, String password) throws RemoteException, NoSuchAlgorithmException{
        ServerUser user = socialUsers.get(username);
        if (!user.comparePassword(password))
            return;
        else
            usersCallbacks.remove(username, ClientInterface);
        System.out.println(usersCallbacks.keySet());
    }

    public synchronized boolean doCallbackFollow(String usernameFollowed) {
        ClientNotifyInterface client = usersCallbacks.get(usernameFollowed);
        try{
            client.notifyNewFollow(usernameFollowed);
            return true;
        }catch(RemoteException e){
            return false;
        }catch(NullPointerException e){
            return true;
        }
    }

    public synchronized boolean doCallbackUnfollow(String usernameUnfollowed){
        ClientNotifyInterface client = usersCallbacks.get(usernameUnfollowed);
        try{
            client.notifyNewUnfollow(usernameUnfollowed);
            return true;
        }catch(RemoteException e){
            return false;
        }catch(NullPointerException e){
            return true;
        }
    }

    public boolean followUser(String username, String followed){
        ServerUser user = socialUsers.get(username);
        ServerUser userFollowed = socialUsers.get(followed);
        boolean seguito = false;
        try{
            userFollowed.lock(1);//aggiunge il follower
            seguito = user.addFollowed(followed);
            userFollowed.addFollower(username);
            for (ServerPost post : userFollowed.getBlog().values()) {
                user.addPostFeed(post);
            }
            doCallbackFollow(followed); //notifico l'utente interessato
        }finally{
            userFollowed.unlock(1);
        }
        return seguito;
    }

    public boolean unFollowUser(String username, String followed){
        ServerUser user = socialUsers.get(username);
        ServerUser userFollowed = socialUsers.get(followed);
        boolean seguito = false;
        try{
            userFollowed.lock(1);//rimuove il follower
            seguito = user.removeFollowed(followed);
            userFollowed.removeFollower(username);
            for (ServerPost post : userFollowed.getBlog().values()) {
                user.removePostFeed(post.getIdpost());
            }
            doCallbackUnfollow(followed); //notifico l'utente interessato
        }finally{
            userFollowed.unlock(1);
        }
        return seguito;
    }

    public double toBitcoin (double wincoins) throws IOException {
        //voglio un numero decimale per evitare che i wincoin valgano più dei bitcoin
        URL url = new URL("https://www.random.org/decimal-fractions/?num=1&dec=10&col=2&format=plain&rnd=new");
        InputStream urlReader = url.openStream();
        BufferedReader buffReader = new BufferedReader(new InputStreamReader(urlReader));
        String line;
        line = buffReader.readLine();

        return Double.parseDouble(line) * wincoins;
    }

    public boolean createPost(String autore, String titolo, String contenuto){
        long id = postID.addAndGet(1);
        ServerPost newPost = new ServerPost(id, titolo, contenuto, autore);
        if(socialPost.putIfAbsent(id, newPost) == null){ //aggiungo l'utente registrato
            socialUsers.get(autore).addPostBlog(newPost);
            System.out.println("NUOVO POST CREATO: " + titolo);
            for (String key : socialUsers.get(autore).getFollowers()) {
                ServerUser auxUser = socialUsers.get(key);
                try {
                    auxUser.lock(0);
                    auxUser.addPostFeed(newPost);
                }finally{
                    auxUser.unlock(0);
                }
            }
            return true;
        }else{
            return false;
        }
    }

    public boolean deletePost(Long idPost, String username){//per evitare il rischio che qualcuno commenti il post mentre viene cancellato
        if (socialPost.remove(idPost) != null) {
            ServerUser user = socialUsers.get(username);
            user.removePostBlog(idPost);
            for (String key : user.getFollowers()) {
                ServerUser auxUser = socialUsers.get(key);
                try {
                    auxUser.lock(0);
                    auxUser.removePostFeed(idPost);
                }finally{
                    auxUser.unlock(0);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean rewinPost(Long idpost, String username){
        ServerUser user = socialUsers.get(username);
        boolean seguito = false;
        boolean rewin = false;
        try{
            user.lock(0);
            if(user.getFeed().containsKey(idpost)){
                seguito = true;
                rewin = user.addPostBlog(socialPost.get(idpost));
            }
        }finally{
            user.unlock(0);
        }
        return (rewin && seguito);
    }

    public String showPost(Long idpost){
        ServerPost post;
            if ((post = socialPost.get(idpost)) == null) {
                return null;
            } else {
                return "" + post.getIdpost() + ", AUTORE: " + post.getAutore() + "\n" + "TITOLO: " + post.getTitolo() +
                        "\n" + post.getContenuto();
            }
    }
}
