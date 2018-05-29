package com.kyperbox.umisc;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class TiledObjectTypes {
	
	private XmlReader xml_reader;
	private Element root;
	private ObjectMap<String, TiledObjectType> types;
	
	public TiledObjectTypes(String file) {
		xml_reader = new XmlReader();
		root = xml_reader.parse(Gdx.files.internal(file));
		types = new ObjectMap<String, TiledObjectTypes.TiledObjectType>();
		
		if(root == null)
			throw new GdxRuntimeException(StringUtils.format("Unable to parse file %s. make sure it is the correct path.", file));
		Array<Element> types = root.getChildrenByName("objecttype");
		for (Element element : types) {
			TiledObjectType tot = new TiledObjectType(element.get("name"));
			Array<Element> properties  = element.getChildrenByName("property");
			for (int i = 0; i < properties.size; i++) {
				Element element2 = properties.get(i);
				TypeProperty property = new TypeProperty(element2.get("name"), element2.get("type"), element2.hasAttribute("default")?element2.get("default"):"");
				tot.addProperty(property);
			}
			this.types.put(tot.name, tot);
		}
		
	}
	
	public XmlReader getXmlReader() {
		return xml_reader;
	}
	
	public TiledObjectType get(String name) {
		if(!types.containsKey(name)) {
			Gdx.app.error(getClass().getName(), "Object Type["+name+"] not found.");
			return null;
		}
		return types.get(name);
	}
	
	@Override
	public String toString() {
		String ret = "TiledObjectTypes:========"+"\n";
		Array<TiledObjectType> t = types.values().toArray();
		for (int i = 0; i < t.size; i++) {
			ret+=t.get(i).toString()+"\n";
		}
		ret+=" ================= END ================";
		return ret;
	}
	
	public static class TiledObjectType{
		private String name;
		private ObjectMap<String, TypeProperty> properties;
		public TiledObjectType(String name) {
			this.name = name;
			properties = new ObjectMap<String, TiledObjectTypes.TypeProperty>();
		}
		
		public String getName() {
			return name;
		}
		
		public void addProperty(String name,String type,String value) {
			addProperty(new TypeProperty(name, type, value));
		}
		
		public Array<TypeProperty> getProperties(){
			return properties.values().toArray();
		}
		
		public void addProperty(TypeProperty property) {
			properties.put(property.getName(), property);
		}
		
		public TypeProperty getProperty(String name) {
			if(!properties.containsKey(name)) {
				Gdx.app.error(getClass().getName(), "Property["+name+"] not found in Type ["+this.name+"]");
				return null;
			}
			return properties.get(name);
		}
		
		public int getInt(String name,int default_value) {
			TypeProperty property = getProperty(name);
			if(property == null || !property.checkInt())
				return default_value;
			return property.getInt();
		}
		
		public float getFloat(String name,float default_value) {
			TypeProperty property = getProperty(name);
			if(property == null || !property.checkFloat())
				return default_value;
			return property.getFloat();
		}
		
		public boolean getBool(String name,boolean default_value) {
			TypeProperty property = getProperty(name);
			if(property == null || !property.checkBool())
				return default_value;
			return property.getBool();
		}
		
		public String getString(String name,String default_value) {
			TypeProperty property = getProperty(name);
			if(property == null || !property.checkString())
				return default_value;
			return property.getString();
		}
		
		@Override
		public String toString() {
			String ret = "Object Type:"+name+"======="+"\n";
			Array<String> property_names = properties.keys().toArray();
			for (int i = 0; i < property_names.size; i++) {
				ret+="---"+properties.get(property_names.get(i)).toString()+"\n";
			}
			return ret;
		}
		
	}
	
	public static class TypeProperty{
		public static final String INTEGER = "int";
		public static final String BOOLEAN = "bool";
		public static final String FLOAT = "float";
		public static final String STRING = "string";
		
		private String name;
		private String type;
		private String value;
		
		public TypeProperty(String name,String type,String value) {
			this.name = name;
			this.type = type;
			this.value = value;
		}
		
		public String getType() {
			return type;
		}
		
		public String getName() {
			return name;
		}
		
		public String getValue() {
			return value;
		}
		
		public Object getAsObject() {
			if(type.equals(INTEGER))
				return Integer.parseInt(value);
			else if(type.equals(BOOLEAN))
				return Boolean.parseBoolean(value);
			else if(type.equals(FLOAT))
				return Float.parseFloat(value);
			else
				return value;
		}
		
		public int getInt() {
			return Integer.parseInt(value);
		}
		
		public boolean getBool() {
			return Boolean.parseBoolean(value);
		}
		
		public float getFloat() {
			return Float.parseFloat(value);
		}
		
		public String getString() {
			return value;
		}
		
		public boolean checkInt() {
			return type.equals(INTEGER);
		}
		
		public boolean checkBool() {
			return type.equals(BOOLEAN);
		}
		
		public boolean checkFloat() {
			return type.equals(FLOAT);
		}
		
		public boolean checkString() {
			return type.equals(STRING);
		}
		
		@Override
		public String toString() {
			return "property:[name="+name+" type="+type+" value="+value+"]";
		}
	}

}
