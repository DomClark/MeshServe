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
import java.util.ArrayList;
import java.util.List;

public class Group {

	private List<Client> clients;
	private Client host;
	private int maxClients;
	private String name;
	private int type;

	public Group(){
		this("");
	}

	public Group(String name){
		this.name = name;
		maxClients = 20;
		type = 0;
		clients = new ArrayList<Client>(maxClients);
	}

	public boolean addClient(Client c){
		if(clients.size() >= maxClients) return false;
		Group g = c.getGroup();
		if(g != null) g.removeClient(c);
		c.setGroup(this);
		clients.add(c);
		return true;
	}

	public void removeClient(Client c){
		clients.remove(c);
	}

	public void sendToClientsExcept(String send, Client exception){
		for(Client c : clients) if(c != exception) c.write(send);
	}

	public List<Client> getClients(){
		return clients;
	}

	public void setMaxClients(int max){
		maxClients = max;
	}

	public int getMaxClients(){
		return maxClients;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setHost(Client newHost){
		host = newHost;
	}

	public Client getHost(){
		return host;
	}

	public void setType(int type){
		this.type = type;
	}

	public int getType(){
		return type;
	}

	public void writeOn(DataOutputStream dos) throws IOException {
		dos.writeUTF(name);
		dos.writeInt(maxClients);
		dos.writeInt(type);
	}

	public void readFrom(DataInputStream dis) throws IOException {
		name = dis.readUTF();
		maxClients = dis.readInt();
		type = dis.readInt();
	}

}
