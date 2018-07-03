
import java.util.ArrayList;

public class TreeMaker {
Config conf;
	public TreeMaker(Config c)
	{
		conf=c;
	}
	public Graph createTree() {

		Graph g=createBaseGraph(conf);
		ArrayList<Graph> answerSet=new ArrayList<Graph>();
		answerSet.add(new Graph());
		//transfer node 0 from old graph to new tree
		answerSet.get(0).addVertex(g.removeVertex(g.findVertexIndex(0)));
		answerSet.get(0).getVertex(0).setUpStreamNode(answerSet.get(0).getVertex(0));
		//repeat algorithm until all vertices are included in the answer set
		int currentLayer=0; 
		while(g.getNumVertices()>0)
		{
			Vertex ASNode; //the number of current node in answer set being considered
			Edge edge;//the edge in g being considered
			//loop through all vertices in current layer of answer set
			answerSet.add(new Graph());
			for(int i=0;i<answerSet.get(currentLayer).getNumVertices();i++)
			{ 
				if(i>=0)  //avoid index out of bounds as algorithm occasionally checks index -1
				{
					ASNode=answerSet.get(currentLayer).getVertex(i);
				}
				else
					continue;
				//for each vertex in answer set, check g for edges leading to vertices not in answer set
				//if found, transfer edge and other vertex on edge from g to answer set
				for(int j=0;j<g.getNumEdges();j++)
				{
					edge=g.getEdge(j);
					if((edge.getEnd1()==ASNode.getNumber()&&g.findVertexIndex(edge.getEnd2())!=-1)) //node on end1 is in AS and node on end 2 is in g
					{
						Vertex targetNode=g.removeVertex(g.findVertexIndex(edge.getEnd2()));
						answerSet.get(currentLayer+1).addEdge(g.removeEdge(j));
						answerSet.get(currentLayer+1).addVertex(targetNode);
						ASNode.addDownStreamNode(targetNode);
						targetNode.setUpStreamNode(ASNode);
						i--;  //check same vertex again in case it has multiple nodes as direct downstreams
					}
					else if(edge.getEnd2()==ASNode.getNumber()&&g.findVertexIndex(edge.getEnd1())!=-1)	//node on end2 is in AS and node on end 1 is in g
					{
						Vertex targetNode=g.removeVertex(g.findVertexIndex(edge.getEnd1()));
						answerSet.get(currentLayer+1).addEdge(g.removeEdge(j));
						answerSet.get(currentLayer+1).addVertex(targetNode);
						ASNode.addDownStreamNode(targetNode);
						targetNode.setUpStreamNode(ASNode);
						i--;
					}
				}
			}
			currentLayer++;
		}
		//combine answerSet into a single graph answerTree
		Graph answerTree= new Graph();
		for(int i=0;i<answerSet.size();i++)
		{
			for(int j=0;j<answerSet.get(i).getNumVertices();j++)
			{
				answerTree.addVertex(answerSet.get(i).getVertex(j));
			}
		}
		for(int i=0;i<answerSet.size();i++)
		{
			for(int j=0;j<answerSet.get(i).getNumEdges();j++)
			{
				answerTree.addEdge(answerSet.get(i).getEdge(j));
			}
		}
		
		
		//print out answer
		/*
		for(int i=0;i<answerTree.getNumVertices();i++)
		{
			Vertex v=answerTree.getVertex(i);
			System.out.print("vertex "+v.getNumber()+" has " + v.getUpStreamNode().getNumber()+" as upstream and ");
			for(int j=0;j<v.getDownStreamNodes().size();j++)
			{
				System.out.print(v.getDownStreamNodes().get(j).getNumber()+" ");
			}
			System.out.println(" as downstream");
		}
		*/
		return answerTree;
	}
	
	public static Graph createBaseGraph(Config conf)
	{
		ArrayList<ArrayList<String>> neighborList=conf.getNeighborList();
		Graph g = new Graph();
		for(int i=0;i<neighborList.size();i++)
		{
			g.addVertex(new Vertex(i));
		}
		for(int i=0;i<neighborList.size();i++)
		{

			for(int j=0;j<neighborList.get(i).size();j++)
			{
				Edge e=new Edge(i,Integer.parseInt(neighborList.get(i).get(j)));
				if((g.findEdgeIndex(e.getEnd1(), e.getEnd2())==-1)&&(g.findEdgeIndex(e.getEnd2(), e.getEnd1())==-1))
				{

					g.addEdge(e);
				}
			}
		}
		return g;
	}

}
