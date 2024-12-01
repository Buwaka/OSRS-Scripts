package IF.Logic.Generators;

import IF.Logic.Cycles.General.InteractCycle;
import IF.Logic.Tasks.SimpleTasks.Bank.BankItemsTask;
import IF.Logic.Tasks.SimpleTasks.ItemProcessing.InteractTask;
import IF.OSRSDatabase.NPCDB;
import IF.Utilities.OSRSUtilities;
import IF.Utilities.Requirement.LevelRequirement;
import IF.Utilities.Scripting.SimpleCycle;
import com.google.gson.Gson;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;

import java.util.ArrayList;
import java.util.List;

public class FishingCycleGenerator
{
    public static SimpleCycle GetFishingCycle()
    {
        List<InteractCycle> Cycles = new ArrayList<>();
        //        final int SmallBaitSpotID   = 1530;
        //        final int RodFishingSpotID  = 1527;
        //        final int CageHarpoonSpotID = 1522;

        final String FishingSpot  = "Fishing spot";
        final int[]  FishingSpots = NPCDB.GetObjectIDsByName(FishingSpot);

        final String SmallNetAction = "Small Net";
        final String RodBaitAction  = "Bait";

        final int SmallNetID   = 303;
        final int RodID        = 307;
        final int FlyRodID     = 309;
        final int LobsterPotID = 301;
        final int HarpoonID    = 311;


        // F2P
        Area LumbridgeSwampNet = new Area(3236, 3150, 3241, 3159);
        Area DraynorNet        = new Area(3089, 3228, 3087, 3234);
        Area[] EdgeVillBait = {
                new Area(3102, 3435, 3106, 3432), new Area(3099, 3426, 3101, 3422)};
        Area KaramjaCageHarpoon = new Area(2924, 3180, 2925, 3172);


        InteractCycle ShrimpCycle = new InteractCycle("Shrimps and Anchovy",
                                                      SmallNetAction,
                                                      FishingSpots);
        ShrimpCycle.SetFilter(InteractTask.InteractableFilter.NPCs);
        ShrimpCycle.SetTargetArea(DraynorNet);
        ShrimpCycle.AddInventoryRequirement(SmallNetID);
        ShrimpCycle.AddRequirement(new LevelRequirement(Skill.FISHING, 0));
        ShrimpCycle.AddEndTask(() -> new BankItemsTask[]{
                BankItemsTask.FullDepositInventory(SmallNetID)});
        Cycles.add(ShrimpCycle);


        Gson gson = OSRSUtilities.OSRSGsonBuilder.create();


        return ShrimpCycle;

    }
}
