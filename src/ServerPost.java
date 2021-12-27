import Utilities.Comment;
import Utilities.FeedBack;

import java.util.Calendar;
import java.util.LinkedList;

public class ServerPost {
    private Long idpost;
    private String autore;
    private String titolo;
    private String contenuto;
    private long timeStamp;
    LinkedList<Comment> comments;
    LinkedList<FeedBack> likes;

    public ServerPost(Long idpost, String titolo, String contenuto, String autore){
        this.contenuto = contenuto;
        this.autore = autore;
        this.idpost = idpost;
        this.titolo = titolo;
        comments = new LinkedList<>();
        likes = new LinkedList<>();
        timeStamp = Calendar.getInstance().getTimeInMillis(); //serve sapere per il calcolo delle ricompense
    }

    public synchronized Long getIdpost() {
        return idpost;
    }

    public synchronized LinkedList<Comment> getComments() {
        return (LinkedList<Comment>)comments.clone();
    }

    public synchronized LinkedList<FeedBack> getLikes() {
        return (LinkedList<FeedBack>)likes.clone();
    }

    public synchronized boolean addComment(String contenuto, String autore){
        return comments.add(new Comment(autore, contenuto));
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
