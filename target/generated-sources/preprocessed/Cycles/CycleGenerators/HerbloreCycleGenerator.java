package Cycles.CycleGenerators;

import Cycles.General.CombineCycle;
import Cycles.General.SimpleInventoryProcessCycle;
import OSRSDatabase.HerbDB;
import OSRSDatabase.ItemDB;
import OSRSDatabase.PotionDB;
import Utilities.Requirement.IRequirement;
import Utilities.Scripting.Logger;
import Utilities.Scripting.SimpleCycle;
import io.vavr.Tuple2;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.wrappers.items.Item;

import java.util.*;

public class HerbloreCycleGenerator
{
    private static final int WaterVialID = 227;

    public class PotionCycleGenerator implements CycleGenerator
    {
        @Override
        public SimpleCycle[] Generate(CycleData param)
        {
            if(param == null)
            {
                return GetProfitablePotionCycle();
            }

            // make cycles based on param
            return null;
        }

        @Override
        public String GetName()
        {
            return "Make Potions";
        }

        @Override
        public Set<CycleRequirement> GetRequirements()
        {
            // Create a static method in LevelRequirement to create a CycleRequirement
            return null;
        }

        @Override
        public EnumSet<ItemDB.Skill> GetTags()
        {
            return EnumSet.of(ItemDB.Skill.HERBLORE);
        }
    }

    public static SimpleCycle[] GetCleanGrimyHerbsCycle()
    {
        List<SimpleCycle> out       = new ArrayList<>();
        int               herbLevel = Skills.getRealLevel(Skill.HERBLORE);

        List<Tuple2<Item, HerbDB.HerbData>> GrimyHerbs = List.of(GetHerbsFromBank(true));


        for(var GrimyHerb : GrimyHerbs)
        {
            if(herbLevel >= GrimyHerb._2.level)
            {
                // do clean cycle
                SimpleInventoryProcessCycle CleanHerb = new SimpleInventoryProcessCycle(
                        "Clean " + GrimyHerb._2.name, GrimyHerb._1.getID());
                CleanHerb.SetInteractEveryItem(true);
                out.add(CleanHerb);

                Logger.log(
                        "Adding Clean Herb cycle for " + GrimyHerb._1.getName() + " " + CleanHerb);
            }
        }

        return out.toArray(new SimpleCycle[0]);
    }

    private static Tuple2<Item, HerbDB.HerbData>[] GetHerbsFromBank(boolean Grimy)
    {
        List<Tuple2<Item, HerbDB.HerbData>> GrimyHerbs = new ArrayList<>();
        List<Tuple2<Item, HerbDB.HerbData>> CleanHerbs = new ArrayList<>();
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

        return Grimy ? GrimyHerbs.toArray(new Tuple2[0]) : CleanHerbs.toArray(new Tuple2[0]);
    }

    @CycleGeneratorID(Name = "Create Profitable Potions", Description = "Checks all possible herblore potions (unf and finished) for the most profitable route and which potion has the largest profit margin (eventually check gp/h)", Skills = {
            ItemDB.Skill.HERBLORE}, Purposes = {
            CycleGeneratorID.Purpose.Money, CycleGeneratorID.Purpose.Experience})
    public static SimpleCycle[] GetProfitablePotionCycle()
    {
        int     herbLV   = Skills.getRealLevel(Skill.HERBLORE);
        boolean FocusEXP = true;

        var options = PotionDB.GetAllPotionsForLV(herbLV);

        if(options.length <= 0)
        {
            Logger.log(
                    "HerbloreCycleGenerator: GetProfitablePotionCycle: No viable potions found, something went wrong");
            return null;
        }


        Tuple2<Integer, PotionDB.PotionStep> best = null;
        for(var option : options)
        {
            if(option == null)
            {
                continue;
            }

            var steps = PotionDB.GetProfitablePotionSteps(option);

            if(best == null || (best._1 < steps.GetProfit() && (!FocusEXP || steps.experience > 0)))
            {
                best = new Tuple2<>(steps.GetProfit(), steps);
            }
        }


        // return new PotionCycle("Creating " + best._2.result.name, best._2, );
        return null;
    }

    public static SimpleCycle[] GetUnfPotionCycle()
    {
        List<SimpleCycle>                   out        = new ArrayList<>();
        int                                 herbLevel  = Skills.getRealLevel(Skill.HERBLORE);
        List<Tuple2<Item, HerbDB.HerbData>> CleanHerbs = List.of(GetHerbsFromBank(false));

        for(var CleanHerb : CleanHerbs)
        {
            var PossiblePotions = PotionDB.GetPotionsWithIngredient(CleanHerb._2.id);
            for(var PossiblePotion : PossiblePotions)
            {
                if(IRequirement.IsAllRequirementMet(PossiblePotion.extra_requirement) &&
                   herbLevel >= PossiblePotion.level)
                {
                    CombineCycle UnfPotion = new CombineCycle(
                            "Creating unf potion with " + CleanHerb._2.name,
                            CleanHerb._2.id,
                            WaterVialID);
                    out.add(UnfPotion);
                }
            }
        }
        return out.toArray(new SimpleCycle[0]);
    }

    public static void main(String[] args)
    {
        var options = PotionDB.GetAllPotions(false);


        List<PotionDB.PotionStep> sortedOptions = new ArrayList<>();
        for(var option : options)
        {
            if(option == null)
            {
                continue;
            }

            var steps = PotionDB.GetProfitablePotionSteps(option);
            sortedOptions.add(steps);
        }
        sortedOptions.sort((x, y) -> x.GetProfit() - y.GetProfit());
        System.out.print(Arrays.toString(sortedOptions.toArray()));
        System.out.print(Arrays.toString(sortedOptions.stream()
                                                      .filter((t) -> !t.purchase)
                                                      .toArray()));

    }
}
