import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;

public interface ConsistencyStrategy
{
    // TODO: the implementations might not store data, so methods might be static
    
    /**
     * central server calls this
     * central server writes to peer servers according to strategy
     */
    public boolean ServerPublish(String article);

    public String ServerRead();
    public String ServerChoose(Integer articleID);
    public boolean ServerReply(String article);
}