package Cycles;

import Cycles.AdvanceTasks.OpenBankTask;
import Cycles.SimpleTasks.Bank.BankItemsTask;
import Cycles.SimpleTasks.Skill.MineTask;
import Cycles.SimpleTasks.TravelTask;
import OSRSDatabase.OSRSDataBase;
import Utilities.Scripting.SimpleCycle;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Sleep;

import java.io.Serializable;
import java.util.Arrays;

public class MineCycle extends SimpleCycle implements Serializable
{

    public BankLocation PreferredBank = null;
    private Area[] MiningArea;
    private Tile   Checkpoint = null;
    private int[]  Targets;

    public MineCycle(String name, Area[] TargetArea, String RockName)
    {
        super(name);
        MiningArea = TargetArea;
        Targets    = OSRSDataBase.GetObjectIDsByName(RockName);
    }

    public MineCycle(String name, Area[] TargetArea, Tile Checkpoint, String RockName)
    {
        super(name);
        MiningArea = TargetArea;
        Targets    = OSRSDataBase.GetObjectIDsByName(RockName);
        this.Checkpoint = Checkpoint;
    }

    private void StartCycle(tpircSScript Script)
    {
        //TODO check if we have a pickaxe, if not, pick one from the bank

        MineTask mineTask = new MineTask(GetName(), Targets);
        mineTask.TaskPriority.set(0);

        TravelTask TravelToMine = new TravelTask("Travel to Mine",
                                                 Arrays.stream(MiningArea).findAny().get().getRandomTile());
        TravelToMine.CompleteCondition = Inventory::isFull;
        TravelToMine.onReachedDestination.Subscribe(TravelToMine, () -> Script.addNodes(new TravelTask("Travel to different Minespot",
                                                                                         Arrays.stream(MiningArea).findAny().get().getRandomTile())));

        if(Checkpoint != null)
        {
//            TravelTask TravelToLadder = new TravelTask("Travel to Ladder", Checkpoint);
//            TravelToLadder.AcceptCondition = () -> !mineTask.isActive();
//            TravelToLadder.TaskPriority.set(-1);
//            TravelToLadder.CompleteCondition = () -> {
//                if(!Players.getLocal().isMoving())
//                {
//                    return Sleep.sleepUntil(() -> GameObjects.closest("Ladder").interact(), 20000) && !Players.getLocal().isMoving();
//                }
//return false;
//            };
//            Script.addNodes(TravelToLadder);
        }


        OpenBankTask OpenBank = new OpenBankTask();
        if(PreferredBank != null)
        {
            OpenBank.SetBankLocation(PreferredBank);
        }

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
