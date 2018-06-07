import java.util.Scanner;
import java.io.File;
import java.util.ArrayList;
public class Config {
	int numNodes;
	int minPerActive;
	int maxPerActive;
	int minSendDelay;
	int snapshotDelay;
	int maxNumber; 
	String [][] portList;
	
	ArrayList<ArrayList<String>> neighborList;
	Config(String filepath)
	{ 
		Scanner sc=null;
		try {
		sc = new Scanner(new File (filepath));
		}
		catch(Exception e)
		{
			System.out.println("Config file not found.");
		}
		String line="";
		int counter1=0;
		int counter2=0;
		boolean readFirstLine=false;
		//iterates through every line in configuration file
		while (sc.hasNextLine())
		{
			line=sc.nextLine();
			//if line is empty read next line
			if(line.length()==0)
			{
				continue;
			}
			char a = line.charAt(0);
			//check if first char in line is an integer, if not skip line
			if(a!='0'&&a!='1'&&a!='2'&&a!='3'&&a!='4'&&a!='5'&&a!='6'&&a!='7'&&a!='8'&&a!='9')
			{
				continue;
			}
			
			if(readFirstLine==false)
			{
				String[] tokens = line.split("\\s+");
				numNodes=Integer.parseInt(tokens[0]);
				minPerActive=Integer.parseInt(tokens[1]);
				maxPerActive=Integer.parseInt(tokens[2]);
				minSendDelay=Integer.parseInt(tokens[3]);
				snapshotDelay=Integer.parseInt(tokens[4]);
				maxNumber=Integer.parseInt(tokens[5]);
				readFirstLine=true;
				portList=new String [numNodes][3];
				neighborList=new ArrayList<ArrayList<String>>(numNodes);
				continue;
			}
			if(counter1<numNodes) 
			{
				String[] tokens = line.split("\\s+");
				portList[counter1][0]=tokens[0];
				portList[counter1][1]=tokens[1];
				portList[counter1][2]=tokens[2];
				counter1++;
				continue;
			}
			
			if(counter2<numNodes)
			{
				neighborList.add(new ArrayList<String>());
				String[] tokens = line.split("\\s+");
				for(int i=0;i<tokens.length;i++)
				{
					if(tokens[i].charAt(0)=='#')  //skip rest of line if comment symbol is found
					{
						break;
					}
					neighborList.get(counter2).add(tokens[i]);
				}
				counter2++;
			}
			
			
			
			
			
		}
		
		
	}

	public int getNumNodes()
	{
		return numNodes;
	}
	public int getMinPerActive()
	{
		return minPerActive;
	}
	public int getMaxPerActive()
	{
		return maxPerActive;
	}
	public int getMinSendDelay()
	{
		return minSendDelay;
	}
	public int getSnapshotDelay()
	{
		return snapshotDelay;
	}
	public int getMaxNumber()
	{
		return maxNumber;
	}
	public String[][] getPortList()
	{
		return portList;
	}
	public ArrayList<ArrayList<String>> getNeighborList()
	{
		return neighborList;
	}

}
