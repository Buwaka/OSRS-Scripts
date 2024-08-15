package Cycles;

import Cycles.AdvanceTasks.OpenBankTask;
import Cycles.SimpleTasks.Bank.BankItemsTask;
import Cycles.SimpleTasks.ItemProcessing.InteractTask;
import Cycles.SimpleTasks.TravelTask;
import OSRSDatabase.ObjectDB;
import Utilities.Scripting.SimpleCycle;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.utilities.Sleep;

import java.io.Serializable;
import java.util.Arrays;


public class MineCycle extends SimpleCycle implements Serializable
{

    public  BankLocation PreferredBank = null;
    private Area[]       MiningArea;
    private int[]        Targets;

    public MineCycle(String name, Area[] TargetArea, String RockName)
    {
        super(name);
        MiningArea = TargetArea;
        Targets    = ObjectDB.GetObjectIDsByName(RockName);
    }

    /**
     * @param Script
     *
     * @return if cycle has successfully started
     */
    @Override
    public boolean onStart(tpircSScript Script)
    {
        StartCycle(Script);
        return super.onStart(Script);
    }

    private void StartCycle(tpircSScript Script)
    {
        //TODO check if we have a pickaxe, if not, pick one from the bank

        InteractTask interactTask = new InteractTask(GetName(), Targets);
        interactTask.SetTaskPriority(0);

        TravelTask TravelToMine = new TravelTask("Travel to Mine",
                                                 Arrays.stream(MiningArea)
                                                       .findAny()
                                                       .get()
                                                       .getRandomTile());
        TravelToMine.CompleteCondition = Inventory::isFull;
        TravelToMine.onReachedDestination.Subscribe(TravelToMine,
                                                    () -> Script.addNodes(new TravelTask(
                                                            "Travel to different Minespot",
                                                            Arrays.stream(MiningArea)
                                                                  .findAny()
                                                                  .get()
                                                                  .getRandomTile())));


        OpenBankTask OpenBank = new OpenBankTask();
        if(PreferredBank != null)
        {
            OpenBank.SetBankLocation(PreferredBank);
        }

        OpenBank.AcceptCondition = () -> !interactTask.isActive();

        BankItemsTask BankOres = new BankItemsTask("Bank Ores");
        BankOres.AddDepositAll();
        BankOres.AcceptCondition = () -> !OpenBank.isActive();

        Script.addNodes(BankOres, OpenBank, TravelToMine, interactTask);
    }

    @Override
    public boolean onEnd(tpircSScript Script)
    {
        if(Sleep.sleepUntil(() -> Bank.open(), 60000))
        {
            Bank.depositAllItems();
        }
        return super.onEnd(Script);
    }

    /**
     * @param Script
     *
     * @return
     */
    @Override
    public boolean onRestart(tpircSScript Script)
    {
        StartCycle(Script);
        return true;
    }
}
