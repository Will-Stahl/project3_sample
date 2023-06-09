import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PubSubServerInterface extends Remote
{
    public boolean Join(String IP, int Port) throws RemoteException;
    
    public boolean Leave(String IP, int Port) throws RemoteException;
    
    public boolean Subscribe(String IP, int Port, String Article) throws RemoteException;
    
    public boolean Unsubscribe(String IP, int Port, String Article) throws RemoteException;
    
    public boolean Publish(String Article, String IP, int Port) throws RemoteException;
    
    public boolean Ping() throws RemoteException;

    public boolean Ping(String Host, String ServerName) throws RemoteException;
}