package OSRSDatabase;

public enum DBTags
{
    cheap("cheap"),
    expensive("expensive"),
    special("special"), // basically different mechanism from what you'd expect
    cosmetic("cosmetic"),


    smithing_exp("smithing_exp"),
    fire_making_exp("fire_making_exp"),
    wood_cutting_exp("wood_cutting_exp");

    DBTags(String value)
    {
        this.value = value;
    }

    String value;

}
