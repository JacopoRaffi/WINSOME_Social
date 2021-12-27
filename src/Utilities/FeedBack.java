package Utilities;

public class FeedBack {
    private String autore;
    private boolean positivo;
    private long timeStamp;
    public FeedBack(String autore, boolean positivo, long timestamp){
        this.autore = autore;
        this.positivo = positivo;
        this.timeStamp = timestamp;
    }

    public boolean isPositivo(){
        return positivo;
    }

    public String getAutore(){
        return autore;
    }

    public long getTime(){
        return timeStamp;
    }
}
