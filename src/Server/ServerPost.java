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

    public ServerPost(ServerPost post){ //mi serve per fare la deepCopy dei post(utile per backup e calcolo ricompense)
        idpost = getIdpost();
        autore = post.autore;
        titolo = post.titolo;
        contenuto = post.contenuto;
        likes = new LinkedList<>();
        for (FeedBack like:post.getLikes()) {
            likes.add(like);
        }
        comments = new Hashtable<>();
        Hashtable<String, LinkedList<Comment>> aux = post.getComments();
        for (String key : aux.keySet()) {
            comments.put(key, new LinkedList<>(aux.get(key)));
        };
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
        if(comments.get(autore) != null) {
            LinkedList<Comment> comm = comments.get(autore);
            comm.add((new Comment(autore, contenuto)));
            comments.replace(autore, comm);
        }
        else{
            LinkedList<Comment> list = new LinkedList<>();
            list.add(new Comment(autore, contenuto));
            comments.put(autore, list);
        }
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

    @Override
    public String toString(){
        return "AUTORE: " + autore + "\n" + "TITOLO: " + titolo + "\n";
    }
}
