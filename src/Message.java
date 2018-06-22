


//messages are passed through the sockets
public class Message {
	int senderNode=-1;
	int receiverNode=-1;
	String text="";
	public Message(int from, int to, String m)
	{
		senderNode=from;
		receiverNode=to;
		text=m;
	}
}
