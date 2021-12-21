import java.io.File;
import java.net.URL;
import java.rmi.server.RMIClassLoader;

public class ThinClientMain {

    public static void main(String[]Args){
        if (Args.length == 0) {
            System.err.println("Usage: java LoadClient <remote URL>");
            System.exit(-1);
        }
        System.setSecurityManager(new MySecurityManager());
        try {
            URL url = new File(Args[0]).toURI().toURL();
            Class clientClasss = RMIClassLoader.loadClass(url, "ClientClass");
            Runnable client = (Runnable) clientClasss.getDeclaredConstructor().newInstance();
            client.run();
        } catch (Exception e) { System.out.println("Exception: " +
                e.getMessage());
            e.printStackTrace();
        }
    }
}
