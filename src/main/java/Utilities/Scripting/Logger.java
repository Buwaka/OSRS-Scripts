package Utilities.Scripting;

import java.awt.*;

public class Logger
{
    public static void debug(Object o)
    {
        org.dreambot.api.utilities.Logger.debug(o);
    }

    public static void error(Object o)
    {
        org.dreambot.api.utilities.Logger.error(o);
    }

    public static void info(Object o)
    {
        org.dreambot.api.utilities.Logger.info(o);
    }

    public static void log(Object o)
    {
        org.dreambot.api.utilities.Logger.log(o);
    }

    public static void log(Color color, Object o)
    {
        org.dreambot.api.utilities.Logger.log(color, o);
    }

    public static void warn(Object o)
    {
        org.dreambot.api.utilities.Logger.warn(o);
    }
}
