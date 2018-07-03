import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class ListenerSocket implements Runnable {
	Socket socket; //socket connected to client of other process
	ArrayList<LinkedBlockingQueue<Message>> serverQueueList; //queue for received messages messages

	int[] nodeQueueLocations;
	public ListenerSocket(Socket s, ArrayList<LinkedBlockingQueue<Message>> sql, int[] nql)
	{
		socket=s;
		serverQueueList=sql;
		nodeQueueLocations=nql;
	}
	public void run()
	{
		try {
			while(true)
			{
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				Message m = (Message) ois.readObject();
				int queueIndex=-1;
				for(int i=0;i<nodeQueueLocations.length;i++)//find the index of the queue the message needs to be passed to
				{
					if(nodeQueueLocations[i]==m.getSender())
					{
						queueIndex=i;
					}
				}
						serverQueueList.get(queueIndex).put(m);

			}
		}
    	catch(Exception e){}
	}

}
