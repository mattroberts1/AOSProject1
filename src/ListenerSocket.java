import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class ListenerSocket implements Runnable {
	Socket socket; //socket connected to client of other process
    LinkedBlockingQueue<Message> serverQueue; //queue for passing data back to controller
	public ListenerSocket(Socket s, LinkedBlockingQueue<Message> q)
	{
		socket=s;
		serverQueue=q;
	}
	public void run()
	{
		
		try {
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			while(true)
			{
			Message m = (Message) ois.readObject();
			serverQueue.put(m);
			}
		}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
	}

}
