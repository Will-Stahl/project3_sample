import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class PeerNode extends UnicastRemoteObject implements PeerNodeInterface {
    private static String IP;
    private static int port;
    private static int machID;
    private static ArrayList<String> fnames;
    private static TrackerInterface server;

    public PeerNode() throws RemoteException {}
    
    public int GetLoad() throws RemoteException {
        return 0;
    }

    public String Download(String fname) throws RemoteException {
        return "";
    }

    //Function for displaying menu options
    private static void DisplayOptions(){
        System.out.println("1. Enter \"Find: <File Name>\" to find a file in the shared directory");
        System.out.println("2. Enter \"Download: <File Name>\" to download a file from a peer");
    }

    // Function for setting a random port number
    private static int GetRandomPortNumber(){
        Random rand = new Random();
        return (rand.nextInt((65535 - 1024) + 1)) + 1024;
    }

    /**
     * scans directory according to machine ID
     * populates static structure with info
     * false if OS failures
     */
    private static boolean ScanFiles() {
        try {
            File dir = new File("files/mach" + machID);
            for (File f : dir.listFiles()) {
                fnames.add(f.getName());
            }
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    public static void main(String[] args){
        if (args.length != 2){
            System.out.println("\n[PEER]: Usage: java PeerNode <hostname> <machID>");
            System.out.println("[PEER]: Exiting...");
            System.exit(0);
        }

        int machID = 0;
        try {
            machID = Integer.parseInt(args[1]);
        } catch (Exception e){
            System.out.println("[PEER]: Mach ID must be a number greater than or equal to 0.");
            System.exit(0);
        }

        try{
            // Initialize peer IP address and port number
            String hostName = args[0];
            IP = InetAddress.getByName(hostName).getHostAddress();
            port = GetRandomPortNumber();

            // Join server as soon as node boots up
            Registry registry = LocateRegistry.getRegistry(hostName, 8000);
            server = (TrackerInterface) registry.lookup("TrackingServer");

            boolean isJoinSuccess = server.Join(IP, port, machID);
            if (isJoinSuccess){
                System.out.println("[PEER]: Connected to server at port 8000.");
            } else {
                throw new RemoteException();
            }
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("[PEER]: It's possible that the server is currently offline. Try joining later.");
            System.exit(0);
        }

        fnames = new ArrayList<String>();
        if (!ScanFiles()) {
            String msg = "[PEER]: Failed to scan directory. Check that src/files/mach";
            msg += machID + " exists with the correct permissions.";
            System.out.println(msg);
            System.exit(0);
        }
    }
}