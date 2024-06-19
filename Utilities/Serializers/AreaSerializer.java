package Utilities.Serializers;

import com.google.gson.*;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.Serializable;
import java.lang.reflect.Type;

public class AreaSerializer implements JsonSerializer<Area>, JsonDeserializer<Area>
{
    private class SerializedArea implements Serializable
    {
        public @Nullable Tile[]    tiles;
        public @Nullable Rectangle rectangle;
        public           int       z;

        public SerializedArea(Area area)
        {
            if(area.getPolygonArea().npoints == 4)
            {
                rectangle = area.getBoundingBox();
            }
            else
            {
                tiles = area.getTiles();
            }
            z = area.getZ();
        }

        public Area toArea()
        {
            Area area;
            if(tiles != null)
            {
                area = new Area(tiles);
                area.setZ(z);
                return area;
            }
            else if(rectangle != null)
            {
                area = new Area(rectangle.x,
                                rectangle.y,
                                rectangle.x + rectangle.width,
                                rectangle.y + rectangle.height,
                                z);
                return area;
            }

            return null;
        }
    }

    public JsonElement serialize(Area src, Type typeOfSrc, JsonSerializationContext context)
    {
        SerializedArea toSerialize = new SerializedArea(src);
        return context.serialize(toSerialize);
    }

    public Area deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                                                                                                JsonParseException
    {
        SerializedArea tempArea = context.deserialize(json, SerializedArea.class);
        Area           area     = new Area(tempArea.tiles);
        area.setZ(tempArea.z);
        return area;
    }


}

