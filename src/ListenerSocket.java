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
    LinkedBlockingQueue<Message> serverQueue; //queue for received application messages
    LinkedBlockingQueue<Message> controlQueue;  //queue for received control messages 
	public ListenerSocket(Socket s, LinkedBlockingQueue<Message> sq, LinkedBlockingQueue<Message> cq)
	{
		socket=s;
		serverQueue=sq;
		controlQueue=cq;
	}
	public void run()
	{
		try {
			while(true)
			{
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				Message m = (Message) ois.readObject();
				if(m.getMessageType().equals("APPMSG"))
					{
					System.out.print("received application message with clock:");
					for(int i=0;i<m.getTimeStamp().length();i++)
					{
						System.out.print(m.getTimeStamp().get(i)+" ");
					}
					System.out.println();
						serverQueue.put(m);
					}
				if(m.getMessageType().equals("CTRLMSG"))
				{
					controlQueue.put(m);
				}

			}
		}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
	}

}
