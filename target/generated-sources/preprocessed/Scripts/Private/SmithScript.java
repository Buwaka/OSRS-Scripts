package Scripts.Private;

import Cycles.Skilling.SmithCycle;
import Utilities.Scripting.ICycle;
import Utilities.Scripting.IFScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

@ScriptManifest(name = "SoloScripts.SmithScript", description = "Smith stuff", author = "Semanresu", version = 1.0, category = Category.SMITHING, image = "")
public class SmithScript extends IFScript
{
    int        SteelBarID        = 2353;
    int        MithrilBar        = 2359;
    int        AdamaniteBar      = 2361;
    SmithCycle SteelSmithing     = new SmithCycle("Smith Steel nails", "Steel nails", SteelBarID);
    SmithCycle SmithMithrilSword = new SmithCycle("Smith Mithril Stuff",
                                                  "Mithril sword",
                                                  MithrilBar);

    @Override
    public void onStart()
    {

        //        SteelSmithing.SetCycleType(ICycle.CycleType.byGoal);
        //        SteelSmithing.Goal = () -> Skills.getRealLevel(Skill.SMITHING) >= 50;
        //        AddCycle(SteelSmithing);

        SmithMithrilSword.SetCycleType(ICycle.CycleType.Endless);
        AddCycle(SmithMithrilSword);

        super.onStart();
    }
}
