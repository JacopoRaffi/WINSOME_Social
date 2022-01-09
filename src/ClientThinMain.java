import java.io.File;
import java.net.URL;
import java.rmi.server.RMIClassLoader;
import java.rmi.RMISecurityManager;

public class ClientThinMain {

    public static void main(String[]Args){
        System.setProperty("java.security.policy", "myGrantAllPolicy.policy");
        String filesName = "./Config/ClientConfig.txt";
        if(Args.length >= 1){
            filesName = Args[0];
        }
        System.setSecurityManager(new SecurityManager());
        try {
            URL url = new File("..\\src\\ClientClass.java").toURI().toURL();
            System.out.println(url);
            Class<?> clientClasss = RMIClassLoader.loadClass(url, "ClientClass");
            Runnable client = (Runnable) clientClasss.getDeclaredConstructor(String.class).newInstance(filesName);
            client.run();
        } catch (Exception e) { 
            System.out.println("Exception: " +
                e.getMessage());
            e.printStackTrace();
        }
    }
}
