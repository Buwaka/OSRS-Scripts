package SoloScripts;

import Cycles.CycleLibrary;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;

@ScriptManifest(name = "SoloScripts.GEAreaWoodCuttingFireMakingPyreScript", description = "Cut wood, Create Pyre Logs, sell on GE, repeat", author = "Semanresu", version = 1.0, category = Category.FIREMAKING, image = "")
public class GEAreaWoodCuttingFireMakingPyreScript extends tpircSScript
{
    final int SacredOilID  = 3430;
    final int SacredOil2ID = 3434;
    boolean SellPyreLogs = true;
    private Boolean CycleLifeTimeCheck = true;

    private void CreateCycles()
    {
        AddCycle(CycleLibrary::FireMakingWoodCuttingTraining);


        // AddCycle(CycleLibrary.GetWoodCuttingCycle(WoodDB.WoodType.Yew));
    }

    //    /**
    //     *
    //     */
    //    @Override
    //    public void onResume()
    //    {
    //        this.StopCurrentCycle();
    //
    //        super.onResume();
    //    }

    public static void main(String[] args)
    {
        //        printVersion(org.apache.commons.math3.util.MathArrays.class);
        //        printVersion(org.apache.hc.client5.http.impl.classic.HttpClients.class);
        System.out.print(CycleLibrary.FireMakingWoodCuttingTraining().toString());

    }

    public static void printVersion(Class<?> clazz)
    {
        Package p = clazz.getPackage();
        Logger.log(clazz.getName() + " " + p.getImplementationTitle() + " " +
                   p.getImplementationVersion() + " " + p.getImplementationVendor());
    }

    /**
     *
     */
    @Override
    public void onStart()
    {
        CreateCycles();
        super.onStart();
    }


}
