import java.util.ArrayList;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
public class Controller {

	public static void main(String[] args) {
		Config conf = new Config(args[0]);
		boolean isActive=false;
		String thisNodesName=getdcxxName();
		String[][] portList =conf.getPortList();
		int thisNodesID=-1;
		for(int i=0;i<portList.length;i++)
		{
			if(thisNodesName.equals(portList[i][1]))
			{
				thisNodesID=i;
			}
		}
		if(thisNodesID==0)
		{
			isActive=true;
		}

	}
	
	

	public static void testReadConfig()
	{
	
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
