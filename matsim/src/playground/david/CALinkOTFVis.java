package playground.david;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.nio.ByteBuffer;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JSplitPane;

import org.matsim.api.basic.v01.network.BasicNode;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.mobsim.queuesim.QueueLink;
import org.matsim.core.mobsim.queuesim.QueueNetwork;
import org.matsim.core.mobsim.queuesim.QueueNode;
import org.matsim.core.mobsim.queuesim.VisData;
import org.matsim.core.mobsim.queuesim.QueueLane.AgentOnLink;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.utils.collections.QuadTree.Rect;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.vis.otfvis.data.OTFClientQuad;
import org.matsim.vis.otfvis.data.OTFConnectionManager;
import org.matsim.vis.otfvis.data.OTFServerQuad;
import org.matsim.vis.otfvis.executables.OTFVisController;
import org.matsim.vis.otfvis.gui.OTFHostControlBar;
import org.matsim.vis.otfvis.gui.OTFVisConfig;
import org.matsim.vis.otfvis.gui.PreferencesDialog;
import org.matsim.vis.otfvis.handler.OTFLinkAgentsHandler;
import org.matsim.vis.otfvis.interfaces.OTFDrawer;
import org.matsim.vis.otfvis.interfaces.OTFLiveServerRemote;
import org.matsim.vis.otfvis.interfaces.OTFQuery;
import org.matsim.vis.otfvis.opengl.drawer.OTFOGLDrawer;
import org.matsim.vis.otfvis.opengl.layer.OGLAgentPointLayer;
import org.matsim.vis.otfvis.opengl.layer.SimpleStaticNetLayer;
import org.matsim.vis.otfvis.opengl.layer.OGLAgentPointLayer.AgentPointDrawer;
import org.matsim.vis.snapshots.writers.PositionInfo;
import org.matsim.vis.snapshots.writers.PositionInfo.VehicleState;


class MyCAVeh {
	int speed ;
	int maxSpeed ;
	boolean truck=false ;

	public boolean isTruck() {
		return truck;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public MyCAVeh ( int maxSpeed ) {
		this.maxSpeed = maxSpeed ; 
	}

	public void setTruck(boolean truck) {
		this.truck = truck;
	}
}

class CALink extends QueueLink {
	public static boolean ueberholverbot = false;

	public static int LINKLEN = 1000;
	public static int LANECOUNT = 2;

	private final VisData visdata = this.new VisDataImpl();
	
	int VMAX=5 ;

	MyCAVeh[][] cells = new MyCAVeh[LANECOUNT][LINKLEN] ; 

	public CALink(LinkImpl l, QueueNetwork queueNetwork, QueueNode toNode) {
		super(l, queueNetwork, toNode);
		MyCAVeh veh = new MyCAVeh(3) ;
		veh.setTruck(true) ;
		cells[0][0] = veh ;
		for ( int lane=0 ; lane<LANECOUNT; lane++ ) {
			for ( int len=1 ; len<LINKLEN ; len++ ) {
				if ( Math.random() < 0.1 ) {
					if ( Math.random() < 0.2 ) {
						veh = new MyCAVeh(3) ;
						veh.setTruck(true) ;
						cells[lane][len] = veh ;
					} else {
						if ( Math.random() < 0.5 ) {
							cells[lane][len] = new MyCAVeh(5) ;
						} else {
							cells[lane][len] = new MyCAVeh(4) ;
						}
					}
				} else {
					cells[lane][len] = null ;
				}
			}
		}
	}

	class VisDataImpl implements VisData {

		public Collection<PositionInfo> getVehiclePositions(Collection<PositionInfo> positions) {
			for ( int lane=0 ; lane<LANECOUNT; lane++ ) {
				for ( int len=0 ; len<LINKLEN ; len++ ) {
					if ( cells[lane][len] != null ) {
						MyCAVeh veh = cells[lane][len] ;
						double speed = 4.*veh.getSpeed() ;
						if ( veh.isTruck() ) {
							positions.add( new PositionInfo(new IdImpl("1"), getLink(), len, 5*lane+2, 500, VehicleState.Driving, ""));
						} else {
							positions.add( new PositionInfo(new IdImpl("0"), getLink(), len, 5*lane+2, speed, VehicleState.Driving, ""));
						}
					}
				}
			}
			return positions;
		}

		public double getDisplayableSpaceCapValue() {
			throw new UnsupportedOperationException("Method not implemented in draft class");		}

		public double getDisplayableTimeCapValue() {
			return 1;
		}

		public Collection<AgentOnLink> getDrawableCollection() {
			throw new UnsupportedOperationException("Method not implemented in draft class");
		}

		public void getVehiclePositionsEquil(Collection<PositionInfo> positions) {
			throw new UnsupportedOperationException("Method not implemented in draft class");
		}

		public void getVehiclePositionsQueue(Collection<PositionInfo> positions) {
			throw new UnsupportedOperationException("Method not implemented in draft class");			
		}
		
	}
	
	
	@Override
	public VisData getVisData() {
		return this.visdata;
	}

	int getGap ( int lane, int len, int speed ) {
		int gap = 0 ;
		while ( gap < speed ) {
			if ( cells[lane][(len+gap+1)%LINKLEN] != null ) {
				break ;
			}
			gap++ ;
		}
		return gap ;
	}

	/* (non-Javadoc)
	 * @see org.matsim.mobsim.QueueLink#moveLink(double)
	 */
	@Override
	protected boolean moveLink(double now) {
		// #############################
		// lane changing:
		
		final int MODE = 1 ;
		if ( LANECOUNT >= 2 ) {
			int mainLane = 0 ;
			int otherLane = 1 ;
			if ( now%2==1 ) {
				mainLane = 1 ;
				otherLane = 0 ;
			}

			for ( int len=0 ; len<LINKLEN ; len++ ) {
				if ( cells[mainLane][len] != null ) {
					MyCAVeh veh = cells[mainLane][len] ;

					// incentive:
					int otherGap = getGap( otherLane, len, veh.getSpeed()+1 ) ;
					if ( MODE==0 ) { // symmetric
						int thisGap = getGap( mainLane, len, veh.getSpeed()+1 ) ;
						if ( thisGap >= otherGap ) {
							continue ; // next cell
						}
					} else if ( MODE==1 ) {
						int rightGap = getGap( 1, len, veh.getSpeed()+1 ) ;
						int leftGap = getGap( 0, len, veh.getSpeed()+1 ) ;
						if ( (rightGap > leftGap) && (mainLane == 1) ) {
							continue ;
						} else if ( Math.random() < 0.5 ) {
							continue ;
						} else if ( ueberholverbot && (mainLane!=0) && veh.isTruck() ) {
							continue ;
						}
					} else {
						System.err.println( " MODE not defined. Abort " ) ;
						System.exit(-1) ;
					}
					// safety
					int backGap = 0 ;
					while ( backGap < VMAX ) {
						if ( cells[otherLane][(len-backGap+LINKLEN)%LINKLEN] != null ) {
							break ;
						}
						backGap++ ;
					}
					if ( backGap < VMAX ) {
						continue ;
					}

					cells[otherLane][len] = veh ;
					cells[mainLane][len] = null ;

				}
			}
		}

		// #############################
		// #############################
		// speed:

		for ( int lane=0 ; lane<LANECOUNT; lane++ ) {
			for ( int len=0 ; len<LINKLEN ; len++ ) {
				if ( cells[lane][len] != null ) {
					MyCAVeh veh = cells[lane][len] ;
					int origSpeed = veh.getSpeed() ;
					int speed = origSpeed+1 ;
					int gap = getGap( lane, len, speed ) ;
					if ( speed > veh.getMaxSpeed() ) { speed = veh.getMaxSpeed() ; }
					if ( speed > gap ) { speed = gap ; }

					if ( origSpeed==0 ) {
						if ( (speed >= 1) && (Math.random() < 0.5) ) { speed -- ; }
					} else {
						if ( (speed >= 1) && (Math.random() < 0.05*lane) ) { speed -- ; }
					}
					veh.setSpeed(speed) ;
				}
			}
		}		
		
		// #############################
		// #############################
		// forward movement:

		for ( int lane=0 ; lane<LANECOUNT; lane++ ) {
			for ( int len=0 ; len<LINKLEN ; len++ ) {
				if ( cells[lane][len] != null ) {
					MyCAVeh veh = cells[lane][len] ;
					if ( veh.getSpeed() > 0 ) {
						cells[lane][(len+veh.getSpeed())%LINKLEN] = veh ;
						cells[lane][len] = null ;
						len+=veh.getSpeed() ;
					}
				}
			}
		}
		
		// #############################
		// #############################
		// tty:

//		for ( int lane=0 ; lane<LANECOUNT; lane++ ) {
//			StringBuilder str = new StringBuilder() ;
//			for ( int len=0 ; len<LINKLEN ; len++ ) {
//				if ( cells[lane][len] != null ) {
//					MyCAVeh veh = cells[lane][len] ;
//					if ( veh.isTruck() ) {
//						str.append('X') ;
//					} else {
//						str.append(veh.getSpeed()) ;
//					}
//				} else {
//					str.append('.') ;
//				}
//			}
//			System.out.println ( str ) ;
//		}
//		System.out.println() ;

		return true;
	}

};

// (end of class)
//###########################################
//###########################################

class OTFServerQUADCA extends OTFServerQuad {


	private static final long serialVersionUID = 1L;

	public CALink link;

	public OTFServerQUADCA(double minX, double minY, double maxX, double maxY) {
		super(minX, minY, maxX, maxY);
		NetworkLayer net = new NetworkLayer();
		BasicNode node1 = net.createAndAddNode(new IdImpl("0"), new CoordImpl(0, 500));
		BasicNode node2 = net.createAndAddNode(new IdImpl("1"), new CoordImpl(CALink.LINKLEN, 500));

		LinkImpl lk = new LinkImpl(new IdImpl("0"), node1, node2, net, CALink.LINKLEN,0,0, CALink.LANECOUNT);
		link = new CALink(lk, null, null);
	}

	@Override
	public void fillQuadTree(OTFConnectionManager connect) {
		OTFLinkAgentsHandler.Writer writer = new OTFLinkAgentsHandler.Writer();
		writer.setSrc(link);
		put(CALink.LINKLEN/2, 500, writer);
	}


}

class CALiveServer implements OTFLiveServerRemote{
	private transient final ByteBuffer buf = ByteBuffer.allocate(20000000);
	OTFServerQUADCA quad;

	int time = 0;

	public int getLocalTime() throws RemoteException {
		return time;
	}

	public OTFServerQuad getQuad(String id, OTFConnectionManager connect) throws RemoteException {
		quad = new OTFServerQUADCA(0,0,CALink.LINKLEN , 1000);
		quad.fillQuadTree(connect);
		return quad;
	}

	public  byte[] getQuadStateBuffer(boolean isConst) {
		buf.position(0);
		if(isConst) quad.writeConstData(buf); 
		else quad.writeDynData(null, buf);

		byte [] result;
		synchronized (buf) {
			result = buf.array();
		}
		return result;
	}

	public byte[] getQuadConstStateBuffer(String id) throws RemoteException {
		return getQuadStateBuffer(true);
	}	

	public byte[] getQuadDynStateBuffer(String id, Rect bounds) throws RemoteException {
		return getQuadStateBuffer(false);
	}

	public boolean isLive() throws RemoteException {
		return true;
	}

	public Collection<Double> getTimeSteps() throws RemoteException {
		return null;
	}

	public boolean requestNewTime(int time, TimePreference searchDirection) throws RemoteException {
		if(time > this.time) this.time++;
		quad.link.moveLink(this.time);
		return true;
	}

	// not needed
	public void pause() throws RemoteException {
	}

	public void play() throws RemoteException {
	}

	public OTFQuery answerQuery(OTFQuery query) throws RemoteException {
		return null;
	}

	public boolean replace(String id, double x, double y, int index, Class clazz)
			throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public int getControllerStatus() throws RemoteException {
		return OTFVisController.NOCONTROL;
	}

	public boolean requestControllerStatus(int status) throws RemoteException {
		return false;
	}
}

public class CALinkOTFVis extends Thread { 

	public static class  MyControlBar extends OTFHostControlBar {
		JButton verbot;
		int delay = 20;

		public MyControlBar(String address) throws RemoteException, InterruptedException, NotBoundException {
			super(address);
			OTFVisConfig config = (OTFVisConfig)Gbl.getConfig().getModule(OTFVisConfig.GROUP_NAME);
			if(config != null) config.setDelay_ms(20);

			verbot = createButton("�-Verbot ist AUS", "vb", null, "toggle �berholverbot");
			verbot.putClientProperty("JButton.buttonType","text");
			verbot.setBorderPainted(true);
			verbot.setMargin(new Insets(10, 10, 10, 10));
			add(verbot);

		}

		@Override
		protected boolean onAction(String command) {
			if(command.equals("vb")) {
				CALink.ueberholverbot = !CALink.ueberholverbot;
				verbot.setText(CALink.ueberholverbot ? "�-Verbot ist AN" :"�-Verbot ist AUS");
			}
			return false; // return id command was handeled
		}


		/* (non-Javadoc)
		 * @see org.matsim.utils.vis.otfvis.gui.OTFHostControlBar#invalidateHandlers()
		 */
		@Override
		public void invalidateHandlers() {
			// TODO Auto-generated method stub
			super.invalidateHandlers();
			try {
				sleep(delay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		@Override
		protected void openAddress(String address) throws RemoteException, InterruptedException, NotBoundException {
			this.host = new CALiveServer();
			if (host != null && host.isLive()) liveHost = (OTFLiveServerRemote)host;
		}

		private static final long serialVersionUID = 1L;

	}	

	private static OTFOGLDrawer.FastColorizer colorizer = new OTFOGLDrawer.FastColorizer(
			new double[] { 0.0, 30., 50., 500}, new Color[] {
					Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE});
	


	@Override
	public void run() {
		if (Gbl.getConfig() == null) Gbl.createConfig(null);

		OTFVisConfig visconf = (OTFVisConfig) Gbl.getConfig().getModule(OTFVisConfig.GROUP_NAME);
		if (visconf == null) {
			visconf = new OTFVisConfig();
			Gbl.getConfig().addModule(OTFVisConfig.GROUP_NAME, visconf);
		}

		((OTFVisConfig)Gbl.getConfig().getModule(OTFVisConfig.GROUP_NAME)).setAgentSize(100.f);
		((OTFVisConfig)Gbl.getConfig().getModule(OTFVisConfig.GROUP_NAME)).setLinkWidth(17.5f*CALink.LANECOUNT +5);
//		((OTFVisConfig)Gbl.getConfig().getModule("otfvis")).setNetworkColor(new Color(0xeeeeee));
		OGLAgentPointLayer.AgentArrayDrawer.setAlpha(255);
		OGLAgentPointLayer.AgentArrayDrawer.setColorizer(colorizer);

		MyControlBar hostControl;
		try {
			hostControl = new MyControlBar("");
			JFrame frame = new JFrame("MATSim OTFVis");

			boolean isMac = System.getProperty("os.name").toLowerCase().startsWith("mac os x");
			if (isMac) {
				frame.getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);
			}

			hostControl.frame = frame;

			frame.getContentPane().add(hostControl, BorderLayout.NORTH);
			JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			pane.setContinuousLayout(true);
			pane.setOneTouchExpandable(true);
			frame.getContentPane().add(pane);
			PreferencesDialog.buildMenu(frame, visconf, hostControl, null);

			OTFConnectionManager connectR = new OTFConnectionManager();

			connectR.add(OTFLinkAgentsHandler.Writer.class,  OTFLinkAgentsHandler.class);
			connectR.add(OTFLinkAgentsHandler.class,  SimpleStaticNetLayer.SimpleQuadDrawer.class);
			connectR.add(SimpleStaticNetLayer.SimpleQuadDrawer.class, SimpleStaticNetLayer.class);
			connectR.add(OTFLinkAgentsHandler.class,  AgentPointDrawer.class);
			connectR.add(OGLAgentPointLayer.AgentPointDrawer.class, OGLAgentPointLayer.class);


			OTFClientQuad clientQ2 = hostControl.createNewView(null,connectR);
			visconf.setCachingAllowed(false);
			
			OTFDrawer drawer2 = new OTFOGLDrawer(frame, clientQ2);
			pane.setRightComponent(drawer2.getComponent());
			hostControl.addHandler("test2", drawer2);
			drawer2.invalidate(0);

			hostControl.finishedInitialisition();
			
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setSize(screenSize.width/2,screenSize.height/2);
			frame.setVisible(true);


		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public CALinkOTFVis() {
		boolean isMac = System.getProperty("os.name").toLowerCase().startsWith("mac os x");
		if (isMac) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
		}
	}

	public static void main(String[] args) {
		CALinkOTFVis me = new CALinkOTFVis();
		me.run();
	}

}
