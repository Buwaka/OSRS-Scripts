package IF.Scripts.AccountManagement;

import IF.DataBase.AccountDatabase;
import IF.Utilities.Scripting.Logger;
import org.dreambot.api.Client;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.AccountManager;
import org.dreambot.core.Instance;

@ScriptManifest(name = "RegisterNewAccount", description = "Register account in database", author = "Semanresu", version = 1.0, category = Category.MISC, image = "")
public class RegisterNewAccount extends AbstractScript
{
    private final Area LumbridgeArea = new Area(3191, 3254, 3271, 3179);

    @Override
    public void onStart(String... params)
    {
        super.onStart(params);
        Instance.getInstance().setMouseInputEnabled(true);
    }

    /**
     * @return
     */
    @Override
    public int onLoop()
    {
        if(LumbridgeArea.contains(Players.getLocal().getTile()))
        {
            if(AccountDatabase.RegisterAccount(AccountManager.getAccountNickname(),
                                               AccountManager.getAccountUsername(),
                                               Client.getPassword(),
                                               Instance.getProxyName(),
                                               System.getProperty("socksProxyHost"),
                                               System.getProperty("java.net.socks.username"),
                                               System.getProperty("java.net.socks.password")))
            {
                //AccountManager.updateAccount()
                Logger.log("Account Successfully registered in Database");
                this.stop();
            }
        }


        return 0;
    }
}
