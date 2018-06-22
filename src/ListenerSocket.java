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
			String message = (String) ois.readObject();
			System.out.println("Server received message: " + message);
//			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
//			oos.writeObject("hello from server.  received message: "+message);
			ois.close();
//			oos.close();
			socket.close();
		}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
	}

}
