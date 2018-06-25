import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;




public class Server implements Runnable{   	
    private static ServerSocket server;
    private static int port;  //port server will listen on
    ArrayList<LinkedBlockingQueue<Message>> serverQueueList; //queue for passing application messages back to controller
    ArrayList<LinkedBlockingQueue<Message>> controlQueueList;
    int[] nodeQueueLocations;
    public Server(int p, ArrayList<LinkedBlockingQueue<Message>> sql, ArrayList<LinkedBlockingQueue<Message>> cql, int[] nql)  
    {
    	port=p;
    	serverQueueList=sql;
    	controlQueueList=cql;
    	nodeQueueLocations=nql;
    }
    public void run(){
    	
    	try {
    		server = new ServerSocket(port);
            while(true)
            {
            	System.out.println("Server is listening for incoming connections");
            	 Socket socket = server.accept();
            	 new Thread(new ListenerSocket(socket, serverQueueList, controlQueueList, nodeQueueLocations)).start();
            }

    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
        
    } 
}
