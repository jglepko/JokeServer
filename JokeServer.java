/*--------------------------------------------------------

1. Josh Glepko / Date: 9-23-18

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

I only used localhost to run each file. I did not try using any
ip addresses when running either of the three programs.

> java JokeClient
> java JokeClientAdmin

5. List of files needed for running the program.

e.g.:

 a. checklist.html
 b. JokeServer.java
 c. JokeClient.java
 d. JokeClientAdmin.java

5. Notes:

e.g.:


----------------------------------------------------------*/

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/* Singleton pattern added so JokeServer class can be called
 * statically anywhere in program.
 * Serializable implemented for when byte streams are sent and
 * recieved over socket pipe.
 */
public class JokeServer implements Serializable
{
    private static volatile JokeServer instance = null;

    private JokeServer() {
    }

    public static JokeServer getInstance() {
        if (instance == null) {
            synchronized (JokeServer.class) {
                if (instance == null) {
                    instance = new JokeServer();
                }
            }
        }
        return instance;
    }

    public static void main(String[] args) throws IOException
    {
        int q_len = 6; // Restricting server to allow 6 or less connections
        int port = 4545; // Port picks which part of network accepts data to connect to

        boolean controlSwitch = true; // So while will loop forever

        AdminLooper AL = new AdminLooper(); // instantiate a separate thread
        Thread t = new Thread(AL);
        t.start(); // listen for Admin client to send data and start

        // Declare socket
        ServerSocket serverSock = null;
        // Create new socket port for worker thread
        try {
            serverSock = new ServerSocket(port);
        } catch (IOException e) {
            System.exit(1);
        }
        // Put socket in while loop so its constantly listening for connections
        while (controlSwitch) {
            Socket sock = serverSock.accept();
            new Worker(sock).start();
        }
    }
}

/* Worker thread created by JokeServer class to do all
 * of work for socket connection that's built.
 */
class Worker extends Thread implements Serializable
{
    Socket sock;

    Worker(Socket s) {
        sock = s;
    }

    public void run() {
        try {
            // Create input stream to receive object from client side of pipe
            ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
            // Cast object type so serialized bytes can be properly rebuilt into object
            CreateObj receivedObject = (CreateObj) in.readObject();
            //System.out.println("Server received obj containing " + receivedObject.text);
            // Create instance of class needed to change mode with administrator
            ToggleMode tm = ToggleMode.getInstance();

            // Output stream object created to send serialized data back to client
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(sock.getOutputStream()));
            if (tm.getMode() == 0)
            {
                System.out.println("JOKE_MODE");
                receivedObject.text = "JOKE_MODE";
                // Writer created to act on stream object
                out.writeObject(receivedObject);
                out.flush();
            }
            else if (tm.getMode() == 1)
            {
                System.out.println("PROVERB_MODE");
                receivedObject.text = "PROVERB_MODE";
                // Writer created to act on stream object
                out.writeObject(receivedObject);
                out.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

class AdminLooper implements Runnable {
    public static boolean adminControlSwitch = true;

    public void run() { // Implement runnable to continuously listen
        System.out.println("In the admin looper thread.");

        int q_len = 6;
        int port = 5050; // Port for admin client to connect on
        Socket sock;

        try {
            ServerSocket servsock = new ServerSocket(port, q_len);
            // Put socket in while loop so its constantly listening for connections from admin
            while (adminControlSwitch) {
                sock = servsock.accept();
                new AdminWorker(sock).start();
            }
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }
}

// Admin controls toggling mode on its own thread
class AdminWorker extends Thread {
    Socket sock; // sock is a variable local to Worker class

    AdminWorker(Socket s) {
        sock = s;
    } // pass s arg through Worker ctor to sock var

    public void run()
    {
        System.out.println("In AdminWorker class run method.");
        try {
            // Create input stream to receive object from client side of pipe
            ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
            // Cast object type so serialized bytes can be properly rebuilt into object
            CreateObj newObj = (CreateObj) in.readObject();
            // Output stream object created to send serialized data back to client
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(sock.getOutputStream()));

            ToggleMode tm = ToggleMode.getInstance();
            // Create new object for wrapping response sent back to admin
            CreateObj outboundObj = new CreateObj(newObj.name, null, null);

            // Logic for changing state invokes singleton in ToggleMode class
            if (newObj.cmd.equals("x")) {
                if (tm.getMode() == 0) {
                    tm.setModetoProverb();
                    System.out.println("Mode has been changed to " + tm.getModeText());
                } else // tm.getMode() == 1
                {
                    tm.setModetoJoke();
                    System.out.println("Mode has been changed to " + tm.getModeText());
                }
            }
            outboundObj.text = "Mode has been set to " + tm.getModeText();
            out.writeObject(outboundObj); out.flush();
        } catch (IOException x) // after lookups performed check data for exceptions
        {
            System.out.println("Server read error");
            x.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

// Class to hold server state for when it needs switched
class ToggleMode implements Serializable {
    private static final int JOKE = 0;
    private static final int PROVERB = 1;
    private int mode = JOKE;

    private static volatile ToggleMode instance = null;

    private ToggleMode() {
    }

    public static ToggleMode getInstance() {
        if (instance == null) {
            synchronized (ToggleMode.class) {
                if (instance == null) {
                    instance = new ToggleMode();
                }
            }
        }
        return instance;
    }

    public void setModetoProverb() {
        this.mode = PROVERB;
    }

    public void setModetoJoke() {
        this.mode = JOKE;
    }

    public String getModeText() {
        if (this.mode == JOKE) {
            return "joke";
        } else // mode == PROVERB
            return "proverb";
    }

    public int getMode() {
        return mode;
    }
}

// Holds joke/proverb data for security reasons
class Passage implements Serializable
{
    private static volatile Passage instance = null;

    private Passage() {
    }

    public static Passage getInstance() {
        if (instance == null) {
            synchronized (Passage.class) {
                if (instance == null) {
                    instance = new Passage();
                }
            }
        }
        return instance;
    }

    public String[] jokeArray = {"What has 132 legs and 8 teeth? The front row of a " +
            "Garth Brooks concert!\n", "Why does Jim Brown want Lebron to remain " +
            "in Cleveland? Because misery loves company!\n",
            "What's the only thing that grows in Cleveland? \n" +
                    "The Crime Rate! \n", "What do you call a Cleveland Brown with a super" +
            "Bowl ring? " + "A thief! \n"};

    public String[] proverbArray = {"Actions speak louder than words.",
            "A journey of a thousand miles begins with a single step.",
            "All good things must come to an end.", "A watched pot never boils."};

    public String[] jokeCodeArray = {"JA", "JB", "JC", "JD"};
    public String[] proverbCodeArray = {"PA", "PB", "PC", "PD"};


}
