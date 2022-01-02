public class ServerFeedBack implements Comparable<ServerFeedBack> {
    private String autore;
    private boolean positivo;
    private long timeStamp;
    public ServerFeedBack(String autore, boolean positivo, long timestamp){
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

    @Override
    public String toString(){
        String pos;
        if(positivo)
            pos = "positivo";
        else
            pos = "negativo";
        return "VOTO: " + pos + ", AUTORE: " + autore;
    }

    public int compareTo(ServerFeedBack feed){
        return (autore.compareTo(feed.getAutore()));
    }
}
