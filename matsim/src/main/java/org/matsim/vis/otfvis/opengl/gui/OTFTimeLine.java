package org.matsim.vis.otfvis.opengl.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.matsim.core.utils.misc.Time;
import org.matsim.vis.otfvis.data.OTFClientQuad;
import org.matsim.vis.otfvis.gui.OTFHostControlBar;
import org.matsim.vis.otfvis.interfaces.OTFDrawer;
import org.matsim.vis.otfvis.interfaces.OTFQueryHandler;

// TODO should not be an OTFDrawer, need to handle invalidate better
/**
 * OTFTimeLine is the time line toolbar.
 * It is only used in case of playing a mvi file.
 *
 * @author dstrippgen
 *
 */
public class OTFTimeLine extends JToolBar implements OTFDrawer, ActionListener, ChangeListener {

	private static final long serialVersionUID = 1L;
	private final OTFHostControlBar hostControl;
	private JSlider times;
	Collection<Integer> cachedTime = new ArrayList<Integer>();

	Hashtable<Integer, JLabel> labelTable =
		new Hashtable<Integer, JLabel>();

	public OTFTimeLine(String string, OTFHostControlBar hostControl) {
		super(string);
		this.hostControl = hostControl;
		hostControl.addDrawer("timeline", this);

		addSlider();

		JButton button = new JButton();
		button.setText("[");
		button.setActionCommand("setLoopStart");
		button.addActionListener(this);
		button.setToolTipText("Sets the loop start time");
		add(button);

		button = new JButton();
		button.setText("]");
		button.setActionCommand("setLoopEnd");
		button.addActionListener(this);
		button.setToolTipText("Sets the loop end time");

		add(button);
		this.setVisible(true);
	}

	public class MyJSlider extends JSlider {
		public MyJSlider(int horizontal, int intValue, int intValue2, int time) {
			super(horizontal,intValue, intValue2, time);
		}

		@Override
		synchronized public void paint(Graphics g) {
			super.paint(g);
			Rectangle bounds = g.getClipBounds();
			bounds.grow(-32, 0);

			double delta = getMaximum() - getMinimum();
			// get cached timesteps for hostctrl and draw them
			synchronized (cachedTime) {
				for(Integer time : cachedTime) {
					g.setColor(Color.LIGHT_GRAY);
					g.fillRect(bounds.x + (int)(bounds.width*((time- getMinimum())/delta)), (int)bounds.getCenterY(), 5, 5);
				}
			}
		}
	}

	void replaceLabel(String label, int newEnd) {
		for (Integer i : labelTable.keySet() ) {
			JLabel value = labelTable.get(i);
			if(value.getText().equals(label)) {
				labelTable.remove(i);
				break;
			}
		}
		labelTable.put(Integer.valueOf(newEnd), new JLabel(label));
		times.setLabelTable(labelTable);

		times.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// remove old label
		// get actual time
		int time = times.getValue();
		if(e.getActionCommand().equals("setLoopStart")){
			hostControl.setLoopBounds(time, -1);
			replaceLabel("[", time);
		}else if(e.getActionCommand().equals("setLoopEnd")){
			hostControl.setLoopBounds(-1, time);
			replaceLabel("]", time);
		}else if(e.getActionCommand().equals("cancelcaching")){
			setCachedTime(-1);
		}
		// insert new label
	}

	/** Listen to the slider. */
	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider)e.getSource();
		if (!source.getValueIsAdjusting()) {
			int newTime_s = source.getValue();
			hostControl.setNEWTime(newTime_s);
		} else {
			hostControl.stopMovie();
		}
	}

	public void addSlider() {

		//Create the slider.
		Collection<Double> steps = hostControl.getOTFHostControl().getTimeStepsdrawer();
		if ((steps == null) || (steps.size() == 0)) {
			times = new MyJSlider(JSlider.HORIZONTAL, 0, 0, 0);
			return; // nothing to display
		}
		Double[] dsteps = steps.toArray(new Double[steps.size()]);

		int min = dsteps[0].intValue();
		int max = dsteps[dsteps.length-1].intValue();
		int value = (int)hostControl.getOTFHostControl().getTime();
		times = new MyJSlider(JSlider.HORIZONTAL, min, max, value);

		times.addChangeListener(this);
		times.setMajorTickSpacing((min-max)/10);
		times.setPaintTicks(true);

		//Create the label table.
		//PENDING: could use images, but we don't have any good ones.
		labelTable.put(Integer.valueOf( min ),
				new JLabel(Time.writeTime(dsteps[0], Time.TIMEFORMAT_HHMM)));
		//new JLabel(createImageIcon("images/stop.gif")) );
		labelTable.put(Integer.valueOf( max ),
				new JLabel(Time.writeTime(dsteps[dsteps.length-1], Time.TIMEFORMAT_HHMM)) );
		//new JLabel(createImageIcon("images/fast.gif")) );

		int n = dsteps.length/10 + 1;

		for(int i= n; i< dsteps.length-1; i+=n) {
			labelTable.put(Integer.valueOf( dsteps[i].intValue() ),
					new JLabel(Time.writeTime(dsteps[i], Time.TIMEFORMAT_HHMM)) );
		}
		times.setLabelTable(labelTable);

		times.setPaintLabels(true);
		times.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
		add(times);
	}

	@Override
	public void clearCache() {
	}

	@Override
	public Component getComponent() {
		return null;
	}

	@Override
	public OTFClientQuad getQuad() {
		return null;
	}

	@Override
	public void handleClick(java.awt.geom.Point2D.Double point, int mouseButton, MouseEvent e) {
	}

	@Override
	public void handleClick(Rectangle currentRect, int button) {
	}

	@Override
	public void invalidate(int time) throws RemoteException {
		if(time >= 0) times.setValue(time);
		else {
			synchronized (cachedTime) {
				cachedTime.add(-time);
				times.repaint();
			}
		}
	}

	private JButton cancelCaching = null;
	public boolean isCancelCaching = false;
	synchronized public void setCachedTime(int time) {
		if(time == -1){
			cachedTime.clear();
			if(cancelCaching != null) {
				isCancelCaching = true;
				remove(cancelCaching);
				this.doLayout();
				invalidate();
				setVisible(true);
				cancelCaching = null;
			}
		} else {
			if(cancelCaching == null) {
				cancelCaching = new JButton();
				cancelCaching.setText("Cancel");
				cancelCaching.setActionCommand("cancelcaching");
				cancelCaching.addActionListener(this);
				cancelCaching.setToolTipText("Cancel the preloading of timesteps");

				add(cancelCaching);
				validate();
				this.setVisible(true);
			}
			synchronized (cachedTime) {
				cachedTime.add(time);
				times.repaint();
			}
		}
	}

	@Override
	public void redraw() {
	}

	@Override
	public void setQueryHandler(OTFQueryHandler queryHandler) {
		// Empty because .. well, because this method implements OTFDrawer although it shouldn't.
	}
	
	
	
}
