import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientNotifyInterface extends Remote {

    public void notifyNewFollow(String username) throws RemoteException;

    public void notifyNewUnfollow(String username) throws RemoteException;
}
