package de.uni_leipzig.simba.saim.core.metric;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Abstract superclass for the components of a metric.*/
public abstract class Node
{
	/** the identifier for this instance, for example "max", "min", or "trigrams".*/
	public final String id;
//	@SuppressWarnings("unchecked")
//	protected static Class<? extends Node>[] subclasses = (Class<? extends Node>[]) new Class[] {Measure.class,Operator.class,Output.class,Property.class};	
	/** returns all possible identifiers for the class of this instance, for example ("min","max","add") for an instance of the Operator class.*/
	public abstract Set<String> identifiers();
	/** the number of "colors" already assigned to nodes (some of which may not exist anymore) to prevent cycles.*/
	protected static int colors = 0;
	/** the "color" of the node which is used for cycle prevention. Not related to any "real" color of a graphical representation of the node.*/
	protected int color;
	public byte getMaxChilds() {return 0;}
	/** returns true if the type of the parent and the child are compatible (e.g. a measure may only have properties as childs). */
	public abstract Set<Class<? extends Node>> validChildClasses();
	public final boolean validParentOf(Node node) {return validChildClasses().contains(node.getClass());}
	protected Node parent = null;
	protected Set<Node> childs = new HashSet<>();

	public Set<Node> getChilds() {return Collections.unmodifiableSet(childs);}

	/**	adds the child node to the node and returns true if it was successfull. */
	public boolean addChild(Node child)
	{		
		if(!acceptsChild(child)) {return false;}		
		childs.add(child);
		child.parent=this;
		child.pushDownColor(this.color);
		return true;
	}

	public void removeChild(Node child)
	{
		childs.remove(child);
		child.parent=null;
		child.pushDownColor(colors++);
	}

	public boolean acceptsChild(Node n) {return validParentOf(n)&&n.parent==null&&childs.size()<getMaxChilds()&&this.color!=n.color;}

	/** Returns true if the subtree having this node as root node is complete.*/
	public boolean isComplete()
	{

		if(childs.size()<getMaxChilds()) {return false;}
		for(Node child : childs) {if(!child.isComplete()) return false;}
		return true;
	}

	public Node(String id)
	{
		this.id=id;
		color = colors++;
	}

	/** recursively sets the color for the node decendents */
	protected void pushDownColor(int color)
	{
		this.color = color;
		for(Node child : childs) {child.pushDownColor(color);}
	}

	/**	returns the Metric Expression for the metric subtree with this node as a root, e.g. <pre>trigrams(y.dc:title,x.linkedct:condition_name).</pre>*/
	@Override public String toString()
	{
		StringBuilder sb = new StringBuilder(id);
		// we don't want changes to the childs until we have finished writing them
		synchronized(childs)
		{
			if(getChilds().isEmpty()) {return sb.toString();}
			boolean first = true;
			for(Node child: getChilds())
			{
				if(!first) {sb.append(',');} else {sb.append('(');first=false;}
				sb.append(child.toString());				
			}
		}
		sb.append(')');
		return sb.toString();
	}
	/** returns an instance of a subclass of Node that contains the identifier (Measure or Operator) if such a class exists, null otherwise.
	 * Because properties don't have a fixed list of identifiers, properties will never be returned this method.
	 * @return an instance of a subclass of Node (Measure or Operator) that contains the identifier if such a class exists, null otherwise 
	 */
	public static Node createNode(String id)
	{
		// I could do it with reflection but there are only 5 subclasses (of which 4 are created by this method) 
		if(Measure.identifiers.contains(id)) return new Measure(id);
		if(Operator.identifiers.contains(id)) return new Measure(id);		
		return null;
	}
}