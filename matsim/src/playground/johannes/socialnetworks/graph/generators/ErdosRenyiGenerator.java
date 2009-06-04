/* *********************************************************************** *
 * project: org.matsim.*
 * AbstractErdosRenyiGenerator.java
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

/**
 * 
 */
package playground.johannes.socialnetworks.graph.generators;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

import playground.johannes.socialnetworks.graph.Edge;
import playground.johannes.socialnetworks.graph.Graph;
import playground.johannes.socialnetworks.graph.GraphFactory;
import playground.johannes.socialnetworks.graph.Partitions;
import playground.johannes.socialnetworks.graph.SparseEdge;
import playground.johannes.socialnetworks.graph.SparseGraph;
import playground.johannes.socialnetworks.graph.SparseGraphFactory;
import playground.johannes.socialnetworks.graph.SparseVertex;
import playground.johannes.socialnetworks.graph.Vertex;
import playground.johannes.socialnetworks.graph.io.GraphMLWriter;

/**
 * @author illenberger
 *
 */
public class ErdosRenyiGenerator<G extends Graph, V extends Vertex, E extends Edge> {

	private GraphFactory<G, V, E> factory;
	
	public ErdosRenyiGenerator(GraphFactory<G, V, E> factory) {
		this.factory = factory;
	}

	public G generate(int numVertices, double p, long randomSeed) {
		G g = factory.createGraph();
		LinkedList<V> pending = new LinkedList<V>();
		for (int i = 0; i < numVertices; i++)
			pending.add(factory.addVertex(g));

		Random random = new Random(randomSeed);
		V v1;
		while ((v1 = pending.poll()) != null) {
			for (V v2 : pending) {
				if (random.nextDouble() <= p) {
					factory.addEdge(g, v1, v2);
				}
			}
		}

		return g;
	}
	
	public static void main(String args[]) throws FileNotFoundException, IOException {
		int N = Integer.parseInt(args[1]);
		double p = Double.parseDouble(args[2]);
		long seed = (long)(Math.random() * 1000);
		if(args.length > 3)
			seed = Long.parseLong(args[3]);
		
		ErdosRenyiGenerator<SparseGraph, SparseVertex, SparseEdge> generator = new ErdosRenyiGenerator<SparseGraph, SparseVertex, SparseEdge>(new SparseGraphFactory());
		Graph g = generator.generate(N, p, seed);
		
		for(String arg : args) {
			if(arg.equalsIgnoreCase("-e")) {
				g = Partitions.subGraphs(g).first();
				break;
			}
		}
		
		GraphMLWriter writer = new GraphMLWriter();
		writer.write(g, args[0]);
	}
}
