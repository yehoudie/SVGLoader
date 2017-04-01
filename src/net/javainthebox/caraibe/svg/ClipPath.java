package net.javainthebox.caraibe.svg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLEventReader;

import javafx.scene.Group;
import javafx.scene.Node;

public class ClipPath
{
	private String id;
	private Map<String, String> overflows; // overflow values of the nodes keyed by their node ids
	private ArrayList<ClipPathElement> elements; // overflow values of the nodes keyed by their node ids
	
	/**
	 * a <clipPath> class
	 * maybe filled with nodes which are used as the clipping path
	 * 
	 * @param	id String the id of the clip path
	 */
	public ClipPath(String id)
	{
		this.id = id;
		this.elements = new ArrayList<>();
		this.overflows = new HashMap<>();
	}
	
	/**
	 * add an element to the clip path
	 * 
	 * @param element ClipPathElement the ClipPathElement
	 * @param overflow String the overflow value 
	 */
	public void addElement(ClipPathElement element, String id, String overflow)
	{
		elements.add(element);
		overflows.put(id, overflow);
	}
	
	/**
	 * add an element to the clip path
	 * 
	 * @param element ClipPathElement the ClipPathElement
	 */
	public void addElement(ClipPathElement element)
	{
		elements.add(element);
	}
	
	/**
	 * get the nodes od the clip path
	 * 
	 * @return	ArrayList<Node
	 */
	public ArrayList<ClipPathElement> getElements()
	{
		return this.elements;
	}
	
	/**
	 * get the overflow values map
	 * 
	 * @return	 Map<String, String>
	 */
	/*public Map<String, String> getOverflows()
	{
		return this.overflows;
	}*/
	
	/**
	 * get a specific overflow value
	 * 
	 * @param	id String the string of the node of the desired overflow value
	 * @return	String
	 */
	/*public String getOverflow(String id)
	{
		return this.overflows.get(id);
	}*/

	/**
	 * get a drawn instance of the clipping path
	 * 
	 * @return
	 */
	public Group getInstance()
	{
		Group clip_path = new Group();

		for ( ClipPathElement el : elements )
		{
			Use use = el.use;
			Node node = use.build();
			clip_path.getChildren().add(node);
		}
		
		return clip_path;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{ ")
			.append("id: ")
			.append(id)
			.append(", nodes: [");
		for ( ClipPathElement el : elements )
			sb.append(el.use)
				.append(", ");
		sb.append("]")
			.append(" }");
		
		return sb.toString();
	}

	public String getId()
	{
		return this.id;
	}
}
class ClipPathElement
{
	public Use use; // the use params
	public XMLEventReader reader; // xml reader event unsupported yet!!
	
	/**
	 * 
	 * @param use Use the use params
	 * @param reader XMLEventReader xml reader event unsupported yet!!
	 */
	public ClipPathElement(Use use, XMLEventReader reader)
	{
		this.use = use;
		this.reader = reader;
	}
}