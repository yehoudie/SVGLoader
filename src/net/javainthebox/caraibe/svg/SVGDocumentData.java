package net.javainthebox.caraibe.svg;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;

/**
 * Class to hold some svg document attributes.<br>
 * Simply public fields.
 */
public class SVGDocumentData
{
	public String id;
	public String version;
	public String xmlns;
	public String xlink;
	public double x = 0;
	public double y = 0;
	public double width = 0;
	public double height = 0;
	public Bounds viewBox;
	public Bounds enable_background;
	public String space;
	
	/**
	 * set x value
	 * 
	 * @param value String the string value "XXXpx"
	 */
	public void setX(String value)
	{
		this.x = Double.valueOf( cutPx(value) );
	}
	
	/**
	 * set y value
	 * 
	 * @param value String the string value "XXXpx"
	 */
	public void setY(String value)
	{
		this.y = Double.valueOf( cutPx(value) );
	}
	
	/**
	 * set width value
	 * 
	 * @param value String the string value "XXXpx"
	 */
	public void setWidth(String value)
	{
		this.width = Double.valueOf( cutPx(value) );
	}
	
	/**
	 * set height value
	 * 
	 * @param value String the string value "XXXpx"
	 */
	public void setHeight(String value)
	{
		this.height = Double.valueOf( cutPx(value) );
	}

	/**
	 * set the viewBox Bounds
	 * 
	 * @param view_box_data String like viewBox="0 0 587 441"
	 */
	public void setViewBox(String view_box_data)
	{
		String[] data = view_box_data.split(" ");
		
		int ln = data.length;
		if ( ln == 5 ) for ( int i = 0; i < ln-1; i++ ) data[i] = data[i+1];
		
		viewBox = new BoundingBox(
					Double.valueOf( cutPx( data[0] ) ),
					Double.valueOf( cutPx( data[1] ) ),
					Double.valueOf( cutPx( data[2] ) ),
					Double.valueOf( cutPx( data[3] ) )
				);
	}
	
	/**
	 * set the enable_background Bounds
	 * 
	 * @param enable_background_data String like viewBox="0 0 587 441"
	 */
	public void setEnableBackground(String enable_background_data)
	{
		String[] data = enable_background_data.split(" ");
		int ln = data.length;
		if ( ln == 5 ) for ( int i = 0; i < ln-1; i++ ) data[i] = data[i+1];
		
		enable_background = new BoundingBox(
					Double.valueOf( cutPx( data[0] ) ),
					Double.valueOf( cutPx( data[1] ) ),
					Double.valueOf( cutPx( data[2] ) ),
					Double.valueOf( cutPx( data[3] ) )
				);
	}
	
	/**
	 * cut of ending "px" from a string
	 * 
	 * @param	value String 
	 * @return	String
	 */
	private String cutPx(String value)
	{
		if ( value.indexOf("px") == -1 ) return value;
		
		return value.substring(0, value.length()-2);
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{ id: ")
			.append(id)
			.append(", version: ")
			.append(version)
			.append(", xmlns: ")
			.append(xmlns)
			.append(", xlink: ")
			.append(xlink)
			.append(", x: ")
			.append(x)
			.append(", y: ")
			.append(y)
			.append(", width: ")
			.append(width)
			.append(", height: ")
			.append(height)
			.append(", viewBox: ")
			.append(viewBox)
			.append(", enable_background: ")
			.append(enable_background)
			.append(", space: ")
			.append(space)
			.append(" }");
		
		return sb.toString();
	}
}
