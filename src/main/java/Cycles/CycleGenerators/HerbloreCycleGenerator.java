package Cycles.CycleGenerators;

import OSRSDatabase.ItemDB;
import Utilities.Scripting.SimpleCycle;

public class HerbloreCycleGenerator
{
    @CycleGeneratorID(Name = "Create Profitable Potions", Description = "Checks all possible herblore potions (unf and finished) for the most profitable route and which potion has the largest profit margin (eventually check gp/h)", Skills = {
            ItemDB.Skill.HERBLORE}, Purposes = {
            CycleGeneratorID.Purpose.Money,
            CycleGeneratorID.Purpose.Experience})
    public static SimpleCycle[] GetProfitablePotionCycle()
    {
return null;
    }
}
