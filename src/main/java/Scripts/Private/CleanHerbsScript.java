package Scripts.Private;


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
    private final int WaterVialID = 227;
    List<Tuple2<Item, HerbDB.HerbData>> GrimyHerbs = new ArrayList<>();
    List<Tuple2<Item, HerbDB.HerbData>> CleanHerbs = new ArrayList<>();

    private void CreateCycles()
    {
        Bank.open();

        int herbLevel = Skills.getRealLevel(Skill.HERBLORE);
        SearchBankForHerbs();

        if(GrimyHerbs.isEmpty() && CleanHerbs.isEmpty())
        {
            this.stop();
        }

        for(var GrimyHerb : GrimyHerbs)
        {
            if(herbLevel >= GrimyHerb._2.level)
            {
                // do clean cycle
                SimpleInventoryProcessCycle CleanHerb = new SimpleInventoryProcessCycle(
                        "Clean " + GrimyHerb._2.name, GrimyHerb._1.getID());
                AddCycle(CleanHerb);

                Logger.log(
                        "Adding Clean Herb cycle for " + GrimyHerb._1.getName() + " " + CleanHerb);
            }
        }


        for(var CleanHerb : CleanHerbs)
        {
            if(herbLevel >= CleanHerb._2.level)
            {
                CombineCycle UnfPotion = new CombineCycle(
                        "Creating unf potion with " + CleanHerb._2.name,
                        CleanHerb._2.id,
                        WaterVialID);
                AddCycle(UnfPotion);
                if(CleanHerbs.getLast() == CleanHerb)
                {
                    UnfPotion.onCycleEnd.Subscribe(this, () -> this.CreateCycles());
                }
            }
        }
    }

    private void SearchBankForHerbs()
    {
        GrimyHerbs.clear();
        CleanHerbs.clear();
        for(var item : Bank.all())
        {
            if(item != null && HerbDB.isHerb(item.getID()))
            {
                Logger.log("Herb found: " + item.getName());
                if(HerbDB.isGrimyHerb(item.getID()))
                {
                    GrimyHerbs.add(new Tuple2<>(item, HerbDB.GetHerbData(item.getID())));
                }
                else
                {
                    CleanHerbs.add(new Tuple2<>(item, HerbDB.GetHerbData(item.getID())));
                }
            }
        }
    }

    /**
     *
     */
    @Override
    public void onStart()
    {
        CreateCycles();

        super.onStart();
    }
}
