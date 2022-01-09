import java.util.Calendar;

public class ServerComment extends ServerFeedBack {
    String contenuto;

    public ServerComment(String autore, String contenuto){
        super(autore, true, System.nanoTime());
        this.contenuto = contenuto;
    }

    public String getContenuto(){
        return contenuto;
    }

    @Override
    public String toString(){
        return "Commento: " + contenuto + "\n" + getTime();
    }
}
