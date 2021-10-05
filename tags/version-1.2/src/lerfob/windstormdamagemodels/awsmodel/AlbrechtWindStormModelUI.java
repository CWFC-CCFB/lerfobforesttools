/** 
 * Copyright (C) 2010-2012 LERFoB INRA/AgroParisTech - FVA Baden-Wurttemberg 
 * 
 * Authors: Mathieu Fortin, Axel Albrecht 
 * 
 * This file is part of the lerfob library. You can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * The awsmodel library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should find a copy of the GNU lesser General Public License at
 * <http://www.gnu.org/licenses/>.
 */
package lerfob.windstormdamagemodels.awsmodel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class AlbrechtWindStormModelUI extends JDialog implements ActionListener, WindowListener {
	
//	private AlbrechtWindStormMaker caller;
	protected JCheckBox stochasticOptionCheckBox;
	private JButton ok;
		
	public AlbrechtWindStormModelUI(AlbrechtWindStormModel caller) {
		super();
		addWindowListener(this);
		
//		this.caller = caller;
		stochasticOptionCheckBox = new JCheckBox("Enable stochastic mode");
		stochasticOptionCheckBox.setSelected(caller.isStochasticModeEnabled);		// synchronization with the caller;
		stochasticOptionCheckBox.addItemListener(caller);
		
		ok = new JButton("Ok");
		ok.addActionListener(this);
		
		createUI();
	}
	
	private void createUI() {
		setLayout(new BorderLayout());
		setTitle("Albrecht et al.'s wind damage model");
		
		JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		checkBoxPanel.add(stochasticOptionCheckBox);
		
		add(checkBoxPanel, BorderLayout.NORTH);
		
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		controlPanel.add(ok);
		
		add(controlPanel, BorderLayout.SOUTH);
		
		Dimension prefSize = new Dimension(300,125);
		setPreferredSize(prefSize);
		setMinimumSize(prefSize);
		
		setModal(true);
		setLocationRelativeTo(null);
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource().equals(ok)) {
			okAction();
		}
	}
	
	
	private void okAction() {
		setVisible(false);
	}
	
	
	/**
	 * Called when the window is closed using the right corner x.
	 */
	@Override
	public void windowClosing(WindowEvent e) {
		okAction();
	}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowOpened(WindowEvent e) {}

	
}
