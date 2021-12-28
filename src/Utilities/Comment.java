package Utilities;

import java.util.Calendar;

public class Comment extends FeedBack{
    String contenuto;

    public Comment(String autore, String contenuto){
        super(autore, true, Calendar.getInstance().getTimeInMillis());
        this.contenuto = contenuto;
    }

    public String getContenuto(){
        return contenuto;
    }

    @Override
    public String toString(){
        return "COMMENTO: " + contenuto;
    }
}
