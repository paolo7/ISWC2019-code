package chase.graph;

import java.util.ArrayList;
import java.util.List;

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
import gqr.JoinInView;
import gqr.Pair;

/**
 * A named graph vertex with optional data.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class Vertex
{
	private ArrayList<Edge> incomingEdges;
	private ArrayList<Edge> outgoingEdges;
	//   private String name;
	private boolean mark;
	private int markState;
	private boolean simpleNode;
	protected boolean isSimpleNode() {
		return simpleNode;
	}

	private JoinDescription joinDescNode;
	private Pair<Pair<Integer,JoinInView>,JoinDescription> node;

	//   /**
	//    * Calls this(null, null).
	//    */ 
	//   public Vertex()
	//   {
	//      this(null, null);
	//   }
	//   /**
	//    * Create a vertex with the given name and no data
	//    * @param n
	//    */ 
	//   public Vertex(String n)
	//   {
	//      this(n, null);
	//   }
	/**
	 * Create a Vertex with name n and given data
	 * @param n - name of vertex
	 * @param data - data associated with vertex
	 */
	public Vertex(JoinDescription node)
	{
		simpleNode = true;
		incomingEdges = new ArrayList<Edge>();
		outgoingEdges = new ArrayList<Edge>();
		//      name = new String(n);
		mark = false;
		this.joinDescNode = node;
	}
	
	public Vertex(int A,JoinInView jv ,JoinDescription jd)
	{
		simpleNode = false;
		incomingEdges = new ArrayList<Edge>();
		outgoingEdges = new ArrayList<Edge>();
		//      name = new String(n);
		mark = false;
		this.node = new Pair<Pair<Integer,JoinInView>,JoinDescription>(new Pair<Integer, JoinInView>(A,jv),jd);
		
	}
	
	public Vertex(Pair<Pair<Integer,JoinInView>,JoinDescription> node)
	{
		simpleNode = false;
		incomingEdges = new ArrayList<Edge>();
		outgoingEdges = new ArrayList<Edge>();
		//      name = new String(n);
		mark = false;
		this.node = node;
		
	}

	@Override
	public int hashCode() {
		if(simpleNode)
			return this.getNode().hashCodeIgnoreRepeatedID();
		else
			return this.getNodePair().getB().hashCodeIgnoreRepeatedID();
	}

	@Override
	public boolean equals(Object arg0) {
		if(! (arg0 instanceof Vertex))
			return false;
		else if(simpleNode)
			return this.getNode().equalsIgnoreRepeatedID(((Vertex)arg0).getNode());
		else
			return this.getNodePair().getB().equalsIgnoreRepeatedID(((Vertex)arg0).getNodePair().getB())&&this.getNodePair().getA().getB().getSourceName().equals(((Vertex)arg0).getNodePair().getA().getB().getSourceName());
	}
	//   /**
	//    * @return the possibly null name of the vertex
	//    */
	//   public String getName()
	//   {
	//      return name;
	//   }

	/**
	 * @return the possibly null data of the vertex
	 */
	public JoinDescription getNode()
	{
		return this.joinDescNode;
	}
	
	public Pair<Pair<Integer,JoinInView>,JoinDescription> getNodePair()
	{
		return this.node;
	}
	//   /**
	//    * @param data The data to set.
	//    */
	//   public void setData(Join data)
	//   {
	//      this.node = data;
	//   }

	/**
	 * Add an edge to the vertex. If edge.from is this vertex, its an
	 * outgoing edge. If edge.to is this vertex, its an incoming
	 * edge. If neither from or to is this vertex, the edge is
	 * not added.
	 * 
	 * @param e - the edge to add
	 * @return true if the edge was added, false otherwise
	 */ 
	public boolean addEdge(Edge e)
	{
		if (e.getFrom() == this && !outgoingEdges.contains(e))
			outgoingEdges.add(e);
		else if (e.getTo() == this && !incomingEdges.contains(e))
			incomingEdges.add(e);
		else 
			return false;
		return true;
	}

	/**
	 * Add an outgoing edge ending at to.
	 * 
	 * @param to - the destination vertex
	 * @param cost the edge cost
	 */
	public void addOutgoingEdge(Vertex to, int cost)
	{
		Edge out = new Edge(this, to, cost);
		outgoingEdges.add(out);
	}

	public boolean isMark() {
		return mark;
	}

	public void setMark(boolean mark) {
		this.mark = mark;
	}

	/**
	 * Add an incoming edge starting at from
	 * 
	 * @param from - the starting vertex
	 * @param cost the edge cost
	 */
	public void addIncomingEdge(Vertex from, int cost)
	{
		Edge out = new Edge(this, from, cost);
		incomingEdges.add(out);
	}

	/**
	 * Check the vertex for either an incoming or outgoing edge
	 * mathcing e.
	 * 
	 * @param e the edge to check
	 * @return
	 */ 
	public boolean hasEdge(Edge e)
	{
		if (e.getFrom() == this)
			return incomingEdges.contains(e);
		else if (e.getTo() == this)
			return outgoingEdges.contains(e);
		else 
			return false;
	}

	/**
	 * Remove an edge from this vertex
	 * 
	 * @param e - the edge to remove
	 * @return true if the edge was removed, false if the
	 * edge was not connected to this vertex 
	 */
	public boolean remove(Edge e)
	{
		if (e.getFrom() == this)
			incomingEdges.remove(e);
		else if (e.getTo() == this)
			outgoingEdges.remove(e);
		else 
			return false;
		return true;
	}

	/**
	 * 
	 * @return the count of incoming edges
	 */
	public int getIncomingEdgeCount()
	{
		return incomingEdges.size();
	}

	/**
	 * Get the ith incoming edge
	 * @param i the index into incoming edges
	 * @return ith incoming edge
	 */
	public Edge getIncomingEdge(int i)
	{
		Edge e = incomingEdges.get(i);
		return e;
	}

	/**
	 * Get the incoming edges
	 * @return incoming edge list
	 */
	public List<Edge> getIncomingEdges()
	{
		return this.incomingEdges;
	}

	/**
	 * 
	 * @return the count of incoming edges
	 */
	public int getOutgoingEdgeCount()
	{
		return outgoingEdges.size();
	}
	/**
	 * Get the ith outgoing edge
	 * @param i the index into outgoing edges
	 * @return ith outgoing edge
	 */
	public Edge getOutgoingEdge(int i)
	{
		Edge e = outgoingEdges.get(i);
		return e;
	}

	/**
	 * Get the outgoing edges
	 * @return outgoing edge list
	 */
	public List<Edge> getOutgoingEdges()
	{
		return this.outgoingEdges;
	}

	/**
	 * Search the outgoing edges looking for an edge whose's
	 * edge.to == dest.
	 * @return the outgoing edge going to dest if one exists,
	 *    null otherwise.
	 */
	public Edge findEdge(Vertex dest)
	{
		for (int i = 0; i < outgoingEdges.size(); i++)
		{
			Edge e = outgoingEdges.get(i);
			if (e.getTo() == dest)
				return e;
		}
		return null;
	}  

	/**
	 * Search the outgoing edges for a match to e.
	 * 
	 * @param e - the edge to check
	 * @return e if its a member of the outgoing edges, null
	 *    otherwise.
	 */
	public Edge findEdge(Edge e)
	{
		if (outgoingEdges.contains(e))
			return e;
		else
			return null;
	}

	/**
	 * What is the cost from this vertext to the dest vertex.
	 * 
	 * @param dest - the destination vertex.
	 * @return Return Integer.MAX_VALUE if we have no edge to dest,
	 * 0 if dest is this vertex, the cost of the outgoing edge
	 * otherwise.
	 */ 
	public int cost(Vertex dest)
	{
		if (dest == this)
			return 0;

		Edge e = findEdge(dest);
		int cost = Integer.MAX_VALUE;
		if (e != null)
			cost = e.getCost();
		return cost;
	}

	/**
	 * Is there an outgoing edge ending at dest.
	 * 
	 * @param dest - the vertex to check
	 * @return true if there is an outgoing edge ending
	 *    at vertex, false otherwise.
	 */
	public boolean hasEdge(Vertex dest)
	{
		return (findEdge(dest) != null);
	}

	/**
	 * Has this vertex been marked during a visit
	 * @return true is visit has been called
	 */
	public boolean visited()
	{
		return mark;
	}

	/**
	 * Set the vertex mark flag.
	 *
	 */
	public void mark()
	{
		mark = true;
	}
	/**
	 * Set the mark state to state.
	 * 
	 * @param state 
	 */
	public void setMarkState(int state)
	{
		markState = state;
	}
	/**
	 * Get the mark state value.
	 * @return
	 */
	public int getMarkState()
	{
		return markState;
	}

	/**
	 * Visit the vertex and set the mark flag to true. 
	 *
	 */
	public void visit()
	{
		mark();
	}

	/**
	 * Clear the visited mark flag.
	 *
	 */
	public void clearMark()
	{
		mark = false;
	}

	/**
	 * @return a string form of the vertex with in and out
	 * edges.
	 */
	public String toString()
	{
		StringBuffer tmp = new StringBuffer("Vertex(");
		//      tmp.append(name);
		tmp.append(", data=");
		tmp.append((simpleNode)?joinDescNode:node);
		tmp.append("), in:[");
		for (int i = 0; i < incomingEdges.size(); i++)
		{
			Edge e = incomingEdges.get(i);
			if( i > 0 )
				tmp.append(',');
			tmp.append('{');
			         tmp.append((simpleNode)?e.getFrom().joinDescNode:node);
			tmp.append(',');
			tmp.append(e.getCost());
			tmp.append('}');
		}
		tmp.append("], out:[");
		for (int i = 0; i < outgoingEdges.size(); i++)
		{
			Edge e = outgoingEdges.get(i);
			if( i > 0 )
				tmp.append(',');
			tmp.append('{');
			         tmp.append((simpleNode)?e.getTo().joinDescNode:node);
			tmp.append(',');
			tmp.append(e.getCost());
			tmp.append('}');
		}
		tmp.append(']');
		return tmp.toString();
	}
}