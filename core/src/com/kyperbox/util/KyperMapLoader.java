package com.kyperbox.util;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.AtlasTmxMapLoader;
import com.badlogic.gdx.maps.tiled.BaseTmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.util.TiledObjectTypes.TypeProperty;
import com.kyperbox.util.TiledTemplates.TiledTemplate;

/**
 * clone of class AtlasTmxMapLoader by Justin Shapcott and Manuel Bua with
 * slight changes to suit kyperbox needs.
 * 
 * @author john
 *
 */
public class KyperMapLoader extends BaseTmxMapLoader<AtlasTmxMapLoader.AtlasTiledMapLoaderParameters> {

	private TiledObjectTypes types;
	private TiledTemplates templates;

	public KyperMapLoader() {
		this(new InternalFileHandleResolver());
	}

	public KyperMapLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	public static class AtlasTiledMapLoaderParameters extends BaseTmxMapLoader.Parameters {
		/** force texture filters? **/
		public boolean forceTextureFilters = false;
	}

	protected Array<Texture> trackedTextures = new Array<Texture>();

	private interface AtlasResolver {

		public TextureAtlas getAtlas(String name);

		public static class DirectAtlasResolver implements AtlasResolver {

			private final ObjectMap<String, TextureAtlas> atlases;

			public DirectAtlasResolver(ObjectMap<String, TextureAtlas> atlases) {
				this.atlases = atlases;
			}

			@Override
			public TextureAtlas getAtlas(String name) {
				return atlases.get(name);
			}

		}

		public static class AssetManagerAtlasResolver implements AtlasResolver {
			private final AssetManager assetManager;

			public AssetManagerAtlasResolver(AssetManager assetManager) {
				this.assetManager = assetManager;
			}

			@Override
			public TextureAtlas getAtlas(String name) {
				return assetManager.get(name, TextureAtlas.class);
			}
		}
	}

	public TiledMap load(String fileName) {
		return load(fileName, new AtlasTiledMapLoaderParameters());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle tmxFile,
			com.badlogic.gdx.maps.tiled.AtlasTmxMapLoader.AtlasTiledMapLoaderParameters parameter) {
		Array<AssetDescriptor> dependencies = new Array<AssetDescriptor>();
		try {
			root = xml.parse(tmxFile);

			Element properties = root.getChildByName("properties");
			if (properties != null) {
				for (Element property : properties.getChildrenByName("property")) {
					String name = property.getAttribute("name");
					String value = property.getAttribute("value");
					if (name.startsWith("atlas")) {
						FileHandle atlasHandle = Gdx.files.internal(value);
						dependencies.add(new AssetDescriptor(atlasHandle, TextureAtlas.class));
					}
				}
			}
		} catch (IOException e) {
			throw new GdxRuntimeException("Unable to parse .tmx file.");
		}
		return dependencies;
	}

	public TiledMap load(String fileName, AtlasTiledMapLoaderParameters parameter) {

		try {
			if (parameter != null) {
				convertObjectToTileSpace = parameter.convertObjectToTileSpace;
				flipY = parameter.flipY;
			} else {
				convertObjectToTileSpace = false;
				flipY = true;
			}

			FileHandle tmxFile = resolve(fileName);
			root = xml.parse(tmxFile);
			ObjectMap<String, TextureAtlas> atlases = new ObjectMap<String, TextureAtlas>();
			FileHandle atlasFile = Gdx.files
					.internal(KyperBoxGame.IMAGE_FOLDER + KyperBoxGame.FILE_SEPARATOR + KyperBoxGame.GAME_ATLAS);
			if (atlasFile == null) {
				throw new GdxRuntimeException("Couldn't load atlas");
			}

			TextureAtlas atlas = new TextureAtlas(atlasFile);
			atlases.put(atlasFile.path(), atlas);

			AtlasResolver.DirectAtlasResolver atlasResolver = new AtlasResolver.DirectAtlasResolver(atlases);
			TiledMap map = loadMap(root, tmxFile, atlasResolver);
			map.setOwnedResources(atlases.values().toArray());
			setTextureFilters(parameter.textureMinFilter, parameter.textureMagFilter);
			return map;
		} catch (IOException e) {
			throw new GdxRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
		}
	}

	/** May return null. */
	protected FileHandle loadAtlas(Element root, FileHandle tmxFile) throws IOException {
		Element e = root.getChildByName("properties");

		if (e != null) {
			for (Element property : e.getChildrenByName("property")) {
				String name = property.getAttribute("name", null);
				String value = property.getAttribute("value", null);
				if (name.equals("atlas")) {
					if (value == null) {
						value = property.getText();
					}

					if (value == null || value.length() == 0) {
						// keep trying until there are no more atlas properties
						continue;
					}
					return Gdx.files.internal(value);
				}
			}
		}
		FileHandle atlasFile = tmxFile.sibling(tmxFile.nameWithoutExtension() + ".atlas");
		return atlasFile.exists() ? atlasFile : null;
	}

	private void setTextureFilters(TextureFilter min, TextureFilter mag) {
		for (Texture texture : trackedTextures) {
			texture.setFilter(min, mag);
		}
		trackedTextures.clear();
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle tmxFile,
			com.badlogic.gdx.maps.tiled.AtlasTmxMapLoader.AtlasTiledMapLoaderParameters parameter) {
		map = null;
		if (parameter != null) {
			convertObjectToTileSpace = parameter.convertObjectToTileSpace;
			flipY = parameter.flipY;
		} else {
			convertObjectToTileSpace = false;
			flipY = true;
		}

		try {
			map = loadMap(root, tmxFile, new AtlasResolver.AssetManagerAtlasResolver(manager));
		} catch (Exception e) {
			throw new GdxRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
		}
	}

	@Override
	public TiledMap loadSync(AssetManager manager, String fileName, FileHandle file,
			com.badlogic.gdx.maps.tiled.AtlasTmxMapLoader.AtlasTiledMapLoaderParameters parameter) {
		if (parameter != null) {
			setTextureFilters(parameter.textureMinFilter, parameter.textureMagFilter);
		}

		return map;
	}

	protected TiledMap loadMap(Element root, FileHandle tmxFile, AtlasResolver resolver) {
		TiledMap map = new TiledMap();

		String mapOrientation = root.getAttribute("orientation", null);
		int mapWidth = root.getIntAttribute("width", 0);
		int mapHeight = root.getIntAttribute("height", 0);
		int tileWidth = root.getIntAttribute("tilewidth", 0);
		int tileHeight = root.getIntAttribute("tileheight", 0);
		String mapBackgroundColor = root.getAttribute("backgroundcolor", null);

		MapProperties mapProperties = map.getProperties();
		if (mapOrientation != null) {
			mapProperties.put("orientation", mapOrientation);
		}
		mapProperties.put("width", mapWidth);
		mapProperties.put("height", mapHeight);
		mapProperties.put("tilewidth", tileWidth);
		mapProperties.put("tileheight", tileHeight);
		if (mapBackgroundColor != null) {
			mapProperties.put("backgroundcolor", mapBackgroundColor);
		}

		mapTileWidth = tileWidth;
		mapTileHeight = tileHeight;
		mapWidthInPixels = mapWidth * tileWidth;
		mapHeightInPixels = mapHeight * tileHeight;

		if (mapOrientation != null) {
			if ("staggered".equals(mapOrientation)) {
				if (mapHeight > 1) {
					mapWidthInPixels += tileWidth / 2;
					mapHeightInPixels = mapHeightInPixels / 2 + tileHeight / 2;
				}
			}
		}

		for (int i = 0, j = root.getChildCount(); i < j; i++) {
			Element element = root.getChild(i);
			String elementName = element.getName();
			if (elementName.equals("properties")) {
				loadProperties(map.getProperties(), element);
				String types_path = "kyperbox_types.xml";
				if (types == null) {
					types = new TiledObjectTypes(types_path);
					templates = new TiledTemplates(types, "");
				}
			} else if (elementName.equals("tileset")) {
				loadTileset(map, element, tmxFile, resolver);
			} else if (elementName.equals("layer")) {
				loadTileLayer(map, map.getLayers(), element);
			} else if (elementName.equals("objectgroup")) {
				loadObjectGroup(map, map.getLayers(), element);
			}
		}
		return map;
	}

	protected void loadTileset(TiledMap map, Element element, FileHandle tmxFile, AtlasResolver resolver) {
		if (element.getName().equals("tileset")) {
			String name = element.get("name", null);
			int firstgid = element.getIntAttribute("firstgid", 1);
			int tilewidth = element.getIntAttribute("tilewidth", 0);
			int tileheight = element.getIntAttribute("tileheight", 0);
			int spacing = element.getIntAttribute("spacing", 0);
			int margin = element.getIntAttribute("margin", 0);
			String source = element.getAttribute("source", null);
			int offsetX = 0;
			int offsetY = 0;

			String imageSource = "";
			int imageWidth = 0, imageHeight = 0;

			@SuppressWarnings("unused")
			FileHandle image = null;
			if (source != null) {
				FileHandle tsx = getRelativeFileHandle(tmxFile, source);
				try {
					element = xml.parse(tsx);
					name = element.get("name", null);
					tilewidth = element.getIntAttribute("tilewidth", 0);
					tileheight = element.getIntAttribute("tileheight", 0);
					spacing = element.getIntAttribute("spacing", 0);
					margin = element.getIntAttribute("margin", 0);
					Element offset = element.getChildByName("tileoffset");
					if (offset != null) {
						offsetX = offset.getIntAttribute("x", 0);
						offsetY = offset.getIntAttribute("y", 0);
					}
					Element imageElement = element.getChildByName("image");
					if (imageElement != null) {
						imageSource = imageElement.getAttribute("source");
						imageSource.replace("../../input_assets", "");
						imageWidth = imageElement.getIntAttribute("width", 0);
						imageHeight = imageElement.getIntAttribute("height", 0);
						image = getRelativeFileHandle(tsx, imageSource);
					}
				} catch (IOException e) {
					throw new GdxRuntimeException("Error parsing external tileset.");
				}
			} else {
				Element offset = element.getChildByName("tileoffset");
				if (offset != null) {
					offsetX = offset.getIntAttribute("x", 0);
					offsetY = offset.getIntAttribute("y", 0);
				}
				Element imageElement = element.getChildByName("image");
				if (imageElement != null) {
					imageSource = imageElement.getAttribute("source");
					imageSource.replace("../../input_assets", "");
					imageWidth = imageElement.getIntAttribute("width", 0);
					imageHeight = imageElement.getIntAttribute("height", 0);
					image = getRelativeFileHandle(tmxFile, imageSource);
				}
			}

			String atlasFilePath = map.getProperties().get("atlas", String.class);
			if (atlasFilePath == null) {
				FileHandle atlasFile = tmxFile.sibling(tmxFile.nameWithoutExtension() + ".atlas");
				if (atlasFile.exists())
					atlasFilePath = atlasFile.name();
			}
			if (atlasFilePath == null) {
				throw new GdxRuntimeException("The map is missing the 'atlas' property");
			}

			// get the TextureAtlas for this tileset
			FileHandle atlasHandle = Gdx.files.internal(atlasFilePath);
			atlasHandle = resolve(atlasHandle.path());
			TextureAtlas atlas = resolver.getAtlas(atlasHandle.path());
			String regionsName = name;

			for (Texture texture : atlas.getTextures()) {
				trackedTextures.add(texture);
			}

			TiledMapTileSet tileset = new TiledMapTileSet();
			MapProperties props = tileset.getProperties();
			tileset.setName(name);
			props.put("firstgid", firstgid);
			props.put("imagesource", imageSource);

			props.put("imagewidth", imageWidth);
			props.put("imageheight", imageHeight);
			props.put("tilewidth", tilewidth);
			props.put("tileheight", tileheight);
			props.put("margin", margin);
			props.put("spacing", spacing);

			if (imageSource != null && imageSource.length() > 0) {
				int lastgid = firstgid + ((imageWidth / tilewidth) * (imageHeight / tileheight)) - 1;

				TextureRegion[][] regions = atlas.findRegion(regionsName).split(tilewidth, tileheight);
				int h = regions.length;
				int w = regions[0].length;

				for (int i = 0; i < w * h; i++) {
					int row = i % w;
					int col = i / w;

					TextureRegion region = regions[col][row];
					if (region != null) {

						int tileid = firstgid + i;

						if (tileid >= firstgid && tileid <= lastgid) {
							StaticTiledMapTile tile = new StaticTiledMapTile(region);
							tile.setId(tileid);
							tile.setOffsetX(offsetX);

							tile.setOffsetY(flipY ? -offsetY : offsetY);
							tileset.putTile(tileid, tile);
						}
					}
				}
				// WOULD NOT LOAD TILES FROM TILESET REGION
				// ONLY INDIVIDUAL TILES IN THE ATLAS
				// #FAIL
				// for (AtlasRegion region : atlas.findRegions(regionsName)) {
				// // handle unused tile ids
				// if (region != null) {
				// int tileid = region.index + firstgid;
				// if (tileid >= firstgid && tileid <= lastgid) {
				// StaticTiledMapTile tile = new StaticTiledMapTile(region);
				// tile.setId(tileid);
				// tile.setOffsetX(offsetX);
				// tile.setOffsetY(flipY ? -offsetY : offsetY);
				// tileset.putTile(tileid, tile);
				// }
				// }
				// }
			}

			for (Element tileElement : element.getChildrenByName("tile")) {
				
				int tileid = firstgid + tileElement.getIntAttribute("id", 0);
				TiledMapTile tile = tileset.getTile(tileid);
				if (tile == null) {
					Element imageElement = tileElement.getChildByName("image");
					if (imageElement != null) {
						// Is a tilemap with individual images.
						String regionName = imageElement.getAttribute("source");
						regionName = regionName.substring(regionName.lastIndexOf("/") + 1, regionName.length());
						regionName = regionName.substring(0, regionName.lastIndexOf('.'));
						AtlasRegion region = atlas.findRegion(regionName);
						if (region == null)
							throw new GdxRuntimeException("Tileset region not found: " + regionName);
						tile = new StaticTiledMapTile(region);
						tile.setId(tileid);
						tile.setOffsetX(offsetX);
						tile.setOffsetY(flipY ? -offsetY : offsetY);
						tileset.putTile(tileid, tile);
					}
				}
				if (tile != null) {
					String terrain = tileElement.getAttribute("terrain", null);
					if (terrain != null) {
						tile.getProperties().put("terrain", terrain);
					}
					String probability = tileElement.getAttribute("probability", null);
					if (probability != null) {
						tile.getProperties().put("probability", probability);
					}
					
					if(tileElement.hasChild("objectgroup")) {
						Element properties = tileElement.getChildByName("objectgroup").getChildByName("properties");
						if (properties != null) {
							loadProperties(tile.getProperties(), properties);
						}
					}
					
				}
			}

			Array<Element> tileElements = element.getChildrenByName("tile");

			Array<AnimatedTiledMapTile> animatedTiles = new Array<AnimatedTiledMapTile>();

			for (Element tileElement : tileElements) {
				int localtid = tileElement.getIntAttribute("id", 0);
				TiledMapTile tile = tileset.getTile(firstgid + localtid);
				if (tile != null) {
					Element animationElement = tileElement.getChildByName("animation");
					if (animationElement != null) {

						Array<StaticTiledMapTile> staticTiles = new Array<StaticTiledMapTile>();
						IntArray intervals = new IntArray();
						for (Element frameElement : animationElement.getChildrenByName("frame")) {
							staticTiles.add((StaticTiledMapTile) tileset
									.getTile(firstgid + frameElement.getIntAttribute("tileid")));
							intervals.add(frameElement.getIntAttribute("duration"));
						}

						AnimatedTiledMapTile animatedTile = new AnimatedTiledMapTile(intervals, staticTiles);
						animatedTile.setId(tile.getId());
						animatedTiles.add(animatedTile);
						tile = animatedTile;
					}

					Element objectgroupElement = tileElement.getChildByName("objectgroup");
					if (objectgroupElement != null) {

						for (Element objectElement : objectgroupElement.getChildrenByName("object")) {
							loadObject(map, tile, objectElement);
						}
					}

					String terrain = tileElement.getAttribute("terrain", null);
					if (terrain != null) {
						tile.getProperties().put("terrain", terrain);
					}
					String probability = tileElement.getAttribute("probability", null);
					if (probability != null) {
						tile.getProperties().put("probability", probability);
					}
					Element properties = tileElement.getChildByName("properties");
					if (properties != null) {
						loadProperties(tile.getProperties(), properties);
					}
				}
			}

			for (AnimatedTiledMapTile tile : animatedTiles) {
				tileset.putTile(tile.getId(), tile);
			}

			Element properties = element.getChildByName("properties");
			if (properties != null) {
				loadProperties(tileset.getProperties(), properties);
			}
			map.getTileSets().addTileSet(tileset);
		}
	}

	//TODO: not used -- remove or repurpose
//	private Element findElementWithAttribute(Array<Element> elements, String attribute, String value) {
//		for (int i = 0; i < elements.size; i++) {
//			Element e = elements.get(i);
//			if (e.hasAttribute(attribute) && e.getAttribute(attribute).equals(value))
//				return e;
//		}
//		return null;
//	}

	protected void loadObject(TiledMap map, MapObjects objects, Element element, float heightInPixels) {

		if (element.getName().equals("object")) {
			MapObject object = null;
			boolean has_template = element.hasAttribute("template");
			boolean has_type = element.hasAttribute("type");
			String template_name = null;
			String type_name = null;
			TiledTemplate template = null;
			if (has_type)
				type_name = element.getAttribute("type");

			if (has_template) {
				template_name = element.get("template");
				template = templates.getTemplate(template_name);
				if (template == null)
					templates.addTemplate(template_name);
				template = templates.getTemplate(template_name);
				if (template == null)
					has_template = false;
			}
			String object_name = element.getAttribute("name", null);
			object_name = object_name != null ? object_name
					: has_template ? template.getTemplateType().getName() : null;

			float scaleX = convertObjectToTileSpace ? 1.0f / mapTileWidth : 1.0f;
			float scaleY = convertObjectToTileSpace ? 1.0f / mapTileHeight : 1.0f;

			float x = element.getFloatAttribute("x", 0) * scaleX;
			float y = (flipY ? (heightInPixels - element.getFloatAttribute("y", 0)) : element.getFloatAttribute("y", 0))
					* scaleY;

			float width = has_template ? template.getWidth() * scaleX : element.getFloatAttribute("width", 0) * scaleX;
			float height = has_template ? template.getHeight() * scaleY
					: element.getFloatAttribute("height", 0) * scaleY;

			if (element.getChildCount() > 0) {
				Element child = null;
				if ((child = element.getChildByName("polygon")) != null) {
					String[] points = child.getAttribute("points").split(" ");
					float[] vertices = new float[points.length * 2];
					for (int i = 0; i < points.length; i++) {
						String[] point = points[i].split(",");
						vertices[i * 2] = Float.parseFloat(point[0]) * scaleX;
						vertices[i * 2 + 1] = Float.parseFloat(point[1]) * scaleY * (flipY ? -1 : 1);
					}
					Polygon polygon = new Polygon(vertices);
					polygon.setPosition(x, y);
					object = new PolygonMapObject(polygon);
				} else if ((child = element.getChildByName("polyline")) != null) {
					String[] points = child.getAttribute("points").split(" ");
					float[] vertices = new float[points.length * 2];
					for (int i = 0; i < points.length; i++) {
						String[] point = points[i].split(",");
						vertices[i * 2] = Float.parseFloat(point[0]) * scaleX;
						vertices[i * 2 + 1] = Float.parseFloat(point[1]) * scaleY * (flipY ? -1 : 1);
					}
					Polyline polyline = new Polyline(vertices);
					polyline.setPosition(x, y);
					object = new PolylineMapObject(polyline);
				} else if ((child = element.getChildByName("ellipse")) != null) {
					object = new EllipseMapObject(x, flipY ? y - height : y, width, height);
				}
			}
			if (object == null) {
				String gid = null;
				if ((gid = has_template ? template.getGid() : element.getAttribute("gid", null)) != null) {
					int id = (int) Long.parseLong(gid);
					boolean flipHorizontally = ((id & FLAG_FLIP_HORIZONTALLY) != 0);
					boolean flipVertically = ((id & FLAG_FLIP_VERTICALLY) != 0);

					TiledMapTile tile = map.getTileSets().getTile(id & ~MASK_CLEAR);
					TiledMapTileMapObject tiledMapTileMapObject = new TiledMapTileMapObject(tile, flipHorizontally,
							flipVertically);
					TextureRegion textureRegion = tiledMapTileMapObject.getTextureRegion();
					tiledMapTileMapObject.getProperties().put("gid", id);
					tiledMapTileMapObject.setX(x);
					tiledMapTileMapObject.setY(flipY ? y : y - height);
					float objectWidth = element.getFloatAttribute("width", textureRegion.getRegionWidth());
					float objectHeight = element.getFloatAttribute("height", textureRegion.getRegionHeight());
					tiledMapTileMapObject.setScaleX(scaleX * (objectWidth / textureRegion.getRegionWidth()));
					tiledMapTileMapObject.setScaleY(scaleY * (objectHeight / textureRegion.getRegionHeight()));
					tiledMapTileMapObject.setRotation(element.getFloatAttribute("rotation", 0));
					object = tiledMapTileMapObject;
				} else {
					object = new RectangleMapObject(x, flipY ? y - height : y, width, height);
				}
			}
			Gdx.app.log(getClass().getSimpleName(), "ObjectName:" + object_name + " template:" + template_name + " x:"
					+ x + " y:" + y + " width:" + width + " height:" + height);
			object.setName(object_name);
			String rotation = element.getAttribute("rotation", null) != null ? element.getAttribute("rotation")
					: has_template ? template.getRotation() + "" : null;
			if (rotation != null) {
				object.getProperties().put("rotation", Float.parseFloat(rotation));
			}
			String type = has_template ? template.getParentTypeName() : type_name;
			if (type != null) {
				object.getProperties().put("type", type);
			}
			int id = element.getIntAttribute("id", 0);
			if (id != 0) {
				object.getProperties().put("id", id);
			}
			object.getProperties().put("x", x);

			if (object instanceof TiledMapTileMapObject) {
				object.getProperties().put("y", y);
			} else {
				object.getProperties().put("y", (flipY ? y - height : y));
			}
			object.getProperties().put("width", width);
			object.getProperties().put("height", height);
			object.setVisible(element.getIntAttribute("visible", 1) == 1);
			Element properties = element.getChildByName("properties");
			if (properties != null) {
				loadProperties(object.getProperties(), properties);
			}

			MapProperties object_properties = object.getProperties();
			Array<TypeProperty> type_properties = new Array<TiledObjectTypes.TypeProperty>();
			if (has_template) {
				type_properties = template.getProperties();
			} else if (has_type) {
				type_properties = types.get(type_name).getProperties();
			}
			for (int i = 0; i < type_properties.size; i++) {
				TypeProperty tp = type_properties.get(i);
				if (object_properties.containsKey(tp.getName()))
					continue;
				object_properties.put(tp.getName(), tp.getAsObject());
			}

			objects.add(object);
		}
	}

}
