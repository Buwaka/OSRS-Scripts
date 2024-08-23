package Cycles.CycleGenerators;

import Cycles.General.InteractCycle;
import Cycles.General.InteractOnPositionCycle;
import Cycles.Skilling.SmeltCycle;
import Cycles.Tasks.SimpleTasks.Bank.BankItemsTask;
import Cycles.Tasks.SimpleTasks.Bank.GETask;
import Cycles.Tasks.SimpleTasks.ItemProcessing.InteractTask;
import Cycles.Tasks.SimpleTasks.Misc.EquipmentTask;
import OSRSDatabase.*;
import Utilities.ECycleTags;
import Utilities.GrandExchange.GEInstance;
import Utilities.GrandExchange.Orders.MarketBuyOrder;
import Utilities.OSRSUtilities;
import Utilities.Requirement.LevelRequirement;
import Utilities.Requirement.MemberRequirement;
import Utilities.Requirement.QuestRequirement;
import Utilities.Scripting.SimpleCycle;
import Utilities.Scripting.tpircSScript;
import com.google.gson.Gson;
import io.vavr.Tuple2;
import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.quest.book.PaidQuest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Logger;

import java.util.*;

public class CycleLibrary
{
    // Json Paths
    protected static tpircSScript OwnerScript            = null;

    static public void init(tpircSScript owner)
    {
        OwnerScript = owner;
    }

}
