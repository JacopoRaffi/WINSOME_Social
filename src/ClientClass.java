import Exceptions.IllegalRegisterException;
import Server.ClientNotifyInterface;
import Server.ServerRegistryInterface;

import java.io.*;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientClass implements Runnable {
    //principali variabili
    private int TCP_PORT = 9011;
    private int UDP_PORT = 33333;
    private int MULTICAST_PORT = 44444;
    private int REG_PORT = 7777;
    private String SERVER_ADDRESS = "192.168.1.2";
    private String MULTICAST_ADDRESS = "239.255.32.32";
    private String REG_SERVICENAME = "serverRegistry";
    private long TIMEOUT = 100000;
    private boolean logged = false;
    private final String fileConfigName;
    private final List<String> followers;
    private final Lock listLock;
    private String username;
    private String password;
    private ServerRegistryInterface regFun;
    private Registry registry;

    public ClientClass(String fileConfigName){
        listLock = new ReentrantLock();
        this.followers = new LinkedList<>();
        this.fileConfigName = fileConfigName;
    }

    public void run() {
        File clientConfigFile;
        
        try {
            clientConfigFile = new File(fileConfigName);
            configClient(clientConfigFile);
            System.out.println("--------CONFIGURAZIONE TERMINATA CON SUCCESSO--------");
        } catch (NullPointerException | IOException | IllegalArgumentException e) {
            restoreValues(); //durante la configurazione ci potrebbero essere degli errori a metà lavoro e quindi ripristino i valori di default
            System.out.println("--------CLIENT AVVIATO CON VALORI DI DEFAULT--------");
        }
        System.out.println("VALORI DEL CLIENT:");
        System.out.println("SERVER_ADDRESS = " + SERVER_ADDRESS);
        System.out.println("REG_SERVICENAME = " + REG_SERVICENAME);
        System.out.println("TCP_PORT = " + TCP_PORT);
        System.out.println("REG_PORT = " + REG_PORT);
        System.out.println("TIMEOUT_SOCKET = " + TIMEOUT);
        try{
            //configurazione TCP
            Socket socket = new Socket("localhost", TCP_PORT);
            //lettura parametri per il Multicast Socket
            DataInputStream inReader = new DataInputStream(socket.getInputStream());
            String multiCastParam = "";
            multiCastParam = inReader.readUTF();
            String[] parametriMS = multiCastParam.split(" ");

            //configurazione multicastSocket
            MulticastSocket msocket = new MulticastSocket(Integer.parseInt(parametriMS[0]));
            InetAddress address = InetAddress.getByName(parametriMS[1]);
            msocket.setReuseAddress(true);
            msocket.joinGroup(address);
            Thread waitingThread = new Thread(new ClientUDPThread(msocket));
            waitingThread.setDaemon(true);
            waitingThread.start();

            //configurazione RMI
            registry = LocateRegistry.getRegistry(REG_PORT);
            regFun = (ServerRegistryInterface) registry.lookup(REG_SERVICENAME);
            ClientNotifyInterface callbackObj = new ClientNotifyClass(followers);
            ClientNotifyInterface stub = (ClientNotifyInterface) UnicastRemoteObject.exportObject(callbackObj, 0);
            socialActivity(socket, stub); //inizio dell'utilizzo del social da parte del client
        }catch(IOException | NotBoundException e){
            System.err.println("ERRORE: connessione col server interrotta");
            System.exit(-1);
        }
    }

    private  void register(String username, String password, String tags){
        try{
            regFun = (ServerRegistryInterface) registry.lookup(REG_SERVICENAME);
            if(regFun.userRegister(username, password, tags)) {
                System.out.println("REGISTRAZIONE EFFETTUATA CON SUCCESSO");
                System.out.println("-------- BENVENUTO SU WINSOME --------");
                System.out.println("Ricordati di fare il login per iniziare la tua attività sul social");
                System.out.println("Digitare il comando help per la lista dei possibili comandi");
                this.username = username;
            }
            else
                System.err.println("ERRORE: username già registrato nel social");
        }catch(RemoteException | NotBoundException e){
            System.err.println("ERRORE: registrazione fallita");
            System.exit(-1);
        }catch(IllegalRegisterException ex){
            System.err.println("ERRORE: la password deve essere compresa tra 8 e 16 caratteri");
        }
    }

    private void restoreValues() {
        TCP_PORT = 9011;
        UDP_PORT = 33333;
        MULTICAST_PORT = 44444;
        REG_PORT = 7777;
        SERVER_ADDRESS = "192.168.1.2";
        MULTICAST_ADDRESS = "239.255.32.32";
        REG_SERVICENAME = "localhost";
        TIMEOUT = 100000;
    }

    private void configClient(File config) throws IOException, NumberFormatException {
        BufferedReader configReader = new BufferedReader(new FileReader(config));
        String line = configReader.readLine();
        while (line != null) {
            if(!line.contains("#") || !line.isEmpty()) {
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
            }
            line = configReader.readLine();
        }
        configReader.close();
    }

    //funzione che legge i comandi da tastiera
    private void socialActivity(Socket socket, ClientNotifyInterface stub) throws IOException{
        String NOT_LOGGED_MESSAGE = "< ERRORE: non hai fatto il login in WINSOME";
        String[] commandLine;
        String serverResponse;
        Scanner scanner = new Scanner(System.in);
        DataOutputStream outWriter = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        DataInputStream inReader = new DataInputStream(socket.getInputStream());

        while(true){
            System.out.printf("> ");
            String line = scanner.nextLine();
            commandLine = line.split(" ");
            String request = commandLine[0].toLowerCase(Locale.ROOT);

            if(request.compareTo("register") == 0){
                if(commandLine.length < 4 || commandLine.length > 8){
                    System.err.println("< ERRORE: il comando è: register <username> <password> <tags>");
                    System.err.println("Numero di tags tra 1 e 5");
                    continue;
                }
                String tags = ""; //in questa stringa mi salvo i tags del client
                int i = 3;
                while(i < commandLine.length){
                    tags += commandLine[i] + " ";
                    i++;
                }
                System.out.println(tags);
                register(commandLine[1], commandLine[2], tags);
            }
            else if(request.compareTo("login") == 0) {
                if (!logged) {
                    if (commandLine.length != 3) {
                        System.err.println("< ERRORE: il comando è: login <username> <password>");
                        continue;
                    }
                    outWriter.writeUTF(line); //invio la richiesta al server con i relativi parametri
                    outWriter.flush();
                    serverResponse = inReader.readUTF(); //leggo la risposta del server
                    if (serverResponse.startsWith("SUCCESSO")) {
                        logged = true;
                        username = commandLine[1];
                        password = commandLine[2];
                        try {
                            followers.addAll(regFun.backUpFollowers(username, password));
                            regFun.registerForCallback(stub, username, password);
                        }catch(NoSuchAlgorithmException | NullPointerException e){
                            System.err.println("ERRORE SERVER: c'è stato un problema, riprovare successivamente");
                        }
                    }
                    System.out.println("< " + serverResponse);
                }
                else
                    System.out.println("< Hai già fatto il login");
            }
            else if(request.compareTo("logout") == 0){
                if(logged) {
                    System.out.println("< Chiusura da WINSOME");
                    try {
                        regFun.unregisterForCallback(stub, username, password);
                        logged = false;
                    }catch(NoSuchAlgorithmException e){
                        System.err.println("< ERRORE SERVER: c'è stato un problema, riprovare successivamente");
                        continue;
                    }
                }
                else{
                    System.err.println(NOT_LOGGED_MESSAGE);
                }
            }
            else if(request.compareTo("help") == 0){
                help();
            }
            else if(request.compareTo("listusers") == 0){
                if(logged){
                    outWriter.writeUTF(line);
                    outWriter.flush();
                    serverResponse = inReader.readUTF();
                    int dim = Integer.parseInt(serverResponse);

                    for(int i = 0; i < dim; i++){
                        serverResponse = inReader.readUTF();
                        System.out.println("< " + serverResponse);
                    }

                }else
                    System.out.println(NOT_LOGGED_MESSAGE);
            }
            else if(request.compareTo("listfollowers") == 0){
                try{
                    listLock.lock();
                    System.out.println(followers);
                }finally {
                    listLock.unlock();
                }
            }
            else if(request.compareTo("listfollowing") == 0){
                if(logged){
                    outWriter.writeUTF(line); //invio la richiesta al server con i relativi parametri
                    outWriter.flush();
                    serverResponse = inReader.readUTF(); //leggo la risposta del server
                    int dim = Integer.parseInt(serverResponse);

                    for(int i = 0; i < dim; i++){
                        serverResponse = inReader.readUTF();
                        System.out.println("< " + serverResponse);
                    }
                }else
                    System.out.println(NOT_LOGGED_MESSAGE);

            }
            else if(request.compareTo("follow") == 0){
                if (logged) {
                    if (commandLine.length != 2) {
                        System.err.println("< ERRORE: il comando è: follow <username>");
                        continue;
                    }
                    outWriter.writeUTF(line); //invio la richiesta al server con i relativi parametri
                    outWriter.flush();
                    serverResponse = inReader.readUTF(); //leggo la risposta del server
                    System.out.println("< " + serverResponse);
                }
                else
                    System.out.println(NOT_LOGGED_MESSAGE);
            }
            else if(request.compareTo("unfollow") == 0){
                if (logged) {
                    if (commandLine.length != 2) {
                        System.err.println("< ERRORE: il comando è: unfollow <username>");
                        continue;
                    }
                    
                    outWriter.writeUTF(line); //invio la richiesta al server con i relativi parametri
                    outWriter.flush();
                    serverResponse = inReader.readUTF(); //leggo la risposta del server
                    System.out.println("< " + serverResponse);
                }
                else
                    System.out.println(NOT_LOGGED_MESSAGE);
            }
            else if(request.compareTo("createpost") == 0){
                if (logged) {
                    if (commandLine.length != 3) {
                        System.err.println("< ERRORE: il comando è: cretepost <titolo> <contenuto>");
                        System.out.println("Lunghezza massima titolo 50 caratteri");
                        System.out.println("Lunghezza massima contenuto 500 caratteri");
                        continue;
                    }
                    if(commandLine[1].length() > 50){
                        System.err.println("ERRORE: lunghezza massima titolo 50 caratteri");
                    }
                    if(commandLine[2].length() > 500){
                        System.err.println("ERRORE: lunghezza massima contenuto 500 caratteri");
                    }
                    
                    outWriter.writeUTF(line); //invio la richiesta al server con i relativi parametri
                    outWriter.flush();
                    serverResponse = inReader.readUTF(); //leggo la risposta del server
                    System.out.println("< " + serverResponse);
                }
                else
                    System.out.println(NOT_LOGGED_MESSAGE);
            }
            else if(request.compareTo("viewblog") == 0){
                if(logged){
                    outWriter.writeUTF(line);
                    outWriter.flush();
                    serverResponse = inReader.readUTF();

                    System.out.println("< " + serverResponse);

                }else
                    System.out.println(NOT_LOGGED_MESSAGE);
            }
            else if(request.compareTo("showfeed") == 0){
                if(logged){
                    outWriter.writeUTF(line);
                    outWriter.flush();
                    serverResponse = inReader.readUTF();

                    System.out.println("< " + serverResponse);

                }else
                    System.out.println(NOT_LOGGED_MESSAGE);
            }
            else if(request.compareTo("showpost") == 0){
                if (logged) {
                    if (commandLine.length != 2) {
                        System.err.println("< ERRORE: il comando è: showpost <idpost>");
                        System.out.println("< idpost deve essere un numero non negativo");
                        continue;
                    }
                    try{
                        long idpost = Long.parseLong(commandLine[1]);
                        if(idpost < 0){
                            System.err.println("< idpost deve essere un numero non negativo");
                            continue;
                        }
                    }catch(RuntimeException e){
                        System.err.println("< idpost deve essere un numero non negativo");
                        continue;
                    }
                    outWriter.writeUTF(line); //invio la richiesta al server con i relativi parametri
                    outWriter.flush();
                    serverResponse = inReader.readUTF(); //leggo la risposta del server
                    System.out.println("< " + serverResponse);
                }
                else
                    System.out.println(NOT_LOGGED_MESSAGE);
            }
            else if(request.compareTo("deletepost") == 0){
                if (logged) {
                    if (commandLine.length != 2) {
                        System.err.println("< ERRORE: il comando è: delete <idpost>");
                        System.out.println("idpost deve essere un numero non negativo");
                        continue;
                    }
                    try{
                        long idpost = Long.parseLong(commandLine[1]);
                        if(idpost < 0){
                            System.err.println("idpost deve essere un numero non negativo");
                            continue;
                        }
                    }catch(RuntimeException e){
                        System.err.println("idpost deve essere un numero non negativo");
                        continue;
                    }
                    outWriter.writeUTF(line); //invio la richiesta al server con i relativi parametri
                    outWriter.flush();
                    serverResponse = inReader.readUTF(); //leggo la risposta del server
                    System.out.println("< " + serverResponse);
                }
                else
                    System.out.println(NOT_LOGGED_MESSAGE);
            }
            else if(request.compareTo("rewinpost") == 0){
                if (logged) {
                    if (commandLine.length != 2) {
                        System.err.println("< ERRORE: il comando è: rewinpost <idpost>");
                        System.out.println("idpost deve essere un numero non negativo");
                        continue;
                    }
                    try{
                        long idpost = Long.parseLong(commandLine[1]);
                        if(idpost < 0){
                            System.err.println("idpost deve essere un numero non negativo");
                            continue;
                        }
                    }catch(RuntimeException e){
                        System.err.println("idpost deve essere un numero non negativo");
                        continue;
                    }
                    outWriter.writeUTF(line); //invio la richiesta al server con i relativi parametri
                    outWriter.flush();
                    serverResponse = inReader.readUTF(); //leggo la risposta del server
                    System.out.println("< " + serverResponse);
                }
                else
                    System.out.println(NOT_LOGGED_MESSAGE);
            }
            else if(request.compareTo("ratepost") == 0){
                if (logged) {
                    if (commandLine.length != 3) {
                        System.err.println("< ERRORE: il comando è: ratepost <idpost> <voto>");
                        System.out.println("< idpost deve essere un numero non negativo");
                        System.out.println("< il voto deve essere +1 o -1");
                        continue;
                    }
                    try{
                        long idpost = Long.parseLong(commandLine[1]);
                        if(idpost < 0){
                            System.err.println("< idpost deve essere un numero non negativo");
                            continue;
                        }
                    }catch(RuntimeException e){
                        System.err.println("< idpost deve essere un numero non negativo");
                        continue;
                    }
                    try{
                        Long.parseLong(commandLine[2]);
                    }catch(RuntimeException e){
                        System.err.println("< il voto deve essere +1 o -1");
                        continue;
                    }
                    outWriter.writeUTF(line); //invio la richiesta al server con i relativi parametri
                    outWriter.flush();
                    serverResponse = inReader.readUTF(); //leggo la risposta del server
                    System.out.println("< " + serverResponse);
                }
                else
                    System.out.println(NOT_LOGGED_MESSAGE);
            }
            else if(request.compareTo("addcomment") == 0){
                if (logged) {
                    if (commandLine.length != 3) {
                        System.err.println("< ERRORE: il comando è: addcomment <idpost> <commento>");
                        System.out.println("< idpost deve essere un numero non negativo");
                        continue;
                    }
                    try{
                        long idpost = Long.parseLong(commandLine[1]);
                        if(idpost < 0){
                            System.err.println("< idpost deve essere un numero non negativo");
                            continue;
                        }
                    }catch(RuntimeException e){
                        System.err.println("< idpost deve essere un numero non negativo");
                        continue;
                    }
                    outWriter.writeUTF(line); //invio la richiesta al server con i relativi parametri
                    outWriter.flush();
                    serverResponse = inReader.readUTF(); //leggo la risposta del server
                    System.out.println("< " + serverResponse);
                }
                else
                    System.out.println(NOT_LOGGED_MESSAGE);
            }
            else if(request.compareTo("getwallet") == 0){
                if(logged){
                    outWriter.writeUTF(line);
                    outWriter.flush();
                    serverResponse = inReader.readUTF();
                    System.out.println(serverResponse);
                    serverResponse = inReader.readUTF();
                    int len = Integer.parseInt(serverResponse);
                    for(int i = 0; i < len; i++){
                        serverResponse = inReader.readUTF();
                        System.out.println("< " + serverResponse);
                    }

                }else
                    System.out.println(NOT_LOGGED_MESSAGE);
            }
            else if(request.compareTo("getwalletinbitcoin") == 0){
                if(logged){
                    outWriter.writeUTF(line);
                    outWriter.flush();
                    serverResponse = inReader.readUTF();

                    System.out.println("< " + serverResponse);

                }else
                    System.out.println(NOT_LOGGED_MESSAGE);
            }
            else if(request.compareTo("quit") == 0){
                System.out.println("Chiusura WINSOME...");
                outWriter.close();
                inReader.close();
                socket.close();
                System.out.println("Arrivederci :)");
                System.exit(0);
            }
            else{
                System.out.println("< Comando non riconosciuto: digitare help per la lista di comandi");
            }
        }
    }

    private void help(){
        System.out.println("LISTA DEI COMANDI");
        System.out.println(
                        "register <username> <password> <tags>\n"+
                        "login <username> <password>\n"+
                        "logout <username>\n"+
                        "listUsers\n"+
                        "listFollowers\n"+
                        "listFollowing\n"+
                        "followUser <username>\n"+
                        "unfollowUser <username>\n"+
                        "viewBlog\n"+
                        "showFeed\n"+
                        "createPost <titolo> <contenuto>\n"+
                        "showPost <idpost>\n"+
                        "deletePost <idpost>\n"+
                        "rewinPost <idpost>\n"+
                        "ratePost <idpost> <voto>\n"+
                        "addComment <idpost> <commento>\n"+
                        "getWallet\n"+
                        "getWalletInBitcoin\n"+
                        "quit\n")
                        ;
    }
}
