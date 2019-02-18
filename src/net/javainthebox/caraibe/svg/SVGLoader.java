package net.javainthebox.caraibe.svg;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

/**
 * SVGLoader is a class for loading SVG file.
 * 
 * <pre>
 *  URL url = ...;
 * SVGContent content SVGLoader.load(url);
 * 
 * container.getChildren.add(content);
 * </pre>
 */
public class SVGLoader
{
	private SVGLoader()
	{
	}

	/**
	 * Load SVG file and convert it to JavaFX shapes.
	 * 
	 * @param	path String The location of SVG file
	 * @return	SVGContent an object that indicates SVG content
	 */
	public static SVGContent load(String path)
	{
		SVGContent root = null;

		URL url;
		try
		{
			url = getUrl(path);
			root = build(root, url);
		}
		catch ( Exception e )
		{
//			e.printStackTrace();
		}

		return root;
	}

	private static URL getUrl(String path) throws Exception
	{
		URL url = null;
		try
		{
			url = new URL(path);
		}
		catch ( MalformedURLException ex )
		{
			url = SVGLoader.class.getResource(path);
			if ( url == null )
			{
				try
				{
					url = new File(path).toURI().toURL();
				}
				catch ( final MalformedURLException e )
				{
					Logger.getLogger(SVGLoader.class.getName()).log(Level.SEVERE, null, e);
					throw new Exception("Could not parse an URL out of: "+path);
				}
			}
		}
		return url;
	}

	/**
	 * Build the svg.
	 * 
	 * @param	root SVGContent
	 * @param	url URL the url of the svg file
	 * @return
	 */
	protected static SVGContent build(SVGContent root, URL url)
	{
		SVGContentBuilder builder = new SVGContentBuilder(url);
		
		try
		{
			root = builder.build();
		}
		catch ( IOException | XMLStreamException ex )
		{
			Logger.getLogger(SVGLoader.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		return root;
	}
	
	/**
	 * Load SVG file and convert it to JavaFX shapes.
	 * 
	 * @param	url String The location of SVG file
	 * @return	SVGContent an object that indicates SVG content
	 */
	public static SVGContent loadRes(String url)
	{
		SVGContent root = null;
		
		URL tempUrl = SVGLoader.class.getResource(url);
		if ( tempUrl == null )
		{
			try
			{
				tempUrl = new File(url).toURI().toURL();
			}
			catch ( final MalformedURLException ex1 )
			{
				Logger.getLogger(SVGLoader.class.getName()).log(Level.SEVERE, null, ex1);
				return root;
			}
		}
		
		root = build(root, tempUrl);
		
		return root;
	}

	/*public static SVGContent loadXMLDoc(String url)
	{
		System.out.println("SVGLoader.loadXMLDoc("+url+")");

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try
		{
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File(url));
			System.out.println(" - doc: "+doc);
			NodeList nodes = doc.getChildNodes();
			int nodes_ln = nodes.getLength();
			for ( int i = 0; i < nodes_ln; i++ )
			{
				System.out.println(" - - :"+nodes.item(i).getNodeName());
			}
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		return null;
	}*/
}
