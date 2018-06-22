import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.ConnectException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


// Controller needs to be able to pass data to client threads to transmit


public class Client implements Runnable{

	int ID;
	String hostName;
	String hostAddr;
	int listenPort;
	LinkedBlockingQueue<Message> clientQueue;
	public Client(int IDarg, String hostNamearg, int listenPortarg, LinkedBlockingQueue<Message> q)  //info for node being connected to
	{
		ID=IDarg;
		hostName=hostNamearg;
		listenPort=listenPortarg;	
		hostAddr=hostName+".utdallas.edu";
		clientQueue = q;
	}
	
    public void run(){
    	
    	Socket socket=null;
    	boolean retry = true;
    	while(retry)
    	{
    		
    		try {
    			System.out.println("Connecting to "+hostAddr +" on port "+listenPort);
    			socket = new Socket(hostAddr,listenPort);
    			retry=false;
    		}
    		catch(ConnectException e)
    		{
    			System.out.println("Server not available, will retry in 5 seconds");
    			try{
    			Thread.sleep(5000);
    			}
    			catch(Exception x){}
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    	}
    	ObjectOutputStream oos = null;
    	try {
    	oos = new ObjectOutputStream(socket.getOutputStream());
    	Message m = clientQueue.poll();
    	if(m!=null)
    	{
    		oos.writeObject(m);
    	}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
        /*
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        oos = new ObjectOutputStream(socket.getOutputStream());
        System.out.println("client sending hello");
        oos.writeObject("hello from client");
        ois = new ObjectInputStream(socket.getInputStream());
        String message = (String) ois.readObject();
        System.out.println("Client received message: " + message);
        oos.writeObject("exit");
        socket.close();
         
         
         */
    }
}