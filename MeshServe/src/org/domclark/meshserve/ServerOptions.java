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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class ServerOptions extends OptionsPanel implements ActionListener {

	private static final long serialVersionUID = 316604L;

	private class IPItem {

		private String ip;
		private String desc;

		private IPItem(String ip, String desc){
			this.ip = ip;
			this.desc = desc;
		}

		public String toString(){
			return ip + " (" + desc + ")";
		}

	}

	private Gui gui;
	private Server target;
	private ButtonGroup typeGroup;
	private JCheckBox meshType;
	private JCheckBox casType;
	private JCheckBox loggingC;
	private JComboBox ipB;
	private JSpinner maxClients;

	public ServerOptions(Gui gui, Server target){
		this.gui = gui;
		this.target = target;
		FlowLayout leading = new FlowLayout(FlowLayout.LEADING);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		typeGroup = new ButtonGroup();
		JPanel general = new JPanel();
		general.setLayout(new BoxLayout(general, BoxLayout.Y_AXIS));
		general.setBorder(BorderFactory.createTitledBorder("General"));
		JPanel ipP = new JPanel(leading);
		JLabel ipL = new JLabel("Server IP address: ");
		ipB = new JComboBox();
		ipP.add(ipL);
		ipP.add(ipB);
		general.add(ipP);
		JPanel mcP = new JPanel(leading);
		JLabel mcL = new JLabel("Max clients: ");
		mcP.add(mcL);
		maxClients = new JSpinner();
		maxClients.setModel(new SpinnerNumberModel(0, 0, null, 1));
		((JTextField) ((JSpinner.DefaultEditor) maxClients.getEditor()).getComponents()[0]).setColumns(3);
		mcP.add(maxClients);
		general.add(mcP);
		JPanel loggingP = new JPanel(leading);
		loggingC = new JCheckBox("Enable logging");
		loggingP.add(loggingC);
		JButton loggingB = new JButton("Clear log");
		loggingB.addActionListener(this);
		loggingP.add(loggingB);
		general.add(loggingP);
		add(general);
		JPanel type = new JPanel();
		type.setLayout(new BoxLayout(type, BoxLayout.Y_AXIS));
		type.setBorder(BorderFactory.createTitledBorder("Server type"));
		JPanel meshDescP = new JPanel(leading);
		JLabel meshDesc = new JLabel("With a mesh server, all messages get sent to all clients.");
		meshDescP.add(meshDesc);
		type.add(meshDescP);
		JPanel meshTypeP = new JPanel(leading);
		meshType = new JCheckBox("This server is of type " + Server.TYPES[0]);
		meshType.setActionCommand("mesh");
		typeGroup.add(meshType);
		meshTypeP.add(meshType);
		type.add(meshTypeP);
		JPanel casDescP1 = new JPanel(leading);
		JPanel casDescP2 = new JPanel(leading);
		JLabel casDesc1 = new JLabel("With a clients and server setup, all messages from the clients are sent to the server,");
		JLabel casDesc2 = new JLabel("which then replies to all of the clients.");
		casDescP1.add(casDesc1);
		casDescP2.add(casDesc2);
		type.add(casDescP1);
		type.add(casDescP2);
		JPanel casTypeP = new JPanel(leading);
		casType = new JCheckBox("This server is of type " + Server.TYPES[1]);
		casType.setActionCommand("cas");
		typeGroup.add(casType);
		casTypeP.add(casType);
		type.add(casTypeP);
		add(type);
	}

	public void onShow(){
		meshType.setSelected(target.getType() == 0);
		casType.setSelected(target.getType() == 1);
		loggingC.setSelected(gui.isLoggingEnabled());
		maxClients.setValue(target.getMaxConnections());
		try {
			Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
			while(nis.hasMoreElements()){
				NetworkInterface ni = nis.nextElement();
				Enumeration<InetAddress> ias = ni.getInetAddresses();
				while(ias.hasMoreElements()){
					String ip = ias.nextElement().getHostAddress();
					IPItem ipi = new IPItem(ip, ni.getDisplayName());
					ipB.addItem(ipi);
					if(ip.equals(target.getIpAddress())) ipB.setSelectedItem(ipi);
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if(cmd.equals("Clear log")){
			gui.clearLog();
		}
	}

	public void saveOptions(){
		ButtonModel bm = typeGroup.getSelection();
		if(bm != null){
			if(bm.getActionCommand().equals("mesh")) target.setType(0);
			if(bm.getActionCommand().equals("cas")) target.setType(1);
		}
		target.setIpAddress(((IPItem) ipB.getSelectedItem()).ip);
		target.setMaxConnections((Integer) maxClients.getValue());
		gui.setLogging(loggingC.isSelected());
	}

	public String getTitle(){
		return "Server";
	}

}
