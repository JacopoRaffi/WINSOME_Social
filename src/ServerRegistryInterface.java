import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerRegistryInterface extends Remote {
    /** metodo per registrare un utente a WINSOME */
    boolean userRegister(String username, String password, String tags, String userAddress) throws RemoteException;

    public void registerForCallback (ClientNotifyInterface ClientInterface, String username) throws RemoteException;

    public void unregisterForCallback (ClientNotifyInterface ClientInterface, String username) throws RemoteException;
}
