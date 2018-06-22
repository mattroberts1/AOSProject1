import java.io.Serializable;


//messages are passed through the sockets
public class Message implements Serializable {
	int senderNode=-1;
	int receiverNode=-1;
	String text="";
	public Message(int from, int to, String m)
	{
		senderNode=from;
		receiverNode=to;
		text=m;
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
}
