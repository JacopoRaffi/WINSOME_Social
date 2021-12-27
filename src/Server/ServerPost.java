package Server;

import Utilities.Comment;
import Utilities.FeedBack;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerPost {
    private final Long idpost;
    private final String autore;
    private final String titolo;
    private final String contenuto;
    private final long timeStamp;
    private int numIterazioni;
    private Hashtable<String, LinkedList<Comment>> comments;
    private LinkedList<FeedBack> likes;
    private Lock postLock;

    public ServerPost(Long idpost, String titolo, String contenuto, String autore){
        numIterazioni = 0;
        postLock = new ReentrantLock();
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

    protected int addGetNumIterazioni(){
        numIterazioni++;
        return numIterazioni;
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
