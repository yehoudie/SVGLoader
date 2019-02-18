package net.javainthebox.caraibe.svg.attributes;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum Display 
{
	NONE	("none"),
	INLINE	("inline"),
	BLOCK	("block")
	;
	
	private final String name;
	public static final String TAG = "display";
	
	/**
	 * Display types.
	 * 
	 * @param	string String the string representation
	 */
	private Display(final String string)
	{
		this.name = string;
	}
	
	@Override
	public String toString()
	{
		return this.name;
	}
	
	// map names to type
	private static final Map<String, Display> nameToTypeMap = new HashMap<String, Display>();
	static
	{
		for ( Display type : EnumSet.allOf(Display.class) )
		{
			nameToTypeMap.put(type.name, type);
		}
	}
	
	public static Display forString(String s)
	{
		return nameToTypeMap.get(s);
	}
}