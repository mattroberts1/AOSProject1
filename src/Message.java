import java.io.Serializable;


//messages are passed through the sockets
public class Message implements Serializable {
	int senderNode=-1;
	int receiverNode=-1;
	String text="";
	int[] timeStamp;
	public Message(int from, int to, String m, int[] t)
	{
		senderNode=from;
		receiverNode=to;
		text=m;
		timeStamp=t;
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
	public int[] getTimeStamp()
	{
		return timeStamp;
	}
}
