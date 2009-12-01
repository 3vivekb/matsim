package playground.david.otfivs.executables;

import java.rmi.RemoteException;

import javax.swing.JFrame;

import org.matsim.vis.otfvis.data.OTFClientQuad;
import org.matsim.vis.otfvis.data.OTFConnectionManager;
import org.matsim.vis.otfvis.gui.NetJComponent;
import org.matsim.vis.otfvis.handler.OTFDefaultNodeHandler;
import org.matsim.vis.otfvis.handler.OTFLinkAgentsHandler;
import org.matsim.vis.otfvis.handler.OTFLinkAgentsNoParkingHandler;
import org.matsim.vis.otfvis.interfaces.OTFDrawer;
import org.matsim.vis.otfvis.opengl.OTFClientFile;
import org.matsim.vis.otfvis.opengl.drawer.OTFOGLDrawer;
import org.matsim.vis.otfvis.opengl.drawer.SimpleBackgroundDrawer;
import org.matsim.vis.otfvis.opengl.layer.OGLAgentPointLayer;
import org.matsim.vis.otfvis.opengl.layer.OGLSimpleBackgroundLayer;
import org.matsim.vis.otfvis.opengl.layer.SimpleStaticNetLayer;
import org.matsim.vis.otfvis.opengl.layer.OGLAgentPointLayer.AgentPointDrawer;



public class OnTheFlyClientQuadSwing extends OTFClientFile{
	public OnTheFlyClientQuadSwing(String filename2, OTFConnectionManager connect, boolean split) {
		super(filename2, connect, split);
		// TODO Auto-generated constructor stub
	}

	static OTFConnectionManager connect1 = new OTFConnectionManager();
	static OTFConnectionManager connect2 = new OTFConnectionManager();

	@Override
	public OTFDrawer getLeftDrawerComponent(JFrame frame) throws RemoteException {
		OTFClientQuad clientQ = hostControl.createNewView(null, connect1);

		OTFDrawer drawer = new OTFOGLDrawer(frame, clientQ);
		return drawer;
	}

	@Override
	public OTFDrawer getRightDrawerComponent(JFrame frame) throws RemoteException {
		OTFClientQuad clientQ2 = hostControl.createNewView(null, connect2);

		OTFDrawer drawer2 = new NetJComponent(frame, clientQ2);

		return drawer2;
	}
	
	public static void main(String[] args) {
		String filename = "test/OTFQuadfileNoParking10p_wip.mvi";

		connect1.add(OTFDefaultNodeHandler.Writer.class, OTFDefaultNodeHandler.class);
		connect1.add(SimpleBackgroundDrawer.class, OGLSimpleBackgroundLayer.class);
		connect1.add(OTFLinkAgentsNoParkingHandler.Writer.class, OTFLinkAgentsHandler.class);
		connect1.add(OTFLinkAgentsHandler.class,  SimpleStaticNetLayer.SimpleQuadDrawer.class);
		connect1.add(SimpleStaticNetLayer.SimpleQuadDrawer.class, SimpleStaticNetLayer.class);
		connect1.add(OTFLinkAgentsHandler.class,  AgentPointDrawer.class);
		connect1.add(OGLAgentPointLayer.AgentPointDrawer.class, OGLAgentPointLayer.class);

		connect2.add(OTFDefaultNodeHandler.Writer.class, OTFDefaultNodeHandler.class);
		connect2.add(SimpleBackgroundDrawer.class, OGLSimpleBackgroundLayer.class);
		connect2.add(OTFLinkAgentsNoParkingHandler.Writer.class, OTFLinkAgentsHandler.class);
		connect2.add(OTFLinkAgentsHandler.class,  NetJComponent.SimpleQuadDrawer.class);
		connect2.add(OTFLinkAgentsHandler.class,  NetJComponent.AgentDrawer.class);
	
		//main2(args);
		
		OTFClientFile client = new OnTheFlyClientQuadSwing(filename, null, true);
		client.run();
	}

 
}
