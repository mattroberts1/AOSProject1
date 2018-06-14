import java.util.ArrayList;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
public class Controller {

	public static void main(String[] args) {
		
		/*
		Server s = new Server();
		try {
			new Thread(s).start();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		Client c1 = new Client();
		Client c2 = new Client();
		try {
			new Thread(c1).start();
			new Thread(c2).start();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		*/
		String name=getdcxxName();
		if(name.equals("dc02"))
		{
			Client c = new Client();
			c.run();
		}
		if(name.equals("dc01"))
		{
			Server s = new Server();
			s.run();
		}
		
	}
	
	

	public static void testReadConfig()
	{
		 /* This code just checks that config file is being read correctly 
		 */
		/*
		Config conf = new Config(args[0]);
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
	
	public static String getdcxxName()
	{
		String hostName="";
		try {
		hostName=InetAddress.getLocalHost().getHostName();
		}
		catch(Exception e)
		{	
			e.printStackTrace();
		}
		String[] temp = hostName.split("\\.");
		return temp[0];
	}
	
}
