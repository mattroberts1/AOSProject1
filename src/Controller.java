import java.util.ArrayList;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
public class Controller {

	public static void main(String[] args) {
		Config conf = new Config(args[0]);
		boolean isActive=false;
		String thisNodesName=getdcxxName();
		String[][] nodeIDList =conf.getNodeIDList();
		int thisNodesID=-1;
		ArrayList<ArrayList<String>> neighborList=conf.getNeighborList();
		for(int i=0;i<nodeIDList.length;i++)
		{
			if(thisNodesName.equals(nodeIDList[i][1]))
			{
				thisNodesID=i;
			}
		}
		if(thisNodesID==0)
		{
			isActive=true;
		}
		
		LinkedBlockingQueue<Message> serverQueue = new LinkedBlockingQueue<>();
		Server s = new Server(Integer.parseInt(nodeIDList[thisNodesID][2]), serverQueue);
		Thread serverThread = new Thread(s);
		serverThread.start();
		//value at x in nodeQueueLocations is the nodeID that the queue at x in clientQueueList is used to send messages to
		int[] nodeQueueLocations = new int[neighborList.get(thisNodesID).size()]; 
		ArrayList<LinkedBlockingQueue<Message>> clientQueueList = new ArrayList<LinkedBlockingQueue<Message>>();
		//establish connections between this node and other nodes listed in config file
		for(int i=0;i<neighborList.get(thisNodesID).size();i++)  
		{
			nodeQueueLocations[i]=Integer.parseInt(neighborList.get(thisNodesID).get(i));
			clientQueueList.add(new LinkedBlockingQueue<Message>());
			int IDarg; 
			String hostNamearg; 
			int listenPortarg;
			IDarg=Integer.parseInt(neighborList.get(thisNodesID).get(i));
			hostNamearg=nodeIDList[IDarg][1]; //find host name of the node we're connecting to
			listenPortarg=Integer.parseInt(nodeIDList[IDarg][2]);
			Client c = new Client(IDarg, hostNamearg, listenPortarg, clientQueueList.get(i));
			Thread clientThread= new Thread(c);
			clientThread.start();
		}
		Message m;
		while(true)
		{
			try {
			Thread.sleep(1000);
			}
			catch(Exception e) {}
			m=serverQueue.poll();
			if(m!=null)
			{
				System.out.println("received message "+m.getText());
			}
			System.out.println("Sending hello message");
			
			try {
				clientQueueList.get(0).put(new Message(0,0,"hello from node "+thisNodesID));
			} catch (InterruptedException e) {e.printStackTrace();}
		}
		
		
		
		
	}
	
	public static String getdcxxName()
	{
		String hostName="";
		try {
		hostName=InetAddress.getLocalHost().getHostName();
		}
		catch(Exception e)
		{	
			e.printStackTrace();
		}
		String[] temp = hostName.split("\\.");
		return temp[0];
	}
	
}
