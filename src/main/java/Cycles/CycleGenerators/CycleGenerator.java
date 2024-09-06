package Cycles.CycleGenerators;

import OSRSDatabase.ItemDB;
import Utilities.Scripting.ICycle;
import Utilities.Scripting.SimpleCycle;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public interface CycleGenerator
{
    abstract class CycleData {}
    abstract class CycleRequirement
    {
        /**
         * @return Display name for the GUI
         */
        public abstract String GetDisplayName();
        /**
         * @return Whether the requirement has been met, return null to defer the result (e.g opening bank, logging in, ...)
         */
        public abstract Optional<Boolean> IsRequirementMet();
    }

    /**
     * @return Name for the CycleGenerator
     */
    String GetName();

    /**
     * @return Which categories to find this generator under
     */
    EnumSet<ItemDB.Skill> GetTags();

    /**
     * @return Unique requirements that have to be met before this generator can be called
     */
    Set<CycleRequirement> GetRequirements();

    /**
     * @return in which types this cycle can run
     */
    default EnumSet<ICycle.CycleType> GetAvailableCycleTypes()
    {
        return EnumSet.allOf(ICycle.CycleType.class);
    }

    default ICycle.CycleType GetDefaultCycleType()
    {
        return ICycle.CycleType.NaturalEnd;
    }


    /**
     * Option to open a GUI form to set up CycleData
     */
    default void OpenParameterConfiguration(){}

    /**
     * @param param abstract data class containing parameters and data to generate the Cycles, null if none are necessary
     *
     * @return Function that is called when new cycle(s) have to be generated
     */
   SimpleCycle[] Generate(@Nullable CycleData param);
}
