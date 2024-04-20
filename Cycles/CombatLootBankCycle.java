package Cycles;


import Cycles.AdvanceTasks.SlaughterAndLoot;
import Cycles.SimpleTasks.BankItemsTask;
import Cycles.SimpleTasks.GetCombatRationsTask;
import Cycles.SimpleTasks.RestoreFullHealthTask;
import Cycles.SimpleTasks.TravelTask;
import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleCycle;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Logger;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;

public class CombatLootBankCycle extends SimpleCycle
{
    private Area[]                                          KillingArea;
    private int[]                                           Targets;
    private BankLocation                                    BankingLocation  = null;
    private List<AbstractMap.SimpleEntry<Integer, Integer>> ItemRequirements = null;

    public CombatLootBankCycle(Area[] KillingArea, int[] Targets, List<AbstractMap.SimpleEntry<Integer, Integer>> ItemRequirements, BankLocation BankLoc)
    {
        this.KillingArea      = KillingArea;
        this.Targets          = Targets;
        this.BankingLocation  = BankLoc;
        this.ItemRequirements = ItemRequirements;

        SetCycleType(CycleType.byCount);
    }

    public CombatLootBankCycle(Area[] KillingArea, int[] Targets)
    {
        this.KillingArea = KillingArea;
        this.Targets     = Targets;

        SetCycleType(CycleType.byCount);
    }

    public CombatLootBankCycle(Area[] KillingArea, int[] Targets, List<AbstractMap.SimpleEntry<Integer, Integer>> ItemRequirements)
    {
        this.KillingArea      = KillingArea;
        this.Targets          = Targets;
        this.ItemRequirements = ItemRequirements;

        SetCycleType(CycleType.byCount);
    }

    public CombatLootBankCycle(Area[] KillingArea, int[] Targets, BankLocation BankLoc)
    {
        this.KillingArea     = KillingArea;
        this.Targets         = Targets;
        this.BankingLocation = BankLoc;

        SetCycleType(CycleType.byCount);
    }

    public boolean IsReadyForCombat(int minHP)
    {
        return OSRSUtilities.CheckInventory(ItemRequirements, false) && Players.getLocal().getHealthPercent() > minHP;
    }

    public TravelTask TravelToBank()
    {
        if(BankingLocation != null)
        {
            return new TravelTask("", BankingLocation.getTile());
        }
        else if(BankLocation.getNearest() != null)
        {
            return new TravelTask("", BankLocation.getNearest().getTile());
        }
        return new TravelTask("", new Tile());
    }

    void StartCycle(tpircSScript Script)
    {
        TravelTask Travel1 = new TravelTask("Travel To Killing Area",
                                            Arrays.stream(KillingArea).findAny().get().getRandomTile());
        Travel1.TaskPriority.set(2);
        Travel1.CompleteCondition = () -> Arrays.stream(KillingArea).anyMatch(t -> t.contains(Players.getLocal().getTile()));
        SlaughterAndLoot SALTask = new SlaughterAndLoot("Killing and Looting", KillingArea, Targets, ItemRequirements);

        if(!IsReadyForCombat(SALTask.GetMaxHit() + 2))
        {
            RestoreFullHealthTask Healup = new RestoreFullHealthTask("Healup with scraps from last cycle");
            Healup.TaskPriority.set(-2);
            Healup.CompleteCondition = () -> OSRSUtilities.InventoryContainsAnyFoods(!Client.isMembers()) ||
                                             Players.getLocal().getHealthPercent() == 100;
            Healup.AcceptCondition   = () -> !Healup.CompleteCondition.get();

            Logger.log("NotReady for combat, first go to bank");
            TravelTask Travel3 = TravelToBank();
            Travel3.SetTaskName("Travel To Bank For ItemRequirements");
            Travel3.TaskPriority.set(0);
            Travel3.CompleteCondition = OSRSUtilities::CanReachBank;
            Script.addNodes(Travel3);

            BankItemsTask Setup = new BankItemsTask("Banking ItemRequirements");
            if(!Inventory.isEmpty())
            {
                Setup.DepositAll();
            }
            for(var item : ItemRequirements)
            {
                Setup.AddWithdraw(item.getKey(), item.getValue());
            }
            if(BankingLocation != null)
            {
                Setup.SetSpecificBank(BankingLocation);
            }
            Setup.TaskPriority.set(-1);

            GetCombatRationsTask Rations = new GetCombatRationsTask("Get Rations",
                                                                    Skills.getRealLevel(Skill.HITPOINTS));
            Rations.TaskPriority.set(-1);

            Script.addNodes(Setup, Rations, Healup);

        }

        TravelTask Travel2 = TravelToBank();
        Travel2.TaskPriority.set(2);
        Travel2.AcceptCondition   = () -> !SALTask.IsAlive();
        Travel2.CompleteCondition = OSRSUtilities::CanReachBank;
        Travel2.SetTaskName("Travel To Bank to drop loot");
        BankItemsTask BankTask = new BankItemsTask("Banking loot");
        BankTask.AddDeposit(-1, -1);
        if(ItemRequirements != null)
        {
            for(var item : ItemRequirements)
            {
                BankTask.AddWithdraw(item.getKey(), item.getValue());
            }
        }

        if(BankingLocation != null)
        {
            BankTask.SetSpecificBank(BankingLocation);
        }

        BankTask.TaskPriority.set(2);
        BankTask.AcceptCondition = () -> !SALTask.IsAlive();

        Script.addNodes(Travel1, Travel2, SALTask, BankTask);
    }

    @Override
    public boolean onRestart(tpircSScript Script)
    {
        StartCycle(Script);
        return true;
    }

    @Override
    public boolean onStart(tpircSScript Script, int CycleCount)
    {
        StartCycle(Script);
        if(CycleCount == -1)
        {
            SetCycleType(CycleType.Endless);
        }
        else
        {
            SetCycleLimit(CycleCount);
        }
        return true;
    }
}
