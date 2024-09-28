package Utilities.Patterns;

import Utilities.OSRSUtilities;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;

import java.awt.*;
import java.time.Duration;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Playtime
{
    public static long GetPlaytimeLong()
    {
        String result = GetPlaytime();
        long   res    = ParseTimePlayed(result);
        Logger.log(
                "GetPlaytimeLong: " + result + Duration.ofNanos(res).toHours() + " hours, " + res);
        return res;
    }


    public static String GetPlaytime()
    {
        int attempts    = 0;
        int maxAttempts = 10;

        while(Bank.isOpen() && attempts < maxAttempts)
        {
            Bank.close();
            attempts++;
        }
        attempts = 0;

        Logger.log("GetPlaytime: Current Tab: " + Tabs.getOpen());
        while(!Tabs.isOpen(Tab.QUEST) && attempts < maxAttempts)
        {
            Logger.log("GetPlaytime: Opening Quest tab");
            Tabs.open(Tab.QUEST);
            Sleep.sleepTicks(2);
            attempts++;
        }
        attempts = 0;

        var rand                   = OSRSUtilities.rand;
        var CharacterSummaryWidget = Widgets.get(629, 3);
        var TimePlayedWidget       = Widgets.get(712, 2, 100);

        if(CharacterSummaryWidget == null || !CharacterSummaryWidget.isVisible())
        {
            Logger.log("GetPlaytime: CharacterSummaryWidget is null, exiting");
            return "GetPlaytime: CharacterSummaryWidget is null, exiting";
        }

        while((TimePlayedWidget == null || !TimePlayedWidget.isVisible()) && attempts < maxAttempts)
        {
            Logger.log("GetPlaytime: TimePlayedWidget is null, trying to open CharacterSummary");

            var rect = CharacterSummaryWidget.getRectangle();
            Logger.log("GetPlaytime: rect: " + rect);
            Point randomPoint = new Point(rect.x + rand.nextInt(Math.min(1, rect.width)),
                                          rect.y + rand.nextInt(Math.min(1, rect.height)));

            Mouse.click(randomPoint);
            Sleep.sleepTicks(2);
            TimePlayedWidget = Widgets.get(712, 2, 100);

            attempts++;
        }
        if(TimePlayedWidget == null)
        {
            Logger.log("GetPlaytime: failed to get TimePlayedWidget too many times, exiting");
            return "GetPlaytime: failed to get TimePlayedWidget too many times, exiting";
        }

        var text = TimePlayedWidget.getText()
                                   .replace("Time Played:", "")
                                   .replaceAll("<\\/?[a-z][a-z0-9]*[^<>]*>|<!--.*?-->", "");
        attempts = 0;
        while(text.toLowerCase().contains("reveal") && attempts < maxAttempts)
        {
            Logger.log("GetPlaytime: Time played is not revealed, trying to reveal");
            var rect = TimePlayedWidget.getRectangle();
            Logger.log("GetPlaytime: rect: " + rect);
            Point randomPoint = new Point(rect.x + rand.nextInt(Math.min(1, rect.width)),
                                          rect.y + rand.nextInt(Math.min(1, rect.height)));
            Mouse.click(randomPoint);
            Sleep.sleepTicks(2);

            if(Dialogues.inDialogue())
            {
                Logger.log("GetPlaytime: dialogue prompt");
                Dialogues.clickOption(1);
                Sleep.sleepTicks(2);
            }

            TimePlayedWidget = Widgets.get(712, 2, 100);
            text             = TimePlayedWidget.getText()
                                               .replace("Time Played:", "")
                                               .replaceAll("<\\/?[a-z][a-z0-9]*[^<>]*>|<!--.*?-->",
                                                           "");
            attempts++;
        }

        if(attempts >= maxAttempts)
        {
            Logger.log("GetPlaytime: failed to reveal time played, exiting");
            return "GetPlaytime: failed to reveal time played, exiting";
        }

        return text;
    }

    /**
     * @param text
     *
     * @return time in nanos
     */
    public static long ParseTimePlayed(String text)
    {
        final String Minutes = "minute";
        final String Hours   = "hour";
        final String Days    = "day";

        String[] parts = text.split(",");

        long total = 0;
        for(var part : parts)
        {
            Scanner s = new Scanner(part);
            if(!s.hasNextInt())
            {
                Logger.log("ParseTimePlayed: part doesn't have a number '" + part + "'");
                continue;
            }

            int number = s.nextInt();

            if(part.contains(Minutes))
            {
                total += TimeUnit.MINUTES.toNanos(number);
            }
            else if(part.contains(Hours))
            {
                total += TimeUnit.HOURS.toNanos(number);
            }
            else if(part.contains(Days))
            {
                total += TimeUnit.DAYS.toNanos(number);
            }
            else
            {
                Logger.log("ParseTimePlayed: couldn't parse part '" + part + "'");
            }
        }

        return total;
    }
}
