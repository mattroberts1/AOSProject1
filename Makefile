	
default:	
	javac Client.java Config.java Controller.java Edge.java Graph.java ListenerSocket.java LocalState.java Message.java Server.java TreeMaker.java Vertex.java
clean: 
	$(RM) *.class -f
	$(RM) outfile.txt -f