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
    LinkedBlockingQueue<Message> serverQueue; //queue for passing data back to controller
    public Server(int p, LinkedBlockingQueue<Message> q)  
    {
    	port=p;
    	serverQueue=q;
    }
    public void run(){
    	
    	try {
    		server = new ServerSocket(port);
            while(true)
            {
            	System.out.println("Server is waiting");
            	 Socket socket = server.accept();
            	 new Thread(new ListenerSocket(socket, serverQueue)).start();
            }

    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
        
    } 
}
