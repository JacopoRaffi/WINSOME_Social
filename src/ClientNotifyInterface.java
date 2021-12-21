import java.rmi.RemoteException;

public interface ClientNotifyInterface {

    public void notifyNewFollow(String username) throws RemoteException;

    public void notifyNewUnfollow(String username) throws RemoteException;
}
