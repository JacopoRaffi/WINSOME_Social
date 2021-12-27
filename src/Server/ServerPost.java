package Server;

import Utilities.Comment;
import Utilities.FeedBack;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.LinkedList;

public class ServerPost {
    private Long idpost;
    private String autore;
    private String titolo;
    private String contenuto;
    private long timeStamp;
    Hashtable<String, LinkedList<Comment>> comments;
    LinkedList<FeedBack> likes;

    public ServerPost(Long idpost, String titolo, String contenuto, String autore){
        this.contenuto = contenuto;
        this.autore = autore;
        this.idpost = idpost;
        this.titolo = titolo;
        comments = new Hashtable<>();
        likes = new LinkedList<>();
        timeStamp = Calendar.getInstance().getTimeInMillis(); //serve sapere per il calcolo delle ricompense
    }

    public synchronized Long getIdpost() {
        return idpost;
    }

    public synchronized Hashtable<String, LinkedList<Comment>> getComments() {
        return (Hashtable<String, LinkedList<Comment>>)comments.clone();
    }

    public synchronized LinkedList<FeedBack> getLikes() {
        return (LinkedList<FeedBack>)likes.clone();
    }

    public synchronized void addComment(String contenuto, String autore){
        LinkedList<Comment> comm = comments.get(autore);
        comm.add((new Comment(autore, contenuto)));
        comments.replace(autore, comm);
    }

    public synchronized boolean ratePost(String autore, Integer voto){
        if (voto > 0){
            return likes.add(new FeedBack(autore, true, Calendar.getInstance().getTimeInMillis()));
        }else{
            return likes.add(new FeedBack(autore, false, Calendar.getInstance().getTimeInMillis()));
        }
    }

    public String getAutore(){
        return autore;
    }
}
