package Cycles;

import Cycles.SimpleTasks.Bank.InventoryCheckTask;
import Cycles.SimpleTasks.TravelTask;
import Utilities.Scripting.SimpleCycle;
import Utilities.Scripting.SimpleTask;
import Utilities.Scripting.tpircSScript;
import io.vavr.Tuple2;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.utilities.Logger;

import java.util.ArrayList;
import java.util.List;


public class SimpleProcessCycle extends SimpleCycle
{
    private List<Tuple2<Integer, Integer>> ItemRequirements = null;
    private SimpleTask                     ProcessTaskTemplate;
    private Area                           TargetArea;
    //private transient SimpleTask                    CurrentProcessTask;

    public SimpleProcessCycle(String name, SimpleTask processTaskTemplate, Area targetArea)
    {
        super(name);
        ProcessTaskTemplate = processTaskTemplate;
        TargetArea          = targetArea;
    }

    public void SetItemRequirements(List<Tuple2<Integer, Integer>> itemRequirements)
    {
        ItemRequirements = itemRequirements;
    }

    public void AddItemRequirements(Tuple2<Integer, Integer>... itemRequirements)
    {
        if(ItemRequirements == null)
        {
            ItemRequirements = new ArrayList<>();
        }
        ItemRequirements.addAll(List.of(itemRequirements));
    }

    private SimpleTask GetNewTask()
    {
        return ProcessTaskTemplate.Copy();
    }


    private void StartCycle(tpircSScript Script)
    {
        //CurrentProcessTask = GetNewTask();
        Script.addNodes(ProcessTaskTemplate);

        if(!TargetArea.contains(Players.getLocal().getTile()))
        {
            TravelTask Travel = new TravelTask("SimpleProcessCycle: Travel to Area",
                                               TargetArea.getRandomTile());
            Travel.CompleteCondition = () -> ProcessTaskTemplate.Ready();
            Script.addNodes(Travel);
        }

        Logger.log("SimpleProcessCycle: onStart: " + ItemRequirements);
        if(ItemRequirements != null && !ItemRequirements.isEmpty())
        {
            InventoryCheckTask Check = new InventoryCheckTask("SimpleProcessCycle: Check inventory",
                                                              ItemRequirements);
            if(!Check.HasRequirements())
            {
                Script.addNodes(Check);
            }
        }
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
     * When a cycle has been completed, this will be called
     *
     * @param Script
     */
    @Override
    public boolean onRestart(tpircSScript Script)
    {
        StartCycle(Script);
        return super.onRestart(Script);
    }

    /**
     * @param Script
     *
     * @return
     */
    @Override
    public int onLoop(tpircSScript Script)
    {
        //        if(CurrentProcessTask.isFinished())
        //        {
        //            CurrentProcessTask = GetNewTask();
        //            Script.addNodes(CurrentProcessTask);
        //        }

        return super.onLoop(Script);
    }
}
