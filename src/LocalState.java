import java.io.Serializable;
public class LocalState implements Serializable{
int[] stateInfo;
int nodeNumber;
boolean snapshotCompleted;
boolean snapshotTransmitted;
boolean activityStatus; //whether the node is active or passive (true is active)
int iterationNumber;

public LocalState(int[] emptyArray, int n, int i, boolean a)
{
	snapshotCompleted=false;
	snapshotTransmitted=false;
	activityStatus=a;
	stateInfo=emptyArray;
	nodeNumber=n;
	iterationNumber=i;
	
}
public boolean getCompletedStatus()
{
	return snapshotCompleted;
}
public void setCompletedStatus(boolean s)
{
	snapshotCompleted=s;
}
public boolean getTransmittedStatus()
{
	return snapshotTransmitted;
}
public void setTransmittedStatus(boolean s)
{
	snapshotTransmitted=s;
}
public void setStateIndex(int index, int value)
{
	stateInfo[index]=value;
}

public int[] getStateInfo()
{
	return stateInfo;
}

public boolean getActivityStatus()
{
	return activityStatus;
}

public void setActivityStatus(boolean status) 
{
	activityStatus = status;
}
public int getNodeNumber()
{
	return nodeNumber;
}
public int getIterationNumber()
{
	return iterationNumber;
}
}
