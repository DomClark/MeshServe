/*
 *  Copyright © 2012 Dominic Clark (TheSuccessor)
 *
 *  This file is part of MeshServe.
 *
 *  MeshServe is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MeshServe is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MeshServe.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.domclark.meshserve;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
	private List<Group> groups;
	private Group defaultGroup;
	private boolean running;
	private Thread serverThread;
	private int type;
	private String ipAddress;

	public Server(Gui gui){
		this.gui = gui;
		maxClients = 20;
		clients = new ArrayList<Client>(maxClients);
		groups = new ArrayList<Group>();
		groups.add(new Group("Default group"));
		defaultGroup = groups.get(0);
		ipAddress = "127.0.0.1";
	}

	public boolean addConnection(Client c){
		if(clients.size() >= maxClients) return false;
		if(!defaultGroup.addClient(c)) return false;
		clients.add(c);
		gui.log("Client joined from IP " + c.ip());
		gui.clientUpdated(true);
		return true;
	}

	public void removeConnection(Client c){
		clients.remove(c);
		if(c.isHost()) c.getGroup().setHost(null);
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
		if(s.isEmpty()) return;
		if(c.isMuted()){
			gui.log("Muted client " + c.ip() + " tried to send message \"" + s + "\"!");
			return;
		}
		if(s.startsWith("broadcast \"<")){
			s = s.substring(12, s.length() - 1);
			for(Group g : groups)
				if(g.getType() == 0 && g.getName().equals(s)){
					gui.log("Client " + c.ip() + " joining group " + s);
					g.addClient(c);
					gui.clientUpdated(false);
					break;
				}
			return;
		}
		gui.log("Received input \"" + s + "\" from client at IP " + c.ip());
		synchronized(c.getGroup()){
			Group g = c.getGroup();
			if(type == 0 || c.isHost()){
				g.sendToClientsExcept(s, c);
				return;
			}
			if(g.getHost() != null) g.getHost().write(s);
		}
	}

	public void sendToClientsExcept(String send, Client exception){
		for(Client c : clients) if(c != exception) c.write(send);
	}

	/*public void connectToServer(String addr){
		try {
			Socket s = new Socket(addr, MESH_PORT);
			host = new Client(this, s);
			host.listen();
			gui.clientUpdated(true);
		} catch (IOException e) {
			gui.log("Failed to connect to server \"" + addr + "\":");
			gui.log(e);
		}
	}*/

	public void run(){
		try {
			sock = new ServerSocket(MESH_PORT, 50, InetAddress.getByName(ipAddress));
		} catch(BindException e){
			gui.log("IP / host name \"" + ipAddress + "\" is invalid, or port is occupied!");
			gui.log(e);
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

	public List<Group> getGroups(){
		return groups;
	}

	public Group getDefaultGroup(){
		return defaultGroup;
	}

	public void setDefaultGroup(Group g){
		defaultGroup = g;
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

	public void readFrom(DataInputStream dis) throws IOException{
		type = dis.readInt();
		maxClients = dis.readInt();
		ipAddress = dis.readUTF();
		int numGroups = dis.readInt();
		if(numGroups > 0) groups.clear();
		for(int i = 0; i < numGroups; i++){
			Group g = new Group();
			g.readFrom(dis);
			groups.add(g);
		}
		defaultGroup = groups.get(dis.readInt());
	}

	public void writeOn(DataOutputStream dos) throws IOException{
		dos.writeInt(type);
		dos.writeInt(maxClients);
		dos.writeUTF(ipAddress);
		dos.writeInt(groups.size());
		for(Group g : groups) g.writeOn(dos);
		dos.writeInt(groups.indexOf(defaultGroup));
	}

}
