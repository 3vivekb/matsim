package playground.mmoyo.input;

import playground.mmoyo.PTRouter.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.utils.io.MatsimXmlParser;

/** 
 * Parses the xml file of PTLines
 *
 */
public class PTLinesReader2 extends MatsimXmlParser {
	private final static String PTLINES = "ptLines";
	private final static String LINE = "ptLine";
	private final static String NODE = "node";
	private final static String DEPARTURES = "departures";
	private IdImpl idPTLine;
	private char type;
	private String direction;
	private List<String> route;
	private List<Double> minutes;
	private List<String> departureList;
	public List<PTLine> ptLineList = new ArrayList<PTLine>();
	
	public PTLinesReader2() {
		super();
	}

	public void readFile(final String filename) {
		try {
			parse(filename);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void startTag(final String name, final Attributes atts, final Stack<String> context) {
		if (LINE.equals(name)) {
			startLine(atts);
		} else if (NODE.equals(name)){
			startNode(atts);
		} else if (DEPARTURES.equals(name)){
			startDepartures();
		} 
	}

	@Override
	public void endTag(final String name, final String content,	final Stack<String> context) {
		if (PTLINES.equals(name)) {
			endLines();
		} else if (LINE.equals(name)) {
			endLine();
		} else if (NODE.equals(name)) {
			endNode(content);
		} else if (DEPARTURES.equals(name)){
			endDepartures(content);
		}
	}

	private void startLine(final Attributes atts) {
		idPTLine = new IdImpl(atts.getValue("id"));
		type = atts.getValue("id").charAt(0);
		direction =atts.getValue("direction");
		minutes= new ArrayList<Double>();
		route = new ArrayList<String>();
		departureList = new ArrayList <String>(); 
	}
	
	private void startNode(final Attributes atts) {
		minutes.add(Double.parseDouble(atts.getValue("minute")));
	}
	
	private void startDepartures(){
	}
	
	private void endDepartures(String departures) {
		departureList = Arrays.asList(departures.split("[ \t\n]+"));
	}
	
	private void endNode(String idNode) {
		route.add(idNode);
	}
	
	private void endLine() {	
		this.ptLineList.add(new PTLine(idPTLine, type, direction, route, minutes, departureList));
	}

	private void endLines(){
		idPTLine = null;
		type = '\u0000'; //null
		direction =null;
		minutes= null;
		route = null;
		departureList = null;
	}

}// class
