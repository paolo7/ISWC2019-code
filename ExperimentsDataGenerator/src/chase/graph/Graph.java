package chase.graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

import gqr.JoinDescription;

/**
 * A directed graph data structure.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class Graph
{
	/** Color used to mark unvisited nodes */
	public static final int VISIT_COLOR_WHITE = 1;
	/** Color used to mark nodes as they are first visited in DFS order */
	public static final int VISIT_COLOR_GREY = 2;
	/** Color used to mark nodes after descendants are completely visited */
	public static final int VISIT_COLOR_BLACK = 3;
	/** Vector<Vertex> of graph verticies */
	private HashSet<Vertex> vertices;
	/** Vector<Edge> of edges in the graph */
	private ArrayList<Edge> edges;
	/** The vertex identified as the root of the graph */
	private Vertex rootVertex;

	/**
	 * Construct a new graph without any vertices or edges
	 */
	public Graph()
	{
		vertices = new HashSet<Vertex>();
		edges = new ArrayList<Edge>();
	}

	/**
	 * Are there any verticies in the graph
	 * @return true if there are no verticies in the graph
	 */ 
	public boolean isEmpty()
	{
		return vertices.size() == 0;
	}

	/**
	 * Add a vertex to the graph
	 * @param v the Vertex to add
	 * @return the vertex that was added, or the one already in the graph.
	 */ 
	public Vertex addVertex(Vertex v)
	{
//		boolean added = false;
		if( vertices.contains(v) == false )
		{
			vertices.add(v);
			return v;
		}
		else
			for(Vertex vx:vertices)
				if(vx.equals(v))
					return vx;
		return v;
	}

	/**
	 * Get the vertex count.
	 * @return the number of verticies in the graph.
	 */ 
	public int size()
	{
		return vertices.size();
	}

	/**
	 * Get the root vertex
	 * @return the root vertex if one is set, null if no vertex has been set as the root.
	 */
	public Vertex getRootVertex()
	{
		return rootVertex;
	}
	/**
	 * Set a root vertex. If root does no exist in the graph it is added.
	 * @param root - the vertex to set as the root and optionally add if it
	 *    does not exist in the graph.
	 */
	public void setRootVertex(Vertex root)
	{
		this.rootVertex = root;
		if( vertices.contains(root) == false )
			this.addVertex(root);
	}

	/**
	 * Get the given Vertex.
	 * @param n the index [0, size()-1] of the Vertex to access
	 * @return the nth Vertex
	 */ 
	public boolean contains(Vertex node)
	{
		return vertices.contains(node);

	}

	/**
	 * Get the graph verticies
	 * 
	 * @return the graph verticies
	 */
	public Set<Vertex> getVertices()
	{
		return this.vertices;
	}

	/**
	 * Insert a directed, weighted Edge into the graph.
	 * 
	 * @param from - the Edge starting vertex
	 * @param to - the Edge ending vertex
	 * @param cost - the Edge weight/cost
	 * @return true if the Edge was added, false if from already has this Edge
	 * @throws IllegalArgumentException if from/to are not verticies in
	 * the graph
	 */ 
	public boolean addEdge(Vertex from, Vertex to, int cost)
			throws IllegalArgumentException
			{
		if( vertices.contains(from) == false )
			throw new IllegalArgumentException("from is not in graph");
		if( vertices.contains(to) == false )
			throw new IllegalArgumentException("to is not in graph");
		Edge e = new Edge(from, to, cost);
		Edge existing = from.findEdge(to);
		if (existing != null && existing.getCost()==cost)
			return false;
		else
		{
			from.addEdge(e);
			to.addEdge(e);
			edges.add(e);
			return true;
		}
			}

	/**
	 * Insert a bidirectional Edge in the graph
	 * 
	 * @param from - the Edge starting vertex
	 * @param to - the Edge ending vertex
	 * @param cost - the Edge weight/cost
	 * @return true if edges between both nodes were added, false otherwise
	 * @throws IllegalArgumentException if from/to are not verticies in
	 * the graph
	 */ 
	public boolean insertBiEdge(Vertex from, Vertex to, int cost)
			throws IllegalArgumentException
			{
		return addEdge(from, to, cost) && addEdge(to, from, cost);
			}

	/**
	 * Get the graph edges
	 * @return the graph edges
	 */
	public List<Edge> getEdges()
	{
		return this.edges;
	}

	/**
	 * Remove a vertex from the graph
	 * @param v the Vertex to remove
	 * @return true if the Vertex was removed
	 */ 
	public boolean removeVertex(Vertex v)
	{
		if (!vertices.contains(v))
			return false;

		vertices.remove(v);
		if( v == rootVertex )
			rootVertex = null;

		// Remove the edges associated with v
		for(int n = 0; n < v.getOutgoingEdgeCount(); n ++)
		{
			Edge e = v.getOutgoingEdge(n);
			v.remove(e);
			Vertex to = e.getTo();
			to.remove(e);
			edges.remove(e);
		}
		for(int n = 0; n < v.getIncomingEdgeCount(); n ++)
		{
			Edge e = v.getIncomingEdge(n);
			v.remove(e);
			Vertex predecessor = e.getFrom();
			predecessor.remove(e);
		}
		return true;
	}

	/**
	 * Remove an Edge from the graph
	 * @param from - the Edge starting vertex
	 * @param to - the Edge ending vertex
	 * @return true if the Edge exists, false otherwise
	 */ 
	public boolean removeEdge(Vertex from, Vertex to)
	{
		Edge e = from.findEdge(to);
		if (e == null)
			return false;
		else
		{
			from.remove(e);
			to.remove(e);
			edges.remove(e);
			return true;
		}
	}

	/**
	 * Clear the mark state of all verticies in the graph by calling
	 * clearMark() on all verticies.
	 * @see Vertex#clearMark()
	 */ 
	public void clearMark()
	{
		for (Vertex w:vertices)//int i = 0; i < vertices.size(); i++/
		{
			//         Vertex w = vertices.get(i);
			w.clearMark();
		}
	}

	/**
	 * Clear the mark state of all edges in the graph by calling
	 * clearMark() on all edges.
	 * @see Edge#clearMark()
	 */ 
	public void clearEdges()
	{
		for (int i = 0; i < edges.size(); i++)
		{
			Edge e = (Edge) edges.get(i);
			e.clearMark();
		}
	}

	/**
	 * Perform a depth first serach using recursion.
	 * @param v - the Vertex to start the search from
	 * @param visitor - the vistor to inform prior to 
	 * @see Visitor#visit(Graph, Vertex) 
	 */ 
	public void depthFirstSearch(Vertex v, final Visitor visitor)
	{
		VisitorEX wrapper = new VisitorEX()
		{
			public void visit(Graph g, Vertex v) throws RuntimeException
			{
				visitor.visit(g, v);
			}
		};
		this.depthFirstSearch(v, wrapper);
	}
	/**
	 * Perform a depth first serach using recursion. The search may
	 * be cut short if the visitor throws an exception.
	 * 
	 * @param v - the Vertex to start the search from
	 * @param visitor - the vistor to inform prior to 
	 * @see Visitor#visit(Graph, Vertex)
	 * @xception if visitor.visit throws an exception 
	 */ 
	public  void depthFirstSearch(Vertex v, VisitorEX visitor)

	{
		if( visitor != null )
			visitor.visit(this, v);      
		v.visit();
		for (int i = 0; i < v.getOutgoingEdgeCount(); i++)
		{
			Edge e = v.getOutgoingEdge(i);
			if (!e.getTo().visited())
			{
				depthFirstSearch(e.getTo(), visitor);
			}
		}
	}

	/**
	 * Perform a breadth first search of this graph, starting at v.
	 * 
	 * @param v - the search starting point
	 * @param visitor - the vistor whose vist method is called prior
	 * to visting a vertex.
	 */
	public void breadthFirstSearch(Vertex v, final Visitor visitor)
	{
		VisitorEX wrapper = new VisitorEX()
		{
			public void visit(Graph g, Vertex v) throws RuntimeException
			{
				visitor.visit(g, v);
			}
		};
		this.breadthFirstSearch(v, wrapper);
	}
	/**
	 * Perform a breadth first search of this graph, starting at v. The
	 * vist may be cut short if visitor throws an exception during
	 * a vist callback.
	 * 
	 * @param v - the search starting point
	 * @param visitor - the vistor whose vist method is called prior
	 * to visting a vertex.
	 * @xception if vistor.visit throws an exception
	 */
	public void breadthFirstSearch(Vertex v, VisitorEX visitor)
	{
		LinkedList<Vertex> q = new LinkedList<Vertex>();

		q.add(v);
		if( visitor != null )
			visitor.visit(this, v);
		v.visit();
		while (q.isEmpty() == false)
		{
			v = q.removeFirst();
			for (int i = 0; i < v.getOutgoingEdgeCount(); i++)
			{
				Edge e = v.getOutgoingEdge(i);
				if (!e.getTo().visited())
				{
					q.add(e.getTo());
					if( visitor != null )
						visitor.visit(this, e.getTo());
					e.getTo().visit();
				}
			}
		}
	}

	/**
	 * Find the spanning tree using a DFS starting from v.
	 * @param v - the vertex to start the search from
	 * @param visitor - visitor invoked after each vertex
	 * is visited and an edge is added to the tree.
	 */
	public void dfsSpanningTree(Vertex v, DFSVisitor visitor)
	{
		v.visit();
		if( visitor != null )
			visitor.visit(this, v);

		for (int i = 0; i < v.getOutgoingEdgeCount(); i++)
		{
			Edge e = v.getOutgoingEdge(i);
			if (!e.getTo().visited())
			{
				if( visitor != null )
					visitor.visit(this, v, e);
				e.mark();
				dfsSpanningTree(e.getTo(), visitor);
			}
		}
	}

	//   /**
	//    * Search the verticies for one with name.
	//    * 
	//    * @param name - the vertex name
	//    * @return the first vertex with a matching name, null if no
	//    *    matches are found
	//    */
	//   public Vertex findVertexByName(String name)
	//   {
	//      Vertex match = null;
	//      for(Vertex v : vertices)
	//      {
	//         if( name.equals(v.getName()) )
	//         {
	//            match = v;
	//            break;
	//         }
	//      }
	//      return match;
	//   }

	@Override
	public Object clone() throws CloneNotSupportedException {

		Graph ret = new Graph();

		HashMap<Vertex,Vertex> retreiveObject = new HashMap<Vertex,Vertex>();
		HashSet<Vertex> retVertices = new HashSet<Vertex>();
		for(Vertex v: this.getVertices())
		{   
			Vertex newCopy = new Vertex(v.getNode());
			newCopy.setMarkState(v.getMarkState());
			newCopy.setMark(v.isMark());
			retVertices.add(newCopy);
			retreiveObject.put(newCopy, newCopy);
		}
		ret.vertices = retVertices;

		ArrayList<Edge> retEdges = new ArrayList<Edge>();

		for(Edge oldEdge: this.getEdges())
		{
			Edge newCopy = new Edge(retreiveObject.get(oldEdge.getFrom()), retreiveObject.get(oldEdge.getTo()), oldEdge.getCost());
			newCopy.setMark(oldEdge.isMarked());
			retEdges.add(newCopy);
		}

		ret.edges = retEdges;
		
		ret.rootVertex = retreiveObject.get(this.rootVertex);
		
		for(Vertex v: this.getVertices())
		{
			Vertex newVertex = retreiveObject.get(v);
			for(Edge e: retEdges)
				newVertex.addEdge(e);
		}

		return ret;

	}

	/**
	 * Search the verticies for one with data.
	 * 
	 * @param data - the vertex data to match
	 * @param compare - the comparator to perform the match
	 * @return the first vertex with a matching data, null if no
	 *    matches are found
	 */
	public Vertex findVertexByData(JoinDescription data, Comparator<JoinDescription> compare)
	{
		Vertex match = null;
		for(Vertex v : vertices)
		{
			if(v.isSimpleNode() && compare.compare(data, v.getNode()) == 0 )
			{
				match = v;
				break;
			}else if(compare.compare(data, v.getNodePair().getB()) == 0 )
			{
				match = v;
				break;
			}
		}
		return match;
	}
	
	//remember to clear marking before this call
//	public ArrayList<Vertex> findCommonSimpleAncestors(Vertex v1, Vertex v2)
//	{
//		//is this always the case?
//		assert(v1.equals(v2)?(v1==v2):true);
//		assert((v1==v2)?v1.equals(v2):true);
//		
//		ArrayList<Vertex> ret= getSimpleAncestors(v1);
//		
//		ret.retainAll(getSimpleAncestors(v2));
//		
//		return ret;
//	}
	
	public ArrayList<Vertex> getExistentialDirectAncestors(Vertex v) {
		
		ArrayList<Vertex> ret = new ArrayList<Vertex>();

		for(Edge e: v.getIncomingEdges())
		{
			if(e.getCost() == 0)
				continue;
			Vertex ancestor = e.getFrom();
			ret.add(ancestor);
		}
		return ret;
	}
	
    //get ancestors only through simple edges
	public ArrayList<Vertex> getSimpleAncestors(Vertex v1) {
		
		for(Vertex v: vertices)
		{
			v.setMarkState(VISIT_COLOR_WHITE);
		}
		
		return this.visitSimpleAncestors(v1);
	}

	private ArrayList<Vertex> visitSimpleAncestors(Vertex v1) {
		
		//I visited this ancestor before
		if(v1.getMarkState() == VISIT_COLOR_GREY)
			return new ArrayList<Vertex>();
		
		v1.setMarkState(VISIT_COLOR_GREY);
		
		ArrayList<Vertex> ret = new ArrayList<Vertex>();
		ret.add(v1);
		
		for(Edge e: v1.getIncomingEdges())
		{
			if(e.getCost() == 1)
				continue;
			Vertex v = e.getFrom();
			ret.addAll(visitSimpleAncestors(v));
		}
		return ret;
	}

	/** Search the graph for cycles. In order to detect cycles, we use
	 * a modified depth first search called a colored DFS. All nodes are
	 * initially marked white. When a node is encountered, it is marked
	 * grey, and when its descendants are completely visited, it is
	 * marked black. If a grey node is ever encountered, then there is
	 * a cycle.
	 * @return the edges that form cycles in the graph. The array will
	 * be empty if there are no cycles. 
	 */
	public Edge[] findCycles()
	{
		ArrayList<Edge> cycleEdges = new ArrayList<Edge>();
		// Mark all verticies as white
		for(Vertex v: vertices)//int n = 0; n < vertices.size(); n ++)
		{
			//Vertex v = getVertex(n);
			v.setMarkState(VISIT_COLOR_WHITE);
		}
		for(Vertex v: vertices)//(int n = 0; n < vertices.size(); n ++)
		{
			//         Vertex v = getVertex(n);
			visit(v, cycleEdges);
		}

		Edge[] cycles = new Edge[cycleEdges.size()];
		cycleEdges.toArray(cycles);
		return cycles;
	}

	private void visit(Vertex v, ArrayList<Edge> cycleEdges)
	{
		v.setMarkState(VISIT_COLOR_GREY);
		int count = v.getOutgoingEdgeCount();
		for(int n = 0; n < count; n ++)
		{
			Edge e = v.getOutgoingEdge(n);
			Vertex u = e.getTo();
			if( u.getMarkState() == VISIT_COLOR_GREY )
			{
				// A cycle Edge
				cycleEdges.add(e);
			}
			else if( u.getMarkState() == VISIT_COLOR_WHITE )
			{
				visit(u, cycleEdges);
			}
		}
		v.setMarkState(VISIT_COLOR_BLACK);
	}

	public String toString()
	{
		StringBuffer tmp = new StringBuffer("Graph[");
		for (Vertex v: vertices)//(int i = 0; i < vertices.size(); i++)
		{
			// Vertex v = vertices.get(i);
			tmp.append(v+"\n");
		}
		tmp.append(']');
		return tmp.toString();
	}

}