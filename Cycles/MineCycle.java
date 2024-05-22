package Cycles;

import Cycles.AdvanceTasks.OpenBankTask;
import Cycles.SimpleTasks.Bank.BankItemsTask;
import Cycles.SimpleTasks.Skill.MineTask;
import Cycles.SimpleTasks.TravelTask;
import Database.OSRSDataBase;
import Utilities.Scripting.SimpleCycle;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.map.Area;

import java.io.Serializable;
import java.util.Arrays;

public class MineCycle extends SimpleCycle implements Serializable
{
    private Area[] MiningArea;
    private int[]  Targets;

    public MineCycle(String name, Area[] TargetArea, String RockName)
    {
        super(name);
        MiningArea = TargetArea;
        Targets    = OSRSDataBase.GetObjectIDsByName(RockName);
    }

    private void StartCycle(tpircSScript Script)
    {
        //TODO check if we have a pickaxe, if not, pick one from the bank

        MineTask mineTask = new MineTask(GetName(), Targets);
        mineTask.TaskPriority.set(0);

        TravelTask TravelToMine = new TravelTask("Travel to Mine",
                                                 Arrays.stream(MiningArea).findAny().get().getRandomTile());
        TravelToMine.CompleteCondition = Inventory::isFull;
        TravelToMine.onReachedDestination.Subscribe(() -> Script.addNodes(new TravelTask("Travel to different Minespot",
                                                                                         Arrays.stream(MiningArea).findAny().get().getRandomTile())));


        OpenBankTask OpenBank = new OpenBankTask();
        OpenBank.AcceptCondition = () -> !mineTask.isActive();

        BankItemsTask BankOres = new BankItemsTask("Bank Ores");
        BankOres.DepositAll();
        BankOres.AcceptCondition = () -> !OpenBank.isActive();

        Script.addNodes(BankOres, OpenBank, TravelToMine, mineTask);
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
