import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;




public class Server implements Runnable{   	
    private static ServerSocket server;
    private static int port;  //port server will listen on
    LinkedBlockingQueue<Message> serverQueue; //queue for passing application messages back to controller
    LinkedBlockingQueue<Message> controlQueue;
    public Server(int p, LinkedBlockingQueue<Message> sq, LinkedBlockingQueue<Message> cq)  
    {
    	port=p;
    	serverQueue=sq;
    	controlQueue=cq;
    }
    public void run(){
    	
    	try {
    		server = new ServerSocket(port);
            while(true)
            {
            	System.out.println("Server is listening for incoming connections");
            	 Socket socket = server.accept();
            	 new Thread(new ListenerSocket(socket, serverQueue, controlQueue)).start();
            }

    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
        
    } 
}
