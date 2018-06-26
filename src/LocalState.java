
public class LocalState  {
int[] stateInfo;
boolean snapshotCompleted;
boolean snapshotTransmitted;

public LocalState(int[] emptyArray)
{
	snapshotCompleted=false;
	snapshotTransmitted=false;
	stateInfo=emptyArray;
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
}
