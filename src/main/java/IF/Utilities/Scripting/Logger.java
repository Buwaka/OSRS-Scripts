package IF.Utilities.Scripting;

import java.awt.*;

public class Logger
{
    public static boolean isDreamBotPackageDefined()
    {
        try
        {
            Class.forName("org.dreambot.api.Client"); // Using a core DreamBot class
            return true; // Class found, package is defined
        } catch(ClassNotFoundException e)
        {
            return false; // Class not found, package likely not defined
        }
    }


    public static void debug(Object o)
    {
        if(isDreamBotPackageDefined())
        {
            org.dreambot.api.utilities.Logger.debug(o);
        }
        else
        {
            System.out.print(o);
        }
    }

    public static void error(Object o)
    {


        if(isDreamBotPackageDefined())
        {
            org.dreambot.api.utilities.Logger.error(o);
        }
        else
        {
            System.out.print(o + "\n");
        }

    }

    public static void info(Object o)
    {
        if(isDreamBotPackageDefined())
        {
            org.dreambot.api.utilities.Logger.info(o);
        }
        else
        {
            System.out.print(o + "\n");
        }
    }

    public static void log(Object o)
    {
        if(isDreamBotPackageDefined())
        {
            org.dreambot.api.utilities.Logger.log(o);
        }
        else
        {
            System.out.print(o + "\n");
        }
    }

    public static void log(Color color, Object o)
    {
        if(isDreamBotPackageDefined())
        {
            org.dreambot.api.utilities.Logger.log(color, o);
        }
        else
        {
            System.out.print(o + "\n");
        }
    }

    public static void warn(Object o)
    {
        if(isDreamBotPackageDefined())
        {
            org.dreambot.api.utilities.Logger.warn(o);
        }
        else
        {
            System.out.print(o + "\n");
        }
    }
}
