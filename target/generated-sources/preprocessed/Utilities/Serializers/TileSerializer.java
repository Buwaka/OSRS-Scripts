package Utilities.Serializers;

import com.google.gson.*;
import org.dreambot.api.methods.map.Tile;

import java.io.Serializable;
import java.lang.reflect.Type;

public class TileSerializer implements JsonSerializer<Tile>, JsonDeserializer<Tile>
{
    private class SerializedTile implements Serializable
    {
        public int x, y, z;

        public SerializedTile(Tile tile)
        {
            x = tile.getX();
            y = tile.getY();
            z = tile.getZ();
        }

        public Tile toTile()
        {
            return new Tile(x, y, z);
        }
    }

    public JsonElement serialize(Tile src, Type typeOfSrc, JsonSerializationContext context)
    {
        SerializedTile toSerialize = new SerializedTile(src);
        return context.serialize(toSerialize);
    }

    public Tile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
            JsonParseException
    {
        SerializedTile tempTile = context.deserialize(json, SerializedTile.class);
        return new Tile(tempTile.x, tempTile.y, tempTile.z);
    }


}
