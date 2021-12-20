package Client;

import Exceptions.IllegalRegisterException;
import Server.ServerRegistry;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Locale;
import java.util.Scanner;

public class ClientMain {
    //principali variabili
    private static int TCP_PORT = 9011;
    private static int UDP_PORT = 33333;
    private static int MULTICAST_PORT = 44444;
    private static int REG_PORT = 7777;
    private static String SERVER_ADDRESS = "192.168.1.2";
    private static String MULTICAST_ADDRESS = "239.255.32.32";
    private static String REG_SERVICENAME = "serverRegistry";
    private static long TIMEOUT = 100000;
    private static boolean logged = false;

    public static void main(String[] Args) {
        File clientConfigFile;
        try {
            clientConfigFile = new File(Args[0]);
            configClient(clientConfigFile);
            System.out.println("--------CONFIGURAZIONE TERMINATA CON SUCCESSO--------");
        } catch (NullPointerException | IOException | IllegalArgumentException e) {
            restoreValues(); //durante la configurazione ci potrebbero essere degli errori a metà lavoro e quindi ripristino i valori di default
            System.out.println("--------CLIENT AVVIATO CON VALORI DI DEFAULT--------");
        }
        System.out.println("VALORI DEL CLIENT:");
        System.out.println("SERVER_ADDRESS = " + SERVER_ADDRESS);
        System.out.println("MULTICAST_ADDRESS = " + MULTICAST_ADDRESS);
        System.out.println("REG_SERVICENAME = " + REG_SERVICENAME);
        System.out.println("TCP_PORT = " + TCP_PORT);
        System.out.println("UDP_PORT = " + UDP_PORT);
        System.out.println("REG_PORT = " + REG_PORT);
        System.out.println("MULTICAST_PORT = " + MULTICAST_PORT);
        System.out.println("TIMEOUT_SOCKET = " + TIMEOUT);
        try{
            Socket socket = new Socket("localhost", TCP_PORT);
            socialActivity(socket); //inizio dell'utilizzo del social da parte del client
        }catch(IOException e){
            System.err.println("ERRORE: connessione col server interrotta");
            System.exit(-1);
        }

    }

    private static void register(String username, String password, String tags){
        try{
            Registry registry = LocateRegistry.getRegistry(REG_PORT);
            ServerRegistry regFun = (ServerRegistry) registry.lookup(REG_SERVICENAME);
            if(regFun.userRegister(username, password, tags, InetAddress.getLocalHost().toString())) {
                System.out.println("REGISTRAZIONE EFFETTUATA CON SUCCESSO");
                System.out.println("-------- BENVENUTO SU WINSOME --------");
            }
            else
                System.err.println("ERRORE: username già registrato nel social");
        }catch(RemoteException | NotBoundException | UnknownHostException e){
            System.err.println("ERRORE: registrazione fallita");
            System.exit(-1);
        }catch(IllegalRegisterException ex){
            System.err.println("ERRORE: la password deve essere compresa tra 8 e 16 caratteri");

        }
    }

    private static void restoreValues() {
        TCP_PORT = 6666;
        UDP_PORT = 33333;
        MULTICAST_PORT = 44444;
        REG_PORT = 7777;
        SERVER_ADDRESS = "192.168.1.2";
        MULTICAST_ADDRESS = "239.255.32.32";
        REG_SERVICENAME = "localhost";
        TIMEOUT = 100000;
    }

    private static void configClient(File config) throws IOException, NumberFormatException {
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
            }
            line = configReader.readLine();
        }
        configReader.close();
    }

    //funzione che legge i comandi da tastiera
    private static void socialActivity(Socket socket) throws IOException{
        String NOT_LOGGED_MESSAGE = "< ERRORE: non hai fatto il login in WINSOME";
        String[] commandLine;
        String sendRequest = "";
        String serverResponse = "";
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
                    if (commandLine.length < 2) {
                        System.err.println("< ERRORE: il comando è: login <username> <password>");
                        continue;
                    }
                    sendRequest = line;
                    outWriter.writeUTF(sendRequest); //invio la richiesta al server con i relativi parametri
                    outWriter.flush();
                    serverResponse = inReader.readUTF(); //leggo la risposta del server
                    if (serverResponse.startsWith("SUCCESSO")) {
                        logged = true;
                    }
                    System.out.println("< " + serverResponse);
                }
                else
                    System.out.println("< Hai già fatto il login");
            }
            else if(request.compareTo("logout") == 0){
                if(logged) {
                    outWriter.close();
                    inReader.close();
                    socket.close();
                    System.out.println("< Chiusura da WINSOME");
                    break;
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
                    sendRequest = line;
                    outWriter.writeUTF(sendRequest);
                    outWriter.flush();
                    serverResponse = inReader.readUTF();

                    System.out.println(serverResponse);

                }else
                    System.out.println(NOT_LOGGED_MESSAGE);
            }
        }
    }

    private static void help(){
        System.out.println("LISTA DEI POSSIBILI COMANDI");
        System.out.println(
                "register <username> <password> <tags>\n" +
                        "login <username> <password>\n" +
                        "logout <username>\n" +
                        "listUsers\n" +
                        "listFollowers\n" +
                        "listFollowing\n" +
                        "followUser <username>\n" +
                        "unfollowUser <username>\n" +
                        "viewBlog\n" +
                        "showFeed\n" +
                        "createPost <titolo> <contenuto>\n" +
                        "showPost <idpost>\n" +
                        "deletePost <idpost>\n" +
                        "rewinPost <idpost>\n" +
                        "ratePost <idpost> <voto>\n" +
                        "addComment <idpost> <commento>\n" +
                        "getWallet\n" +
                        "getWalletInBitcoin\n");
    }
}
