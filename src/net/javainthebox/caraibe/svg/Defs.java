package net.javainthebox.caraibe.svg;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;

public class Defs
{
	private String id;
	private StartElement element;
	
	/**
	 * a <defs> data class
	 * 
	 * @param	id String the id of the element
	 * @param	element StartElement the element as XML Data itself
	 */
	public Defs(String id, StartElement element)
	{
		this.id = id;
		this.element = element;
	}
	
	/**
	 * a <defs> data class
	 * 
	 * @param	element StartElement the element as XML Data itself
	 */
	public Defs(StartElement element)
	{
		this(element.getAttributeByName(new QName("id")).getValue(), element);
	}
	
	/**
	 * the the id of the element
	 * 
	 * @return	String
	 */
	public String getId()
	{
		return this.id;
	}
	
	/**
	 * get the element, xml data
	 * 
	 * @return	StartElement
	 */
	public StartElement getElement()
	{
		return this.element;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{ ")
			.append("id: ")
			.append(id)
			.append(", element: ")
			.append(element.toString())
			.append(" }");
		
		return sb.toString();
	}
	
}
