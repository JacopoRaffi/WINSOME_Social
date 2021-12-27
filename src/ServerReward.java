import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

public class ServerReward extends Thread {
    private DatagramSocket socketUDP;
    private ServerWinsomeSocial social;
    private InetAddress address;
    private int port;
    private long timeout;


    public ServerReward(ServerWinsomeSocial social, long timeout, DatagramSocket socketUDP, InetAddress address, int port){
        this.socketUDP = socketUDP;
        this.address = address;
        this.port = port;
        this.social = social;
        this.timeout = timeout;
    }

    public void run(){
        while(!Thread.currentThread().isInterrupted()){
            try{
                Thread.sleep(timeout);
                calcoloRicompense();
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
        long startTimeMillis = Calendar.getInstance().getTimeInMillis();
        for (ServerUser user: social.getSocialUsers().values()) {
            for (ServerPost post: user.getBlog().values()) {
                if(post.getAutore().compareTo(user.getUsername()) == 0){ //usata per evitare di fare calcoli su post rewined
                    
                }
            }
        }
    }

}
