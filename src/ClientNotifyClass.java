import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientNotifyClass extends RemoteObject implements ClientNotifyInterface{
    private List<String> followers;
    private final Lock listLock;

    public ClientNotifyClass(List<String> followers) throws RemoteException {
        super( );
        listLock = new ReentrantLock();
        this.followers = followers;
    }

    public void notifyNewFollow(String username) throws RemoteException{
        try{
            listLock.lock();
            followers.add(username);
        }finally {
            listLock.unlock();
        }

    }

    public void notifyNewUnfollow(String username) throws RemoteException{
        try{
            listLock.lock();
            followers.remove(username);
        }finally {
            listLock.unlock();
        }
    }
}
