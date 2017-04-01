package net.javainthebox.caraibe.svg;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import javafx.scene.Node;

public interface ShapeBuilderCallback
{
	public Node build(XMLEventReader reader, StartElement element) throws Exception;
}
