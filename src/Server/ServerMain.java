package Server;

import java.io.*;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class ServerMain {
    //principali variabili(con relativi valori di default)
    private static int TCP_PORT = 6789;
    private static int UDP_PORT = 33333;
    private static int MULTICAST_PORT = 44444;
    private static int REG_PORT = 7777;
    private static String SERVER_ADDRESS = "192.168.1.2";
    private static String MULTICAST_ADDRESS = "239.255.32.32";
    private static String REG_SERVICENAME = "serverRegistry";
    private static long TIMEOUT = 100000;
    private static long TIMELAPSE = 100000;
    private static long TIMELAPSEBACKUP = 1000;
    private static double AUTHOR_RATE = 0.8;

    public static void main(String[] Args){
        File serverConfigFile;
        try {
            serverConfigFile = new File(Args[0]);
            configServer(serverConfigFile);
            System.out.println("--------CONFIGURAZIONE TERMINATA CON SUCCESSO--------");
        } catch (NullPointerException | IOException | IllegalArgumentException e) {
            restoreValues(); //durante la configurazione ci potrebbero essere degli errori a metà lavoro e quindi ripristino i valori di default
            System.out.println("--------SERVER AVVIATO CON VALORI DI DEFAULT--------");
        }
        System.out.println("VALORI DEL SERVER:");
        System.out.println("SERVER_ADDRESS = " + SERVER_ADDRESS);
        System.out.println("MULTICAST_ADDRESS = " + MULTICAST_ADDRESS);
        System.out.println("REG_SERVICENAME = " + REG_SERVICENAME);
        System.out.println("TCP_PORT = " + TCP_PORT);
        System.out.println("UDP_PORT = " + UDP_PORT);
        System.out.println("REG_PORT = " + REG_PORT);
        System.out.println("MULTICAST_PORT = " + MULTICAST_PORT);
        System.out.println("TIMEOUT_SOCKET = " + TIMEOUT);
        System.out.println("AUTHOR PERCENTAGE REWARD = " + AUTHOR_RATE*100 + "%");
        System.out.println("TIMELAPSE BETWEEN REWARDS = " + TIMELAPSE);
        System.out.println("TIMELAPSE BETWEEN BACKUPS = " + TIMELAPSEBACKUP);

        //in questi due file mi salvo il backup dei post del social e degli utenti registrati(periodicamente)
        File socialUserStatus = new File(".\\StatusServer\\usersStatus.json");
        File postStatus = new File(".\\StatusServer\\postStatus.json");
        try{
            postStatus.createNewFile();
        }catch(IOException e){
            System.err.println("ERRORE: errore durante la creazione dei file di backup del social");
            System.exit(-1);
        }
        try{
            socialUserStatus.createNewFile();
        }catch(IOException e){
            System.err.println("ERRORE: errore durante la creazione dei file di backup degli utenti");
            System.exit(-1);
        }
        WinsomeSocial socialNetwork = new WinsomeSocial(); //creo il social vero e proprio
        try{
            rebootSocial(socialNetwork, socialUserStatus, postStatus);
        }catch(FileNotFoundException e){
            System.err.println("ERRORE: errore durante il ripristino dell'ultimo backup");
            System.exit(-1);
        }

        //creo e avvio il thread che si occuperà del backup
        Thread autoSaving = new Thread(new AutomaticSaving(socialNetwork, socialUserStatus, postStatus, TIMELAPSEBACKUP));
        autoSaving.start();

        try{
            ServerRegistry stub = (ServerRegistry) UnicastRemoteObject.exportObject(socialNetwork, 0);
            LocateRegistry.createRegistry(REG_PORT);
            Registry registry = LocateRegistry.getRegistry(REG_PORT);
            registry.rebind(REG_SERVICENAME, stub);
        }catch(RemoteException e){
            e.printStackTrace();
            System.err.println("ERRORE: errore con RMI");
            System.exit(-1);
        }

        ExecutorService threadPool = Executors.newCachedThreadPool(); //pool di worker(uno per client)
        //il server main si occupa delle connessioni TCP
        while (true) {
            try {
                ServerSocket welcomeSocket = new ServerSocket(8080);
                welcomeSocket.setSoTimeout((int) TIMEOUT);

                Socket clientSocket = welcomeSocket.accept();
                try (DataInputStream inReader = new DataInputStream(clientSocket.getInputStream());
                     BufferedOutputStream outWriter = new BufferedOutputStream(clientSocket.getOutputStream())) {
                    System.out.println(clientSocket);
                    String line = inReader.readUTF();
                    System.out.println("ho letto: " + line);
                } catch (IOException e) {
                    System.err.println("ERRORE: problemi durante la lettura dal clientSocket");
                } catch (NullPointerException ne) {
                    System.err.println("ERRORE: problemi con la richiesta");
                } finally {
                    try {
                        clientSocket.close();
                        welcomeSocket.close();
                    } catch (IOException e) { //
                        System.err.println("ERRORE: errore con la chiusura dei socket");
                        System.exit(-1);
                    }
                }
            } catch (IOException ex) {
                System.err.println("ERRORE: errore con il socket");
                System.exit(-1);
            }
        }

    }

    private static void restoreValues() {
        TCP_PORT = 6666;
        UDP_PORT = 33333;
        MULTICAST_PORT = 44444;
        REG_PORT = 7777;
        String SERVER_ADDRESS = "192.168.1.2";
        String MULTICAST_ADDRESS = "239.255.32.32";
        String REG_SERVICENAME = "localhost";
        TIMEOUT = 100000;
        TIMELAPSE = 100000;
        AUTHOR_RATE = 0.8;
    }

    private static void configServer(File config) throws IOException, NumberFormatException {
        BufferedReader configReader = new BufferedReader(new FileReader(config));
        String line = configReader.readLine();
        while (line != null) {
            if(!line.contains("#") || !line.isBlank()) {
                String[] tokens = line.split("=");
                if (tokens[0].compareTo("SERVER_ADDRESS") == 0) {
                    SERVER_ADDRESS = tokens[1];
                } else if (tokens[0].compareTo("MULTICAST_ADDRESS") == 0) {
                    MULTICAST_ADDRESS = tokens[1];
                } else if (tokens[0].compareTo("REG_SERVICENAME") == 0) {
                    REG_SERVICENAME = tokens[1];
                } else if (tokens[0].compareTo("TCP_PORT") == 0) {
                    TCP_PORT = Integer.parseInt(tokens[1]);
                } else if (tokens[0].compareTo("UDP_PORT") == 0) {
                    UDP_PORT = Integer.parseInt(tokens[1]);
                } else if (tokens[0].compareTo("REG_PORT") == 0) {
                    REG_PORT = Integer.parseInt(tokens[1]);
                } else if (tokens[0].compareTo("MULTICAST_PORT") == 0) {
                    MULTICAST_PORT = Integer.parseInt(tokens[1]);
                } else if (tokens[0].compareTo("TIMEOUT") == 0) {
                    TIMEOUT = Long.parseLong(tokens[1]);
                }
                else if (tokens[0].compareTo("AUTHOR_RATE") == 0) {
                    AUTHOR_RATE = Double.parseDouble(tokens[1]);
                    if(AUTHOR_RATE >= 1 || AUTHOR_RATE <= 0){
                        throw new IllegalArgumentException();
                    }
                }
                else if (tokens[0].compareTo("TIMELAPSE") == 0) {
                    TIMELAPSE = Long.parseLong(tokens[1]);
                    if(TIMELAPSE < 0){
                        throw new IllegalArgumentException();
                    }
                }
                else if (tokens[0].compareTo("TIMELAPSEBACKUP") == 0) {
                    TIMELAPSEBACKUP = Long.parseLong(tokens[1]);
                    if(TIMELAPSEBACKUP <= 0){
                        throw new IllegalArgumentException();
                    }
                }
            }
            line = configReader.readLine();
        }
    }

    private static void rebootSocial(WinsomeSocial social, File socialUserStatus, File postStatus) throws FileNotFoundException{
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonReader reader = new JsonReader(new FileReader(socialUserStatus));
        Type typeOfMap = new TypeToken<ConcurrentHashMap<String, User>>() {}.getType();
        ConcurrentHashMap<String, User> mapUser = gson.fromJson(reader, typeOfMap);
        if(mapUser == null)
            mapUser = new ConcurrentHashMap<>();
        social.setSocialUsers(mapUser);

        JsonReader readerPost = new JsonReader(new FileReader(postStatus));
        Type typeOfMapPost = new TypeToken<ConcurrentHashMap<Integer, Post>>() {}.getType();
        ConcurrentHashMap<Integer, Post> mapPost = gson.fromJson(readerPost, typeOfMapPost);
        if(mapPost == null)
            mapPost = new ConcurrentHashMap<>();
        social.setSocialPost(mapPost);
    }
}
