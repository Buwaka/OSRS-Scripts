package IF.Logic.Generators;

import IF.Logic.Cycles.Minigame.MasteringMixologyCycle;
import IF.OSRSDatabase.ItemDB;
import IF.Utilities.Scripting.SimpleCycle;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

public class MinigameCycleGenerator
{
    public static class MasteringMixologyGenerator implements CycleGenerator
    {
        @Override
        public SimpleCycle[] Generate(@Nullable CycleGenerator.CycleData param)
        {
            return new SimpleCycle[]{new MasteringMixologyCycle("MasteringMixology")};
        }

        @Override
        public String GetName()
        {
            return "Mastering Mixology";
        }

        @Override
        public Set<CycleRequirement> GetRequirements()
        {
            // lv 60 herblore
            return null;
        }

        @Override
        public EnumSet<ItemDB.Skill> GetTags()
        {
            return EnumSet.of(ItemDB.Skill.HERBLORE);
        }
    }
}
