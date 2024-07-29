package Cycles;


import Cycles.AdvanceTasks.SlaughterAndLoot;
import Cycles.SimpleTasks.Bank.BankItemsTask;
import Cycles.SimpleTasks.Bank.GetCombatRationsTask;
import Cycles.SimpleTasks.Combat.RestoreFullHealthTask;
import Cycles.SimpleTasks.TravelTask;
import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleCycle;
import Utilities.Scripting.tpircSScript;
import com.fasterxml.jackson.annotation.JsonTypeName;
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
import java.util.concurrent.atomic.AtomicInteger;

@JsonTypeName("CombatLootBankCycle")
public class CombatLootBankCycle extends SimpleCycle
{

    public  AtomicInteger                                   HPtoCarry        = new AtomicInteger(
            Skills.getRealLevel(Skill.HITPOINTS) / 2);
    private Area[]                                          KillingArea;
    private int[]                                           Targets;
    private BankLocation                                    BankingLocation  = null;
    private List<AbstractMap.SimpleEntry<Integer, Integer>> ItemRequirements = null;
    private int[]                                           IgnoreLoot       = null;
    private boolean                                         PrayBones        = false;

    public CombatLootBankCycle(String Name, Area[] KillingArea, int[] Targets, List<AbstractMap.SimpleEntry<Integer, Integer>> ItemRequirements, BankLocation BankLoc)
    {
        super(Name);
        this.KillingArea      = KillingArea;
        this.Targets          = Targets;
        this.BankingLocation  = BankLoc;
        this.ItemRequirements = ItemRequirements;
    }

    public CombatLootBankCycle(String Name, Area[] KillingArea, int[] Targets)
    {
        super(Name);
        this.KillingArea = KillingArea;
        this.Targets     = Targets;
    }

    public CombatLootBankCycle(String Name, Area[] KillingArea, int[] Targets, List<AbstractMap.SimpleEntry<Integer, Integer>> ItemRequirements)
    {
        super(Name);
        this.KillingArea      = KillingArea;
        this.Targets          = Targets;
        this.ItemRequirements = ItemRequirements;
    }

    public CombatLootBankCycle(String Name, Area[] KillingArea, int[] Targets, BankLocation BankLoc)
    {
        super(Name);
        this.KillingArea     = KillingArea;
        this.Targets         = Targets;
        this.BankingLocation = BankLoc;
    }

    public void SetPray(boolean Pray)
    {
        PrayBones = Pray;
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

    public void setIgnoreLoot(int... ignoreLoot)
    {
        IgnoreLoot = ignoreLoot;
    }

    @Override
    public boolean onStart(tpircSScript Script)
    {
        StartCycle(Script);
        return super.onStart(Script);
    }

    @Override
    public boolean onRestart(tpircSScript Script)
    {
        StartCycle(Script);
        return true;
    }

    void StartCycle(tpircSScript Script)
    {
        TravelTask Travel1 = new TravelTask("Travel To Killing Area",
                                            Arrays.stream(KillingArea)
                                                  .findAny()
                                                  .get()
                                                  .getRandomTile());
        Travel1.SetTaskPriority(2);
        Travel1.CompleteCondition = () -> Arrays.stream(KillingArea)
                                                .anyMatch(t -> t.contains(Players.getLocal()
                                                                                 .getTile()));
        SlaughterAndLoot SALTask = new SlaughterAndLoot("Killing and Looting",
                                                        KillingArea,
                                                        Targets,
                                                        ItemRequirements);
        SALTask.setIgnoreLoot(IgnoreLoot);
        SALTask.setPrayBones(PrayBones);

        if(!OSRSUtilities.CheckInventory(ItemRequirements, false))
        {
            Logger.log("NotReady for combat, first go to bank");
            TravelTask Travel3 = TravelToBank();
            Travel3.SetTaskName("CLBCTravel To Bank For ItemRequirements");
            Travel3.SetTaskPriority(0);
            Travel3.CompleteCondition = OSRSUtilities::CanReachBank;
            Script.addNodes(Travel3);

            BankItemsTask Setup = new BankItemsTask("Banking ItemRequirements");
            if(!Inventory.isEmpty())
            {
                Setup.AddDepositAll();
            }
            for(var item : ItemRequirements)
            {
                Setup.AddWithdraw(item.getKey(), item.getValue());
            }
            if(BankingLocation != null)
            {
                Setup.SetSpecificBank(BankingLocation);
            }
            Setup.SetTaskPriority(-1);
            Script.addNodes(Setup);
        }

        Logger.log(HPtoCarry.get() + " " + OSRSUtilities.InventoryHPCount());
        if(OSRSUtilities.InventoryHPCount() < HPtoCarry.get())
        {
            GetCombatRationsTask Rations = new GetCombatRationsTask("Get Rations", HPtoCarry.get());
            Rations.SetTaskPriority(-1);
            Script.addNodes(Rations);
        }

        if(Players.getLocal().getHealthPercent() < (Skills.getRealLevel(Skill.HITPOINTS) / 2))
        {
            RestoreFullHealthTask Healup = new RestoreFullHealthTask("FullHeal");
            Healup.SetTaskPriority(-2);
            Script.addNodes(Healup);
        }

        TravelTask Travel2 = TravelToBank();
        Travel2.SetTaskPriority(2);
        Travel2.AcceptCondition   = () -> !SALTask.isActive();
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

        BankTask.SetTaskPriority(2);
        BankTask.AcceptCondition = () -> !SALTask.isActive();

        Script.addNodes(Travel1, Travel2, SALTask, BankTask);
    }
}
