package net.javainthebox.caraibe.svg;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import net.javainthebox.caraibe.svg.attributes.Attributes;
import net.javainthebox.caraibe.svg.attributes.Display;

public class SVGContentBuilder
{
	private URL url;
	private SVGContent root;
	private SVGDocumentData doc_data;
	private XMLEventReader reader;
	
	private Map<String, Paint> gradients;
	private Map<String, Defs> defs_map;
	private Map<String, ClipPath> clip_path_map;

	private ShapeBuilderCallback createRectCb;
	private ShapeBuilderCallback createCircleCb;
	private ShapeBuilderCallback createEllipseCb;
	private ShapeBuilderCallback createPathCb;
	private ShapeBuilderCallback createPolygonCb;
	private ShapeBuilderCallback createLineCb;
	private ShapeBuilderCallback createPolylineCb;
//	private ShapeBuilderCallback createTextCb;
	private ShapeBuilderCallback createImageCb;
//	private ShapeBuilderCallback createGroupCb;
//	private ShapeBuilderCallback createLinearGradientCb;
//	private ShapeBuilderCallback createRadialGradientCb;

	/**
	 * An SVG builder.
	 * 
	 * @param	url URL the url of the svg file
	 */
	public SVGContentBuilder(URL url)
	{
		this.url = url;
		this.root = new SVGContent();

		gradients = new HashMap<>();
		defs_map = new HashMap<>();
		clip_path_map = new HashMap<>();
		
		initCallbacks();
	}
	
	/**
	 * Initialize callbacks used in Use to build elements, when needed,<br>
	 * because shapes can't be cloned.
	 */
	private void initCallbacks()
	{
		createRectCb = (XMLEventReader reader, StartElement element)-> buildRect(element);
		createCircleCb = (XMLEventReader reader, StartElement element)-> buildCircle(element);
		createEllipseCb = (XMLEventReader reader, StartElement element)-> buildEllipse(element);
		createPathCb = (XMLEventReader reader, StartElement element)-> buildPath(element);
		createPolygonCb = (XMLEventReader reader, StartElement element)-> buildPolygon(element);
		createLineCb = (XMLEventReader reader, StartElement element)-> buildLine(element);
		createPolylineCb = (XMLEventReader reader, StartElement element)-> buildPolyline(element);
//		createTextCb = (XMLEventReader reader, StartElement element)-> buildText(reader, element);
		createImageCb = (XMLEventReader reader, StartElement element)-> buildImage(reader, element);
//		createGroupCb = (XMLEventReader reader, StartElement element)-> buildGroup(reader, element);
//		createLinearGradientCb = (XMLEventReader reader, StartElement element)-> buildLinearGradient(reader, element);
//		createRadialGradientCb = (XMLEventReader reader, StartElement element)-> buildRadialGradient(reader, element);
	}

	/**
	 * Build the svg.
	 * 
	 * @return	SVGContent
	 * @throws	IOException
	 * @throws	XMLStreamException
	 */
	protected SVGContent build() throws IOException, XMLStreamException
	{
		XMLInputFactory factory = XMLInputFactory.newInstance();
		factory.setProperty("javax.xml.stream.isValidating", false);
		factory.setProperty("javax.xml.stream.isNamespaceAware", false);
		factory.setProperty("javax.xml.stream.supportDTD", false);

		try ( BufferedInputStream bufferedStream = new BufferedInputStream(url.openStream()) )
		{
			reader = factory.createXMLEventReader(bufferedStream);

			// build svg
			eventLoop(reader, root);
			reader.close();
		}

		applyDocData();
		
		return root;
	}

	private void applyDocData()
	{
		if ( doc_data != null )
		{
			addStage();
			applyViewBox();
		}
	}

	/**
	 * Add svg stage as invisible bg. 
	 */
	private void addStage()
	{
		if ( doc_data.width > 0 && doc_data.height > 0 )
		{
			Rectangle stage = new Rectangle(doc_data.width, doc_data.height, Color.TRANSPARENT);
			root.getChildren().add(0, stage); // test
		}
	}

	/**
	 * Apply view Box values as clipping.
	 */
	private void applyViewBox()
	{
		if ( doc_data.viewBox != null )
		{
			Bounds view_box_data = doc_data.viewBox;
			Rectangle view_box = new Rectangle(	view_box_data.getMinX(), 
												view_box_data.getMinY(), 
												view_box_data.getWidth(), 
												view_box_data.getHeight());
//			view_box.setOpacity(0.5);
//			root.getChildren().add( view_box ); // test
			root.setClip(view_box);
		}
	}

	/**
	 * Iterate through the svg and build its contents.
	 * 
	 * @param	reader XMLEventReader the xml event reader to loop through
	 * @param	group Pane the instance to hold the shapes
	 * @throws	IOException
	 * @throws	XMLStreamException
	 */
	private void eventLoop(XMLEventReader reader, Pane group) throws IOException, XMLStreamException
	{
		if ( group == null )
		{
			group = new Pane();
		}

		while ( reader.hasNext() )
		{
			XMLEvent event = reader.nextEvent();
			
			if ( event.isStartElement() )
			{
				StartElement element = (StartElement) event;

				Node node = null;
				switch ( element.getName().toString() )
				{
					case "rect":
						node = buildRect(element);
						break;
					case "circle":
						node = buildCircle(element);
						break;
					case "ellipse":
						node = buildEllipse(element);
						break;
					case "path":
						node = buildPath(element);
						break;
					case "polygon":
						node = buildPolygon(element);
						break;
					case "line":
						node = buildLine(element);
						break;
					case "polyline":
						node = buildPolyline(element);
						break;
					case "text":
						node = buildText(reader, element);
						break;
					case "image":
						node = buildImage(reader, element);
						break;
					case "svg":
						doc_data = getSVGData(element);
						node = buildGroup(reader, element);
						break;
					case "g":
						node = buildGroup(reader, element);
						break;
					case "linearGradient":
						buildLinearGradient(reader, element);
						break;
					case "radialGradient":
						buildRadialGradient(reader, element);
						break;
					case "defs":
						buildDefs(reader, element);
						break;
					case "clipPath":
						buildClipPath(reader, element);
						break;
					case "use":
						Use use = buildUse(reader, element);
						node = useUse(use);
						break;
					default:
						Logger.getLogger(SVGContentBuilder.class.getName()).log(Level.INFO, "In {0}: Non Support Element: {1}", new Object[] { url, element} );
						break;
				}
				if ( node != null )
				{
					if ( node instanceof Shape )
					{
						setShapeStyle((Shape) node, element);
					}

					setDisplay(node, element);
					setOpacity(node, element);
					setTransform(node, element);
					setClipPath(node, element);
					setNodeId(node, element);
					
					group.getChildren().add(node);
				}
			}
			else if ( event.isEndElement() )
			{
				EndElement element = (EndElement) event;
				if ( element.getName().toString().equals("g") )
				{
					return;
				}
			}
		}
	}

	/**
	 * Get the svg document data<br>
	 * like<br>
	 *  {@code<svg version="1.1" id="Ebene_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
	 * width="587px" height="441px" viewBox="0 0 587 441" enable-background="new 0 0 587 441" xml:space="preserve">}.
	 * 
	 * @param	element StartElement the {@code <svg>} element
	 * @return	SVGDocumentData
	 */
	private SVGDocumentData getSVGData(StartElement element)
	{
//		System.out.println("SVGContentBuilder.getSVGData("+element+")");
		SVGDocumentData doc_data = new SVGDocumentData();
		
		@SuppressWarnings("unchecked")
		Iterator<Attribute> it = element.getAttributes();
		while ( it.hasNext() )
		{
			Attribute attribute = it.next();
			switch ( attribute.getName().getLocalPart() )
			{
				case "id":
					doc_data.id = attribute.getValue();
					break;
				case "version":
					doc_data.version = attribute.getValue();
					break;
				case "xmlns":
					doc_data.xmlns = attribute.getValue();
					break;
				case "xlink":
					doc_data.xlink = attribute.getValue();
					break;
				case "x":
					doc_data.setX( attribute.getValue() );
					break;
				case "y":
					doc_data.setY( attribute.getValue() );
					break;
				case "width":
					doc_data.setWidth( attribute.getValue() );
					break;
				case "height":
					doc_data.setHeight( attribute.getValue() );
					break;
				case "viewBox":
					doc_data.setViewBox( attribute.getValue() );
					break;
				case "enable-background":
					doc_data.setEnableBackground( attribute.getValue() );
					break;
				case "space":
					doc_data.space = attribute.getValue();
					break;
				default:
					break;
			}
		}
//		System.out.println(" - data: "+doc_data);
		return doc_data;
	}

	/**
	 * Build a group.<br>
	 * Called for {@code <svg>} and {@code <g>} elements.
	 * 
	 * @param reader
	 * @param element
	 * @return
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	private Pane buildGroup(XMLEventReader reader, StartElement element) throws IOException, XMLStreamException
	{
//		System.out.printf("SVGContentBuilder.buildGroup(%s, %s)\n",reader.toString(), element.toString());
		
		Pane group = new Pane();
		eventLoop(reader, group);

		Attribute id_attribute = element.getAttributeByName(new QName("id"));
		if ( id_attribute != null )
		{
			root.putGroup(id_attribute.getValue(), group);
		}
		
		return group;
	}

	private Node buildRadialGradient(XMLEventReader reader, StartElement element) throws IOException, XMLStreamException
	{
		String id = null;
		Double fx = null;
		Double fy = null;
		Double cx = null;
		Double cy = null;
		Double r = null;
		Transform transform = null;

		@SuppressWarnings("unchecked")
		Iterator<Attribute> it = element.getAttributes();
		while ( it.hasNext() )
		{
			Attribute attribute = it.next();
			switch ( attribute.getName().getLocalPart() )
			{
				case "id":
					id = attribute.getValue();
					break;
				case "gradientUnits":
					String gradientUnits = attribute.getValue();
					if ( !gradientUnits.equals("userSpaceOnUse") )
					{
						Logger.getLogger(SVGContentBuilder.class.getName()).log(Level.INFO, "LinearGradient supports only userSpaceOnUse: {0}", element);
						return root;
					}
					break;
				case "fx":
					fx = Double.valueOf(attribute.getValue());
					break;
				case "fy":
					fy = Double.valueOf(attribute.getValue());
					break;
				case "cx":
					cx = Double.valueOf(attribute.getValue());
					break;
				case "cy":
					cy = Double.valueOf(attribute.getValue());
					break;
				case "r":
					r = Double.valueOf(attribute.getValue());
					break;
				case "gradientTransform":
					transform = extractTransform(attribute.getValue());
					break;
				default:
					Logger.getLogger(SVGContentBuilder.class.getName()).log(Level.INFO, "RadialGradient doesn''t supports: {0}", element);
					break;
			}
		}

		// Stop の読み込み
		List<Stop> stops = buildStops(reader, "radialGradient");

		if ( id != null && cx != null && cy != null && r != null )
		{
			double fDistance = 0.0;
			double fAngle = 0.0;

			if ( transform != null && transform instanceof Affine )
			{
				double tempCx = cx;
				double tempCy = cy;
				double tempR = r;

				Affine affine = (Affine) transform;
				cx = tempCx * affine.getMxx() + tempCy * affine.getMxy() + affine.getTx();
				cy = tempCx * affine.getMyx() + tempCy * affine.getMyy() + affine.getTy();

				// これは多分違う
				r = Math.sqrt(tempR * affine.getMxx() * tempR * affine.getMxx() + tempR * affine.getMyx() * tempR * affine.getMyx());

				if ( fx != null && fy != null )
				{
					double tempFx = fx;
					double tempFy = fy;
					fx = tempFx * affine.getMxx() + tempFy * affine.getMxy() + affine.getTx();
					fy = tempFx * affine.getMyx() + tempFy * affine.getMyy() + affine.getTy();
				}
				else
				{
					fAngle = Math.asin(affine.getMyx()) * 180.0 / Math.PI;
					// これもかなり怪しい
					fDistance = Math.sqrt((cx - tempCx) * (cx - tempCx) + (cy - tempCy) * (cy - tempCy));
				}
			}

			if ( fx != null && fy != null )
			{
				fDistance = Math.sqrt((fx - cx) * (fx - cx) + (fy - cy) * (fy - cy)) / r;
				fAngle = Math.atan2(cy - fy, cx - fx) * 180.0 / Math.PI;
			}

			RadialGradient gradient = new RadialGradient(fAngle, fDistance, cx, cy, r, false, CycleMethod.NO_CYCLE, stops);
			gradients.put(id, gradient);
		}
		
		return root;
	}

	private Node buildLinearGradient(XMLEventReader reader, StartElement element) throws IOException, XMLStreamException
	{
		String id = null;
		double x1 = Double.NaN;
		double y1 = Double.NaN;
		double x2 = Double.NaN;
		double y2 = Double.NaN;
		Transform transform = null;

		@SuppressWarnings("unchecked")
		Iterator<Attribute> it = element.getAttributes();
		while ( it.hasNext() )
		{
			Attribute attribute = it.next();
			switch ( attribute.getName().getLocalPart() )
			{
				case "id":
					id = attribute.getValue();
					break;
				case "gradientUnits":
					String gradientUnits = attribute.getValue();
					if ( !gradientUnits.equals("userSpaceOnUse") )
					{
						Logger.getLogger(SVGContentBuilder.class.getName()).log(Level.INFO, "LinearGradient supports only userSpaceOnUse: {0}", element);
						return root;
					}
					break;
				case "x1":
					x1 = Double.parseDouble(attribute.getValue());
					break;
				case "y1":
					y1 = Double.parseDouble(attribute.getValue());
					break;
				case "x2":
					x2 = Double.parseDouble(attribute.getValue());
					break;
				case "y2":
					y2 = Double.parseDouble(attribute.getValue());
					break;
				case "gradientTransform":
					transform = extractTransform(attribute.getValue());
					break;
				default:
					Logger.getLogger(SVGContentBuilder.class.getName()).log(Level.INFO, "LinearGradient doesn''t supports: {0}:{1}", new Object[] { attribute, element });
					break;
			}
		}

		// Stop の読み込み
		List<Stop> stops = buildStops(reader, "linearGradient");

		if ( id != null && x1 != Double.NaN && y1 != Double.NaN && x2 != Double.NaN && y2 != Double.NaN )
		{
			if ( transform != null && transform instanceof Affine )
			{
				double x1d = x1;
				double y1d = y1;
				double x2d = x2;
				double y2d = y2;
				Affine affine = (Affine) transform;
				x1 = x1d * affine.getMxx() + y1d * affine.getMxy() + affine.getTx();
				y1 = x1d * affine.getMyx() + y1d * affine.getMyy() + affine.getTy();
				x2 = x2d * affine.getMxx() + y2d * affine.getMxy() + affine.getTx();
				y2 = x2d * affine.getMyx() + y2d * affine.getMyy() + affine.getTy();
			}

			LinearGradient gradient = new LinearGradient(x1, y1, x2, y2, false, CycleMethod.NO_CYCLE, stops);
			gradients.put(id, gradient);
		}
		
		return root;
	}

	private List<Stop> buildStops(XMLEventReader reader, String kindOfGradient) throws XMLStreamException
	{
		List<Stop> stops = new ArrayList<>();

		while ( true )
		{
			XMLEvent event = reader.nextEvent();
			if ( event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(kindOfGradient) )
			{
				break;
			}
			else if ( event.isStartElement() )
			{
				StartElement element = event.asStartElement();
				if ( !element.getName().getLocalPart().equals("stop") )
				{
					Logger.getLogger(SVGContentBuilder.class.getName()).log(Level.INFO, "LinearGradient doesn''t supports: {0}", element);
					continue;
				}

				double offset = Double.NaN;
				String color = null;
				double opacity = 1.0;

				@SuppressWarnings("unchecked")
				Iterator<Attribute> it = element.getAttributes();
				while ( it.hasNext() )
				{

					Attribute attribute = it.next();
					switch ( attribute.getName().getLocalPart() )
					{
						case "offset":
							offset = Double.parseDouble(attribute.getValue());
							break;
						case "style":
							String style = attribute.getValue();
							StringTokenizer tokenizer = new StringTokenizer(style, ";");
							while ( tokenizer.hasMoreTokens() )
							{
								String item = tokenizer.nextToken().trim();
								if ( item.startsWith("stop-color") )
								{
									color = item.substring(11);
								}
								else if ( item.startsWith("stop-opacity") )
								{
									opacity = Double.parseDouble(item.substring(13));
								}
								else
								{
									Logger.getLogger(SVGContentBuilder.class.getName()).log(Level.INFO, "LinearGradient Stop doesn''t supports: {0} [{1}] ''{2}''", new Object[] { attribute, element, item });
								}
							}
							break;
						default:
							Logger.getLogger(SVGContentBuilder.class.getName()).log(Level.INFO, "LinearGradient Stop doesn''t supports: {0} [{1}]", new Object[] { attribute, element });
							break;
					}
				}

				if ( offset != Double.NaN && color != null )
				{
					Color colour = Color.web(color, opacity);
					Stop stop = new Stop(offset, colour);
					stops.add(stop);
				}
			}
		}

		return stops;
	}

	private Shape buildRect(StartElement element)
	{
//		System.out.println("SVGContentBuilder.buildRect("+element+")");
		Attribute xAttribute = element.getAttributeByName(new QName("x"));
		Attribute yAttribute = element.getAttributeByName(new QName("y"));
		Attribute widthAttribute = element.getAttributeByName(new QName("width"));
		Attribute heightAttribute = element.getAttributeByName(new QName("height"));
		
		double x = 0.0;
		double y = 0.0;

		if ( xAttribute != null )
		{
			x = Double.parseDouble(xAttribute.getValue());
		}
		if ( yAttribute != null )
		{
			y = Double.parseDouble(yAttribute.getValue());
		}
		Rectangle rect = new Rectangle(x, y, Double.parseDouble(widthAttribute.getValue()), Double.parseDouble(heightAttribute.getValue()));

		return rect;
	}

	private Shape buildCircle(StartElement element)
	{
		Attribute cxAttribute = element.getAttributeByName(new QName("cx"));
		Attribute cyAttribute = element.getAttributeByName(new QName("cy"));
		Attribute radiusAttribute = element.getAttributeByName(new QName("r"));

		Circle circle = new Circle(Double.parseDouble(cxAttribute.getValue()), Double.parseDouble(cyAttribute.getValue()), Double.parseDouble(radiusAttribute.getValue()));

		return circle;
	}

	private Shape buildEllipse(StartElement element)
	{
		Attribute cxAttribute = element.getAttributeByName(new QName("cx"));
		Attribute cyAttribute = element.getAttributeByName(new QName("cy"));
		Attribute radiusXAttribute = element.getAttributeByName(new QName("rx"));
		Attribute radiusYAttribute = element.getAttributeByName(new QName("ry"));

		Ellipse ellipse = new Ellipse(Double.parseDouble(cxAttribute.getValue()), Double.parseDouble(cyAttribute.getValue()), Double.parseDouble(radiusXAttribute.getValue()), Double.parseDouble(radiusYAttribute.getValue()));

		return ellipse;
	}

	private Shape buildPath(StartElement element)
	{
		Attribute dAttribute = element.getAttributeByName(new QName("d"));

		SVGPath path = new SVGPath();
		path.setContent(dAttribute.getValue());

		return path;
	}

	private Shape buildPolygon(StartElement element)
	{
		Attribute pointsAttribute = element.getAttributeByName(new QName("points"));
		Polygon polygon = new Polygon();

		StringTokenizer tokenizer = new StringTokenizer(pointsAttribute.getValue(), " ");
		while ( tokenizer.hasMoreTokens() )
		{
			String point = tokenizer.nextToken();

			StringTokenizer tokenizer2 = new StringTokenizer(point, ",");
			Double x = Double.valueOf(tokenizer2.nextToken());
			Double y = Double.valueOf(tokenizer2.nextToken());

			polygon.getPoints().add(x);
			polygon.getPoints().add(y);
		}

		return polygon;
	}

	private Shape buildLine(StartElement element)
	{
		Attribute x1Attribute = element.getAttributeByName(new QName("x1"));
		Attribute y1Attribute = element.getAttributeByName(new QName("y1"));
		Attribute x2Attribute = element.getAttributeByName(new QName("x2"));
		Attribute y2Attribute = element.getAttributeByName(new QName("y2"));

		if ( x1Attribute != null && y1Attribute != null && x2Attribute != null && y2Attribute != null )
		{
			double x1 = Double.parseDouble(x1Attribute.getValue());
			double y1 = Double.parseDouble(y1Attribute.getValue());
			double x2 = Double.parseDouble(x2Attribute.getValue());
			double y2 = Double.parseDouble(y2Attribute.getValue());

			Line line = new Line(x1, y1, x2, y2);
			setDashStyle(line, element);
			
			return line;
		}
		else
		{
			return null;
		}
	}

	private Shape buildPolyline(StartElement element)
	{
//		System.out.println("SVGContentBuilder.buildPolyline("+ element+")");
		Polyline polyline = new Polyline();
		Attribute pointsAttribute = element.getAttributeByName(new QName("points"));

		StringTokenizer tokenizer = new StringTokenizer(pointsAttribute.getValue(), " ");
		while ( tokenizer.hasMoreTokens() )
		{
			String points = tokenizer.nextToken();
			StringTokenizer tokenizer2 = new StringTokenizer(points, ",");
			double x = Double.parseDouble(tokenizer2.nextToken());
			double y = Double.parseDouble(tokenizer2.nextToken());
			polyline.getPoints().add(x);
			polyline.getPoints().add(y);
		}

		setDashStyle(polyline, element);
		
		return polyline;
	}
	
	private void setDashStyle(Shape line, StartElement element)
	{
//		System.out.printf("SVGContentBuilder.setDashStyle(%s, %s)\n", line.toString(), element.toString());
		Attribute dash_array_attribute = element.getAttributeByName(new QName("stroke-dasharray"));
		if ( dash_array_attribute != null )
		{
			StringTokenizer tokenizer = new StringTokenizer(dash_array_attribute.getValue(), ",");
			while ( tokenizer.hasMoreTokens() )
			{
				String dash = tokenizer.nextToken();
				line.getStrokeDashArray().add( Double.valueOf(dash) );
			}
		}
		else
		{
			return;
		}
			
		Attribute dash_offset_attribute = element.getAttributeByName(new QName("stroke-dashoffset"));
		if ( dash_offset_attribute != null )
		{
			line.setStrokeDashOffset( Double.valueOf(dash_offset_attribute.getValue()) );
		}
	}

	private Shape buildText(XMLEventReader reader, StartElement element) throws XMLStreamException
	{
		Attribute fontFamilyAttribute = element.getAttributeByName(new QName("font-family"));
		Attribute fontSizeAttribute = element.getAttributeByName(new QName("font-size"));

		// TODO styleにfontの指定がある場合
		Font font = null;
		if ( fontFamilyAttribute != null && fontSizeAttribute != null )
		{
			font = Font.font(fontFamilyAttribute.getValue().replace("'", ""), Double.parseDouble(fontSizeAttribute.getValue()));
		}

		XMLEvent event = reader.nextEvent();
		if ( event.isCharacters() )
		{
			Text text = new Text(((Characters) event).getData());
			if ( font != null )
			{
				text.setFont(font);
			}

			return text;
		}
		else
		{
			throw new XMLStreamException("Illegal Element: " + event);
		}
	}

	private ImageView buildImage(XMLEventReader reader, StartElement element) throws IOException
	{
		Attribute widthAttribute = element.getAttributeByName(new QName("width"));
		double width = Double.parseDouble(widthAttribute.getValue());
		Attribute heightAttribute = element.getAttributeByName(new QName("height"));
		double height = Double.parseDouble(heightAttribute.getValue());
		Attribute hrefAttribute = element.getAttributeByName(new QName("href"));

		URL imageUrl = null;
		try
		{
			imageUrl = new URL(hrefAttribute.getValue());
		}
		catch ( MalformedURLException ex )
		{
			try
			{
				imageUrl = new URL(url, hrefAttribute.getValue());
			}
			catch ( MalformedURLException ex1 )
			{
				Logger.getLogger(SVGContentBuilder.class.getName()).log(Level.SEVERE, null, ex1);
			}
		}
		Image image = new Image(imageUrl.toString(), width, height, true, true);

		return new ImageView(image);
	}
	
	/**
	 * Build def element, like:
	 * {@code <defs>
	 * 		<rect id="SVGID_1_" x="-40.7" y="-57" width="668.4" height="618"/>
	 * </defs>}.<br>
	 * Put data into map to draw in a {@code <use>} case, cloning of Shapes is not possible.
	 * 
	 * @param	reader XMLEventReader
	 * @throws	XMLStreamException 
	 * @throws	IOException 
	 */
	private void buildDefs(XMLEventReader reader, StartElement defs_el) throws XMLStreamException, IOException
	{
//		System.out.printf("SVGContentBuilder.buildDefs(%s, %s, )\n", reader.toString(), defs_el.toString());

		while ( reader.hasNext() )
		{
//			System.out.println(" - - has next: ");
			XMLEvent event = reader.nextEvent();

			// check for closing </defs>
			if ( event.isEndElement() )
			{
				EndElement end_element = (EndElement) event;
				String end_element_name = end_element.getName().getLocalPart();
				if ( end_element_name.equals("defs") ) return;
				else continue;
			}
			
			if ( event.isStartElement() )
			{
				StartElement element = (StartElement) event;

				String defs_id = element.getAttributeByName(new QName("id")).getValue();
				Defs defs = new Defs(defs_id, element);
				defs_map.put(defs_id, defs);
			}
		}

		return;
	}
	
	/**
	 * build clip path, like:<br>
	 * StartElement<clipPath id="SVGID_2_">
	 * 		<use xlink:href="#SVGID_1_"  overflow="visible"/>
	 * </clipPath>\.
	 * 
	 * @param	reader XMLEventReader
	 * @param	clip_path_el StartElement
	 * @throws	XMLStreamException
	 * @throws	IOException
	 */
	private void buildClipPath(XMLEventReader reader, StartElement clip_path_el) throws XMLStreamException, IOException
	{
//		System.out.printf("buildClipPath.buildClipPath(%s, %s, )\n", reader.toString(), clip_path_el.toString());
		Attribute clip_id = clip_path_el.getAttributeByName(new QName("id"));
//		System.out.println(" - clip_id: "+clip_id);
		
		ClipPath clip_path = new ClipPath(clip_id.getValue());
		clip_path_map.put(clip_id.getValue(), clip_path);
		
		while ( reader.hasNext() )
		{
			XMLEvent event = reader.nextEvent();
//			System.out.println(" - reader has next");
//			System.out.println(" - event: "+event);

			// check for closing </clipPath>
			if ( event.isEndElement() )
			{
				EndElement end_element = (EndElement) event;
				String end_element_name = end_element.getName().getLocalPart();
				if ( end_element_name.equals("clipPath") ) return;
				else continue;
			}
			
			if ( event.isStartElement() )
			{
//				System.out.println(" - - is starting element");
				StartElement element = (StartElement) event;

//				System.out.println(" - - name: "+element.getName().toString());
				switch ( element.getName().toString() )
				{
//					TODO: check other cases with inline content too
					case "use":
						Use use = buildUse(reader, element);
						ClipPathElement el = new ClipPathElement(use, null);
						clip_path.addElement(el);
						break;
					default:
						Logger.getLogger(SVGContentBuilder.class.getName()).log(Level.INFO, "In {0}: Non Support Element: {1}", new Object[] { url, element });
						break;
				}
			}
		}
		
		return;
	}
	
	/**
	 * Handle a {@code <use>} element like:<br>
	 * {@code <use xlink:href="#SVGID_1_"  overflow="visible"/>}<br>
	 * Clone the node referenced by {@code href="id"} and put into clip_path
	 * 
	 * @param	reader XMLEventReader xml event reader
	 * @param	used_element StartElement the use node
	 * @return	Use
	 * @throws	XMLStreamException 
	 * @throws	IOException 
	 */
	private Use buildUse(XMLEventReader reader, StartElement used_element) throws XMLStreamException, IOException
	{
//		System.out.println("SVGContentBuilder.buildUse("+used_element+")");
		
		Attribute x_link = used_element.getAttributeByName(new QName("href"));
//		System.out.printf(" - x_link: %s : %s\n",x_link.getName(), x_link.getValue());
		
		// get id of used node
		// cut off starting "#"
		String used_id = x_link.getValue().substring(1);
		Defs used_defs = defs_map.get(used_id);
//		System.out.println(" - used_defs: "+used_defs);
		if ( used_defs == null )
		{
			Logger.getLogger(SVGContentBuilder.class.getName()).log(Level.INFO, "In {0}: Not found used Element: {1}", new Object[] { url, used_element} );
			return null;
		}
		StartElement href_element = used_defs.getElement();
//		Node node = null;
		ShapeBuilderCallback callback = null;
//		System.out.println(" - element: "+element);
//		System.out.println(" - element.getName: "+element.getName().toString());
		
		switch ( href_element.getName().toString() )
		{
			case "rect":
				callback = createRectCb;
//				node = buildRect(element);
				break;
			case "circle":
				callback = createCircleCb;
//				node = buildCircle(element);
				break;
			case "ellipse":
				callback = createEllipseCb;
//				node = buildEllipse(element);
				break;
			case "path":
				callback = createPathCb;
//				node = buildPath(element);
				break;
			case "polygon":
				callback = createPolygonCb;
//				node = buildPolygon(element);
				break;
			case "line":
				callback = createLineCb;
//				node = buildLine(element);
				break;
			case "polyline":
				callback = createPolylineCb;
//				node = buildPolyline(element);
				break;
			case "text":
				System.err.println(" - not implemented yet");
//				callback = createTextCb;
//				node = buildText(reader, element);
				break;
			case "image":
				callback = createImageCb;
//				node = buildImage(reader, element);
				break;
			case "svg":
			case "g":
				System.err.println(" - not implemented yet");
//				callback = createGroupCb;
//				node = buildGroup(reader, element);
				break;
			case "linearGradient":
				System.err.println(" - not implemented yet");
//				callback = createLinearGradientCb;
//				buildLinearGradient(reader, element);
				break;
			case "radialGradient":
				System.err.println(" - not implemented yet");
//				callback = createRadialGradientCb;
//				buildRadialGradient(reader, element);
				break;
			default:
				Logger.getLogger(SVGContentBuilder.class.getName()).log(Level.INFO, "In {0}: Non Support Element: {1}", new Object[] { url, href_element} );
				break;
		}
		Use use = null;
		if ( callback != null )
		{
			use = new Use(used_element, href_element, callback, this);
//			System.out.println(" - use: "+use);
		}
		
		return use;
	}

	/**
	 * Use a (standalone) {@code <use>}.
	 * 
	 * @param	use Use the use to use
	 * @return	Node
	 */
	private Node useUse(Use use)
	{
		Node node = use.build();
		
		return node;
	}
	
	/**
	 * Set the transform of a node.
	 * 
	 * @param node Node the node to transform
	 * @param element StartElement the xml element with transform infos
	 */
	void setTransform(Node node, StartElement element)
	{
		Attribute transformAttribute = element.getAttributeByName(new QName(Attributes.TRANSFORM));
		if ( transformAttribute != null )
		{
			String transforms = transformAttribute.getValue();

			Transform transform = extractTransform(transforms);
			node.getTransforms().add(transform);
		}
	}

	private Transform extractTransform(String transforms)
	{
		Transform transform = null;

		StringTokenizer tokenizer = new StringTokenizer(transforms, ")");

		while ( tokenizer.hasMoreTokens() )
		{
			String transformTxt = tokenizer.nextToken();
			if ( transformTxt.startsWith("translate(") )
			{
				throw new UnsupportedOperationException("Transform:Translate");
			}
			else if ( transformTxt.startsWith("scale(") )
			{
				throw new UnsupportedOperationException("Transform:Scale");
			}
			else if ( transformTxt.startsWith("rotate(") )
			{
				throw new UnsupportedOperationException("Transform:Rotate");
			}
			else if ( transformTxt.startsWith("skewX(") )
			{
				throw new UnsupportedOperationException("Transform:SkewX");
			}
			else if ( transformTxt.startsWith("skewY(") )
			{
				throw new UnsupportedOperationException("Transform:SkewY");
			}
			else if ( transformTxt.startsWith("matrix(") )
			{
				transformTxt = transformTxt.substring(7);
				StringTokenizer tokenizer2 = new StringTokenizer(transformTxt, " ");
				double mxx = Double.parseDouble(tokenizer2.nextToken());
				double myx = Double.parseDouble(tokenizer2.nextToken());
				double mxy = Double.parseDouble(tokenizer2.nextToken());
				double myy = Double.parseDouble(tokenizer2.nextToken());
				double tx = Double.parseDouble(tokenizer2.nextToken());
				double ty = Double.parseDouble(tokenizer2.nextToken());

				transform = Transform.affine(mxx, myx, mxy, myy, tx, ty);
			}
		}

		return transform;
	}
	
	/**
	 * Set the display status of a node.
	 * 
	 * @param	node Node the to set the opacity
	 * @param	element StartElement the xml element with opacity infos
	 */
	void setDisplay(Node node, StartElement element)
	{
		Attribute displayAttribute = element.getAttributeByName(new QName(Attributes.DISPPLAY));
		if ( displayAttribute != null )
		{
			// possible values:
			// inline | block | list-item | run-in | compact | marker | table | inline-table | table-row-group | table-header-group | table-footer-group | table-row | table-column-group | table-column | table-cell | table-caption | none | inherit
			// handled:
			// none |
			Display display = Display.forString(displayAttribute.getValue());
			
			if ( Display.NONE == display ) node.setVisible(false);
		}
	}
	
	/**
	 * Set the opacity of a node.
	 * 
	 * @param	node Node the to set the opacity
	 * @param	element StartElement the xml element with opacity infos
	 */
	void setOpacity(Node node, StartElement element)
	{
		Attribute opacityAttribute = element.getAttributeByName(new QName(Attributes.OPACITY));
		if ( opacityAttribute != null )
		{
			double opacity = Double.parseDouble(opacityAttribute.getValue());
			node.setOpacity(opacity);
		}
	}

	private Paint expressPaint(String value)
	{
		Paint paint = null;
		if ( !value.equals("none") )
		{
			if ( value.startsWith("url(#") )
			{
				String id = value.substring(5, value.length() - 1);
				paint = gradients.get(id);
			}
			else
			{
				paint = Color.web(value);
			}
		}

		return paint;
	}

	/**
	 * Set the style of a shape: color and border.<br>
	 * Use their xml attributes or applied css styles.
	 * 
	 * @param shape	Shape the shape to style
	 * @param element StartElement the shape xml element to get the style infos of
	 */
	void setShapeStyle(Shape shape, StartElement element)
	{
		Attribute fillAttribute = element.getAttributeByName(new QName(Attributes.FILL));
		if ( fillAttribute != null )
		{
			Attribute fillOpacityAttribute = element.getAttributeByName(new QName(Attributes.FILL_OPACITY));
			String value = fillAttribute.getValue();
			if ( fillOpacityAttribute != null )
			{
				// fillOpacityAttribute.getValue() \in [0,1]
				int fillOpacityValue = (int) Math.round(Double.valueOf(fillOpacityAttribute.getValue())*255);
				StringBuilder hexOpacity = new StringBuilder();
				hexOpacity.append( Integer.toHexString(fillOpacityValue) );
				if ( hexOpacity.length() == 1 ) hexOpacity.insert(0, "0");
				value += hexOpacity.toString();
			}
			shape.setFill(expressPaint(value));
		}

		Attribute strokeAttribute = element.getAttributeByName(new QName(Attributes.STROKE));
		if ( strokeAttribute != null )
		{
			shape.setStroke(expressPaint(strokeAttribute.getValue()));
		}
		else
		{
			shape.setStroke(null);
		}

		Attribute strokeWidthAttribute = element.getAttributeByName(new QName(Attributes.STROKE_WIDTH));
		if ( strokeWidthAttribute != null )
		{
			double strokeWidth = Double.parseDouble(strokeWidthAttribute.getValue());
//			System.out.println(" - stroke_width: "+strokeWidth);
			shape.setStrokeWidth(strokeWidth);
		}

		Attribute styleAttribute = element.getAttributeByName(new QName(Attributes.STYLE));
		if ( styleAttribute != null )
		{
			String styles = styleAttribute.getValue();
			StringTokenizer tokenizer = new StringTokenizer(styles, ";");
			while ( tokenizer.hasMoreTokens() )
			{
				String style = tokenizer.nextToken();

				StringTokenizer tokenizer2 = new StringTokenizer(style, ":");
				String styleName = tokenizer2.nextToken();
				String styleValue = tokenizer2.nextToken();

				switch ( styleName )
				{
					case "fill":
						shape.setFill(expressPaint(styleValue));
						break;
					case "stroke":
						shape.setStroke(expressPaint(styleValue));
						break;
					case "stroke-width":
						double strokeWidth = Double.parseDouble(styleValue);
						shape.setStrokeWidth(strokeWidth);
						break;
					case "stroke-linecap":
						StrokeLineCap linecap = StrokeLineCap.BUTT;
						if ( styleValue.equals("round") )
						{
							linecap = StrokeLineCap.ROUND;
						}
						else if ( styleValue.equals("square") )
						{
							linecap = StrokeLineCap.SQUARE;
						}
						else if ( !styleValue.equals("butt") )
						{
							Logger.getLogger(SVGContentBuilder.class.getName()).log(Level.INFO, "No Support Style: {0} {1}", new Object[] { style, element });
						}

						shape.setStrokeLineCap(linecap);
						break;
					case "stroke-miterlimit":
						double miterLimit = Double.parseDouble(styleValue);
						shape.setStrokeMiterLimit(miterLimit);
						break;
					case "stroke-linejoin":
						StrokeLineJoin linejoin = StrokeLineJoin.MITER;
						if ( styleValue.equals("bevel") )
						{
							linejoin = StrokeLineJoin.BEVEL;
						}
						else if ( styleValue.equals("round") )
						{
							linejoin = StrokeLineJoin.ROUND;
						}
						else if ( !styleValue.equals("miter") )
						{
							Logger.getLogger(SVGContentBuilder.class.getName()).log(Level.INFO, "No Support Style: {0} {1}", new Object[] { style, element });
						}

						shape.setStrokeLineJoin(linejoin);
						break;
					case "opacity":
						double opacity = Double.parseDouble(styleValue);
						shape.setOpacity(opacity);
						break;
					default:
						Logger.getLogger(SVGContentBuilder.class.getName()).log(Level.INFO, "No Support Style: {0} {1}", new Object[] { style, element });
						break;
				}
			}
		}
	}

	/**
	 * Set the clip path of an element, if any.
	 * 
	 * @param	node Node the node to clip
	 * @param	element StartElement the xml element with clip path id
	 */
	private void setClipPath(Node node, StartElement element)
	{
		Attribute clip_path_attribute = element.getAttributeByName(new QName(Attributes.CLIP_PATH));
		
		if ( clip_path_attribute == null ) return;
//		System.out.printf("SVGContentBuilder.setClipPath(%s, %s)\n", node.toString(), element.toString());
//		System.out.println(" - clip_path: "+clip_path_attribute);

		// attribute value is something like: url(#the_id)
		String clip_id = clip_path_attribute.getValue().substring(5, clip_path_attribute.getValue().length()-1);
//		System.out.println(" - clip_id: "+clip_id);
		
		ClipPath clip_path = clip_path_map.get(clip_id);
//		System.out.println(" - clip_path: "+clip_path);
		if ( clip_path == null ) return;
		
		node.setClip( clip_path.getInstance() );
//		root.getChildren().add(clip_path);
//		root.getChildren().add(clip_path.getInstance());
	}

	/**
	 * Fill map with (id, node) pairs<br>
	 * to easily access nodes lateron. 
	 * 
	 * @param node Node the node to set the id of
	 * @param element StartElement the xml element with id infos
	 */
	private void setNodeId(Node node, StartElement element)
	{
		Attribute idAttribute = element.getAttributeByName(new QName(Attributes.ID));
		String id = null;
		if ( idAttribute == null )
		{
			StringBuilder sb = new StringBuilder();
			sb.append("SVGNode.#").append(root.getNodes().size());
			id = sb.toString();
		}
		else
		{
			id = idAttribute.getValue();
		}
		
		if ( !id.isEmpty() )
		{
			root.putNode(id, node);
//			node.setId(idAttribute.getValue());
		}
	}

	/**
	 * @return	SVGContent the root
	 */
	public SVGContent getRoot()
	{
		return root;
	}
	
	public void stop()
	{
		try
		{
			reader.close();
			reader = null;
		}
		catch ( XMLStreamException e )
		{
			e.printStackTrace();
		}
	}
}
