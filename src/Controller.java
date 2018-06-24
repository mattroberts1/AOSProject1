import java.util.ArrayList;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Random;


public class Controller {

	public static void main(String[] args) {
		int totalMessagesSent=0;
		long timeOfLastSnapshot;
		long timeOfLastMessageSend;
		Config conf = new Config(args[0]);
		int[] clock = new int[conf.getNumNodes()];
		boolean isActive=false;
		String thisNodesName=getdcxxName();
		String[][] nodeIDList =conf.getNodeIDList();
		int thisNodesID=-1;
		ArrayList<ArrayList<String>> neighborList=conf.getNeighborList();
		//find id of current node
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
		
		
		int[] thisNodesNeighbors= new int[neighborList.get(thisNodesID).size()];  //contains node ids of neighbors (as ints)
		for(int i=0;i<thisNodesNeighbors.length;i++)
		{
			thisNodesNeighbors[i]=Integer.parseInt(neighborList.get(thisNodesID).get(i));
		}
		timeOfLastSnapshot=System.currentTimeMillis();
		timeOfLastMessageSend=System.currentTimeMillis();
		int messagesForThisActive; 
		if(isActive) //node 0 starts active so need to initialize this 
		{
			messagesForThisActive=chooseNumMessages(conf.getMinPerActive(),conf.getMaxPerActive());
		}
		while(true)
		{
			//check whether have received message
			Message mReceived=serverQueue.poll();
			if(mReceived!=null)
			{
				//update this nodes clock
				for(int i=0;i<mReceived.getTimeStamp().length;i++)
				{
					clock[i]=Math.max(clock[i], mReceived.getTimeStamp()[i]);
				}
				int receiverIndex=findIndexOfNode(conf,thisNodesName);
				clock[receiverIndex]++;
				if(!isActive)
				{
					isActive=true;
					messagesForThisActive=chooseNumMessages(conf.getMinPerActive(),conf.getMaxPerActive());
				}
			}
			
			//check whether to send message
			if(isActive)
			{
				if(System.currentTimeMillis()>(timeOfLastMessageSend+conf.getMinSendDelay()))  
				{  
					//send out a message to a random neighbor
					timeOfLastMessageSend=System.currentTimeMillis();
					Random rand = new Random();
					int destinationIndex=rand.nextInt(thisNodesNeighbors.length);
					int destinationID=thisNodesNeighbors[destinationIndex];
					int senderIndex=findIndexOfNode(conf, thisNodesName);
					clock[senderIndex]++;
					Message mSend= new Message(thisNodesID, destinationID, "", clock);
					try{
					clientQueueList.get(destinationIndex).put(mSend);
					}
					catch(Exception e) {}
					System.out.print("sending message to node "+destinationID+". clock is now ");
					for(int i=0;i<clock.length;i++)
					{
						System.out.print(clock[i]+" ");
					}
				}
			}
			

		}
		

		
		

		
		
	}
	
	
	
	
	public void takeSnapshot()
	{
		
	}
	
	
	public static int findIndexOfNode(Config conf, String nodeName)
	{
		
		String[][] nodeIDList =conf.getNodeIDList();
		for(int i=0;i<nodeIDList.length;i++)
		{
			if(nodeName.equals(nodeIDList[i][1]))
					{
						return i;
					}
		}
		System.out.println("Error finding node index");
		return -1;
	}
	
	//returns a number of messages to send between min and max
	public static int chooseNumMessages(int min, int max)
	{
		Random rand = new Random();
		return (rand.nextInt(max-min)+min+1);
	}
	
	//returns dcxx part of host name
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
