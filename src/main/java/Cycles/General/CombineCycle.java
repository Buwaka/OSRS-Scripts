package Cycles.General;

import Cycles.Tasks.SimpleTasks.Bank.BankItemsTask;
import Cycles.Tasks.SimpleTasks.ItemProcessing.CombineTask;
import Cycles.Tasks.SimpleTasks.TravelTask;
import Utilities.OSRSUtilities;
import Utilities.Scripting.IFScript;
import Utilities.Scripting.SimpleCycle;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.utilities.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;


public class CombineCycle extends SimpleCycle implements Serializable
{
    private           int           source;
    @Nullable
    private           Integer       sourceRatio;
    private           int           target;
    @Nullable
    private           Integer       targetRatio;
    private           boolean       UseSkillingMenu = true;
    private transient CombineTask   combineTask;
    private transient BankItemsTask bankItemsTask;

    private CombineCycle()
    {
        super("");
        sourceRatio = 1;
        targetRatio = 1;
    }

    public CombineCycle(String name, int Source, int Target)
    {
        super(name);
        source      = Source;
        sourceRatio = 1;
        target      = Target;
        targetRatio = 1;
    }

    public CombineCycle(String name, int Source, int RatioSource, int Target, int RatioTarget)
    {
        super(name);
        source      = Source;
        sourceRatio = RatioSource;
        target      = Target;
        targetRatio = RatioTarget;
    }

    /**
     * @return how many times we can perform this cycle
     */
    public int GetPossibleCycleCount()
    {
        Logger.log("CombineCycle:GetPossibleCycleCount: " + Bank.count(source) / sourceRatio + " " +
                   Bank.count(target) / targetRatio + " " +
                   OSRSUtilities.InventorySpace / (sourceRatio + targetRatio));
        return Math.min(Bank.count(source) / sourceRatio, Bank.count(target) / targetRatio) /
               (OSRSUtilities.InventorySpace / (sourceRatio + targetRatio));
    }

    public void SetUseSkillingMenu(boolean Use)
    {
        UseSkillingMenu = Use;
    }

    /**
     * @return Whether we have the items necessary to do this combine cycle
     */
    public boolean isValid()
    {
        if(!Bank.isCached())
        {
            Logger.log("CombineCycle.isValid: Bank isn't cached, count as valid to check");
            return true;
        }

        if(Bank.count(source) >= sourceRatio && Bank.count(target) >= targetRatio)
        {
            return true;
        }
        return false;
    }

    /**
     * will be called once there are no active tasks anymore, aka a single cycle has been completed
     *
     * @param Script
     *
     * @return Cycle completed, ready for a restart
     */
    @Override
    public boolean isCycleComplete(IFScript Script)
    {
        boolean result =
                Inventory.count(source) < sourceRatio || Inventory.count(target) < targetRatio;
        Logger.log("CombineCycle: isCycleComplete: " + result);
        return result;
    }

    /**
     * @return
     */
    @Override
    public boolean isCycleFinished(IFScript Script)
    {
        boolean result =
                (Inventory.count(source) < sourceRatio || Inventory.count(target) < targetRatio) &&
                (Bank.count(source) < sourceRatio || Bank.count(target) < targetRatio);
        Logger.log("CombineCycle: isGoalMet: " + result);
        return result;
    }

    @Override
    public boolean onStart(IFScript Script)
    {
        StartCycle(Script);
        return super.onStart(Script);
    }

    private void StartCycle(IFScript script)
    {
        if(!OSRSUtilities.CanReachBank())
        {
            TravelTask Travel = new TravelTask("", BankLocation.getNearest().getTile());
            Travel.SetTaskName("CC Travel To Bank For ItemRequirements");
            Travel.SetTaskPriority(0);
            Travel.CompleteCondition = OSRSUtilities::CanReachBank;
            script.addNodes(Travel);
        }

        bankItemsTask = new BankItemsTask("Grabbing items to combine");
        if(!Inventory.isEmpty())
        {
            bankItemsTask.AddDepositAll();
        }

        bankItemsTask.FillInventory(source, sourceRatio, target, targetRatio);

        combineTask                 = new CombineTask("Combining items",
                                                      source,
                                                      target,
                                                      UseSkillingMenu);
        combineTask.AcceptCondition = () -> !bankItemsTask.isActive();

        script.addNodes(bankItemsTask, combineTask);
    }

    @Override
    public void onReset(IFScript Script)
    {
        bankItemsTask = null;
        combineTask   = null;
    }

    @Override
    public boolean onRestart(IFScript Script)
    {
        StartCycle(Script);
        return true;
    }

    //    public static void main(String[] args) throws IOException
    //    {
    //        Gson      gson       = new Gson();
    ////        final int PieShellID = 2313;
    ////        final int PastyDoughID = 1953;
    ////        var temp = new CombineCycle("Pie Shells", PieShellID, PastyDoughID);
    ////
    ////        String json = gson.toJson(temp);
    ////        var json2 = gson.fromJson(json, CombineCycle.class);
    ////
    ////        System.out.println(json2);
    //
    //        var               input  = new BufferedInputStream(Objects.requireNonNull(OSRSDataBase.class.getClassLoader().getResourceAsStream("CombineCycles.json")));
    //        InputStreamReader File   = new InputStreamReader(input);
    //        JsonReader        Reader = new JsonReader(File);
    //
    //        Reader.beginArray();
    //        var json2 = gson.fromJson(Reader, CombineCycle.class);
    //        Reader.endArray();
    //
    //
    //        System.out.println(json2);
    //    }

}
