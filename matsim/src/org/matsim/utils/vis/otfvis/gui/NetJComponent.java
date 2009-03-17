/* *********************************************************************** *
 * project: org.matsim.*
 * NetJComponent.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

package org.matsim.utils.vis.otfvis.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.rmi.RemoteException;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;

import org.matsim.utils.vis.netvis.renderers.ValueColorizer;
import org.matsim.utils.vis.otfvis.caching.SceneGraph;
import org.matsim.utils.vis.otfvis.data.OTFClientQuad;
import org.matsim.utils.vis.otfvis.data.OTFData;
import org.matsim.utils.vis.otfvis.data.OTFDataQuad;
import org.matsim.utils.vis.otfvis.data.OTFDataSimpleAgent;
import org.matsim.utils.vis.otfvis.interfaces.OTFDrawer;
import org.matsim.utils.vis.otfvis.interfaces.OTFQuery;


/**
 * @author david
 *
 */
abstract class OTFSwingDrawable implements OTFDrawable, OTFData.Receiver{
	static Graphics2D g2d = null;
	//static AffineTransform boxTransform = null;

	public final void draw() {
		onDraw(g2d);
	}
	
	abstract public void onDraw(Graphics2D g2d);

	public void invalidate(SceneGraph graph) {
		graph.addItem(this);
	}
}

public class NetJComponent extends JComponent  implements OTFDrawer {

	public static interface NetVisResizable {
		public void scaleNetwork(float scale);
		public float getScale();
		public void repaint();
	}


	public static class MyNetVisScrollPane extends NetVisScrollPane implements NetVisResizable {

		private float scale = 1.f;
		public MyNetVisScrollPane(NetJComponent networkComponent) {
			super(networkComponent);
		}

		@Override
		public void scaleNetwork(float scale){
			this.scale = scale;
			System.out.println("Scalee " + this.scale);
			super.scaleNetwork(scale);
		}
		
		public float getScale() {
			return scale;
		}

		@Override
		public float scaleNetwork(Rectangle destrect, float factor) {
			this.scale = super.scaleNetwork(destrect, factor);
			System.out.println("Scalee " + this.scale);
			return this.scale;
		}

	}
	private static final Color netColor = new Color(180,180,210,128);
	private static final long serialVersionUID = 1L;

	private static final double BORDER_FACTOR = 0.0;
	public static float linkWidth = 100;

    private final int frameDefaultWidth;

    private final int frameDefaultHeight;


    private double viewMinX, viewMinY, viewMaxX, viewMaxY;

	private final OTFClientQuad quad;

	private transient SceneGraph sceneGraph;
	private MyNetVisScrollPane networkScrollPane = null;

	private transient final VizGuiHandler mouseMan;

    public void setViewClipCoords( double minX, double minY, double maxX, double maxY) {
    	viewMinX  = networkClippingMinEasting() +  minX * networkClippingWidth();
    	viewMaxX  = networkClippingMinEasting() +  maxX * networkClippingWidth();

    	viewMinY  = networkClippingMinNorthing() + (1.-maxY) * networkClippingHeight();
    	viewMaxY  = networkClippingMinNorthing() + (1.-minY) * networkClippingHeight();
    }

    public void moveViewClipCoords( double deltaX, double deltaY) {
    	viewMinX  += deltaX * networkClippingWidth();
    	viewMaxX  += deltaX * networkClippingWidth();

    	viewMinY  -= deltaY * networkClippingHeight();
    	viewMaxY  -= deltaY * networkClippingHeight();
    }

    // returns something like this
    // 0  1  2
    // 4  5  6
    // 8  9 10
    //
    // so 5 means the cord is IN the clipping region
    public int checkViewClip(double x, double y) {
    	// check for quadrant
    	int xquart = x < viewMinX ? 0 : x > viewMaxX ? 2 : 1;
    	int yquart = y < viewMinY ? 0 : y > viewMaxY ? 2 : 1;
    	return xquart + 4* yquart;
    }

    public boolean checkLineInClip(double sx, double sy, double ex, double ey) {
    	int qstart = checkViewClip(sx,sy);
    	int qend = checkViewClip(ex,ey);

    	// both in same sector, that is not middle sector
    	if ( (qstart == qend) && qstart != 5) return false;
    	// both are either left or right and not in the middle
    	if( (qstart % 4) == ( qend % 4) && (qstart % 4) != 1 ) return false;
    	// both are either top or bottom but not in the middle
    	if( (qstart / 4) == ( qend / 4) && (qstart / 4) != 1 ) return false;

    	return true; // all other cases are possibly visible
    }

    // --------------- CONSTRUCTION ---------------

    public NetJComponent(JFrame frame, OTFClientQuad quad) {
        this.quad = quad;
 
        // calculate size of frame

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double factor = screenSize.getWidth() / networkClippingWidth();
        factor = Math.min(factor, screenSize.getHeight() / networkClippingHeight());
        factor *= 0.8f;

        frameDefaultWidth = (int) Math.floor(networkClippingWidth() * factor);
        frameDefaultHeight = (int) Math.floor(networkClippingHeight() * factor);

        scale(1);
        setViewClipCoords(0,0,1,1);
        
        networkScrollPane = new MyNetVisScrollPane(this);
		VizGuiHandler handi = new VizGuiHandler();
		networkScrollPane.addMouseMotionListener(handi);
		networkScrollPane.addMouseListener(handi);
		networkScrollPane.getViewport().addChangeListener(handi);
		mouseMan = handi;
		networkScrollPane.addMouseWheelListener(mouseMan);

        // linkWidth = 5;
        // nodeRadius = 5;
        //
        // networkRenderer.setNodeRadius(nodeRadius);
        // networkRenderer.setLinkWidth(linkWidth);
    }

    public void scale(double factor) {
        if (factor > 0) {
            int scaledWidth = (int) Math.round(factor * frameDefaultWidth);
            int scaledHeight = (int) Math.round(factor * frameDefaultHeight);

            this.setPreferredSize(new Dimension(scaledWidth, scaledHeight));
        }
    }

    // -------------------- COORDINATE TRANSFORMATION --------------------

    private double networkClippingEastingBorder() {
        return Math.max(1, BORDER_FACTOR
                * (quad.getMaxEasting() - quad.getMinEasting()));
    }

    private double networkClippingNorthingBorder() {
        return Math.max(1, BORDER_FACTOR
                * (quad.getMaxNorthing() - quad.getMinNorthing()));
    }

    private double networkClippingMinEasting() {
        return 0 - networkClippingEastingBorder();
    }

    private double networkClippingMaxEasting() {
        return quad.getMaxEasting() -quad.getMinEasting() + networkClippingEastingBorder();
    }

    private double networkClippingMinNorthing() {
        return 0 - networkClippingNorthingBorder();
    }

    private double networkClippingMaxNorthing() {
        return quad.getMaxNorthing() - quad.getMinNorthing() + networkClippingNorthingBorder();
    }

    private double networkClippingWidth() {
        return networkClippingMaxEasting() - networkClippingMinEasting();
    }

    private double networkClippingHeight() {
        return networkClippingMaxNorthing() - networkClippingMinNorthing();
    }

    private AffineTransform getBoxTransform() {

        // two original extreme coordinates ...

        double v1 = networkClippingMinEasting();
        double w1 = networkClippingMinNorthing();

        double v2 = networkClippingMaxEasting();
        double w2 = networkClippingMaxNorthing();

        // ... mapped onto two extreme picture coordinates ...

        Dimension prefSize = this.getPreferredSize();

        double x1 = 0;
        double y1 = (int) prefSize.getHeight();

        double x2 = (int) prefSize.getWidth();
        double y2 = 0;

        // ... yields a simple affine transformation without shearing:

        double m00 = (x1 - x2) / (v1 - v2);
        double m02 = x1 - m00 * v1;

        double m11 = (y1 - y2) / (w1 - w2);
        double m12 = y1 - m11 * w1;

        return new AffineTransform(m00, 0.0, 0.0, m11, m02, m12);
    }

    // --------------------
    public Point2D.Double getNetCoord (double x, double y) {
    	Point2D.Double result = new Point2D.Double();
        Dimension prefSize = getPreferredSize();
    	result.x = x /prefSize.width;
    	result.y = 1.- y /prefSize.height;
    	result.x *= (quad.getMaxEasting() - quad.getMinEasting());
    	result.y *= (quad.getMaxNorthing() - quad.getMinNorthing());
    	//result.x += network.minEasting();
    	//result.y += network.minNorthing();

    	return result;
    }
    // -------------------- PAINTING --------------------

    @Override
		public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        boolean useAntiAliasing = true;
        
		if (useAntiAliasing ) {
        	g2.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        } else {
        	g2.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF));
        }

		AffineTransform originalTransform = g2.getTransform();

		//OTFSwingDrawable.boxTransform = getBoxTransform();
		OTFSwingDrawable.g2d = g2;

		g2.setStroke(new BasicStroke(Math.round(0.05 * linkWidth)));
	
		AffineTransform linkTransform = new AffineTransform(originalTransform);
		linkTransform.concatenate(getBoxTransform());
		g2.setTransform(linkTransform);

		sceneGraph.draw();
		g2.setTransform(new AffineTransform());
		mouseMan.drawElements(g2);
		g2.setTransform(originalTransform);
   }

	public Component getComponent() {
		return networkScrollPane;
	}

	public OTFClientQuad getQuad() {
		return quad;
	}

	public void invalidate(int time) throws RemoteException {
		this.sceneGraph = quad.getSceneGraph(time, null, this);
		redraw();
	}

	public void redraw() {
		networkScrollPane.invalidate();
		networkScrollPane.repaint();
	}
	
	
	/***
	 * Drawer class for drawing simple quads
	 */
	public static class SimpleQuadDrawer extends OTFSwingDrawable implements OTFDataQuad.Receiver{
		protected final Point2D.Float[] quad = new Point2D.Float[4];
		protected float coloridx = 0;
		

		Point2D.Float calcOrtho(Point2D.Float start, Point2D.Float end, int nrLanes){
			double dx = end.y - start.y;
			double dy = end.x -start.x;
			double sqr1 = Math.sqrt(dx*dx +dy*dy);
			final double cellWidth_m = nrLanes*linkWidth;

			dx = dx*cellWidth_m/sqr1;
			dy = -dy*cellWidth_m/sqr1;

			return new Point2D.Float((float)dx,(float)dy);
		}

		public void setQuad(float startX, float startY, float endX, float endY) {
			setQuad(startX, startY,endX, endY, 1);
		}

		public void setQuad(float startX, float startY, float endX, float endY, int nrLanes) {
			this.quad[0] = new Point2D.Float(startX, startY);
			this.quad[1] = new Point2D.Float(endX, endY);
			final Point2D.Float ortho = calcOrtho(this.quad[0], this.quad[1], nrLanes);
			this.quad[2] = new Point2D.Float(startX + ortho.x, startY + ortho.y);
			this.quad[3] = new Point2D.Float(endX + ortho.x, endY + ortho.y);
		}

		public void setColor(float coloridx) {
			this.coloridx = coloridx;
		}

		@Override
		public void onDraw(Graphics2D display) {
			Polygon poly = new Polygon();

			poly.addPoint((int)(quad[0].x), (int)(quad[0].y));
			poly.addPoint((int)(quad[1].x), (int)(quad[1].y));
			poly.addPoint((int)(quad[3].x), (int)(quad[3].y));
			poly.addPoint((int)(quad[2].x), (int)(quad[2].y));
			display.setColor(netColor);
			display.fill(poly);
			//display.setColor(Color.BLUE);
			//display.draw(poly);
		}

		public void setId(char[] idBuffer) {
			// TODO Auto-generated method stub
			
		}
	}
	
	
	/***
	 * Drawer class for drawing agents 
	 */
	
	public static class AgentDrawer extends OTFSwingDrawable implements OTFDataSimpleAgent.Receiver{
		//Anything above 50km/h should be yellow!
		private final static ValueColorizer colorizer = new ValueColorizer(
				new double[] { 0.0, 30., 50.}, new Color[] {
						Color.RED, Color.YELLOW, Color.GREEN});

		protected char[] id; 
		protected float startX, startY, color;
		protected int state;

		public void setAgent(char[] id, float startX, float startY, int state, int user, float color) {
			this.id = id;
			this.startX = startX;
			this.startY = startY;
			this.color = color;
			this.state = state;
		}

		protected void setColor(Graphics2D display) {
			Color color = colorizer.getColor(0.1 + 0.9*this.color);
			if ((state & 1) != 0) {
				color = Color.lightGray;
			}
			display.setColor(color);

		}
		

		@Override
		public void onDraw(Graphics2D display) {
			Color color = colorizer.getColor(0.1 + 0.9*this.color);
			if ((state & 1) != 0) color = Color.lightGray;

			Point2D.Float pos = new Point2D.Float(startX, startY);
			// draw agent...
//			final int lane = (RANDOMIZE_LANES ? (agent.hashCode()
//			% lanes + 1) : agent.getLane());


			final double agentWidth = linkWidth *0.9;
			final double agentLength = agentWidth*0.9;
			final double offsetX = - 0.5 * agentLength;

			// there is only ONE displayvalue!
			if (state == 1 ) {
				display.setColor(Color.gray);
			} else {
				display.setColor(color);
			}

			display.fillOval((int)Math.round(pos.x + offsetX), (int)pos.y, (int)Math.round(agentLength), (int)Math.round(agentWidth));
		}

	}

	
	/***
	 * VizGuiHandler handles mouse input etc
	 */
	class VizGuiHandler extends MouseInputAdapter implements ChangeListener,MouseWheelListener {
		public Point start = null;

		public Rectangle currentRect = null;

		public int button = 0;

		public void drawElements(Graphics2D g2) {
			if (currentRect != null) {
				g2.setColor(Color.GREEN);
				g2.drawRect(currentRect.x,
						currentRect.y, currentRect.width, currentRect.height);
				}
				
			
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			button = e.getButton();
			start = new Point(x, y);
			// networkComponent.repaint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (button == 1)
				updateSize(e);
			else if (button == 2) {
				int deltax = start.x - e.getX();
				int deltay = start.y - e.getY();
				start.x = e.getX();
				start.y = e.getY();
				networkScrollPane.moveNetwork(deltax, deltay);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (button == 1) {
				updateSize(e);
				if ((currentRect.getHeight() > 10)
						&& (currentRect.getWidth() > 10)) {
					float scale =  networkScrollPane.getScale();
					/*scale = */networkScrollPane.scaleNetwork(currentRect,scale);
				} else {
					// try to find agent under mouse
					// calc mouse pos to component pos
			        //Rectangle rect = networkScrollPane.getViewport().getViewRect();
			    	//Point2D.Double p =  getNetCoord(e.getX() + rect.getX(), e.getY()+ + rect.getY());
//					String id = visnet.getAgentId(p);
//					Plan plan = null;
//					try {
//						plan = host.getAgentPlan(id);
//					} catch (Exception e1) {
//						// _TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//					if(plan != null) agentRenderer.setPlan(plan);

					networkScrollPane.invalidate();
					networkScrollPane.repaint();
				}
				currentRect = null;
			}
			button = 0;
		}

		void updateSize(MouseEvent e) {
			currentRect = new Rectangle(start);
			currentRect.add(e.getX(), e.getY());
			networkScrollPane.invalidate();
			networkScrollPane.repaint();
		}

		public void stateChanged(ChangeEvent e) {
			networkScrollPane.updateViewClipRect();
		}

		private void pressed_ZOOM_OUT() {
			float scale = networkScrollPane.getScale() / 1.42f;
			if (scale > 0.02) networkScrollPane.scaleNetwork(scale);
		}

		private void pressed_ZOOM_IN() {
			float scale = networkScrollPane.getScale() * 1.42f;
			if ( scale < 100) networkScrollPane.scaleNetwork(scale);
		}
		
		public void mouseWheelMoved(MouseWheelEvent e) {
			int i = e.getWheelRotation();
			if(i>0)pressed_ZOOM_OUT();
			else if ( i<0) pressed_ZOOM_IN();
		}

	}


	public void addQuery(OTFQuery query) {
		// TODO Auto-generated method stub
		
	}

	public void removeQueries() {
		// TODO Auto-generated method stub
		
	}
	
	public void clearCache() {
		if(quad != null) quad.clearCache();
	}

	public void handleClick(Double point, int mouseButton, MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleClick(Rectangle currentRect, int button) {
		// TODO Auto-generated method stub
		
	}

}