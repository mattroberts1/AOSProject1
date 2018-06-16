import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

//Client threads need to be tracked and controller needs to be able to pass them data to transmit


public class Client implements Runnable{

	int ID;
	String hostName;
	String hostAddr;
	int listenPort;
	public Client(int IDarg, String hostNamearg, int listenPortarg)  //info for node being connected to
	{
		ID=IDarg;
		hostName=hostNamearg;
		listenPort=listenPortarg;	
		hostAddr=hostName+".utdallas.edu";
	}
	
    public void run(){
    	
    	try {
       
        Socket socket = new Socket(hostAddr,listenPort);

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
    	}
        catch(Exception e)
       	{
       	 e.printStackTrace();
       	}
    	 
        
    }
}