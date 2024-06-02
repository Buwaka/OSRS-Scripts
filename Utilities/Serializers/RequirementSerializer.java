package Utilities.Serializers;

import Utilities.Requirement.IRequirement;
import com.google.gson.*;

import java.lang.reflect.Type;

public class RequirementSerializer implements JsonSerializer<IRequirement>, JsonDeserializer<IRequirement>
{
    /**
     * @param jsonElement
     * @param type
     * @param jsonDeserializationContext
     *
     * @return
     *
     * @throws JsonParseException
     */
    @Override
    public IRequirement deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws
                                                                                                                               JsonParseException
    {
        //TODO
        return null;
    }

    /**
     * @param iRequirement
     * @param type
     * @param jsonSerializationContext
     *
     * @return
     */
    @Override
    public JsonElement serialize(IRequirement iRequirement, Type type, JsonSerializationContext jsonSerializationContext)
    {
        //TODO
        return null;
    }
}
