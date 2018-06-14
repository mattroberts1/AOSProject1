import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable{

    public void run(){
    	
    	try {
       
        Socket socket = new Socket("dc01.utdallas.edu",9878);
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
    	
    	
    	/*
    	try {
        //get the localhost IP address, if server is running on some other IP, you need to use that
        InetAddress host = InetAddress.getLocalHost();
        Socket socket = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        for(int i=0; i<5;i++){
            //establish socket connection to server
            socket = new Socket(host.getHostName(), 9878);
            //write to socket using ObjectOutputStream
            oos = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("Sending request to Socket Server");
            if(i==4)oos.writeObject("exit");
            else oos.writeObject(""+i);
            //read the server response message
            ois = new ObjectInputStream(socket.getInputStream());
            String message = (String) ois.readObject();
            System.out.println("Message: " + message);
            //close resources
            ois.close();
            oos.close();
            Thread.sleep(100);
        }
    }
     catch(Exception e)
    	{
    	 e.printStackTrace();
    	}
       */ 
        
    }
}