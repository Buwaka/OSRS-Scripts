package IF.Utilities.Serializers;

import IF.OSRSDatabase.ItemDB;
import IF.Utilities.Requirement.*;
import com.google.gson.*;
import org.dreambot.api.methods.quest.book.PaidQuest;
import org.dreambot.api.methods.skills.Skill;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RequirementSerializer
        implements JsonSerializer<IRequirement>, JsonDeserializer<IRequirement>
{
    public static void main(String[] args)
    {
        List<IRequirement> reqs = new ArrayList<>();

        reqs.add(new LevelRequirement(Skill.WOODCUTTING, 25));
        reqs.add(new QuestRequirement(PaidQuest.SHADES_OF_MORTTON));
        reqs.add(new MemberRequirement());

        var OSRSGsonBuilder = new GsonBuilder().setPrettyPrinting()
                                               .setLenient()
                                               .disableHtmlEscaping()
                                               .excludeFieldsWithModifiers(Modifier.STATIC,
                                                                           Modifier.TRANSIENT)
                                               .registerTypeAdapter(IRequirement.class,
                                                                    new RequirementSerializer())
                                               .registerTypeAdapter(ItemDB.Requirement.class,
                                                                    new ItemDB.Requirement.RequirementDeserializer())
                                               .registerTypeAdapter(SerializableSupplier.class,
                                                                    new SerializableSupplierSerializer<>())
                                               .create();

        String json = OSRSGsonBuilder.toJson(reqs.toArray(), IRequirement[].class);

        System.out.print(json);

        IRequirement[] New = OSRSGsonBuilder.fromJson(json, IRequirement[].class);
        System.out.print(New);
    }

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
        var elements = jsonElement.getAsJsonObject().entrySet();

        for(var element : elements)
        {
            IRequirement.RequirementType ReqType = IRequirement.RequirementType.valueOf(element.getKey());

            switch(ReqType)
            {
                case Equipment ->
                {
                    return jsonDeserializationContext.deserialize(element.getValue(),
                                                                  EquipmentRequirement.class);
                }
                case Favor ->
                {
                    return jsonDeserializationContext.deserialize(element.getValue(),
                                                                  FavorRequirement.class);
                }
                case Item ->
                {
                    return jsonDeserializationContext.deserialize(element.getValue(),
                                                                  ItemRequirement.class);
                }
                case Kill ->
                {
                    return jsonDeserializationContext.deserialize(element.getValue(),
                                                                  KillRequirement.class);
                }
                case Kudos ->
                {
                    return jsonDeserializationContext.deserialize(element.getValue(),
                                                                  KudosRequirement.class);
                }
                case Level ->
                {
                    return jsonDeserializationContext.deserialize(element.getValue(),
                                                                  LevelRequirement.class);
                }
                case QuestPoint ->
                {
                    return jsonDeserializationContext.deserialize(element.getValue(),
                                                                  QuestPointRequirement.class);
                }
                case Quest ->
                {
                    return jsonDeserializationContext.deserialize(element.getValue(),
                                                                  QuestRequirement.class);
                }
                case Member ->
                {
                    return jsonDeserializationContext.deserialize(element.getValue(),
                                                                  MemberRequirement.class);
                }
            }
        }
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
        //var TypeJson = jsonSerializationContext.serialize(iRequirement.getClass());
        var ObjJson = jsonSerializationContext.serialize(iRequirement);
        var out     = new JsonObject();
        out.add(iRequirement.GetRequirementType().name(), ObjJson);
        return out;
    }
}
