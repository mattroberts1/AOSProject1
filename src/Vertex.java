
import java.util.ArrayList;

public class Vertex {
int number;
Vertex upStreamNode;
ArrayList<Vertex> downStreamNodes;
public Vertex(int n)
{
	number=n;
	downStreamNodes=new ArrayList<Vertex>();
}
public void addDownStreamNode(Vertex v)
{
	downStreamNodes.add(v);
}
public ArrayList<Vertex> getDownStreamNodes()
{
	return downStreamNodes;
}
public Vertex getUpStreamNode()
{
	return upStreamNode;
}
public void setUpStreamNode(Vertex v)
{
	upStreamNode=v;
}
public int getNumber()
{
	return number;
}
}
