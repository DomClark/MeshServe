package org.domclark.meshserve;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable {

	private static int exceptionBreak = 12;

	public static final int MESH_PORT = 42001;

	public static final String[] TYPES = new String[]{
		"Mesh",
		"Clients and Server"
	};

	private Gui gui;
	private int maxClients;
	private ServerSocket sock;
	private List<Client> clients;
	private Client casHost;
	private boolean running;
	private Thread serverThread;
	private int type;
	private String ipAddress;

	public Server(Gui gui){
		this.gui = gui;
		maxClients = 20;
		clients = new ArrayList<Client>(maxClients);
		casHost = null;
		ipAddress = "127.0.0.1";
	}

	public boolean addConnection(Client c){
		if(clients.size() >= maxClients) return false;
		clients.add(c);
		gui.log("Client joined from IP " + c.ip());
		gui.clientUpdated(true);
		return true;
	}

	public void removeConnection(Client c){
		clients.remove(c);
		if(casHost == c) casHost = null;
		gui.log("Client left from IP " + c.ip());
		gui.clientUpdated(false);
		gui.updateButtons();
		c.close();
	}

	public void start(){
		running = true;
		serverThread = new Thread(this, "Server listener");
		serverThread.start();
		gui.updateButtons();
	}

	public void stop(){
		running = false;
		for(Client c : clients) c.close();
		if(sock != null) try {
			sock.close();
			if(serverThread.isAlive())
				try {
					serverThread.join();
				} catch (InterruptedException e) {
					gui.log(e);
				}
		} catch (IOException e) {
			gui.log(e);
		}
		clients.clear();
		gui.log("Server stopped!");
		gui.clientUpdated(false);
		gui.updateButtons();
	}

	public void input(Client c, String s){
		if(c.isMuted()){
			gui.log("Muted client " + c.ip() + " tried to send message \"" + s + "\"!");
			return;
		}
		gui.log("Received input \"" + s + "\" from client at IP " + c.ip());
		if(type == 0 || casHost == c){
			sendToClientsExcept(s, c);
			return;
		}
		if(casHost != null) casHost.write(s);
	}

	public void sendToClientsExcept(String send, Client exception){
		for(Client c : clients) if(c != exception) c.write(send);
	}

	public void run(){
		try {
			sock = new ServerSocket(MESH_PORT, 50, InetAddress.getByName(ipAddress));
		} catch(BindException e){
			gui.log("IP / host name \"" + ipAddress + "\" is invalid!");
			return;
		} catch (IOException e) {
			gui.log(e);
			return;
		}
		gui.log("Server started!");
		gui.log("IP address is " + sock.getInetAddress().getHostAddress());
		int exceptions = 0;
		while(running){
			try {
				Socket conn = sock.accept();
				Client c = new Client(this, conn);
				if(addConnection(c)) c.listen();
				else c.close();
				exceptions = 0;
			} catch (IOException e) {
				if(running && exceptions == 0) gui.log(e);
				if(++exceptions >= exceptionBreak) running = false;
			}
		}
	}

	public void setMaxConnections(int max){
		maxClients = max;
	}

	public int getMaxConnections(){
		return maxClients;
	}

	public List<Client> getClients(){
		return clients;
	}

	public boolean isRunning(){
		return running;
	}

	public void setType(int type){
		this.type = type;
	}

	public int getType(){
		return type;
	}

	public void setIpAddress(String ip){
		ipAddress = ip;
	}

	public String getIpAddress(){
		return ipAddress;
	}

	public void setHost(Client host){
		casHost = host;
		if(!clients.contains(host)) clients.add(host);
		gui.clientUpdated(false);
	}

	public Client getHost(){
		return casHost;
	}

}
