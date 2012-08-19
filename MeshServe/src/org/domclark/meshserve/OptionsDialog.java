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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class OptionsDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 461375L;

	private OptionsPanel[] panels;
	private Server target;
	private JButton ok;
	private Gui gui;

	public OptionsDialog(Gui gui, JFrame owner, Server target){
		super(owner, "Configure MeshServe", true);
		this.gui = gui;
		this.target = target;
		panels = new OptionsPanel[2];
		panels[0] = new ServerOptions(gui, target);
		panels[1] = new GroupOptions(gui, target);
		setLayout(new BorderLayout());
		JTabbedPane tabs = new JTabbedPane();
		for(OptionsPanel panel : panels) tabs.addTab(panel.getTitle(), panel);
		add(tabs, BorderLayout.CENTER);
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		ok = new JButton("OK");
		ok.addActionListener(this);
		buttons.add(ok);
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		buttons.add(cancel);
		add(buttons, BorderLayout.SOUTH);
		setPreferredSize(new Dimension(720, 400));
		pack();
	}

	public void setVisible(boolean visible){
		ok.setEnabled(!target.isRunning());
		if(visible) for(OptionsPanel panel : panels) panel.onShow();
		super.setVisible(visible);
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if(cmd.equals("OK") && !target.isRunning()) for(OptionsPanel panel : panels) panel.saveOptions();
		gui.saveOptions();
		gui.updateButtons();
		setVisible(false);
	}

}
