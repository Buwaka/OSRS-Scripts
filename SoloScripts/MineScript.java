package SoloScripts;

import Cycles.MineCycle;
import Utilities.OSRSUtilities;
import Utilities.Scripting.ICycle;
import Utilities.Scripting.tpircSScript;
import com.google.gson.Gson;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;

@ScriptManifest(name = "SoloScripts.MineScript", description = "Mine stuff", author = "Semanresu", version = 1.0, category = Category.MINING, image = "")

public class MineScript extends tpircSScript
{
    static Area[]    MineArea1   = {new Area(3281, 3370, 3289, 3361)};
    static Area[]    MineArea2   = {
            new Area(3298, 3315, 3301, 3298), new Area(3302, 3279, 3295, 3282)};
    static Area[]    MineArea3   = {new Area(3080, 3425, 3087, 3422)};
    static String    TinName     = "Tin Rocks";
    static String    CopperName  = "Copper Rocks";
    static String    IronName    = "Iron Rocks";
    static String    CoalName    = "Coal Rocks";
    static MineCycle TinCycle    = new MineCycle("Tin ore", MineArea1, TinName);
    static MineCycle CopperCycle = new MineCycle("Copper ore", MineArea1, CopperName);
    static MineCycle IronCycle   = new MineCycle("Iron ore", MineArea2, IronName);
    static MineCycle CoalCycle   = new MineCycle("Coal", MineArea3, CoalName);

    public static void main(String[] args)
    {
        Gson   gson = OSRSUtilities.OSRSGsonBuilder.create();
        String json = gson.toJson(CoalCycle) + ",";
        json += gson.toJson(IronCycle) + ",";
        json += gson.toJson(CopperCycle) + ",";
        json += gson.toJson(TinCycle);

        System.out.println(json);
    }

    @Override
    public void onStart()
    {
//        CopperCycle.SetCycleType(ICycle.CycleType.byCount);
//        CopperCycle.GetCycleType().Count = 2;
//        AddCycle(CopperCycle);
//        TinCycle.SetCycleType(ICycle.CycleType.byCount);
//        TinCycle.GetCycleType().Count = 20;
//        AddCycle(TinCycle);
//        IronCycle.SetCycleType(ICycle.CycleType.Endless);
//        AddCycle(IronCycle);
        CoalCycle.SetCycleType(ICycle.CycleType.Endless);
        AddCycle(CoalCycle);


        Gson   gson = OSRSUtilities.OSRSGsonBuilder.create();
        String json = gson.toJson(CoalCycle);
        json += gson.toJson(IronCycle);
        json += gson.toJson(CopperCycle);
        json += gson.toJson(TinCycle);

        Logger.log(json);

        super.onStart();
    }
}
