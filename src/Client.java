import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.ConnectException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


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
    	while(retry) //keep trying to connect to server until it comes online
    	{
    		
    		try {
    			socket = new Socket(hostAddr,listenPort);
    			retry=false;
    			System.out.println("connected to "+hostAddr);
    		}
    		catch(ConnectException e)
    		{
    			try{
    			Thread.sleep(1000);
    			}
    			catch(Exception x){}
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    	}
 
    	ObjectOutputStream oos = null;
    	try 
    	{
    		oos = new ObjectOutputStream(socket.getOutputStream());
    		//loop for sending out messages received from controller
    		while(true)
    		{
    			Message m = clientQueue.poll();
    			if(m!=null)
    			{
    				oos.writeObject(m);
    			}
    		}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }


}