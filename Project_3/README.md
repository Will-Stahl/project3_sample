# Project 3: Simple xFS

## Authors: Jashwin Acharya (`achar061`), William Stahl (`stahl186`)

Individual Contributions:

<ul>
    <li> William Stahl
        <ul>
            <li> Implemented peer booting (scanning file list, latency file) </li>
            <li> Implemented peer tracking with data structures </li>
            <li> Implemented `FileDownload` class with random corruption </li>
            <li> Implemented fault tolerance for downloading from peers </li>
            <li> Unit tests for running system as a whole </li>
        </ul>
    </li>
    <li> Jashwin Acharya
        <ul>
            <li> Implemented the complete client UI as well as client request validation functions. </li>
            <li> Implemented all client side unit tests. </li>
            <li> Implemented Fault Tolerance for Server crashing. </li>
            <li> Implemented functionality for server being able to detect offline peers </li>
            <li> Updated Design Documentation and README with instructions on how to run the whole system and test cases and also performed the Download Time Analysis.</li>
        </ul>
    </li>
</ul>

Pledge:

<img src="images/Pledge.png"  width="60%" height="60%">

## Running Server and clients

Navigate to the src folder.

Open one terminal window in this folder and compile Tracker.java using the following command:

```
javac Tracker.java
```

You can start the server using the following command:

```
java Tracker
```

Example image below:

<img src="images/run_server.png"  width="60%" height="60%">

## Running clients and using client UI

Note: Only machIDs between 0 and 4 (inclusive) can be used.

Open a separate terminal window (apart from the server one) and compile PeerNode.java using the command:

```
javac PeerNode.java
```

In the same terminal window, use the following command:

```
java PeerNode localhost 0
```

Example image below:

<img src="images/run_client.png"  width="60%" height="60%">

The above command has the format `java PeerNode <server host name> <machID>`. You can only assign a value of 0, 1, 2, 3 or 4 to the machID. Any other machID produces an error and you will have to run the command again using a valid machID.

Example image below of using an incorrect machID:

<img src="images/run_client_error.png"  width="60%" height="60%">

### Joining the tracker server

The peers automatically join the tracking server when they are first launched, but in the event that the server goes down and the client wants to join again, they can simply enter `join` in the client UI.

Example image below:

<img src="images/join_tracker.png"  width="60%" height="60%">

A peer cannot join the tracker server if it's already part of it.

Example image below:

<img src="images/join_again_error.png"  width="60%" height="60%">

Every peer needs to have a different machID before attempting to join the server. Launching two peers with the same machID causes the peer that was launched at a later time to exit gracefully.

Example image below:

<img src="images/join_machID_error.png"  width="60%" height="60%">

### Leaving the tracker server

A peer can leave the tracker server at any time by entering `leave` in their respective peer UI terminal.

Example image below:

<img src="images/leave_success.png"  width="60%" height="60%">

A peer cannot leave the tracker server if it was never part of it.

Example image below:

<img src="images/leave_error.png"  width="60%" height="60%">

### Finding a file

In order to find a file, a peer can enter the `find: <file name>` in their respective UI terminal. Case doesn't matter for the `find` command, so you can enter `FIND: <file name>` and that should work too.

Example image of a successful `find` command:

<img src="images/find_success.png"  width="60%" height="60%">

Attempting to find a file that doesn't belong to any peer results in an error message being printed.

Example image of an unsuccessful `find` command:

<img src="images/find_error.png"  width="60%" height="60%">

The format of the `find` function matters.

Example image of an incorrectly formatted `find` function:

<img src="images/find_error_2.png"  width="60%" height="60%">

In-case multiple peers have a file, then all peer IDs are printed to the terminal:

<img src="images/find_multiple.png"  width="60%" height="60%">

### Downloading a file

In order to download a file, a peer can enter `download: <file name>` in their respective UI terminal. Case doesn't matter for the `download` command, so you can enter `DOWNLOAD: <file name>` and that should work too.

Note: Since we have simulated file latency, it takes a maximum of 5 seconds to download a file into a peer's folder.

Example image of a successful `download` command:

<img src="images/download_success.png"  width="60%" height="60%">

The format of the `download` function matters.

Example image of an incorrectly formatted `download` function:

<img src="images/download_error.png"  width="60%" height="60%">

Example image of when a `download` function returns an error if the file is already present in the requesting peer's folder or when the file itself doesn't exist:

<img src="images/download_error_2.png"  width="60%" height="60%">

First download command succeeds since peer didn't have the file; second download command fails since file is already present in the requesting peer's folder; and third download command fails since file is not being tracked by any peer.

### Peer attempts to communicate while server is down

Peers are blocked from communicating with a server if it is down.

Example image below:

<img src="images/server_down_error.png"  width="60%" height="60%">

### Peer can communicate with server once its back up

Once Tracker is online, peers can join and communicate with other peers.

Example images below:

<img src="images/server_online_join.png"  width="60%" height="60%">

<img src="images/server_back_online.png"  width="60%" height="60%">

### Server is online but peer crashes

If a peer crashes while it is connected to the server, then the user can simply relaunch the peer from its respective terminal window and it will automatically connect to the server and update its file sharing information.

Example screenshots displaying how fault tolerance works for the above described scenario:

Note: In the images below, the terminal at the bottom runs peer 0 and the one on top right runs peer 1 while the terminal on top left runs the Tracker.

1. Server and 2 peers are in a session and peer 0 downloads a file peer 1:

<img src="images/peer_fault_0.png"  width="60%" height="60%">

2. Peer 0 is forcefully crashed and is no longer communicating with the server:

<img src="images/peer_fault_1.png"  width="60%" height="60%">

3. Peer 0 joins back and automatically connects to the Tracker:

<img src="images/peer_fault_2.png"  width="60%" height="60%">

4. Peer 0 attempts to download file1.txt and is unable to since it had already downloaded it previously, thus proving that rejoining the server after a crash allows the peer to provide the most up-to-date file sharing information to the server.

<img src="images/peer_fault_3.png"  width="60%" height="60%">

Note: Once the peer is launched again, it can around 2-3 seconds for it to connect back to the server since the server needs to update its list of tracked peers.

## Class Design Descriptions

### PeerNode

Runs the command line interface as well as implements `PeerNodeInterface` for inter-peer communication. Thus its remote methods are `Find` and `Download`, `GetLoad` and `Ping`. The handlers for command line input eventually call these methods depending on the input or remote methods on the tracker.
On startup, this class reads its file list into a data structure, joins the server, and updates the server on its files list. It finally starts a thread that listens for command line input. When attempting to handle a download request in particular, there are mechanisms to handle corrupt content and unreachable peers.
To make a download request, it uses `Find` to determine which peers can share the file (the peer does not track state from `Find` between commands). Then, it sorts the peers based on our latency-load function and attempts downloading from peers in that order. It will re-attempt a peer if there is corruption or a `RemoteException` before moving on to the next peer.
The latency-load function simply multiplies the known latency with a call to `GetLoad`, which creates a "ping" index on which to sort the peers. Multiplication was chosen so that load and latency can have an equal contribution to the sorting.
We have multiple command line validation functions that validate the format of different operations such as "join", "leave", "find" and "download". We ensured that the case of the command (ex: JOIN, FIND etc) does not matter in order to keep our user experience simple and straigh-forward.
We decided on a limit of 5 peers for our system since we didn't want to overload the server with too many consecutive requests and also made the decision to have each peer have its own unique MachID which makes it easier for the tracker server to identify the right peers for exchanging files.

### Tracker

Implements the `TrackerInterface`, from which it has the remote methods `Join`, `Leave`, `Find`, `UpdateList`, and `Ping`(unused). When a node joins, the tracker tracks its info with the `TrackedPeer` class. It also tracks reachable files with `FileInfo`. `TrackedPeer` and `FileInfo` are used together as a two-way table, where a `FileInfo` entry has a list of `TrackedPeer`s that can service the file, and each `TrackerPeer` has a list of filenames with which it can hash into the `FileInfo` list. The tracker updates these accordingly when nodes join, leave, use `UpdateList`, and die. This information is used to service a `Find` request.
The tracker also starts and maintains the Java RMI registry for the whole system, where peers must register themselves on this registry. One limitation of the tracker itself creating the registry is that in the situation that the tracker is down, we can launch peers with the same machID since peers use the registry for finding out if a peer with a machID is already currently running. If the tracker is first launched and peers are launched after that, then this is not an issue and if a peer attempts to use a machID currently in the register, then our code prompts the user to run the new peer with some other machID.

### TrackedPeer

This class is useful for tracking information associated with a peer such as their machID, IP address, port number, currently tracked files, a remote reference to its object on the RMI registry, and also the "ping" which indicates the current latency and load of the peer. The class is particularly useful when different peers need to be queried for a file download and we have defined code in PeerNode.java which chooses peers based on their latency and load.

### FileInfo

This class houses information for a file such as what peers currently possess the file, what's the name of the file as well as its checksum details. This is particularly useful for the `Find` function defined in Tracker.java which returns a list of peers that currently possess this file in their respective directories.

### FileDownload

This class allows File Downloads to occur between clients by computing checksum values using the CRC32 hash operation and also has a function called `AddNoise()` that corrupts the file randomly, which is called in the constructor. If a file has 5 bytes, then there exists a 5/10000 chance that it will be corrupted. In case a download operation fails due to a file being corrupted, the peer attempts to re-download the file one time before trying another peer (according to code we've written in the DownloadAsClient function in PeerNode.java).

### ComparePeer

This is a class that implements the `Comparator<T>` interface. It compares the load and latency index of peers (`ping` attribute) so that the `ArrayList`'s sort function returns them in sorted order. This way, our algorithm in the `DownloadAsClient()` function defined in `PeerNode.java` can make an accurate decision on what peer to query for a file.

## Handling Fault Tolerance Scenarios

### Downloaded files can be corrupted

As we've defined earlier in the "Class Design Descriptions" section, there is a random chance of num_bytes/10000 of a file being corrupted. If a downloaded file's checksum value does not match the correct checksum value of the file (i.e., it was corrupted while the download was happening), then we attempt to re-download the file from the same peer again. If our 2nd attempt also leads to a checksum failure, then we move onto the peer who has the next smallest "ping" value.

### Tracking Server Crashes

Once a server crashes, all currently active peers are unable to send any requests to the server. You can still type "join", "leave" etc in their respective UI terminals, but none of these requests will be forwarded to the server as the remote server object is offline. Peers are also unable to communicate with each other while the server is down as the server is responsible for keeping track of which peers contain certain files. Once the server is back up, you can type "`join`" in each of the individual peer UI terminals, thus allowing the peers to rejoin the new registry created by the server, and the server is also populated with each peer's respective file and folder structure since `UpdateList` is called everytime a peer joins the network.

### Peer Crashes

Anytime a peer goes down, the server stops keeping track of the peer since it is offline. We implemented a pinging mecahnism for peers where the server pings peers every 2 seconds to ensure they are online (line 151 in `Tracker.java`). The reason for keeping the ping interval at 2 seconds is that we have multiple clients in our system that are constantly sharing files with each other and since the tracker is the central point of communication, it is imperative that the tracker is always updated with the list of currently active peers. To make a peer join the tracker server again, you can simply relaunch the peer in a new terminal window. If the server is online, then the peer will be able to join the server and also calls the `UpdateList` function defined in Tracker.java using the remote server object to let the tracker know of its file and folder structure. If the server is offline and is back online at a later time, then you can simply type "`join`" in the peer UI terminal and you should be able to join the server.

### Exceptional situations

#### All peers are down

In this case, no communication and exchange of files takes place among peers since they are all down.

#### Fail to find a file

In this case, the peer terminal usually prints an error message saying that the tracker is currently not tracking the requested file and the user is prompted to enter a new command.

Note: One small limitation of our system is that the peer does not periodically update the server with its list of files. A peer only updates its file sharing details when it is launched, when it leaves/joins (using the server using the command line operations "join" and "leave" that we have defined), or when it successfully downloads from another peer. So, if a user were to manually delete a file from a peer's folder, the peer would have to leave the tracking server and then join back to allows the tracker to be updated with the latest file sharing information associated with that peer. A successful download would NOT accomplish this since the peer tracks its files state with data structures when it is up (it only scans its directory on startup).

### Download Time Analysis

We noticed that distributing a file across different peers causes the download time for that particular file to lower as the number of peers possessing the file increases. The following analysis was done based on different load and latency situations where Peer 0 picked different peers depending on if those peers were doing subsequent download operations or not.

In all these cases, we only care about how long it takes to download "foo.txt" into peer 0's folder depending on how many peers have it.

<ul>
    <li>Peer 1 contains "foo.txt": Only Peer 0 and Peer 1 are active and Peer 0 issues a download command for "foo.txt": Time to download is 4 seconds</li>
    <li>Peer 2 and 3 contain "foo.txt": Peer 0, 2 and 3's terminal windows are active and peer 0 issues a download request for "foot.txt". Time to download is 3.67 seconds and the peer selected for download was Peer 3.</li>
    <li>Peer 2, 3 and 4 contain "foo.txt": Peer 0, 2, 3 and 4's terminal windows are active and peer 0 issues a download request for "foo.txt"; peer 3 also issues a download request for "file2.txt"; peer 2 issues a download request for "file4.txt"; and peer 4 issues a download request for "file3.txt". Time to download is 3.36 seconds and the peer selected for download is peer 4.</li>
    <li> Peer 1, 2, 3 and 4 contain "foo.txt": All peer terminals are active and peer 0 issues a download request for "foo.txt"; peer 4 issues a download request for "file3.txt"; peer 3 issues a download request for "file2.txt"; peer 2 issues a download request for "file3.txt"; and peer 1 issues a download request for "file2.txt". Time to download is 2.9 seconds and the peer selected for download is peer 4.
</ul>

<img src="images/analysis.png"  width="60%" height="60%">

As we can see in the graph above, as the number of peers holding a file increases along with the number of subsequent download operations, the download time for "foo.txt" generally decreases for peer 0.

## Testing Description

We have two separate test files for testing UI related commands and server commands.

The first file is ClientTestCases.java which contains 4 tests for checking valid `find` and `download` commands and 8 test cases for checking invalid `find` and `download` commands.

The second file is SystemTests.java which contains the following test functions:

<ul>
    <li>TestFind(): Contains 5 assert statements for checking if the `Find` command returns valid answers from the server.</li>
    <li>TestDownload(): Contains 4 asserts for checking `Download` functionality, where the actual contents of the file are checked against the original source of that file. </li>
    <li>TestShare(): Contains 7 asserts for checking that a file is shareable once it is downloaded from another peer's folder. </li>
    <li>TestTrackerFault(): Contains 4 asserts that check if a peer can join once the tracker is back online and that files can be found once the peers have joined the tracker. </li>
    <li>TestPeerFault(): Contains 4 assert checks for checking if a peer can gracefully rejoin once it has crashed and can also share files.</li>
    <li>TestLatencyChoice(): 1 test that checks if the correct peers are chosen based on latency given that there is no additional load.</li>
    <li>TestPeerChoice(): Contains 2 test cases that check if a peer is chosen based on their current load.</li>
    <li>TestSimultaneousDownloads(): Contains 4 asserts for checking that simulataneous downloads complete successfully.</li>
</ul>

## Running Tests

Make sure all currently active terminals have been closed before attempting to run any tests.

Navigate to the test directory from root with

```
cd test
```

### Running Client Side tests

Compile Client side tests with the following command:

```
javac -cp ./../lib/junit-4.13.2.jar:. RunClientTestCases.java
```

Run the client side tests with the following command:

```
java -cp ./../lib/junit-4.13.2.jar:./../lib/hamcrest-core-1.3.jar:. RunClientTestCases
```

Example image below of all client side tests passing:

<img src="images/client_tests.png"  width="60%" height="60%">

### Running System tests

Compile ystem tests with the following command:

```
javac -cp ./../lib/junit-4.13.2.jar:. RunTests.java
```

Run the system tests with the following command:

```
java -cp ./../lib/junit-4.13.2.jar:./../lib/hamcrest-core-1.3.jar:. RunTests
```

Example image below of all System tests passing:

<img src="images/test_passing.png"  width="60%" height="60%">

Notes:

- Note that the tests manipulate the `files` directory, so any files downloaded via command line interface may be removed.
- It is assumed that files with names corresponding to their respective directories are always present, so do not delete them (i.e. `mach0/file0.txt`).
- An oddity we noted on the CSE lab machines was that tests seemed fail when run for the first time after compilation, but subsequent attempts tended to yeild successful tests. Due to process scheduling, the tests don't seem to run in a totally deterministic way. During our testing, the tests passed almost 99% of the time without throwing any errors.
- Tests will take over a minute to complete due to simulated download latency and process scheduling.
- If it takes significantly longer than a minute, and is accompanied with no new output from from the test process, it might have blocked due to waiting for stream output (which means something went wrong). This, however, never actually happened with our final build and final tests implementation.
- If the tests keep failing on repeated attempts (the chances are rare), please `pkill java` to kill all java processes and then you can restart the tests. It's possible that there are lingering java processes running in the background that might be interfering with the tests. If this doesn't work, restart your PC and then run the tests again.

One limitation of our tests is that we could not verify that our checksum functionality detected a corrupt file. This is because the file corruption, as simulated, is not test-deterministic. The peers only silently re-attempt when a file is corrupt.
