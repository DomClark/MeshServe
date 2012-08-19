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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client implements Runnable, Comparable<Client> {

	private static int exceptionBreak = 12;

	private Server server;
	private Socket sock;
	private InputStream in;
	private OutputStream out;
	private boolean muted;
	private boolean running;
	private Thread listener;
	private Group group;

	public Client(Server server, Socket sock){
		this.server = server;
		this.sock = sock;
		this.muted = false;
		try {
			in = sock.getInputStream();
			out = sock.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void listen(){
		running = true;
		listener = new Thread(this, "Client listener for " + ip() + ":" + port());
		listener.start();
	}

	public synchronized void write(String s){
		byte[] string = s.getBytes();
		int size = string.length;
		byte[] sizefield = new byte[]{
				(byte) ((size >> 24) & 0xff),
				(byte) ((size >> 16) & 0xff),
				(byte) ((size >> 8) & 0xff),
				(byte) (size & 0xff)
		};
		try {
			out.write(sizefield);
			out.write(string);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close(){
		running = false;
		try {
			out.close();
			sock.close();
			if(listener.isAlive() && Thread.currentThread() != listener) listener.join();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void run(){
		int exceptions = 0;
		byte[] sizefield = new byte[4];
		byte[] buf = new byte[128];
		while(running){
			try {
				in.read(sizefield, 0, 4);
				int size = (sizefield[0] & 0xff) << 24 |
						(sizefield[1] & 0xff) << 16 |
						(sizefield[2] & 0xff) << 8 |
						(sizefield[3] & 0xff);
				if(buf.length < size) buf = new byte[size];
				in.read(buf, 0, size);
				server.input(this, new String(buf, 0, size));
				exceptions = 0;
			} catch (IOException e) {
				if(e.getMessage().equals("Connection reset")){
					server.removeConnection(this);
					return;
				}
				if(running && exceptions == 0) e.printStackTrace();
				if(++exceptions >= exceptionBreak) running = false;
			}
		}
	}

	public String ip(){
		return sock.getInetAddress().getHostAddress();
	}

	public int port(){
		return sock.getPort();
	}

	public void setMuted(boolean muted){
		this.muted = muted;
	}

	public boolean isMuted(){
		return muted;
	}

	public void setGroup(Group group){
		this.group = group;
	}

	public Group getGroup(){
		return group;
	}

	public boolean isHost(){
		Client c = group.getHost();
		if(c == null) return false;
		return c == this;
	}

	protected void finalize(){
		running = false;
		try {
			out.close();
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int compareTo(Client o) {
		return this.group.getName().compareTo(o.group.getName());
	}

}
