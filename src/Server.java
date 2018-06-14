import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{
    
    //static ServerSocket variable
    private static ServerSocket server;
    //socket server port on which it will listen
    private static int port = 9878;
    public Server()
    {
    }
    public void run(){
    	
    	try {
    		server = new ServerSocket(port);
            while(true)
            {
            	System.out.println("Server is waiting");
            	 Socket socket = server.accept();
            	 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            	 String message = (String) ois.readObject();
            	 System.out.println("Server received message: " + message);
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                 oos.writeObject("hello from server.  received message: "+message);
                 ois.close();
                 oos.close();
                 socket.close();
                 if(message.equalsIgnoreCase("exit")) break;
            }

    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    	/*
    	try {
        //create the socket server object
        server = new ServerSocket(port);
        //keep listens indefinitely until receives 'exit' call or program terminates
        while(true){
            System.out.println("Waiting for client request");
            //creating socket and waiting for client connection
            Socket socket = server.accept();
            //read from socket to ObjectInputStream object
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            //convert ObjectInputStream object to String
            String message = (String) ois.readObject();
            System.out.println("Message Received: " + message);
            //create ObjectOutputStream object
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            //write object to Socket
            oos.writeObject("Hi Client "+message);
            //close resources
            ois.close();
            oos.close();
            socket.close();
            //terminate the server if client sends exit request
            if(message.equalsIgnoreCase("exit")) break;
        }
        System.out.println("Shutting down Socket server!!");
        //close the ServerSocket object
        server.close();
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
        */
        
        
    } 
}
