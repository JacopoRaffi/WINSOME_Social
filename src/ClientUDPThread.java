import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

//questo thread sta in ascolto(tramite multicast) della notifica di avvenuta modifica del suo "wallet"
public class ClientUDPThread implements Runnable{
    private MulticastSocket msocket;
    private static boolean logged = false;


    public ClientUDPThread(MulticastSocket msocket){
        this.msocket = msocket;
    }

    public static void login(){
        logged = true;
    }

    public static void logout(){
        logged = false;
    }

    public void run(){
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while(!Thread.currentThread().isInterrupted()){
            try{
                msocket.receive(packet);
                String message = new String(buffer, StandardCharsets.UTF_8);
                message = message.replace("\u0000", "");
                if(logged)
                    System.out.println("< NOTIFICA: " + message + " >");
            }catch(IOException ex){
                System.err.println("< ERRORE: PROBLEMA COL MULTICAST");
                continue;
            }
        }
    }
}
