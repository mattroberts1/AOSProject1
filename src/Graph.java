
//to find a nodes upstream node call graph.getVertex(findVertexIndex("this nodes ID")).getUpStreamNodes()
//to find a nodes downstream nodes call graph.getVertex(findVertexIndex("this nodes ID")).getDownStreamNodes() this returns a graph containing
	//all the downstream nodes, then iterate through it with getNumVertices().length and for each use vertex.getNumber() to get the node ID
import java.util.ArrayList;
public class Graph {
ArrayList<Edge> edges;	
ArrayList<Vertex> vertices;
public Graph()
{
	edges = new ArrayList<Edge>();
	vertices=new ArrayList<Vertex>();
}
public void addEdge(Edge e)
{
	edges.add(e);
}

public Edge removeEdge(int index)
{
	Edge e=edges.get(index);
	edges.remove(index);
	return e;
}

public int findEdgeIndex(int a, int b)
{
	int index=-1;
	for(int i=0;i<edges.size();i++)
	{
		if((edges.get(i).getEnd1()==a)&&(edges.get(i).getEnd2()==b))
		{
			index=i;
		}
	}
	return index;
}

public Edge getEdge(int index) 
{
	return edges.get(index);
}

public int getNumEdges()
{
	return edges.size();
}

public void addVertex(Vertex v)
{
	vertices.add(v);
}

public Vertex removeVertex(int index)
{
Vertex v=vertices.get(index);
vertices.remove(index);
return v;
}

public Vertex getVertex(int index)
{
	return vertices.get(index);
}

public int findVertexIndex(int n)
{
	int index=-1;
	for(int i=0;i<vertices.size();i++)
	{
		if(vertices.get(i).getNumber()==n)
		{
			index=i;
		}
	}
	return index;
}

public int getNumVertices()
{
	return vertices.size();
}
}
