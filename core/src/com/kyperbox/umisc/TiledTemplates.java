package com.kyperbox.umisc;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.kyperbox.umisc.TiledObjectTypes.TiledObjectType;
import com.kyperbox.umisc.TiledObjectTypes.TypeProperty;

public class TiledTemplates {
	
	private TiledObjectTypes types;
	private String template_folder;
	private ObjectMap<String, TiledTemplate> templates;
	
	public TiledTemplates(TiledObjectTypes object_types,String template_folder) {
		this.types = object_types;
		this.template_folder = template_folder;
		templates = new ObjectMap<String, TiledTemplates.TiledTemplate>();
	}
	
	public void addTemplate(String template) {
		XmlReader xml = types.getXmlReader();
		Element root = null;
		try {
			root = xml.parse(Gdx.files.internal(template_folder+template));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(root == null) {
			throw new GdxRuntimeException("Unable to parse template ["+template+"]. Make sure it exists or that you have the correct template folder set.");
		}
		Element object = root.getChildByName("object");
		if(object!=null) {
			String parent_type = object.getAttribute("type");
			TiledTemplate t = new TiledTemplate(template,object.getAttribute("name"), types.get(parent_type));
			t.setWidth(object.getInt("width", 0));
			t.setHeight(object.getInt("height",0));
			t.setGid(object.getAttribute("gid",null));
			t.setRotation(object.getFloat("rotation",0f));
			
			if(object.hasChild("properties")) {
				Array<Element> properties = object.getChildByName("properties").getChildrenByName("property");
				for (Element property : properties) {
					t.getTemplateType().addProperty(property.get("name"), property.hasAttribute("type")?property.get("type"):TypeProperty.STRING, property.get("value"));
				}
			}
			
			Gdx.app.log(getClass().getName(), "template ["+template+"] has been added");
			templates.put(template, t);
		}
	}
	
	public TiledTemplate getTemplate(String template) {
		if(!templates.containsKey(template)) {
			Gdx.app.error(getClass().getName(), "template does not exist ["+template+"].");
			return null;
		}
		return templates.get(template);
	}
	
	public static class TiledTemplate{
		
		private String template_name;
		private TiledObjectType parent_type;
		private TiledObjectType template_type;
		private int width;
		private int height;
		private String gid;
		private float rotation;
		
		public TiledTemplate(String template_name,String name,TiledObjectType parent_type) {
			this.template_name = template_name;
			template_type = new TiledObjectType(name);
			this.parent_type = parent_type;
			width = 0;
			height = 0;
			gid = null;
			rotation = 0;
		}
		
		public void setRotation(float rotation) {
			this.rotation = rotation;
		}
		
		public float getRotation() {
			return rotation;
		}
		
		public void setGid(String gid) {
			this.gid = gid;
		}
		
		public String getGid() {
			return gid;
		}
		
		/**
		 * this is the name of the template file (.tx extension)
		 * to get the name of the template object use getTemplateType().getName();
		 * @return
		 */
		public String getTemplateName() {
			return template_name;
		}
		
		public String getParentTypeName() {
			return parent_type.getName();
		}
		
		public void setWidth(int width) {
			this.width = width;
		}
		
		public void setHeight(int height) {
			this.height = height;
		}
		
		public int getWidth() {
			return width;
		}
		
		public int getHeight() {
			return height;
		}
		
		public TiledObjectType getTemplateType() {
			return template_type;
		}
		
		public String getName() {
			return template_type.getName();
		}
		
		public Array<TypeProperty> getProperties(){
			Array<TypeProperty> ret_properties = new Array<TiledObjectTypes.TypeProperty>();
			ret_properties.addAll(template_type.getProperties());
			
			if(parent_type!=null) {
				Array<TypeProperty> parent_properties = parent_type.getProperties();
				for(int i = 0;i < parent_properties.size;i++) {
					boolean added = false;
					for(int l = 0;l < ret_properties.size;l++) {
						if(parent_properties.get(i).getName().equals(ret_properties.get(l).getName())) {
							added = true;
							break;
						}
					}
					if(!added)
						ret_properties.add(parent_properties.get(i));
				}
			}
			return ret_properties;
		}
		
	}
}