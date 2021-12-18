package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerRegistry extends Remote {
    /** metodo per registrare un utente a WINSOME */
    boolean userRegister(String username, String password, String tags) throws RemoteException;
}
