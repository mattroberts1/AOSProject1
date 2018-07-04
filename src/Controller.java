import java.util.ArrayList;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.*;
import java.util.Random;
public class Controller {
	static int totalMessagesSent=0;
	static long timeOfLastSnapshot;
	static long timeOfLastMessageSend;
	static AtomicIntegerArray clock;
	static Config conf;
	static ArrayList<LinkedBlockingQueue<Message>> clientQueueList;
	static ArrayList<LinkedBlockingQueue<Message>> serverQueueList;
	static ArrayList<LocalState[]> collectedSnapshots;
	static boolean isActive;
	static ArrayList<boolean[]> channelsMarkedList;
	static ArrayList<LocalState> snapShots;
	static ArrayList<Boolean> beganSnapshot;
	static String thisNodesName;
	static String[][] nodeIDList;
	static int thisNodesID;
	static int messagesForThisCycle;
	static int[] nodeQueueLocations;
	static int[] thisNodesNeighbors;
	static AtomicIntegerArray connectionEstablished;
	static ArrayList<ArrayList<String>> neighborList;
	static int nextSnapshotNumber;
	static LinkedBlockingQueue<Message> upStreamClientQueue;
	static int upStreamNodeID;
	
	public static void main(String[] args) {

		conf = new Config(args[0]);
		clock = new AtomicIntegerArray(conf.getNumNodes());
		clientQueueList = new ArrayList<LinkedBlockingQueue<Message>>();
		serverQueueList = new ArrayList<LinkedBlockingQueue<Message>>();
		collectedSnapshots=new ArrayList<LocalState[]>(); //arraylist of arrays of localstates to store snapshot info in node 0
		//array at index x in list is x'th snapshot, localstate at index i in x is for process with ID i
		nextSnapshotNumber=0; //this will only be used by node 0
		isActive=false;
		thisNodesName=getdcxxName();
		nodeIDList =conf.getNodeIDList();
		thisNodesID=-1;
		neighborList=conf.getNeighborList();
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
		nodeQueueLocations = new int[neighborList.get(thisNodesID).size()]; 
		
	
		//establish connections between this node and other nodes listed in config file
		connectionEstablished = new AtomicIntegerArray(neighborList.get(thisNodesID).size());
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
		for(int i=0;i<neighborList.get(thisNodesID).size();i++)
		{
			serverQueueList.add(new LinkedBlockingQueue<Message>());
		}
		//create server thread
		Server s = new Server(Integer.parseInt(nodeIDList[thisNodesID][2]), serverQueueList, nodeQueueLocations);
		Thread serverThread = new Thread(s);
		serverThread.start();
		
		thisNodesNeighbors= new int[neighborList.get(thisNodesID).size()];  //contains node ids of neighbors (as ints)
		for(int i=0;i<thisNodesNeighbors.length;i++)
		{
			thisNodesNeighbors[i]=Integer.parseInt(neighborList.get(thisNodesID).get(i));
		}

		messagesForThisCycle=0; 
		if(isActive) //node 0 starts active so need to initialize this 
		{
			messagesForThisCycle=chooseNumMessages(conf.getMinPerActive(),conf.getMaxPerActive());
		}

		//find upstream and downstream nodes and store queues so they can be referenced later (remember no upstream node for node 0)
		TreeMaker t = new TreeMaker(conf);
		Graph tree=t.createTree();
		Vertex thisNodesVertex=tree.getVertex(tree.findVertexIndex(thisNodesID));
		upStreamNodeID=0;
		upStreamClientQueue=null;
		if(thisNodesID!=0)
		{
			upStreamNodeID=thisNodesVertex.getUpStreamNode().getNumber();
			int upStreamIndex=findChannelIndex(nodeQueueLocations,thisNodesVertex.getUpStreamNode().getNumber());
			upStreamClientQueue=clientQueueList.get(upStreamIndex);
		}
		ArrayList<LinkedBlockingQueue<Message>> downStreamClientQueueList = new ArrayList<LinkedBlockingQueue<Message>>();
		//number at index x is the id of node the queue at x in downStreamClientQueueList connects to
		ArrayList<Integer> downStreamClientQueueIDs = new ArrayList<Integer>();
		for(int i=0;i<thisNodesVertex.getDownStreamNodes().size();i++)
		{
			int downStreamNodeID=thisNodesVertex.getDownStreamNodes().get(i).getNumber();
			int downStreamNodeIndex=findChannelIndex(nodeQueueLocations, downStreamNodeID);
			downStreamClientQueueList.add(clientQueueList.get(downStreamNodeIndex));
			downStreamClientQueueIDs.add(downStreamNodeID);
		}
		
		
		
		
//MAIN LOOP		

		channelsMarkedList= new ArrayList<boolean[]>();  //each index in an array is true if that channel has received a marker for that snapshot
		//ie if channelsMarkedList.get(4)[1] is true the channel at index 1 has received a marker for the 5th snapshot
		 snapShots= new ArrayList<LocalState>();//each array is a saved clock (a snapshot), once that snapshot is completed, any messages
		//received after that snapshot began will be taken into account in the corresponding array
		beganSnapshot= new ArrayList<Boolean>(); //index is true if this node has received a marker message for that iteration of snapshot
		timeOfLastSnapshot=System.currentTimeMillis();
		timeOfLastMessageSend=System.currentTimeMillis(); 
		boolean terminate=false;
		while(!terminate)
		{
			//check whether this node is node 0 and if so whether it's time to start a snapshot
			node0CheckSendMarkers();
			
			//check whether to send application message
			checkSendAppMessage();
			
			//check whether have received application message
			checkReceiveAppMessages();

			
			//check for marker messages
			checkReceiveMarkerMessage();

			//check if there are any completed state reports to send (that haven't already been sent out) and if so send them upstream
			checkSendStateReports();

			//if received a statereport and this isn't node 0 pass it upstream
			passOnStateReports();
			
			//if received a statereport and this is node 0 store it in collectedSnapshots
			if(thisNodesID==0)
			{
				for(int i=0;i<serverQueueList.size();i++)
				{
					if(serverQueueList.get(i).peek()!=null &&serverQueueList.get(i).peek().getMessageType().equals("STATEREPORT"))
					{
						Message m=serverQueueList.get(i).poll();
						LocalState report=m.getStateReport();
						int iteration=report.getIterationNumber();
						collectedSnapshots.get(iteration)[report.getNodeNumber()]=report; 
						
						//if received a state report from a passive process check whether system is passive and should terminate
						if(m.getStateReport().getActivityStatus()==false)
						{
							//check whether all nodes were passive in latest snapshot
							boolean allNodesPassive=true;
							for(int j=0;j<collectedSnapshots.get(iteration).length;j++)
							{
								if(collectedSnapshots.get(iteration)[j]==null||collectedSnapshots.get(iteration)[j].getActivityStatus()==true)//there is an active node in the snapshot
								{
									allNodesPassive=false;
								}
							}
							//if allNodesPassive is true there are no active nodes in the current snapshot
							//if previous snapshot was also passive and clocks have not changed then system is passive
							boolean allPrevPassive=true;
							if(iteration==0)
							{
								allPrevPassive=false;
							}
							if(allNodesPassive&&iteration>=1)
							{
								for(int j=0;j<collectedSnapshots.get(iteration-1).length;j++)
								{
									if(collectedSnapshots.get(iteration-1)[j]==null||collectedSnapshots.get(iteration-1)[j].getActivityStatus()==true)//there is an active node in the snapshot
									{
										allPrevPassive=false;
									}
								}
							}
							boolean clocksEqual=true;
							if(allNodesPassive&&allPrevPassive)
							{
								for(int j=0;j<collectedSnapshots.get(iteration).length;j++) //for each local state j in snapshot iteration
								{
									LocalState latestState= collectedSnapshots.get(iteration)[j];
									LocalState prevState=collectedSnapshots.get(iteration-1)[j];
									for(int k=0;k<latestState.getStateInfo().length;k++)
									{
										if(latestState.getStateInfo()[k]!=prevState.getStateInfo()[k])
										{
											clocksEqual=false;
										}
									}
								}
							}
							//if true then all nodes are passive and all channels are empty
							if(allNodesPassive&&allPrevPassive&&clocksEqual) //Send out termination message
							{
								for(int j=0;j<downStreamClientQueueList.size();j++)
								{
									Message terminator = new Message(thisNodesID, downStreamClientQueueIDs.get(j),"",clock,"TERMINATE",null);
									try {downStreamClientQueueList.get(j).put(terminator);}  catch(Exception e) {}
								}
								terminate=true;
							}
						}	
					}
				}
			}
//TODO: add termination procedures to all nodes and detection to node 0	
			

			
			//if terminate message is received, ends loop and propagates terminate command
			for(int i=0;i<serverQueueList.size();i++)
			{
				if(serverQueueList.get(i).peek()!=null &&serverQueueList.get(i).peek().getMessageType().equals("TERMINATE"))
				{
					terminate=true;
					//send terminate message to downstream nodes
					for(int j=0;j<downStreamClientQueueList.size();j++) 
					{
						Message terminator= new Message(thisNodesID, downStreamClientQueueIDs.get(j), "", clock, "TERMINATE",null);
						try 
						{
							downStreamClientQueueList.get(j).put(terminator);	
						}
						catch(Exception e) {e.printStackTrace();}
					}
					break;  //send out terminate messages to all neighbors once then exit for loop
				}
			}
		}//end main while loop
//TODO: remove test code
//TEST CODE, PRINT OUT ENTIRE SNAPSHOT RECORD		
			System.out.println("Printing out snapshot record");
			for(int i=0;i<snapShots.size();i++)
			{
				int[] snap=snapShots.get(i).getStateInfo();
				for(int j=0;j<snap.length;j++)
				{
					System.out.print(snap[j]+" ");
				}
				System.out.println();
			}
	}

	
	//checks whether node should send app msg and if so sends one
	public static void checkSendAppMessage()
	{
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
							System.out.print(mSend.getTimeStamp().get(k)+" ");
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
					if(messagesForThisCycle==0||totalMessagesSent>=conf.getMaxNumber())
					{
						isActive=false;
					}
				}
			}
		}
	}
	
	//checks if this is node 0 and it's time to start for the next snapshot
	public static void node0CheckSendMarkers()
	{
		if(thisNodesID==0&&System.currentTimeMillis()>timeOfLastSnapshot+conf.getSnapshotDelay())
		{
			collectedSnapshots.add(new LocalState[clock.length()]);
			System.out.println("node 0 initiating snapshot "+nextSnapshotNumber);
			timeOfLastSnapshot=System.currentTimeMillis();//reset timer for next snapshot
			//send out marker messages
			for(int i=0;i<clientQueueList.size();i++)
			{
				Message marker = new Message(thisNodesID, nodeQueueLocations[i],Integer.toString(nextSnapshotNumber),clock, "MARKMSG",null);
				try {
					clientQueueList.get(i).put(marker);
				}
				catch(Exception e) {e.printStackTrace();}	
			}
			//take snapshot on node 0 (since it won't be started by receipt of a marker msg like on other nodes)
			beganSnapshot.add(true);
			channelsMarkedList.add(new boolean[neighborList.get(thisNodesID).size()]);
			//instantly complete snapshot for node 0 by setting all channels to marked
			for(int i=0;i<channelsMarkedList.get(nextSnapshotNumber).length;i++)
			{
				channelsMarkedList.get(nextSnapshotNumber)[i]=true;
			}
			//save clock of node 0 to snapshot
			snapShots.add(new LocalState(new int[conf.getNumNodes()],thisNodesID,nextSnapshotNumber,isActive));
			for(int i=0;i<clock.length();i++)
			{
				snapShots.get(nextSnapshotNumber).setStateIndex(i, clock.get(i));
			}
			collectedSnapshots.get(nextSnapshotNumber)[thisNodesID]=snapShots.get(nextSnapshotNumber);
			nextSnapshotNumber++; //increment counter for next time a snapshot is started
		}
	}
	
	//check whether node has received any marker messages and if so handles them
	public static void checkReceiveMarkerMessage()
	{
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
					channelsMarkedList.add(new boolean[conf.getNeighborList().get(thisNodesID).size()]);
					int temp=snapShots.size();//the iteration number for the next snapshot
					snapShots.add(new LocalState(new int[conf.getNumNodes()],thisNodesID,temp, isActive));
				}
				if(!beganSnapshot.get(iteration))//have not yet received marker for this iteration of snapshot
				{
					beganSnapshot.set(iteration,  true);
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
					if(clientQueueList.size()==1)
					{
						//if only one other node then first marker is also last
						snapShots.get(iteration).setCompletedStatus(true); 
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
					}	
				}//end else loop for not first marker in iteration
			} //end if for all marker messages
		}//end for loop for check serverqueue for marker messages
	}
	
	//checks if node has received any application messages and if so handles them 
	public static void checkReceiveAppMessages()
	{
		for(int i=0;i<serverQueueList.size();i++)
		{
			while(serverQueueList.get(i).peek()!=null&&serverQueueList.get(i).peek().getMessageType().equals("APPMSG")) 
			{
				Message mReceived=serverQueueList.get(i).poll();
					System.out.print("received application message from node "+mReceived.getSender()+" with timestamp ");
					for(int k=0;k<mReceived.getTimeStamp().length();k++)
					{
						System.out.print(mReceived.getTimeStamp().get(k)+" ");
					}
					//update this nodes clock
					for(int j=0;j<mReceived.getTimeStamp().length();j++)
					{
						clock.set(j,Math.max(clock.get(j), mReceived.getTimeStamp().get(j)));
					}
					int receiverIndex=findIndexOfNode(conf,thisNodesName);
					
					// for each active snapshot process,  if haven't already received a marker on channel appmsg came in on, update the stored localstate
					for(int j=0;j<beganSnapshot.size();j++)
					{
						//check if snapshot is active (started but not completed) before iterating through channels
						if(beganSnapshot.get(j)&&!snapShots.get(j).getCompletedStatus())
						{
							//check that channel message came in on isn't marked yet for this snapshot
							if(!channelsMarkedList.get(j)[findChannelIndex(nodeQueueLocations,mReceived.getSender())])
							{
								//merge timestamp in message with one in snapshot state info
								for(int k=0;k<snapShots.get(j).getStateInfo().length;k++)
								{
								int max=Math.max(clock.get(k), mReceived.getTimeStamp().get(k));
								snapShots.get(j).setStateIndex(k, max);
								}
							}
						}
					}
					clock.getAndIncrement(receiverIndex);
					if(!isActive&&totalMessagesSent<conf.getMaxNumber())
					{
						isActive=true;
						messagesForThisCycle=chooseNumMessages(conf.getMinPerActive(),conf.getMaxPerActive());
					}
			}
		}
	}
	
	//check if have received any state reports destined for node 0 (and this node is not node 0)
	public static void passOnStateReports()
	{
		if(thisNodesID!=0)
		{
			for(int i=0;i<serverQueueList.size();i++)
			{
				if(serverQueueList.get(i).peek()!=null &&serverQueueList.get(i).peek().getMessageType().equals("STATEREPORT"))
				{
					Message m=serverQueueList.get(i).poll();
					try {
					upStreamClientQueue.put(m);
					}
					catch(Exception e) {e.printStackTrace();}
				}
			}
		}
	}
	
	//checks if there are any state reports that need to be sent out and if so passes them to upstream node
	public static void checkSendStateReports()
	{
		if(thisNodesID!=0)
		{
			for(int i=0;i<snapShots.size();i++)
			{
				if(snapShots.get(i).getCompletedStatus()==true&&snapShots.get(i).getTransmittedStatus()==false)
				{
					Message m= new Message(thisNodesID,upStreamNodeID,"",clock,"STATEREPORT",snapShots.get(i));
					try 
					{
						System.out.println("sending state report with activity status "+m.getStateReport().getActivityStatus());
						upStreamClientQueue.put(m);
					}
					catch(Exception e) {e.printStackTrace();}
					snapShots.get(i).setTransmittedStatus(true);
				}
			}
		}
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
