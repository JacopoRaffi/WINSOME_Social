import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

public class ServerPost {
    private final Long idpost;
    private final String autore;
    private final String titolo;
    private final String contenuto;
    private int numIterazioni;
    private final Hashtable<String, LinkedList<Comment>> comments;
    private final LinkedList<FeedBack> likes;
    private final ReentrantLock[] locks;
    private long lastTimeReward;

    public ServerPost(Long idpost, String titolo, String contenuto, String autore){
        numIterazioni = 0;
        locks = new ReentrantLock[2];
        locks[0] = new ReentrantLock();
        locks[1] = new ReentrantLock();
        this.contenuto = contenuto;
        this.autore = autore;
        this.idpost = idpost;
        this.titolo = titolo;
        lastTimeReward = System.nanoTime();
        comments = new Hashtable<>();
        likes = new LinkedList<>();
    }

   public ServerPost(Long idpost, Long reward, String titolo, String contenuto, String autore, Hashtable<String, LinkedList<Comment>> comm, LinkedList<FeedBack> likes, int numIt){
        numIterazioni = numIt;
        locks = new ReentrantLock[2];
        locks[0] = new ReentrantLock();
        locks[1] = new ReentrantLock();
        this.contenuto = contenuto;
        this.autore = autore;
        this.idpost = idpost;
        this.titolo = titolo;
        lastTimeReward = reward;
        comments = comm;
        this.likes = likes;
    }

    public void lock(int index){
        //0->likes, 1->comments
        locks[index].lock();
    }

    public void unlock(int index){
        //0->likes, 1->comments
        locks[index].unlock();
    }

    public Long getIdpost() {
        return idpost;
    }

    public Hashtable<String, LinkedList<Comment>> getComments() {
        return comments;
    }

    public int addGetNumIterazioni(){
        numIterazioni++;
        return numIterazioni;
    }

    public int getNumIterazioni() {
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

    public boolean ratePost(String autore, Integer voto){
        FeedBack feedback;
        if (voto > 0){
            feedback = new FeedBack(autore, true, System.nanoTime());
        }else{
            feedback = new FeedBack(autore, false, System.nanoTime());
        }
        if(!likes.contains(feedback))
            return likes.add(feedback);
        return false;
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

    public void setLastTimeReward(long newTime){
        lastTimeReward = newTime;
    }

    public long getLastTimeReward(){
        return lastTimeReward;
    }

    @Override
    public String toString(){
        return "AUTORE: " + autore + "\n" + "TITOLO: " + titolo + "\n";
    }
}
