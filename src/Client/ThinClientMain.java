package Client;

import java.net.URL;
import java.rmi.server.RMIClassLoader;

public class ThinClientMain {

    public static void main(String[]Args){
        if (Args.length == 0) {
            System.err.println("Usage: java LoadClient <remote URL>");
            System.exit(-1);}
        System.setSecurityManager(new SecurityManager());
        try {
            URL url = new URL(Args[0]);
            Class clientClasss = RMIClassLoader.loadClass(url, "ClientClass");
            Runnable client = (Runnable) clientClasss.getDeclaredConstructor().newInstance();
            client.run();
        } catch (Exception e) { System.out.println("Exception: " +
                e.getMessage());
            e.printStackTrace();
        }
    }
}
