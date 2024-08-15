package Utilities.Scripting;

import Utilities.OSRSUtilities;
import Utilities.Patterns.Delegates.Delegate;
import com.google.gson.Gson;
import org.dreambot.api.data.GameState;
import org.dreambot.api.script.listener.GameStateListener;
import org.dreambot.api.utilities.AccountManager;
import org.dreambot.api.utilities.Logger;

import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;


public class PlayerConfig implements Serializable, GameStateListener
{
    @Serial
    private static final long serialVersionUID = -3370036818752925057L;

    private static final String ConfigFolderName = "IF-Scripts";
    //public static int Hash;
    public static        String Hash;

    static
    {
        init();
    }

    public Delegate onAccountChanged = new Delegate();

    public <T> T LoadState(String ObjectID, Class<T> type)
    {
        Path   ObjectPath     = GetObjectPath(ObjectID);

        if(!Files.exists(ObjectPath))
        {
            Logger.log("PlayerConfig: LoadState: File does not exist, : " + ObjectPath);
            return null;
        }

        String ObjectContents = null;
        try
        {
            ObjectContents = Files.readString(GetObjectPath(ObjectID));
        } catch(Exception e)
        {
            Logger.log("PlayerConfig: LoadState: Failed to write file with ID: " + ObjectID);
            Logger.log("PlayerConfig: LoadState: Path: " + ObjectPath);
            Logger.log("PlayerConfig: LoadState: Type: " + type);
        }

        if(ObjectContents == null)
        {
            Logger.log("PlayerConfig: LoadState: Could not read Object with ID: " + ObjectID);
            Logger.log("PlayerConfig: LoadState: Type: " + type);
            return null;
        }

        Gson gson = OSRSUtilities.OSRSGsonBuilder.create();

        try
        {
            return gson.fromJson(ObjectContents, type);
        } catch(Exception e)
        {
            Logger.log("PlayerConfig: LoadState: JSON could not be read: " + ObjectContents);
            Logger.log("PlayerConfig: LoadState: Type: " + type);
            return null;
        }
    }

    public static Path GetObjectPath(String ObjectID)
    {
        return Path.of(GetPlayerConfigFolder() + "\\" + ObjectID + ".json");
    }

    public static Path GetPlayerConfigFolder()
    {
        return Path.of(GetScriptConfigFolder() + "\\" + Hash);
    }

    public static Path GetScriptConfigFolder()
    {
        return Path.of(System.getProperty("scripts.path") + "\\" + ConfigFolderName);
    }

    public void SaveState(String ObjectID, Object object)
    {
        Path   ObjectPath = GetObjectPath(ObjectID);
        Gson   gson       = OSRSUtilities.OSRSGsonBuilder.create();
        String toWrite    = gson.toJson(object);

        try
        {
            Files.writeString(GetObjectPath(ObjectID), toWrite);
        } catch(Exception e)
        {
            Logger.log("PlayerConfig: SaveState: Failed to write file with ID: " + ObjectID);
            Logger.log("PlayerConfig: SaveState: object: " + object.toString());
            Logger.log("PlayerConfig: SaveState: Path: " + ObjectPath);
            Logger.log("PlayerConfig: SaveState: json: " + toWrite);
        }
    }

    private static void init()
    {
        RefreshHash();
        CreateConfigFolder();
        CreatePlayerConfigFolder();
    }

    private static void CreatePlayerConfigFolder()
    {
        var PlayerFolder = GetPlayerConfigFolder();
        if(!Files.isDirectory(PlayerFolder))
        {
            try
            {
                PlayerFolder = Files.createDirectory(PlayerFolder);
            } catch(Exception e)
            {
                Logger.log(
                        "PlayerConfig: Init: Something went wrong trying to create the config folder, exception: " +
                        e);
            }
        }

        Logger.log("PlayerConfig: Init: Folder: " + PlayerFolder);
    }

    private static void CreateConfigFolder()
    {
        var ScriptFolder = GetScriptConfigFolder();
        if(!Files.isDirectory(ScriptFolder))
        {
            try
            {
                Files.createDirectory(ScriptFolder);
            } catch(Exception e)
            {
                Logger.log(
                        "PlayerConfig: CreateConfigFolder: Something went wrong trying to create the global config folder, exception: " +
                        e);
            }
        }
    }

    /**
     * @param gameState
     */
    @Override
    public void onGameStateChange(GameState gameState)
    {
        if(!Hash.equals(AccountManager.getAccountHash()))
        {
            onAccountChanged.Fire();
            RefreshHash();
        }
    }

    private static void RefreshHash()
    {
        Hash = AccountManager.getAccountHash();
    }
}
