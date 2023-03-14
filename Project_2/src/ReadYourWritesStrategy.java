import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;


// TODO: use blocking or nonblocking?
// Nonblocking probably can't guarantee this consistency
public class ReadYourWritesStrategy implements ConsistencyStrategy {
    public boolean ServerPublish(String article, int replyTo,
                        BulletinBoardServer selfServer) {
        return false;
    }

    public String ServerRead(BulletinBoardServer selfServer) {
        return "";
    }

    public String ServerChoose(int articleID, ReferencedTree contentTree) {
        return "";
    }

}