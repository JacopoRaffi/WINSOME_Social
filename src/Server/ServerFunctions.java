package Server;

import java.rmi.Remote;

public interface ServerFunctions extends Remote {
    /** metodo per registrare un utente a WINSOME */
    public boolean userRegister(String username, String password, String tags);
}
