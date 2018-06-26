import java.util.ArrayList;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.*;
import java.util.Random;


public class Controller {

	public static void main(String[] args) {
		int totalMessagesSent=0;
		long timeOfLastSnapshot;
		long timeOfLastMessageSend;
		Config conf = new Config(args[0]);
		AtomicIntegerArray clock = new AtomicIntegerArray(conf.getNumNodes());
		ArrayList<LinkedBlockingQueue<Message>> clientQueueList = new ArrayList<LinkedBlockingQueue<Message>>();
		ArrayList<LinkedBlockingQueue<Message>> serverQueueList = new ArrayList<LinkedBlockingQueue<Message>>();

		
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
		
		//value at index x in nodeQueueLocations is the nodeID that the queue at x in nodeQueueLists is used to send messages to
		int[] nodeQueueLocations = new int[neighborList.get(thisNodesID).size()]; 
		
	
		//establish connections between this node and other nodes listed in config file
		AtomicIntegerArray connectionEstablished = new AtomicIntegerArray(neighborList.size());
		for(int i=0;i<connectionEstablished.length();i++)  //initialize all values to 0, they will be set to 1 once each socket connection is established in the Client threads
		{
			connectionEstablished.set(i, 0);
		}
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
			Client c = new Client(IDarg, hostNamearg, listenPortarg, clientQueueList.get(i), connectionEstablished, i);
			Thread clientThread= new Thread(c);
			clientThread.start();
		}
		//populate serverQueueList and ControlQueueList
		for(int i=0;i<neighborList.size();i++)
		{
			serverQueueList.add(new LinkedBlockingQueue<Message>());
		}
		//create server thread
		Server s = new Server(Integer.parseInt(nodeIDList[thisNodesID][2]), serverQueueList, nodeQueueLocations);
		Thread serverThread = new Thread(s);
		serverThread.start();
		
		int[] thisNodesNeighbors= new int[neighborList.get(thisNodesID).size()];  //contains node ids of neighbors (as ints)
		for(int i=0;i<thisNodesNeighbors.length;i++)
		{
			thisNodesNeighbors[i]=Integer.parseInt(neighborList.get(thisNodesID).get(i));
		}
		timeOfLastSnapshot=System.currentTimeMillis();
		timeOfLastMessageSend=System.currentTimeMillis();
		int messagesForThisCycle=0; 
		if(isActive) //node 0 starts active so need to initialize this 
		{
			messagesForThisCycle=chooseNumMessages(conf.getMinPerActive(),conf.getMaxPerActive());
		}

		ArrayList<boolean[]> channelsMarkedList = new ArrayList<boolean[]>();  //each index in an array is true if that channel has received a marker for that snapshot
		//ie if channelsMarkedList.get(4)[1] is true the channel at index 1 has received a marker for the 5th snapshot
		ArrayList<LocalState> snapShots = new ArrayList<LocalState>();//each array is a saved clock (a snapshot), once that snapshot is completed, any messages
		//received after that snapshot began will be taken into account in the corresponding array
		ArrayList<Boolean> beganSnapshot= new ArrayList<Boolean>(); //index is true if this node has received a marker message for that iteration of snapshot

//MAP LOOP
		Message mReceived;
		while(true)
		{
			//check whether have received message
			for(int i=0;i<serverQueueList.size();i++)
			{
				while(serverQueueList.get(i).peek()!=null&&serverQueueList.get(i).peek().getMessageType().equals("APPMSG")) 
				{
					mReceived=serverQueueList.get(i).poll();
					if(mReceived!=null)
					{
						System.out.print("received application message from node "+mReceived.getSender()+" with timestamp ");
						for(int k=0;k<mReceived.getTimeStamp().length();k++)
						{
							System.out.print(mReceived.getTimeStamp().get(k));
						}
						System.out.println();
						//update this nodes clock
						for(int j=0;j<mReceived.getTimeStamp().length();j++)
						{
							clock.set(j,Math.max(clock.get(j), mReceived.getTimeStamp().get(j)));
						}
						int receiverIndex=findIndexOfNode(conf,thisNodesName);
						clock.getAndIncrement(receiverIndex);
//TODO: for each active snapshot process,  if haven't already received a marker on channel appmsg came in on, update the stored localstate
						
						if(!isActive)
						{
							isActive=true;
							messagesForThisCycle=chooseNumMessages(conf.getMinPerActive(),conf.getMaxPerActive());
						}
					}	
				}
			}
			
			//check whether to send application message
			if(isActive)
			{
				if(System.currentTimeMillis()>(timeOfLastMessageSend+conf.getMinSendDelay()))  
				{  
					if(messagesForThisCycle>0&& totalMessagesSent<conf.getMaxNumber())
					{
						//send out a message to a random neighbor
						Random rand = new Random();
						int destinationIndex=rand.nextInt(thisNodesNeighbors.length); //index of dest node in neighborList
						int destinationID=thisNodesNeighbors[destinationIndex];
						int senderIndex=findIndexOfNode(conf, thisNodesName);
						if(connectionEstablished.get(destinationIndex)==1)
						{
							clock.getAndIncrement(senderIndex);
							Message mSend= new Message(thisNodesID, destinationID, "", clock, "APPMSG", null);
							System.out.print("sent application message to node "+mSend.getReceiver()+" with timestamp ");
							for(int k=0;k<mSend.getTimeStamp().length();k++)
							{
								System.out.print(mSend.getTimeStamp().get(k));
							}
							System.out.println();
							try{
								clientQueueList.get(destinationIndex).put(mSend);
							}
							catch(Exception e) {}
							timeOfLastMessageSend=System.currentTimeMillis();
							messagesForThisCycle--;
							totalMessagesSent++;
						}
					}
				}
			}
			
			
			//check for marker messages
			Message marker;
			for(int i=0;i<serverQueueList.size();i++)  
			{
				//all marker messages
				if(serverQueueList.get(i).peek()!=null&&serverQueueList.get(i).peek().getMessageType().equals("MARKMSG"))
				{
					marker=serverQueueList.get(i).poll();
					//handle marker message
					int iteration = Integer.parseInt(marker.getText());
					while(beganSnapshot.size()<=iteration)  //make sure we have space in lists, will only run if iteration would exceed current highest index
					{
						beganSnapshot.add(false);
						channelsMarkedList.add(new boolean[neighborList.size()]);
						snapShots.add(new LocalState(new int[conf.getNumNodes()]));
					}
					if(!beganSnapshot.get(iteration))//have not yet received marker for this iteration of snapshot
					{
						beganSnapshot.set(i,  true);
						for(int j=0;j<channelsMarkedList.get(iteration).length;j++)
						{
							channelsMarkedList.get(iteration)[j]=false;
						}
						//set channelmarked to true for channel marker message came in on
						channelsMarkedList.get(iteration)[findChannelIndex(nodeQueueLocations, marker.getSender())]=true;
						for(int j=0;j<clock.length();j++)
						{
							{
								snapShots.get(iteration).setStateIndex(j, clock.get(j));
							}
						}
					//send out marker marker messages on all other channels
						for(int j=0;j<clientQueueList.size();j++)
						{
							if(!channelsMarkedList.get(iteration)[j])
							{
								Message outMarker= new Message(thisNodesID, nodeQueueLocations[j], Integer.toString(iteration), clock, "MARKMSG",null);
								try{
									clientQueueList.get(j).put(outMarker);
								}
								catch(Exception e) {}
							}
						}
					}//end if loop for first marker in iteration
					
					else //we have already started current iteration of snapshot algorithm
					{
						int indexOfChannelMessageFrom=findChannelIndex(nodeQueueLocations, marker.getSender());
						channelsMarkedList.get(iteration)[indexOfChannelMessageFrom]=true;
						//check if node has received marker on all channels
						boolean done=true;
						for(int j=0;j<channelsMarkedList.get(iteration).length;j++)
						{
							if(!channelsMarkedList.get(iteration)[j]) //still have a channel that hasn't received marker yet
							{
								done=false;
							}
						}
						if(done)
						{
							snapShots.get(iteration).setCompletedStatus(true);
	//TODO: SEND LOCAL STATE REPORT TO NODE 0
						}	
					}//end not first marker in iteration
					
					
					
					
				} //endif all marker messages
			}
		}
	}

	
	public void takeSnapshot()
	{
		
	}

	
	//returns index of node in neighbor list (same as in queue locations list)
	public static int findChannelIndex(int[] nodeQueueLocations, int targetID)
	{
		for(int i=0;i<nodeQueueLocations.length;i++)
		{
			if(targetID==nodeQueueLocations[i])
			{
				return i;
			}
		}
		return -1;
	}

	//returns index of node in nodeIDList (ie the node ID)
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
