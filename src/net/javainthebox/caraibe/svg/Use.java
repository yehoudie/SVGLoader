package net.javainthebox.caraibe.svg;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

public class Use
{
	public StartElement href_element; // the used element
	public StartElement use_element; // the used element
	public ShapeBuilderCallback shape_builder; // a callback to build the used shape
	private SVGContentBuilder svg_builder;
	
	// useless...
	public String overflow;
	public String href;
	public String clip_rule;
	public double stroke_width;
	public Color fill;
	public String fill_rule;
	public Color stroke;
	public double stroke_miterlimit;
	
	public Use() {}
	
	/**
	 * a use param holder
	 * 
	 * @param href_element StartElement the used referenced element
	 * @param shape_builder ShapeBuilderCallback a callback to build the used shape
	 * @param use_element StartElement the element to get the attributes of
	 * @param	builder SVGContentBuilder the svg content buidler to call functions of 
	 */
	public Use(StartElement use_element, StartElement href_element, ShapeBuilderCallback shape_builder, SVGContentBuilder builder)
	{
		this.use_element = use_element;
		this.href_element = href_element;
		this.shape_builder = shape_builder;
		this.svg_builder = builder;

//		System.out.println("Use("+use_element+")");
		Attribute overflow_attribute = use_element.getAttributeByName(new QName("overflow"));
		if ( overflow_attribute != null ) this.overflow = overflow_attribute.getValue();
		
		Attribute href_attribute = use_element.getAttributeByName(new QName("href"));
		if ( href_attribute != null ) this.href = href_attribute.getValue();
		
		Attribute clip_rule_attribute = use_element.getAttributeByName(new QName("clip-rule"));
		if ( clip_rule_attribute != null ) this.clip_rule = clip_rule_attribute.getValue();
		
		Attribute stroke_width_attribute = use_element.getAttributeByName(new QName("stroke-width"));
		if ( stroke_width_attribute != null ) this.stroke_width = Double.valueOf( stroke_width_attribute.getValue() );
		
		Attribute fill_attribute = use_element.getAttributeByName(new QName("fill"));
		if ( fill_attribute != null )
		{
			if ( ( !fill_attribute.getValue().equals("none")) )
			{
				this.fill = Color.web( fill_attribute.getValue() );
			}
		}
		
		Attribute fill_rule_attribute = use_element.getAttributeByName(new QName("fill-rule"));
		if ( fill_rule_attribute != null ) this.fill_rule = fill_rule_attribute.getValue();
		
		Attribute stroke_attribute = use_element.getAttributeByName(new QName("stroke"));
		if ( stroke_attribute != null ) this.stroke = Color.web( stroke_attribute.getValue() );
		
		Attribute stroke_miterlimit_attribute = use_element.getAttributeByName(new QName("stroke-miterlimit"));
		if ( stroke_miterlimit_attribute != null ) this.stroke_miterlimit = Double.valueOf( stroke_miterlimit_attribute.getValue() );
	}

	public Node build()
	{
		Node node = null;
		try
		{
			node = shape_builder.build(null, href_element);
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		};

		if ( node != null )
		{
			if ( node instanceof Shape )
			{
				svg_builder.setShapeStyle((Shape) node, href_element);
				svg_builder.setShapeStyle((Shape) node, use_element);
			}

			svg_builder.setOpacity(node, href_element);
			svg_builder.setTransform(node, href_element);
			svg_builder.setOpacity(node, use_element);
			svg_builder.setTransform(node, use_element);
		}
		
		return node;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{ ")
			.append("href_element: ")
			.append(href_element)
			.append(", shape_builder: ")
			.append(shape_builder)
			.append(", overflow: ")
			.append(overflow)
			.append(", href: ")
			.append(href)
			.append(", clip_rule: ")
			.append(clip_rule)
			.append(", stroke_width: ")
			.append(stroke_width)
			.append(", fill: ")
			.append(fill)
			.append(", fill_rule: ")
			.append(fill_rule)
			.append(", stroke: ")
			.append(stroke)
			.append(", stroke_miterlimit: ")
			.append(stroke_miterlimit)
			.append(" }");

		return sb.toString();
	}
}
