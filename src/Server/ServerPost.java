package Server;

import Utilities.Comment;
import Utilities.FeedBack;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServerPost {
    private final Long idpost;
    private final String autore;
    private final String titolo;
    private final String contenuto;
    private int numIterazioni;
    private final Hashtable<String, LinkedList<Comment>> comments;
    private final LinkedList<FeedBack> likes;

    public ServerPost(Long idpost, String titolo, String contenuto, String autore){
        numIterazioni = 0;
        ReentrantReadWriteLock auxLock = new ReentrantReadWriteLock();
        this.contenuto = contenuto;
        this.autore = autore;
        this.idpost = idpost;
        this.titolo = titolo;
        comments = new Hashtable<>();
        likes = new LinkedList<>();
    }

    public Long getIdpost() {
        return idpost;
    }

    public Hashtable<String, LinkedList<Comment>> getComments() {
        return comments;
    }

    protected int addGetNumIterazioni(){
        numIterazioni++;
        return numIterazioni;
    }

    public LinkedList<FeedBack> getLikes() {
        return likes;
    }

    public void addComment(String contenuto, String autore){
        LinkedList<Comment> comm = comments.get(autore);
        comm.add((new Comment(autore, contenuto)));
        comments.replace(autore, comm);
    }

    public void ratePost(String autore, Integer voto){
        if (voto > 0){
            likes.add(new FeedBack(autore, true, Calendar.getInstance().getTimeInMillis()));
        }else{
            likes.add(new FeedBack(autore, false, Calendar.getInstance().getTimeInMillis()));
        }
    }

    public String getAutore(){
        return autore;
    }

    public String getTitolo(){
        return titolo;
    }

    public String getContenuto(){
        return contenuto;
    }
}
