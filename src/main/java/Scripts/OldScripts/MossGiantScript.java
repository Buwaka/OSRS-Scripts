package Scripts.OldScripts;

import Cycles.General.CombatLootBankCycle;
import OSRSDatabase.MonsterDB;
import Utilities.OSRSUtilities;
import Utilities.Scripting.ICycle;
import Utilities.Scripting.IFScript;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

import java.util.AbstractMap;
import java.util.List;

@ScriptManifest(name = "SoloScripts.MossGiantScript", description = "go kill moss giants", author = "Semanresu", version = 1.0, category = Category.COMBAT, image = "")
public class MossGiantScript extends IFScript
{
    List<AbstractMap.SimpleEntry<Integer, Integer>> ItemRequirements = OSRSUtilities.CreateItemRequirements(
            556,
            3,
            554,
            1,
            563,
            1);
    CombatLootBankCycle                             Cycle            = new CombatLootBankCycle(
            "Moss Giant slaughter",
            new Area[]{
                    new Area(new Tile[]{
                            new Tile(3169, 9880, 0),
                            new Tile(3165, 9876, 0),
                            new Tile(3162, 9876, 0),
                            new Tile(3162, 9879, 0),
                            new Tile(3165, 9883, 0),
                            new Tile(3169, 9883, 0)}), new Area(new Tile[]{
                    new Tile(3164, 9906, 0),
                    new Tile(3165, 9904, 0),
                    new Tile(3159, 9898, 0),
                    new Tile(3156, 9900, 0),
                    new Tile(3156, 9902, 0),
                    new Tile(3153, 9903, 0),
                    new Tile(3154, 9909, 0),
                    new Tile(3158, 9908, 0)})},
            MonsterDB.GetMonsterIDsByName("Moss giant", false),
            ItemRequirements);

    @Override
    public void onStart()
    {
        Cycle.SetCycleType(ICycle.CycleType.Endless);
        AddCycle(Cycle);
        super.onStart();
    }
}
