/*--------------------------------------------------------

1. Name Joshua Glepko/ Date: 9-23-18

2. Java version used, if not the official version for the class:

e.g. build 1.8.0_181

3. Precise command-line compilation examples / instructions:

e.g.:

> javac JokeServer.java


4. Precise examples / instructions to run this program:

e.g.:

In separate shell windows:

> java JokeServer
> java JokeClient
> java JokeClientAdmin

All acceptable commands are displayed on the various consoles.

I only ran all three files exactly as shown below:

> java JokeServer
> java JokeClient
> java JokeClientAdmin

I did not run them with an ip address listed.

5. List of files needed for running the program.

e.g.:

 a. checklist.html
 b. JokeServer.java
 c. JokeClient.java
 d. JokeClientAdmin.java

5. Notes:

e.g.:

After three or four requests for jokes/proverbs from the client
side there may or may not be a bug with the flush function. As a result of this
the user may have to press enter twice if the console hangs.

----------------------------------------------------------*/

import java.io.*;
import java.net.Socket;
import java.util.*;

public class JokeClient {
    public static void main(String[] args) {
        String serverName;
        Boolean flag = true;

        if (args.length < 1)
            serverName = "localhost";
            // Assign argument to serverName variable
        else serverName = args[0];

        System.out.println("Glepko's Joke Client, 1.8.\n");
        System.out.println("Server one: " + serverName + ", Port: 4545");
        System.out.println("Now communicating with: " + serverName + ", Port: 4545");
        System.out.println("Enter quit to exit from client at any time.\n");

        Scanner kbInput = new Scanner(System.in);

        System.out.println("Please enter user name. ");
        String name = kbInput.nextLine();

        ToggleMode tm = ToggleMode.getInstance();
        CreateObj newClientObj = new CreateObj(name, null, tm);

        // Put prompt in while loop so its constantly listening for commands from user
        do {
            System.out.println
                    ("Press enter for joke or proverb.");
            String cmd = kbInput.nextLine();
            // Assigning command to new client object to pass to server
            newClientObj.cmd = cmd;

            if (cmd == "quit")
                break;
            // Call encapsulated function to perform logic of messages displayed
            clientWorker(newClientObj);
        } while (flag);
        System.out.println("Cancelled by user request.");
    }

    // Client only alters the object even though joke/proverb data is stored on server
    // for security purposes.
    static void clientWorker(CreateObj newClientObj) {
        try {
            // Create socket connection for localhost listening at port 4545
            Socket socket = new Socket("localhost", 4545);

            // Output stream object created to send serialized data back to client
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            // Write objects data to server sending command over
            out.writeObject(newClientObj);
            out.flush();

            // Create input stream to receive object from client side of pipe
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            // Cast object type so serialized bytes can be properly rebuilt into object
            CreateObj serverObj = (CreateObj) in.readObject();

            // Receive from server which mode it's currently in then print message to console based on
            // logic shown below
            if (serverObj != null)
                if (serverObj.text.equals("JOKE_MODE") == true)
                {
                    if (newClientObj.jokeAL.isEmpty() != true) {
                        newClientObj.text = newClientObj.jokeAL.get(0);
                        newClientObj.passageCode = newClientObj.jokeHashMap.get(newClientObj.text);
                        System.out.println(newClientObj.passageCode + " " + newClientObj.name + ": "
                                + newClientObj.text);
                    } else { // if jokeAL is empty
                        newClientObj.buildJokeAL();
                        newClientObj.text = newClientObj.jokeAL.get(0);
                        newClientObj.passageCode = newClientObj.jokeHashMap.get(newClientObj.text);
                        System.out.println(newClientObj.passageCode + " " + newClientObj.name + ": "
                                + newClientObj.text);
                    }
                }
                else if (serverObj.text.equals("PROVERB_MODE") == true)
                {
                    if (newClientObj.proverbAL.isEmpty() != true) {
                        newClientObj.text = newClientObj.proverbAL.get(0);
                        newClientObj.passageCode = newClientObj.proverbHashMap.get(newClientObj.text);
                        System.out.println(newClientObj.passageCode + " " + newClientObj.name + ": "
                                + newClientObj.text);
                    } else {
                        newClientObj.buildProverbAL();
                        newClientObj.text = newClientObj.proverbAL.get(0);
                        newClientObj.passageCode = newClientObj.proverbHashMap.get(newClientObj.text);
                        System.out.println(newClientObj.passageCode + " " + newClientObj.name + ": "
                                + newClientObj.text);
                    }
                }
            // If joke has been used we will find it in objects stored array and remove it so not shown again
            for (int i = 0; i < newClientObj.jokeAL.size(); i++) {
                if (newClientObj.jokeAL.get(i).equals(newClientObj.text)) {
                    newClientObj.jokeAL.remove(i);
                    break;
                }
            }

            // If proverb has been used we will find it in objects stored array and remove it so not shown again
            for (int i = 0; i < newClientObj.proverbAL.size(); i++) {
                if (newClientObj.proverbAL.get(i).equals(newClientObj.text)) {
                    newClientObj.proverbAL.remove(i);
                    break;
                }
            }
            // Disconnect socket
            socket.close();
        } catch (IOException x) {
            System.out.println("Socket error.");
            x.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

// Class to create object for each client that connects to server.
class CreateObj implements Serializable {
    public String name;
    public String cmd;
    public String text;
    public String passageCode;
    public ToggleMode tm;

    public ArrayList<String> jokeAL = new ArrayList<String>();
    public ArrayList<String> proverbAL = new ArrayList<String>();
    //    public ArrayList<String> jokeAL = new ArrayList<>();
//    public ArrayList<String> proverbAL = new ArrayList<>();
    public HashMap<String, String> jokeHashMap = new HashMap<String, String>();
    public HashMap<String, String> proverbHashMap = new HashMap<String, String>();

    public CreateObj() {
    }

    public CreateObj(String name, String cmd, ToggleMode tm) {
        this.name = name;
        this.cmd = cmd;
        this.text = text;
        this.passageCode = passageCode;
        this.tm = tm;
        // Instantiate object of class on server holding joke/proverb data needed
        Passage passage = Passage.getInstance();
        // Pull joke/proverb data from server and load into client objects arraylists for logic
        buildJokeAL(); buildProverbAL();

        // Store jokes corresponding joke code prefix in hashmap for accessability
        for (int i = 0; i < passage.jokeCodeArray.length; i++) {
            jokeHashMap.put(passage.jokeArray[i], passage.jokeCodeArray[i]);
        }
        // Store proverbs corresponding joke code prefix in hashmap for accessability
        for (int i = 0; i < passage.jokeCodeArray.length; i++) {
            proverbHashMap.put(passage.proverbArray[i], passage.proverbCodeArray[i]);
        }
    }
    // Pull joke/proverb data from server and load into client objects arraylists then randomize
    public void buildJokeAL() {
        Passage passage = Passage.getInstance();
        for (int i = 0; i < 4; i++) {
            this.jokeAL.add(passage.jokeArray[i]);
        }
        Collections.shuffle(jokeAL);
    }
    // Pull joke/proverb data from server and load into client objects arraylists then randomize
    public void buildProverbAL() {
        Passage passage = Passage.getInstance();
        for (int i = 0; i < 4; i++) {
            this.proverbAL.add(passage.proverbArray[i]);
        }
        Collections.shuffle(proverbAL);
    }
}