package net.javainthebox.caraibe.svg;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

/**
 * SVGContent express SVG content.
 * <p>SVGContent has a root group. The root is a Group object, therefore is able to be added to scene graph:</p>
 * 
 * <pre> 
    URL url = ...;
    SVGContent content = SVGLoader.load(url);
 
    container.getChildren().add(content.getRoot());</pre>
 * 
 * <p>getNode() method returns Node object represented by ID. When loading following SVG file, Rectangle object is gotten by getNode() method.</p>
 * 
 * <p>rectangle.svg</p>
 * <pre>
 &lt;?xml version="1.0" encoding="iso-8859-1"?&gt;
 &lt;!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd"&gt;
 &lt;svg version="1.1" id="layer_1"
                     xmlns="http://www.w3.org/2000/svg"
                     xmlns:xlink="http://www.w3.org/1999/xlink"
                     x="0px" y="0px" width="300px" height="200px"
                     viewBox="0 0 300 200"
                     style="enable-background:new 0 0 300 200;"
                     xml:space="preserve"&gt;
   &lt;rect id="rect" 
         x="100" y="50"
         width="100" height="80"
         style="fill:#FFFFFF; stroke:#000000;"/&gt;
&lt;/svg&gt;
 * </pre>
 * 
 * <p>Java code is follows:</p>
 * <pre>
    SVGContent content = SVGLoader.load("rectangle.svg");
    Rectangle rect = (Rectangle) content.getNode("rect");
 * </pre>
 * 
 * <p>getGroup() method returns Group object represented by ID. When loading following SVG file, Group object is gotten by getNode() method.</p>
 * <p>group.svg</p>
 * <pre>
&lt;?xml version="1.0" encoding="iso-8859-1"?&gt;
&lt;!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd"&gt;
&lt;svg version="1.1" xmlns="http://www.w3.org/2000/svg"
                   xmlns:xlink="http://www.w3.org/1999/xlink"
                   x="0px" y="0px" width="200px" height="200px" viewBox="0 0 200 200"
                   style="enable-background:new 0 0 200 200;" xml:space="preserve"&gt;
  &lt;g id="group"&gt;
    &lt;circle style="fill:#FF0000;stroke:#000000;" cx="100" cy="100" r="50"/&gt;
  &lt;/g&gt;
&lt;/svg&gt;
 * </pre>
 * 
 * <p>Java code is follows:</p>
 * <pre>
    SVGContent content = SVGLoader.load("group.svg");
    Group group = content.getGroup("group");
 * </pre>
 *
 * note: There are many unsupport SVG element.
 * 
 * Updates by yehoudie: https://github.com/yehoudie/<br>
 * - added basic clip-path support<br>
 * - added view box clipping<br>
 * - added dash array, dash offset support for line and polyline<br> 
 * - switched Group to Pane because of layout/sizing errors:<br>
 * 		Group seems to set the layout bounds to the minX element<br>
 * - group id map is filled<br>
 */
public class SVGContent extends Pane
{
	private Map<String, Node> nodes = new HashMap<>();
	private Map<String, Pane> groups = new HashMap<>();

	public SVGContent()
	{
		this.setManaged(false);
	}
	
	void putNode(String id, Node node)
	{
		nodes.put(id, node);
	}

	/**
	 * Gets node object indicated by id. When there is no node indicated by id, return null.
	 * 
	 * @param id the name of node
	 * @return node represented by id
	 */
	public Node getNode(String id)
	{
		return nodes.get(id);
	}
	
	/**
	 * Get all the nodes of the svg content.
	 * 
	 * @return	Map<String, Node>
	 */
	public Map<String, Node> getNodes()
	{
		return nodes;
	}
	
    /**
     * Put group in group map.
     *  
     * @param	id String group id
     * @param	group Pane the group to add
     */
	void putGroup(String id, Pane group)
	{
		groups.put(id, group);
	}

	/**
	 * Gets group object indicated by id. When there is no group indicated by id, return null.
	 * 
	 * @param id the name of group
	 * @return group represented by id
	 */
	public Pane getGroup(String id)
	{
		return groups.get(id);
	}

	/**
	 * Change fill of a node.
	 * 
	 * @param id String the node id
	 * @param color String the rgb hex color
	 */
	public void setFill(String id, String color)
	{
		setFill(id, Color.web(color));
	}

	/**
	 * Change fill of a {@code Node}.
	 * 
	 * @param id String the node id
	 * @param color Color the color
	 */
	public void setFill(String id, Color color)
	{
		Node node = getNode(id);
		if ( node == null ) return;

		colorFill(node, color);
	}

	/**
	 * Color a node.
	 * 
	 * @param node Node the node to color
	 * @param color Color the color to set
	 */
	public void colorFill(Node node, Color color)
	{
		Shape shape = (Shape) node;
		shape.setFill(color);
	}
}
