package org.domclark.meshserve;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class OptionsDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 461375L;

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

	public OptionsDialog(Gui gui, JFrame owner, Server target){
		super(owner, "Configure MeshServe");
		this.gui = gui;
		this.target = target;
		FlowLayout leading = new FlowLayout(FlowLayout.LEADING);
		JPanel main = new JPanel();
		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
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
		JPanel loggingP = new JPanel(leading);
		loggingC = new JCheckBox("Enable logging");
		loggingP.add(loggingC);
		JButton loggingB = new JButton("Clear log");
		loggingB.addActionListener(this);
		loggingP.add(loggingB);
		general.add(loggingP);
		main.add(general);
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
		main.add(type);
		setLayout(new BorderLayout());
		add(main, BorderLayout.CENTER);
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		JButton ok = new JButton("OK");
		ok.addActionListener(this);
		buttons.add(ok);
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		buttons.add(cancel);
		add(buttons, BorderLayout.SOUTH);
		setPreferredSize(new Dimension(720, 360));
		pack();
	}

	public void setVisible(boolean visible){
		if(visible){
			meshType.setSelected(target.getType() == 0);
			casType.setSelected(target.getType() == 1);
			loggingC.setSelected(gui.isLoggingEnabled());
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
		super.setVisible(visible);
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if(cmd.equals("Clear log")){
			gui.clearLog();
			return;
		}
		if(cmd.equals("OK")){
			ButtonModel bm = typeGroup.getSelection();
			if(bm != null){
				if(bm.getActionCommand().equals("mesh")) target.setType(0);
				if(bm.getActionCommand().equals("cas")) target.setType(1);
			}
			target.setIpAddress(((IPItem) ipB.getSelectedItem()).ip);
			gui.setLogging(loggingC.isSelected());
		}
		setVisible(false);
		gui.updateButtons();
	}

}
