package Scripts.Private;


import Cycles.CycleGenerators.HerbloreCycleGenerator;
import Cycles.General.CombineCycle;
import Cycles.General.SimpleInventoryProcessCycle;
import OSRSDatabase.HerbDB;
import Utilities.Scripting.tpircSScript;
import io.vavr.Tuple2;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;

import java.util.ArrayList;
import java.util.List;

@ScriptManifest(name = "SoloScripts.CleanHerbsScript", description = "Clean Herbs", author = "Semanresu", version = 1.0, category = Category.HERBLORE, image = "")
public class CleanHerbsScript extends tpircSScript
{


    /**
     *
     */
    @Override
    public void onStart()
    {
        AddCycle(HerbloreCycleGenerator::GetCleanGrimyHerbsCycle);
        AddCycle(HerbloreCycleGenerator::GetUnfPotionCycle);

        super.onStart();
    }
}
