package IF.Scripts.Private;


import IF.Logic.Generators.HerbloreCycleGenerator;
import IF.Utilities.Scripting.IFScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

@ScriptManifest(name = "SoloScripts.CleanHerbsScript", description = "Clean Herbs", author = "Semanresu", version = 1.0, category = Category.HERBLORE, image = "")
public class CleanHerbsScript extends IFScript
{


    /**
     *
     */
    @Override
    public void onStart()
    {
//        var test = (EncryptionTest) EncryptedClassLoader.GetInstance(EncryptionTest.class);
//        Logger.log(test.DoAThing());
        AddCycle(HerbloreCycleGenerator::GetCleanGrimyHerbsCycle);
        AddCycle(HerbloreCycleGenerator::GetUnfPotionCycle);

        super.onStart();
    }
}
