import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ClientMain {
    //principali variabili
    private static int TCP_PORT = 6666;
    private static int UDP_PORT = 33333;
    private static int MULTICAST_PORT = 44444;
    private static int REG_PORT = 7777;
    private static String SERVER_ADDRESS = "192.168.1.2";
    private static String MULTICAST_ADDRESS = "239.255.32.32";
    private static String REG_ADDRESS = "localhost";
    private static long TIMEOUT = 100000;

    public static void main(String[] Args) {
        File clientConfigFile;
        try {
            clientConfigFile = new File(Args[0]);
            configServer(clientConfigFile);
            System.out.println("--------CONFIGURAZIONE TERMINATA CON SUCCESSO--------");
        } catch (NullPointerException | IOException | IllegalArgumentException e) {
            restoreValues(); //durante la configurazione ci potrebbero essere degli errori a metà lavoro e quindi ripristino i valori di default
            System.out.println("--------CLIENT AVVIATO CON VALORI DI DEFAULT--------");
        }
        System.out.println("VALORI DEL CLIENT:");
        System.out.println("SERVER_ADDRESS = " + SERVER_ADDRESS);
        System.out.println("MULTICAST_ADDRESS = " + MULTICAST_ADDRESS);
        System.out.println("REG_ADDRESS = " + REG_ADDRESS);
        System.out.println("TCP_PORT = " + TCP_PORT);
        System.out.println("UDP_PORT = " + UDP_PORT);
        System.out.println("REG_PORT = " + REG_PORT);
        System.out.println("MULTICAST_PORT = " + MULTICAST_PORT);
        System.out.println("TIMEOUT_SOCKET = " + TIMEOUT);

    }

    private static void restoreValues() {
        TCP_PORT = 6666;
        UDP_PORT = 33333;
        MULTICAST_PORT = 44444;
        REG_PORT = 7777;
        String SERVER_ADDRESS = "192.168.1.2";
        String MULTICAST_ADDRESS = "239.255.32.32";
        String REG_ADDRESS = "localhost";
        TIMEOUT = 100000;
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
                } else if (tokens[0].compareTo("REG_ADDRESS") == 0) {
                    REG_ADDRESS = tokens[1];
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
}
