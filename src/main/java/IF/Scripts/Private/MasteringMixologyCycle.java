package IF.Scripts.Private;


import IF.Logic.Generators.MinigameCycleGenerator;
import IF.Utilities.Scripting.IFScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

@ScriptManifest(name = "IF Mastering Mixology", description = "Mastering Mixology", author = "Semanresu", version = 1.0, category = Category.HERBLORE, image = "")
public class MasteringMixologyCycle extends IFScript
{

    @Override
    public void onStart()
    {
        AddCycle(new MinigameCycleGenerator.MasteringMixologyGenerator()::Generate);
        //AddCycle(HerbloreCycleGenerator::GetUnfPotionCycle);

        super.onStart();
    }
}
