package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface ServerRegistryInterface extends Remote {
    /** metodo per registrare un utente a WINSOME */
    boolean userRegister(String username, String password, String tags, String userAddress) throws RemoteException;

    public List<String> backUpFollowers(String username, String password) throws RemoteException, NoSuchAlgorithmException;

    public void registerForCallback (ClientNotifyInterface ClientInterface, String username, String password) throws RemoteException, NoSuchAlgorithmException;

    public void unregisterForCallback (ClientNotifyInterface ClientInterface, String username, String password) throws RemoteException, NoSuchAlgorithmException;
}
