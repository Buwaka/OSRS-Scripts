package SoloScripts;

import Cycles.SmithCycle;
import Utilities.Scripting.ICycle;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

@ScriptManifest(name = "SoloScripts.SmithScript", description = "Smith stuff", author = "Semanresu", version = 1.0, category = Category.SMITHING, image = "")
public class SmithScript extends tpircSScript
{
    int        SteelBarID    = 2353;
    SmithCycle SteelSmithing = new SmithCycle("Smith Steel nails", "Steel nails", SteelBarID);

    @Override
    public void onStart()
    {

        SteelSmithing.SetCycleType(ICycle.CycleType.byGoal);
        SteelSmithing.GetCycleType().Goal = () -> Skills.getRealLevel(Skill.SMITHING) >= 50;
        AddCycle(SteelSmithing);


        super.onStart();
    }
}
