import java.util.ArrayList;
public class Controller {

	public static void main(String[] args) {
		Config conf = new Config(args[0]);
		
		
		/*
		 * This code just checks that config file is being read correctly 
		 * 
		
		System.out.println("numNodes: "+conf.getNumNodes());
		System.out.println("minPerActive: "+conf.getMinPerActive());
		System.out.println("maxPerActive: "+conf.getMaxPerActive());
		System.out.println("minSendDelay: "+conf.getMinSendDelay());
		System.out.println("snapshotDelay: "+conf.getSnapshotDelay());
		System.out.println("maxNumber: "+conf.getMaxNumber());
		
		System.out.println("--------------");
		System.out.println("Printing port list.");
		String[][] portList = conf.getPortList();
		for(int i=0;i<portList.length;i++)
		{
			for(int j=0; j<3;j++)
			{
				System.out.print(portList[i][j]+" ");
			}
			System.out.println();
		}
		
		System.out.println("--------------");
		System.out.println("Printing neighbors list.");
		ArrayList<ArrayList<String>> neighborList = conf.getNeighborList();
		for(int i=0;i<neighborList.size();i++)
		{
			for(int j=0;j<neighborList.get(i).size();j++)
			{
				System.out.print(neighborList.get(i).get(j)+" ");
			}
			System.out.println();
		}
		
		*/
	}

}
