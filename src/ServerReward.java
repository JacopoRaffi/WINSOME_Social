import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ServerReward extends Thread {
    private final DatagramSocket socketUDP;
    private final ServerWinsomeSocial social;
    private final InetAddress address;
    private final int port;
    private final long timeout;
    private long lastCalculation;
    private double percentualeAutore;


    public ServerReward(ServerWinsomeSocial social, long timeout, DatagramSocket socketUDP, InetAddress address, int port, double percentualeAutore){
        lastCalculation = Calendar.getInstance().getTimeInMillis();
        this.socketUDP = socketUDP;
        this.address = address;
        this.port = port;
        this.social = social;
        this.timeout = timeout;
        this.percentualeAutore = percentualeAutore;
    }

    public void run(){
        while(!Thread.currentThread().isInterrupted()){
            try{
                Thread.sleep(timeout * 1000); //da secondi porto a millisecondi
                calcoloRicompense();
                lastCalculation = Calendar.getInstance().getTimeInMillis(); //aggiorno la data dell'ultimo calcolo
                byte[] buffer = "RICOMPENSA AGGIORNATA".getBytes(StandardCharsets.UTF_8);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                try{
                    socketUDP.send(packet);
                }catch(IOException ex){
                    System.err.println("ERRORE: MESSAGGIO MULTICAST NON INVIATO");
                    continue;
                }
            }catch(InterruptedException e){
                break;
            }
        }
    }

    private void calcoloRicompense(){
        ConcurrentHashMap<Long, ServerPost> copyPost = social.getSocialPost();
        Iterator<Long> iterator = social.getSocialPost().keySet().iterator(); //non lancia la concurrent exception
        while(iterator.hasNext()) {
            double guadagnoTotale = 0;
            ServerPost post = copyPost.get(iterator.next());
            String autore = post.getAutore();
            Set<String> curatori = new TreeSet<>(); //voglio i curatori per aumentare la loro ricompensa(Set perchè non voglio ripetere gli stessi curatori)
            guadagnoTotale += guadagno(post, curatori); //calcola il guadagno derivato da un post

            int dimCuratori = curatori.size() == 0 ? 1 : curatori.size(); //serve per evitare di dividere per 0
            double percCuratori = guadagnoTotale * (1 - percentualeAutore) / dimCuratori;
            double percAutore = guadagnoTotale * percentualeAutore;

            for (String cur:curatori) {
                social.getSocialUsers().get(cur).getWallet().addTransazione(percCuratori + ", " + Calendar.getInstance().getTime());
                social.getSocialUsers().get(cur).getWallet().addIncremento(percCuratori);
            }
            social.getSocialUsers().get(autore).getWallet().addIncremento(percAutore);
            social.getSocialUsers().get(autore).getWallet().addTransazione(percAutore + ", " + Calendar.getInstance().getTime());
        }
    }
    private double guadagno(ServerPost post, Set<String> curatori){
        //usata per evitare di fare calcoli su post rewined
        double somma1 = 0;
        double somma2 = 0;
        double sommaAux = 0;
        double guadagnoPost = 0;
        int numIterazioni = post.addGetNumIterazioni();
        try {
            post.lock(0);
            post.lock(1);
            List<FeedBack> filteredFeedback = post.getLikes().stream().filter(feedback -> feedback.getTime() > post.getLastTimeReward()).collect(Collectors.toList());
            int numPositivi = 0;
            for (FeedBack feedback : filteredFeedback) {
                if (feedback.isPositivo()) {
                    numPositivi++;
                    curatori.add(feedback.getAutore()); //aggiungo il curatore che ha messo il voto positivo
                } else {
                    numPositivi--;
                }
            }
            somma1 = Math.log(Math.max(0, numPositivi) + 1);
            Hashtable<String, LinkedList<Comment>> comments = post.getComments();
            //le key sono gli autori
            for (String key : filterNewPeopleCommenting(post)) {
                int Cp = comments.get(key).size();
                sommaAux += 2 / (1 + Math.pow(Math.E, -Cp + 1));
            }
            somma2 = Math.log(somma2 + 1);
            guadagnoPost = (somma1 + somma2) / numIterazioni;
        }finally{
            post.setLastTimeReward(System.nanoTime());
            post.unlock(0);
            post.unlock(1);
        }

        return guadagnoPost;
    }

    private Set<String> filterNewPeopleCommenting(ServerPost post){
        Set<String> newPeople = new TreeSet<>(); //qui metto le persone che avranno commentato di recente

        for (String key: post.getComments().keySet()) {
            List<Comment> filteredComment = post.getComments().get(key).stream().filter(comment -> comment.getTime() > post.getLastTimeReward()).collect(Collectors.toList());
            if(!filteredComment.isEmpty()){
                newPeople.add(key);
            }
        }
        return newPeople;
    }
}
