import java.rmi.Remote;

public interface ServerRegistration extends Remote {
    /** metodo per registrare un utente a WINSOME */
    public String userRegister(String username, String password, String tags);
}
