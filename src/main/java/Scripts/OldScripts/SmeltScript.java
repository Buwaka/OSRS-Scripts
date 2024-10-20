package Scripts.OldScripts;


import Cycles.Skilling.SmeltCycle;
import Utilities.Scripting.ICycle;
import Utilities.Scripting.IFScript;
import io.vavr.Tuple2;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

@ScriptManifest(name = "SoloScripts.SmeltScript", description = "Smelt stuff", author = "Semanresu", version = 1.0, category = Category.SMITHING, image = "")
public class SmeltScript extends IFScript
{


    SmeltCycle SmeltBronze  = new SmeltCycle("Smelt Bronze",
                                             "Bronze bar",
                                             new Tuple2<>(436, 1),
                                             new Tuple2<>(438, 1));
    SmeltCycle SmeltIron    = new SmeltCycle("Smelt Iron", "Iron bar", new Tuple2<>(440, 1));
    SmeltCycle SmeltSteel   = new SmeltCycle("Smelt Steel",
                                             "Steel bar",
                                             new Tuple2<>(440, 1),
                                             new Tuple2<>(453, 2));
    SmeltCycle SmeltMithril = new SmeltCycle("Smelt Mithril",
                                             "Mithril bar",
                                             new Tuple2<>(447, 1),
                                             new Tuple2<>(453, 4));

    /**
     *
     */
    @Override
    public void onStart()
    {
        //        AddCycle(SmeltIron);
        //        SmeltIron.NeedForgingRing = true;
        //        SmeltIron.SetCycleType(ICycle.CycleType.Endless);
        //
        //        AddCycle(SmeltSteel);
        //        SmeltSteel.SetCycleType(ICycle.CycleType.byGoal);
        //        SmeltSteel.GetCycleType().Goal = () -> Bank.count(453) <= 2;

        SmeltMithril.SetCycleType(ICycle.CycleType.byGoal);
        //        SmeltMithril.Goal = () -> {
        //            Logger.log("SmeltMithril count: " + Bank.count(453) + " " + Bank.count(447));
        //            return Bank.isCached() && (Bank.count(447) == 0 || Bank.count(453) < 4);
        //        };
        AddCycle(SmeltMithril);
        super.onStart();
    }
}
