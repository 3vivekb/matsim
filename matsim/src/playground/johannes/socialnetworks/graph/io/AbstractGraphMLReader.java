/* *********************************************************************** *
 * project: org.matsim.*
 * GraphMLReader.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

/**
 * 
 */
package playground.johannes.socialnetworks.graph.io;

import gnu.trove.TIntObjectHashMap;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.matsim.core.utils.io.IOUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import playground.johannes.socialnetworks.graph.SparseEdge;
import playground.johannes.socialnetworks.graph.SparseGraph;
import playground.johannes.socialnetworks.graph.SparseVertex;

/**
 * Abstract class providing basic parsing functionality for graphs stored in the
 * GraphML file format (http://graphml.graphdrawing.org/). Subclasses must
 * implement the factory methods for creating new graphs, vertices and edges.
 * 
 * @author illenberger
 * 
 */
public abstract class AbstractGraphMLReader {
	
	private static final Logger logger = Logger.getLogger(AbstractGraphMLReader.class);
	
	private TIntObjectHashMap<SparseVertex> vertexMappings;
	
	protected SparseGraph graph;
	
	private int numVertex;
	
	private int numEdge;
	
	private boolean parseEdges;
	
	/**
	 * Creates a new graph out of the graph data stored in <tt>file</tt>.
	 * 
	 * @param file
	 *            the graph file (can be gzip-compressed).
	 * @return a new graph.
	 */
	public SparseGraph readGraph(String file) {
		XMLHandler handler = new XMLHandler();
		vertexMappings = new TIntObjectHashMap<SparseVertex>();
		numVertex = 0;
		numEdge = 0;

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(false);
		SAXParser parser;
		try {
			parser = factory.newSAXParser();

			XMLReader reader = parser.getXMLReader();
			reader.setContentHandler(handler);
			/*
			 * The GraphML specification does not required nodes and edges to be
			 * in a specific order. Thus we need to first parse the nodes and
			 * then the edges.
			 */
			parseEdges = false;
			reader.parse(new InputSource(IOUtils.getBufferedReader(file)));
			parseEdges = true;
			reader.parse(new InputSource(IOUtils.getBufferedReader(file)));
			
			printProgress();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return graph;
	}

	/**
	 * Creates a new empty graph with the attributes specified in <tt>attrs</tt>
	 * .
	 * 
	 * @param attrs
	 *            the graph's attributes.
	 * @return a new empty graph.
	 */
	protected abstract SparseGraph newGraph(Attributes attrs);

	/**
	 * Creates a new vertex with the attributes specified in <tt>attrs</tt> and
	 * inserts it into the graph.
	 * 
	 * @param attrs
	 *            the vertex's attributes.
	 * @return a new vertex.
	 */
	protected abstract SparseVertex addVertex(Attributes attrs);

	/**
	 * Creates a new edge with the attributes specified in <tt>attrs</tt> and
	 * inserts it into the graph between <tt>v1</tt> and <tt>v2</tt>.
	 * 
	 * @param v1
	 *            one of the two vertices the edge is connected to.
	 * @param v2
	 *            one of the two vertices the edge is connected to.
	 * @param attrs
	 *            the edge's attributes.
	 * @return a new edge.
	 */
	protected abstract SparseEdge addEdge(SparseVertex v1, SparseVertex v2,
			Attributes attrs);
	
	
	private SparseVertex newNode(Attributes attrs) {
		SparseVertex v = addVertex(attrs);
		if(v == null)
			throw new IllegalArgumentException("A vertex must not be null!");
		
		numVertex++;
		int id = Integer.parseInt(attrs.getValue(GraphML.ID_TAG));
		vertexMappings.put(id, v);
		
		if(numVertex % 100000 == 0)
			printProgress();
		
		return v;
	}
	
	private SparseEdge newEdge(Attributes attrs) {
		int id1 = Integer.parseInt(attrs.getValue(GraphML.SOURCE_TAG));
		int id2 = Integer.parseInt(attrs.getValue(GraphML.TARGET_TAG));
		SparseVertex v1 = vertexMappings.get(id1);
		SparseVertex v2 = vertexMappings.get(id2);
		if(v1 != null && v2 != null) {
			numEdge++;
			if(numEdge % 100000 == 0)
				printProgress();
			return addEdge(v1, v2, attrs);
		} else {
			throw new IllegalArgumentException("A vertex must not be null!");
		}
	}
	
	private void printProgress() {
		logger.info(String.format("Loading graph... %1$s vertices, %2$s edges.", numVertex, numEdge));
	}
	
	private class XMLHandler extends DefaultHandler {

		@Override
		public void startElement(String uri, String localName, String name,
				Attributes atts) throws SAXException {
			if (GraphML.NODE_TAG.equalsIgnoreCase(name) && parseEdges == false) {
				newNode(atts);
			} else if (GraphML.EDGE_TAG.equalsIgnoreCase(name)
					&& parseEdges == true) {
				newEdge(atts);
			} else if (GraphML.GRAPH_TAG.equalsIgnoreCase(name)
					&& parseEdges == false) {
				if (graph != null)
					throw new UnsupportedOperationException(
							"For the time being this reader allows only one graph per file!");

				graph = newGraph(atts);
			}
		}
	}
}
