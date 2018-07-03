import java.io.Serializable;
import java.util.concurrent.atomic.*;

//messages are passed through the sockets
public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	int senderNode=-1;
	int receiverNode=-1;
	String text="";  //text of marker message will be the iteration of the snapshot algorithm it belongs to
	AtomicIntegerArray timeStamp;
	String messageType="";  //APPMSG or MARKMSG or STATEREPORT or TERMINATE
	LocalState stateReport=null;
	public Message(int from, int to, String m, AtomicIntegerArray time, String type, LocalState sR)
	{
		senderNode=from;
		receiverNode=to;
		text=m;
		timeStamp=time;
		messageType=type;
		if(sR!=null)
		{
			stateReport=sR;
		}
	}
	public String getText()
	{
		return text;
	}
	public int getSender()
	{
		return senderNode;
	}
	public int getReceiver()
	{
		return receiverNode;
	}
	public AtomicIntegerArray getTimeStamp()
	{
		return timeStamp;
	}
	public String getMessageType()
	{
		return messageType;
	}
	public LocalState getStateReport()
	{
		return stateReport;
	}
}
