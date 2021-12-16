import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import com.google.gson.*;

public class WinsomeSocial implements ServerFunctions{
    private Hashtable<Integer, Post> socialPost;
    private Hashtable<String, User> socialUsers;

    public WinsomeSocial(File socialUserStatus, File winsomeStatus){
        socialPost = new Hashtable<>();
        socialUsers = new Hashtable<>();
    }

    public boolean userRegister(String username, String password, String tags){
        if(socialUsers.containsKey(username)){
            return false; //utente già registrato
        }
        try{
            socialUsers.put(username, new User(username, password, tags)); //aggiungo l'utente registrato
            return true;
        }catch(NoSuchAlgorithmException e){
            return false;
        }

    }

    public boolean login(String username, String password){
        try {
            return socialUsers.get(username).login(username, password);
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

}
