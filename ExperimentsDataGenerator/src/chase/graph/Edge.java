package chase.graph;
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

/**
 * A directed, weighted edge in a graph
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class Edge
{
	private Vertex from;
	private Vertex to;
	private int cost;

	private boolean mark;

	/**
	 * Create a zero cost edge between from and to
	 * @param from the starting vertex
	 * @param to the ending vertex 
	 */
	public Edge(Vertex from, Vertex to)
	{
		this(from, to, 0);
	}

	/**
	 * Create an edge between from and to with the given cost.
	 * 
	 * @param from the starting vertex
	 * @param to the ending vertex
	 * @param cost the cost of the edge 
	 */
	public Edge(Vertex from, Vertex to, int cost)
	{
		this.from = from;
		this.to = to;
		this.cost = cost;
		mark = false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(! (obj instanceof Edge))
			return false;
		else 
			return ((Edge)obj).from.equals(this.from) && ((Edge)obj).to.equals(this.to) && ((Edge)obj).getCost() == this.getCost();
	}

	/**
	 * Get the ending vertex
	 * @return ending vertex
	 */
	public Vertex getTo()
	{
		return to;
	}

	public void setMark(boolean mark) {
		this.mark = mark;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	/**
	 * Get the starting vertex
	 * @return starting vertex
	 */
	public Vertex getFrom()
	{
		return from;
	}

	/**
	 * Get the cost of the edge
	 * @return cost of the edge
	 */
	public int getCost()
	{
		return cost;
	}

	/**
	 * Set the mark flag of the edge
	 *
	 */
	public void mark()
	{
		mark = true;
	}

	/**
	 * Clear the edge mark flag
	 *
	 */
	public void clearMark()
	{
		mark = false;
	}

	/**
	 * Get the edge mark flag
	 * @return edge mark flag
	 */
	public boolean isMarked()
	{
		return mark;
	}

	/**
	 * String rep of edge
	 * @return string rep with from/to vertex names and cost
	 */
	public String toString()
	{
		StringBuffer tmp = new StringBuffer("Edge[from: ");
		      tmp.append(from.getNode());
		tmp.append(",to: ");
		      tmp.append(to.getNode());
		tmp.append(", cost: ");
		tmp.append(cost);
		tmp.append("]");
		return tmp.toString();
	}
}