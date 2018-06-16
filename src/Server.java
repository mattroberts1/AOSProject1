import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;

//NEEDS TO PASS DATA BACK TO MAIN THREAD

public class Server implements Runnable{   	
    private static ServerSocket server;
    private static int port;
    public Server(int p)  //port server will listen on
    {
    	port=p;
    }
    public void run(){
    	
    	try {
    		server = new ServerSocket(port);
            while(true)
            {
            	System.out.println("Server is waiting");
            	 Socket socket = server.accept();
            	 new Thread(new ListenerSocket(socket)).start();
            }

    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
        
    } 
}
