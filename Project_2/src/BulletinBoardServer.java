import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class BulletinBoardServer extends UnicastRemoteObject
implements BulletinBoardServerInterface, ServerToServerInterface {
    
    private int serverPort; 
    private int serverNum;
    private static ArrayList<ClientInfo> clients = new ArrayList<>();
    private ArrayList<BulletinBoardServerInterface> serverList;
    private final int MAX_CLIENTS = 5;
    private static int clientCount = 0;
    private List<BulletinBoardServerInterface> readQuorum;
    private List<BulletinBoardServerInterface> writeQuorum;

    // P2P data structures
    private int coordNum;
    private int coordPort;
    private String coordHost;
    private int nextID;
    private String consistency;
    private ConsistencyStrategy cStrat;  // initialize to specifc strategy
    private ReferencedTree contentTree;  // article tree data structure
    private String serverHost;

    public BulletinBoardServer(int serverPort, int serverNum, String hostname, String consistency) throws RemoteException{
        this.serverPort = serverPort;
        this.serverNum = serverNum;
        coordNum = 5;  // coordinator hard-chosen as highest number
        nextID = 1;
        contentTree = new ReferencedTree();
        coordPort = 2004;
        coordHost = "localhost";
        if (serverNum == 5){
            serverHost = coordHost;
        } else {
            serverHost = hostname;
        }
        serverList = new ArrayList<>();
        readQuorum = new ArrayList<>();
        writeQuorum = new ArrayList<>();
        this.consistency = consistency;
        
        if (consistency.equals("sequential")) {
            cStrat = new SequentialStrategy();
        } else if (consistency.equals("readyourwrites")) {
            cStrat = new ReadYourWritesStrategy();
        } else if (consistency.equals("quorum")) {
            cStrat = new QuorumStrategy();
        }
        else {
            System.out.println("Invalid strategy entered, defaulting to sequential");
            cStrat = new SequentialStrategy();
            this.consistency = "sequential";
        }
    }

    /**
     * Function for registering a client on this server.
     * @param IP: Client IP address
     * @param Port: Client Port 
     */
    public boolean Join(String IP, int Port) throws RemoteException
    {
        if (Port > 65535 || Port < 0) {
            System.out.println("[SERVER]: Client port number is invalid and cannot be used for communication.");
            return false;
        }

        // If server is at max capacity, then prompt user to try joining again at a later time.
        if (clientCount == MAX_CLIENTS) {

            System.out.println("[SERVER]: Server Capacity reached! Please try joining again later.");
            return false;
        }

        // If client has been added earlier, then don't add them again.
        for (int i = 0; i < clients.size(); i++) {
            ClientInfo cl = clients.get(i);
            if (cl.GetIP().equals(IP) && cl.GetPort() == Port) {
                System.out.println("[SERVER]: Client is already part of the group server.");
                return false;
            }
        }

        // check for valid IP address
        if (IsValidIPAddress(IP)){
            clients.add(new ClientInfo(IP, Port));
            clientCount += 1;
            System.out.printf("\n[SERVER]: Added new client with IP: %s, Port: %d\n", IP, Port);
            return true;
        }
        System.out.println("[SERVER]: Invalid IP Address");
        return false;
    }

    /**
     * Function for checking if the client IP address is valid
     * @param IP
     * @return
     */
    private static boolean IsValidIPAddress(String IP) {
        String[] parts = IP.split("\\.");

        if (parts.length != 4) return false;

        for (int i = 0; i < parts.length; i++){
            String part = parts[i];
            if (Integer.parseInt(part) < 0 || Integer.parseInt(part) > 255) return false;
        }
        return true;
    }

    /**
     * Function for de-registering a client from the server.
     * @param IP: Client IP address
     * @param Port: Client Port
     */
    public boolean Leave(String IP, int Port) throws RemoteException{
        // check for subscriber in Subscribers
        ClientInfo clientPtr = null;
        for (int i = 0; i < clients.size(); i++) {
            ClientInfo Sub = clients.get(i);
            if (Sub.GetIP().equals(IP) && Sub.GetPort() == Port) {
                clientPtr = clients.get(i);
                clients.remove(i);
            }
        }
        if (clientPtr == null) {
            System.out.printf("[SERVER]: Client at IP Address %s is and Port %d is not currently part of the server.\n", IP, Port);
            return false;
        }
        
        clientCount -= 1;
        System.out.printf("[SERVER]: Removed client at address %s and Port %d.\n", IP, Port);
        return true;
    }

    /**
     * @param article content to publish
     * calls strategy
     */
    public boolean Publish(String article) throws RemoteException {
        return cStrat.ServerPublish(article, 0, this);
    }

    /**
     * @param article content to publish
     * @param replyTo ID of article to reply to
     * if primary server, uses strategy (non-remote method)
     * if not, uses RMI to contact primary
    */
    public boolean Reply(String article, int replyTo) throws RemoteException {
        // same as call to ServerPublish() in Publish(), but with replyTo
        if (replyTo < 0) {
            return false;
        }
        return cStrat.ServerPublish(article, replyTo, this);
    }

    /**
     * returns indented string previewing all articles with ID
     * for now, we delegate viewing details to client
    */
    public String Read() throws RemoteException {
        return cStrat.ServerRead(this);
    }

    /**
     * @param articleID ID of article requested in full
    */
    public String Choose(int articleID) throws RemoteException {
        if (articleID < 1) {
            return "";
        }
        return cStrat.ServerChoose(this, articleID);
    }


    /**
     * from ServerToServerInterface
     * @param article content to publish
     * @param replyTo ID of article to reply
     * this server should be the coordinator if this method is called on it
     * calls ServerPublish() using strategy object
     */
    public boolean CoordinatorPost(String article, int replyTo) throws RemoteException {
        try {
            return cStrat.ServerPublish(article, replyTo, this);
        } catch (Exception e) {
            return false;
        } 
    }

    /**
     * from ServerToServerInterface
     * should be called by coordinator on non-coordinators
     * @param newID unique ID generated by coordinator
     * @param article string consisting of article
     * @param replyTo specify article to reply to 
     * if 0, it replies to root, which is just a new post
     */
    public boolean UpdateTree(int newID, String article, int replyTo)
                                throws RemoteException {
        try {
            return contentTree.AddNode(newID, article, replyTo);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * from ServerToServerInterface
     * should only be called on coordinator server object
     * @param articleID ID if article to return in full
     * returns message if article isn't found
     */
    public String CoordinatorChoose(int articleID) throws RemoteException {
        return cStrat.ServerChoose(this, articleID);
    }

    private static boolean CheckValidPort(int port){
        Set<Integer> ports = new HashSet<>();
        ports.add(2000);
        ports.add(2001);
        ports.add(2002);
        ports.add(2003);
        ports.add(2004);

        return ports.contains(port);
    }

    public void IncrementID() {
        nextID++;
    }

    /**
     * Function for initializing read and write quorums
     * @param NR: Number of read quorum servers
     * @param NR: Number of write quorum servers
     */
    public void SetQuorums(int NR, int NW){
        // Add coordinator to server list so that have have Nr + Nw - 1 servers
        if (!serverList.contains(this)){
            this.serverList.add(this);
        }
        Collections.shuffle(this.serverList);

        this.writeQuorum = new ArrayList<>();
        this.readQuorum = new ArrayList<>();

        // Add to write quorum after shuffling server list
        for (int i = 0; i < NW; i++){
            this.writeQuorum.add(serverList.get(i));
        }

        // Add to read quorum after shuffling server list
        for (int i = NW-1; i < NR + NW - 1; i++){
            this.readQuorum.add(serverList.get(i));
        }
    }

    // function for pinging coordinator from other servers
    public boolean PingCoordinator() throws RemoteException{
        try{
            Registry registry = LocateRegistry.getRegistry(this.GetServerHost(), 2004);
            BulletinBoardServerInterface server = (BulletinBoardServerInterface) 
                                                    registry.lookup("BulletinBoardServer_" + 5);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    // Function for client pinging other servers
    public boolean Ping() throws RemoteException {
        try{
            Registry registry = LocateRegistry.getRegistry(this.GetServerHost(), this.GetServerPort());
            BulletinBoardServerInterface server = (BulletinBoardServerInterface) 
                                                    registry.lookup("BulletinBoardServer_" + this.GetServerNumber());
            System.out.println("[SERVER]: Client pinged server. Server is online.");
            return true;
        } catch (Exception e){
            return false;
        }
    }

    /**
     * Function for finding the most recent ID as that indicates which is the latest article
     * on the bulletin board.
     * @param articleString - String representing all the articles in the bulletin board that are separated
     * by newline characters to account for replies and new posts.
     * @return - The latest ID
     */
    private static int GetMostRecentArticleID(String articleString){
        // Split article String into individual articles
        String[] lines = articleString.split("\n");
        int mostRecentID = -1;

        for (int i = 0; i < lines.length; i++){
            String line = lines[i].trim();

            // Extract article ID from the article.
            int articleID = Integer.parseInt(line.substring(0, 1));

            // Update value of most recent article
            if (articleID > mostRecentID){
                mostRecentID = articleID;
            }
        }

        return mostRecentID;
    }

    /**
     * Sync operation called repeatedly in the background to make sure all replicas are upto date.
     * This function also updates the current list of online servers.
     * @param coordinator - Coordinator server object for retrieving the current list of online servers
     */
    private static void Synch(BulletinBoardServerInterface coordinator){
        try {
            // Regularly update the current list of joined servers for the coordinator to keep
            // track off.
            Iterator<BulletinBoardServerInterface> it = coordinator.GetServerList().iterator();
            while (it.hasNext()){
                BulletinBoardServerInterface serv = it.next();
                try {
                    serv.PingCoordinator();
                } catch (Exception e){
                    it.remove();
                }
            }

            // Get most upto date replica (doesn't always have to be the coordinator)
            ReferencedTree mostRecentTree = coordinator.GetTree();
            if (mostRecentTree.ReadTree().length() != 0){
                int mostRecentID = GetMostRecentArticleID(mostRecentTree.ReadTree());

                // Loop through all replicas to ensure we have the most latest article ID
                for (BulletinBoardServerInterface serv : coordinator.GetServerList()){
                    String article = serv.GetTree().ReadTree();
                    int articleID = GetMostRecentArticleID(article);
                    if ( articleID > mostRecentID){
                        mostRecentID = articleID;
                        mostRecentTree = serv.GetTree();
                    }
                }
            }
            
            // Update all servers with the latest bulletin board information (including coordinator)
            coordinator.SetTree(mostRecentTree);
            for (BulletinBoardServerInterface serv : coordinator.GetServerList()){
                serv.SetTree(mostRecentTree);
            }
            
        } catch (Exception e){
            System.out.println("[SERVER]: Ensure coordinator is online. Exiting...");
            System.exit(0);
        }
    }

    // =============== getters/setters ==================
    public String GetCoordHost() {
        return coordHost;
    }

    public int GetCoordPort() {
        return coordPort;
    }

    public int GetCoordNum() {
        return coordNum;
    }

    public int GetServerNumber() {  // as in self server number
        return serverNum;
    }

    public int GetServerPort() {
        return serverPort;
    }

    public String GetServerHost(){
        return serverHost;
    }

    public ReferencedTree GetTree() {
        return contentTree;  // allow strategies to manipulate tree
    }

    public int GetCurrID() {
        return nextID;
    }

    public String GetConsistenyString(){
        return consistency;
    }

    public ArrayList<BulletinBoardServerInterface> GetServerList(){
        return serverList;
    }

    public void AddToServerList(BulletinBoardServerInterface server){
        serverList.add(server);
    }

    public void SetTree(ReferencedTree tree){
        contentTree = new ReferencedTree(tree);
    }

    public List<BulletinBoardServerInterface> GetWriteQuorum(){
        return writeQuorum;
    }

    public List<BulletinBoardServerInterface> GetReadQuorum(){
        return readQuorum;
    }

    public static void main(String[] args){
        // If no argument is specified, then print error message and exit
        if (args.length != 3){
            System.out.println("\n[SERVER]: Usage: java BulletinBoardServer <hostname> <port> <consistency>");
            System.out.println("[SERVER]: Exiting...");
            System.exit(0);
        }

        int port = 0;
        try {
            port = Integer.parseInt(args[1]);
        } catch (Exception e){
            System.out.println("\n[SERVER]: Port number specified is invalid. Valid port numbers are 2000, 2001, 2002, 2003 and 2004");
            System.out.println("[SERVER]: Exiting...");
            System.exit(0);
        }

        // If port is invalid, then print error message and exit.
        if (!CheckValidPort(port)){
            System.out.println("\n[SERVER]: Port number specified is invalid. Valid port numbers are 2000, 2001, 2002, 2003 and 2004");
            System.out.println("[SERVER]: Exiting...");
            System.exit(0);
        }

        // Fixed set of ports are mapped to specific server numbers
        HashMap<Integer, Integer> portToServerMap = new HashMap<>();
        portToServerMap.put(2000, 1);
        portToServerMap.put(2001, 2);
        portToServerMap.put(2002, 3);
        portToServerMap.put(2003, 4);
        portToServerMap.put(2004, 5);

        try{
            int serverNum = portToServerMap.get(port);
            BulletinBoardServerInterface server = new BulletinBoardServer(port, serverNum, args[0], args[2]);
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("BulletinBoardServer_" + serverNum, server);

            // Connect to central server if this is a replica
            if (serverNum != 5){
                try{
                    registry = LocateRegistry.getRegistry("localhost", 2004);
                    // Update the content tree of a newly joined server if it joins later in the session.
                    BulletinBoardServerInterface coordinator = (BulletinBoardServerInterface) registry.lookup("BulletinBoardServer_" + 5);

                    if (!coordinator.GetConsistenyString().equals(server.GetConsistenyString())){
                        System.out.println("[SERVER]: Replica's consistency protocol must match coordinator's consistency protocol.");
                        System.out.println("[SERVER]: Please re-run the replica with the correct consistency. Exiting...");
                        System.exit(0);
                    }

                    server.SetTree(coordinator.GetTree());
                    coordinator.AddToServerList(server);

                    // Ping coordinator server periodically
                    Timer timer = new Timer();
                    TimerTask task = new TimerTask() {
                        public void run(){
                            try {
                                coordinator.PingCoordinator();
                            } catch (Exception e){
                                System.out.println("[SERVER]: Coordinator is offline. Exiting...");
                                System.exit(0);
                            }
                        }
                    };
                    timer.schedule(task, 0, 1000);
                } catch (Exception e){
                    System.out.println("[SERVER]: Please start the coordinator server first.");
                    System.exit(0);
                }
            } else {
                // Is the coordinator
                // Perform SYNCH operation every 2 seconds to update all servers
                // with the latest bulletin board.
                Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    public void run(){
                        Synch(server);
                    }
                };
                timer.schedule(task, 0, 2000);
            }
            System.out.printf("\n[SERVER]: Bulletin Board Server %d is ready at port %d. \n", serverNum, port);
        } catch(Exception e) {
            System.out.println("\n[SERVER]: Error occurred while launching server. It's possible that the port specified is currently in use.");
            System.out.println("[SERVER]: Exiting...");
            System.exit(0);
        }
    }
}