package IF.Utilities.Serializers;

import IF.Utilities.Scripting.Logger;
import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Type;

public class SerializableRunnableSerializer
        implements JsonSerializer<SerializableRunnable>, JsonDeserializer<SerializableRunnable>
{


    /**
     * Gson invokes this call-back method during serialization when it encounters a field of the
     * specified type.
     *
     * <p>In the implementation of this call-back method, you should consider invoking {@link
     * JsonSerializationContext#serialize(Object, Type)} method to create JsonElements for any
     * non-trivial field of the {@code src} object. However, you should never invoke it on the {@code
     * src} object itself since that will cause an infinite loop (Gson will call your call-back method
     * again).
     *
     * @param src       the object that needs to be converted to Json.
     * @param typeOfSrc the actual type (fully genericized version) of the source object.
     * @param context
     *
     * @return a JsonElement corresponding to the specified object.
     */
    @Override
    public JsonElement serialize(SerializableRunnable src, Type typeOfSrc, JsonSerializationContext context)
    {
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream))
        {
            outputStream.writeObject(src);
            return context.serialize(byteArrayOutputStream.toByteArray());
        } catch(IOException e)
        {
            Logger.log("SerializableRunnableSerializer: serialize: Failed to Serialize: " + e);
        }
        return null;
    }

    /**
     * Gson invokes this call-back method during deserialization when it encounters a field of the
     * specified type.
     *
     * <p>In the implementation of this call-back method, you should consider invoking {@link
     * JsonDeserializationContext#deserialize(JsonElement, Type)} method to create objects for any
     * non-trivial field of the returned object. However, you should never invoke it on the same type
     * passing {@code json} since that will cause an infinite loop (Gson will call your call-back
     * method again).
     *
     * @param json    The Json data being deserialized
     * @param typeOfT The type of the Object to deserialize to
     * @param context
     *
     * @return a deserialized object of the specified type typeOfT which is a subclass of {@code T}
     *
     * @throws JsonParseException if json is not in the expected format of {@code typeOfT}
     */
    @Override
    public SerializableRunnable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
            JsonParseException
    {
        byte[] value = new byte[json.getAsJsonArray().size()];
        for(var i = 0; i < json.getAsJsonArray().size(); i++)
        {
            value[i] = json.getAsJsonArray().get(i).getAsByte();
        }
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(value);
            ObjectInputStream inputStream = new ObjectInputStream(byteArrayInputStream))
        {
            return (SerializableRunnable) inputStream.readObject();
        } catch(Exception e)
        {
            Logger.log("SerializableRunnableSerializer: deserialize: Failed to Serialize: " + e);
        }
        return null;
    }
}
