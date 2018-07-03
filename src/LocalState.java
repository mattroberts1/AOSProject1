import java.io.Serializable;
public class LocalState implements Serializable{
int[] stateInfo;
int nodeNumber;
boolean snapshotCompleted;
boolean snapshotTransmitted;
boolean activityStatus;

public LocalState(int[] emptyArray, int n)
{
	snapshotCompleted=false;
	snapshotTransmitted=false;
	activityStatus=false;
	stateInfo=emptyArray;
	nodeNumber=n;
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

}
