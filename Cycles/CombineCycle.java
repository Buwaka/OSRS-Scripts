package Cycles;

import Cycles.SimpleTasks.Bank.BankItemsTask;
import Cycles.SimpleTasks.ItemProcessing.CombineTask;
import Cycles.SimpleTasks.TravelTask;
import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleCycle;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.utilities.Logger;

import javax.annotation.Nullable;
import java.io.Serializable;

public class CombineCycle extends SimpleCycle implements Serializable
{
    int source;
    @Nullable
    Integer sourceRatio;
    int target;
    @Nullable
    Integer targetRatio;
    transient CombineTask   combineTask;
    transient BankItemsTask bankItemsTask;

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
     * @return how many times we can perform this cycle
     */
    public int GetPossibleCycleCount()
    {
        Logger.log("CombineCycle:GetPossibleCycleCount: " + Bank.count(source) / sourceRatio + " " +
                   Bank.count(target) / targetRatio + " " + OSRSUtilities.InventorySpace / (sourceRatio + targetRatio));
        return Math.min(Bank.count(source) / sourceRatio, Bank.count(target) / targetRatio) /
               (OSRSUtilities.InventorySpace / (sourceRatio + targetRatio));
    }

    private void StartCycle(tpircSScript script)
    {
        if(!OSRSUtilities.CanReachBank())
        {
            TravelTask Travel = new TravelTask("", BankLocation.getNearest().getTile());
            Travel.SetTaskName("Travel To Bank For ItemRequirements");
            Travel.TaskPriority.set(0);
            Travel.CompleteCondition = OSRSUtilities::CanReachBank;
            script.addNodes(Travel);
        }

        bankItemsTask = new BankItemsTask("Grabbing items to combine");
        if(!Inventory.isEmpty())
        {
            bankItemsTask.DepositAll();
        }
        bankItemsTask.FillInventory(source, sourceRatio);
        bankItemsTask.FillInventory(target, targetRatio);


        combineTask                 = new CombineTask("Combining items", source, target);
        combineTask.AcceptCondition = () -> !bankItemsTask.isActive();

        script.addNodes(bankItemsTask, combineTask);
    }

    @Override
    public boolean onRestart(tpircSScript Script)
    {
        StartCycle(Script);
        return true;
    }


    @Override
    public boolean onStart(tpircSScript Script)
    {
        StartCycle(Script);
        return super.onStart(Script);
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
