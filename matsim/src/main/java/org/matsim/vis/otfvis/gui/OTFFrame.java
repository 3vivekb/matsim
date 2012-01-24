/* *********************************************************************** *
 * project: org.matsim.*
 * OTFFrame
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.vis.otfvis.gui;

import java.awt.event.WindowEvent;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;


/**
 * @author dgrether
 *
 */
public class OTFFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OTFFrame(String title) {
		super(title);
		this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		JFrame.setDefaultLookAndFeelDecorated(true);
		boolean isMac = System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("mac os x");
		if (isMac){
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			this.getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);
		}
		//Make sure menus appear above JOGL Layer
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			this.endProgram(0);
		} else {
			super.processWindowEvent(e);
		}
	}

	public void endProgram(int code) {
//		if(OTFClientControl.getInstance().getOTFVisConfig().isModified()) {
//			final JDialog dialog = new JDialog((JFrame)null, "Preferences are unsaved and modified...", true);
//			final JOptionPane optionPane = new JOptionPane(
//					"There are potentially unsaved changes in Preferences.\nQuit anyway?",
//					JOptionPane.QUESTION_MESSAGE,
//					JOptionPane.YES_NO_OPTION);
//			dialog.setContentPane(optionPane);
//			dialog.setDefaultCloseOperation(
//					JDialog.DO_NOTHING_ON_CLOSE);
//			dialog.addWindowListener(new WindowAdapter() {
//				@Override
//				public void windowClosing(WindowEvent we) {
//
//				}
//			});
//			optionPane.addPropertyChangeListener(
//					new PropertyChangeListener() {
//						@Override
//						public void propertyChange(PropertyChangeEvent e) {
//							String prop = e.getPropertyName();
//
//							if (dialog.isVisible()
//									&& (e.getSource() == optionPane)
//									&& (prop.equals(JOptionPane.VALUE_PROPERTY))) {
//								//If you were going to check something
//								//before closing the window, you'd do
//								//it here.
//								dialog.setVisible(false);
//							}
//						}
//					});
//			dialog.pack();
//			dialog.setVisible(true);
//			int value = ((Integer)optionPane.getValue()).intValue();
//			if (value == JOptionPane.NO_OPTION) {
//				return; // do not quit
//			}
//
//		}
		System.exit(code);
	}

}
