import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class ServerMain {
    //principali variabili(con relativi valori di default)
    private static int TCP_PORT = 9011;
    private static int UDP_PORT = 33333;
    private static int REG_PORT = 7777;
    private static String SERVER_ADDRESS = "192.168.1.2";
    private static String MULTICAST_ADDRESS = "239.255.32.32";
    private static String REG_SERVICENAME = "serverRegistry";
    private static long TIMEOUT = 10000000;
    private static long TIMELAPSE = 5; //default 5 secondi
    private static long TIMELAPSEBACKUP = 5; //5 minuti di default
    private static double AUTHOR_RATE = 0.8;

    public static void main(String[] Args){
        File serverConfigFile;
        try {
            serverConfigFile = new File(Args[0]);
            configServer(serverConfigFile);
            System.out.println("--------CONFIGURAZIONE TERMINATA CON SUCCESSO--------");
        } catch (NullPointerException | IOException | IllegalArgumentException | IndexOutOfBoundsException e) {
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
        System.out.println("TIMEOUT_SOCKET = " + TIMEOUT);
        System.out.println("AUTHOR PERCENTAGE REWARD = " + AUTHOR_RATE*100 + "%");
        System.out.println("TIMELAPSE BETWEEN REWARDS(SECONDS) = " + TIMELAPSE);
        System.out.println("TIMELAPSE BETWEEN BACKUPS(MINUTES) = " + TIMELAPSEBACKUP);

        //in questi due file mi salvo il backup dei post del social e degli utenti registrati(periodicamente)
        File socialUserStatus = new File("..\\StatusServer\\usersStatus.json");
        File postStatus = new File("..\\StatusServer\\postStatus.json");
        System.out.println(postStatus.getPath());
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
        ServerWinsomeSocial socialNetwork = new ServerWinsomeSocial(); //creo il social vero e proprio
       try{
           rebootSocial(socialNetwork, socialUserStatus, postStatus);
        }catch(IOException e){
            System.err.println("ERRORE: errore durante il ripristino dell'ultimo backup");
            System.exit(-1);
        }

        //creo e avvio il thread che si occuperà del backup
        ServerBackup autoSaving = new ServerBackup(socialNetwork, socialUserStatus, postStatus, TIMELAPSEBACKUP);
        autoSaving.start();

        try{
            ServerRegistryInterface stub = (ServerRegistryInterface) UnicastRemoteObject.exportObject(socialNetwork, 0);
            LocateRegistry.createRegistry(REG_PORT);
            Registry registry = LocateRegistry.getRegistry(REG_PORT);
            registry.rebind(REG_SERVICENAME, stub);
        }catch(RemoteException e){
            System.err.println("ERRORE: errore con RMI");
            System.exit(-1);
        }

        DatagramSocket socketUDP = null;
        InetAddress multiCastAddress = null;
        try {
            //creazione del socket usato per il multicast
            multiCastAddress = InetAddress.getByName(MULTICAST_ADDRESS);
            socketUDP = new DatagramSocket();
        }catch(IOException e){
            System.err.println("ERRORE: problemi con multicast socket" + e.getMessage());
            System.exit(-1);
        }
        ServerReward threadUDP = new ServerReward(socialNetwork, TIMELAPSE, socketUDP, multiCastAddress, UDP_PORT, AUTHOR_RATE);
        threadUDP.start();
        ExecutorService threadPool = Executors.newCachedThreadPool(); //pool di worker(uno per client)
        //il server main si occupa delle connessioni TCP
        ServerSocket welcomeSocket = null;
        try{
            welcomeSocket = new ServerSocket(TCP_PORT);
            System.out.printf("Server pronto su porta %d\n", TCP_PORT);
        }catch(IOException e){
            System.err.println("ERRORE: problemi con la creazione del welcome socket...chiusura server");
            System.exit(-1);
        }
        closeServer(welcomeSocket, socketUDP, threadPool, threadUDP, autoSaving);
        while (true) {
            try {
                welcomeSocket.setSoTimeout((int) TIMEOUT);
                Socket clientSocket = welcomeSocket.accept();
                DataOutputStream outWriter = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
                outWriter.writeUTF(UDP_PORT + " " + MULTICAST_ADDRESS);
                outWriter.flush();

                threadPool.execute(new ServerWorker(clientSocket, socialNetwork)); //genero un thread worker legato a quel client
            } catch (IOException ex) {
                System.err.println("ERRORE: chiusura welcomeSocket");
                System.exit(-1);
            }
        }
    }

    private static void restoreValues() {
        TCP_PORT = 6666;
        UDP_PORT = 33333;
        REG_PORT = 7777;
        SERVER_ADDRESS = "192.168.1.2";
        MULTICAST_ADDRESS = "239.255.32.32";
        REG_SERVICENAME = "localhost";
        TIMEOUT = 0;
        TIMELAPSE = 5;
        AUTHOR_RATE = 0.8;
        TIMELAPSEBACKUP = 5;
    }

    private static void configServer(File config) throws IOException, NumberFormatException {
        BufferedReader configReader = new BufferedReader(new FileReader(config));
        String line = configReader.readLine();
        while (line != null) {
            if(!line.contains("#")) {
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
                }  else if (tokens[0].compareTo("TIMEOUT") == 0) {
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

    private static void rebootSocial(ServerWinsomeSocial social, File socialUserStatus, File postStatus) throws IOException{
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonReader userReader = new JsonReader(new InputStreamReader(new FileInputStream(socialUserStatus)));
        JsonReader postReader = new JsonReader(new InputStreamReader(new FileInputStream(postStatus)));

        if(postStatus.length() > 0)
            rebootPosts(postReader, gson, social);
        if(socialUserStatus.length() > 0)
            rebootUsers(userReader, gson, social);
    }

    private static void rebootPosts(JsonReader reader, Gson gson, ServerWinsomeSocial social) throws IOException{
        ConcurrentHashMap<Long, ServerPost> socialPosts = new ConcurrentHashMap<>();
        Type typeOfComments = new TypeToken<Hashtable<String, LinkedList<Comment>>>() {}.getType();
        Type typeOfLikes = new TypeToken<LinkedList<FeedBack>>() {}.getType();

        reader.beginArray();
        while(reader.hasNext()){
            reader.beginObject();
            //parametri di un ServerPost
        Long idpost = null; String autore = null; String titolo = null; String contenuto = null;
        int numIterazioni = 0; long rewardTime = 0;
        Hashtable<String, LinkedList<Comment>> comments = null; LinkedList<FeedBack> likes = null;
        //fine parametri
            while(reader.hasNext()){
                String next = reader.nextName();
                if(next.equals("idpost")){
                    idpost = reader.nextLong();
                }
                else if(next.equals("autore")){
                    autore = reader.nextString();
                }
                else if(next.equals("titolo")){
                    titolo = reader.nextString();
                }
                else if(next.equals("contenuto")){
                    contenuto = reader.nextString();
                }
                else if(next.equals("numIterazioni")){
                    numIterazioni = reader.nextInt();
                }
                else if(next.equals("comments")){
                    comments = gson.fromJson(reader.nextString(), typeOfComments);
                }
                else if(next.equals("likes")){
                    likes = gson.fromJson(reader.nextString(), typeOfLikes);
                }
                else if(next.equals("lastTimeReward")){
                    rewardTime = reader.nextLong();
                }
                else
                    reader.skipValue();
            }
            reader.endObject();
            if(autore != null) {
                ServerPost post = new ServerPost(idpost, rewardTime, titolo, contenuto, autore, comments, likes, numIterazioni);
                socialPosts.putIfAbsent(idpost, post);
            }
        }
        reader.endArray();
        reader.close();
        social.setSocialPost(socialPosts);
    }

    private static void rebootUsers(JsonReader reader, Gson gson, ServerWinsomeSocial social) throws IOException{
        ConcurrentHashMap<String, ServerUser> socialUsers = new ConcurrentHashMap<>();
        Type typeOfFollowers_ed = new TypeToken<LinkedHashSet<String>>() {}.getType();
        Type typeOfMap = new TypeToken<LinkedList<Long>>() {}.getType();
        //parametri di un ServerUser
        //fine parametri
        reader.beginArray();
        while(reader.hasNext()){
            reader.beginObject();
            String seed = null; String[] tags = null; String username = null; String hashedPassword = null;
            LinkedHashSet<String> followers = null; LinkedHashSet<String> followed = null;
            ConcurrentHashMap<Long, ServerPost> feed = new ConcurrentHashMap<>();
            ConcurrentHashMap<Long, ServerPost> blog = new ConcurrentHashMap<>();
            Wallet wallet = null;
            while(reader.hasNext()){
                String next = reader.nextName();
                if(next.equals("seed")){
                    seed = reader.nextString();
                }
                else if(next.equals("tags")){
                    tags = gson.fromJson(reader.nextString(), String[].class);
                }
                else if(next.equals("username")){
                    username = reader.nextString();
                }
                else if(next.equals("hashedPassword")){
                    hashedPassword = reader.nextString();
                }
                else if(next.equals("followers")){
                    followers = gson.fromJson(reader.nextString(), typeOfFollowers_ed);
                }
                else if(next.equals("followed")){
                    followed = gson.fromJson(reader.nextString(), typeOfFollowers_ed);
                }
                else if(next.equals("feed")){
                    LinkedList<Long> aux = gson.fromJson(reader.nextString(), typeOfMap);
                    for (Long id:aux) {
                        feed.putIfAbsent(id, social.getSocialPost().get(id));
                    }
                }
                else if(next.equals("blog")){
                    LinkedList<Long> aux = gson.fromJson(reader.nextString(), typeOfMap);
                    for (Long id:aux) {
                        blog.putIfAbsent(id, social.getSocialPost().get(id));
                    }
                }
                else if(next.equals("wallet")){
                    wallet = gson.fromJson(reader.nextString(), Wallet.class);
                }
                else
                    reader.skipValue();
            }
            reader.endObject();
            if(username != null) {
                ServerUser user = new ServerUser(username, tags, seed, hashedPassword, followers, followed, feed, blog, wallet);
                socialUsers.putIfAbsent(username, user);
            }
        }
        reader.endArray();
        reader.close();
        social.setSocialUsers(socialUsers);
    }

    private static void closeServer(ServerSocket socketTCP, DatagramSocket socketUDP, ExecutorService pool, ServerReward reward, ServerBackup autoSaving){
        new Thread(() -> {
            Scanner scan = new Scanner(System.in);
            String line = "";
            while ("quit".compareTo(line) != 0) {
                System.out.println("PER TERMINARE IL SERVER DIGITARE quit");
                line = scan.nextLine();
            }
            System.out.println("CHIUSURA DEL SERVER...");
            try {
                reward.interrupt();
                autoSaving.interrupt();
                autoSaving.backupUser();
                autoSaving.backupPost();
                try{
                    reward.join();
                }catch(InterruptedException e){}
                pool.shutdownNow();
                socketTCP.close();
                socketUDP.close();
                System.out.println("SERVER TERMINATO");
            } catch (IOException e) {
                System.err.println("ERRORE: problemi con la chiusura dei socket" + e.getMessage());
                System.exit(-1);
            }
        }).start();
    }
}
