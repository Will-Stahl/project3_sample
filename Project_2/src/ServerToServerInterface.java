import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerToServerInterface extends Remote
{
    public boolean UpdateTree(int ID, String article, int replyTo) throws RemoteException;
    public boolean CoordinatorPost(String article, int replyTo) throws RemoteException;
    public String CoordinatorChoose(int articleID) throws RemoteException;
}