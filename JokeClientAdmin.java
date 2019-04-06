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

I only ran all three files exactly as shown below:

> java JokeServer
> java JokeClient
> java JokeClientAdmin

I did not run them in terminal with an ip address.

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
import java.net.*;
import java.util.Scanner;

// Check all variable names.
public class JokeClientAdmin
{
    public static void main (String args[])
    {
        String cmd;

        String serverName = "localhost";

        // Output to console for user
        System.out.println("Glepko's Admin Client started.\n");
        System.out.println("Server one: " + serverName + ", Port: 5050");
        System.out.println("Now communicating with: " + serverName + ", Port: 5050");
        System.out.println("Enter quit to exit from client.\n");
        Scanner kbInput = new Scanner(System.in);

        // Put prompt in while loop so its constantly listening for commands from user
        do {
            System.out.println
                    ("Press x to toggle between joke and proverb mode, (quit) to end.");
            cmd = kbInput.nextLine();
            if (cmd != "quit")
            {
                // Call encapsulated function to perform read/write to server
                getRemoteAddress(cmd);
            }

        } while (cmd != "quit");
    }

    static void getRemoteAddress(String cmd){
        try{
            // Create new object to wrap so data can sent over socket to server
            CreateObj objToSend = new CreateObj(null, cmd, null);
            // Create socket connection for localhost listening at server port 5050
            Socket socket = new Socket("localhost", 5050);

            // Output stream object created to send serialized data back to client
            ObjectOutputStream objectOut = new ObjectOutputStream(socket.getOutputStream());
            //System.out.println("Admin is connected.");
            objectOut.writeObject(objToSend); objectOut.flush();
            //System.out.println("sent cmd to server.");

            // Create input stream to receive object from client side of pipe
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            // Cast object type so serialized bytes can be properly rebuilt into object
            CreateObj textFromServer = (CreateObj) in.readObject();
            if (textFromServer != null) System.out.println(textFromServer.text);

            socket.close();
        } catch (IOException x) {
            System.out.println ("Socket error.");
            x.printStackTrace ();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}
