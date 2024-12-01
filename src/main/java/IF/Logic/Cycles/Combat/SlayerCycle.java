package IF.Logic.Cycles.Combat;

import IF.Logic.Tasks.SimpleTasks.Bank.BankItemsTask;
import IF.Logic.Tasks.SimpleTasks.Combat.SlaughterTask;
import IF.Logic.Tasks.SimpleTasks.TravelTask;
import IF.Utilities.OSRSUtilities;
import IF.Utilities.Patterns.Delegates.Delegate2;
import IF.Utilities.Scripting.IScript;
import IF.Utilities.Scripting.Logger;
import IF.Utilities.Scripting.SimpleCycle;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.combat.CombatStyle;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.wrappers.interactive.NPC;

public class SlayerCycle extends SimpleCycle
{

    public  Delegate2<SimpleCycle, NPC> onKill              = new Delegate2<>();
    private boolean                     EscapeOnLowHealth   = true;
    private boolean                     EscapeOnLowSupplies = false;
    private int                         MinimumHealth       = 3;
    private int                         EatThreshold        = 5;
    private CombatStyle                 Style               = Combat.getCombatStyle();

    private SlaughterTask Slaughter    = null;
    private Area[]        TargetAreas  = null;
    private int[]         TargetIDs    = null;
    private TravelTask    Travel       = null;
    private boolean       ShouldEscape = false;
    private boolean       ReturnedToBank = false;

    public SlayerCycle(String Name, Area[] TargetAreas, int[] TargetIDs)
    {
        super(Name, null);
        this.TargetAreas = TargetAreas;
        this.TargetIDs   = TargetIDs;
    }

    public SlayerCycle(String Name, Area TargetArea, int[] TargetIDs)
    {
        super(Name, null);
        this.TargetAreas = new Area[]{TargetArea};
        this.TargetIDs   = TargetIDs;
    }

    public void setCombatStyle(CombatStyle style)
    {
        Style = style;
        if(Slaughter != null)
        {
            Slaughter.setCombatStyle(style);
        }
    }

    public void setEatThreshold(int eatThreshold)
    {
        EatThreshold = eatThreshold;
        if(Slaughter != null)
        {
            Slaughter.setEatThresholdCondition(EatThreshold);
        }
    }

    public void setEscapeOnLowHealth(boolean escapeOnLowHealth)
    {
        EscapeOnLowHealth = escapeOnLowHealth;
    }

    public void setEscapeOnLowSupplies(boolean escapeOnLowSupplies)
    {
        EscapeOnLowSupplies = escapeOnLowSupplies;
    }

    public void setMinimumHealth(int minimumHealth)
    {
        MinimumHealth = minimumHealth;
        if(Slaughter != null)
        {
            Slaughter.setMinimumHPCondition(minimumHealth);
        }
    }

    @Override
    public int onLoop(IScript Script)
    {
        if(Travel != null)
        {
            Logger.log("SlayerCycle: onLoop: Traveltask is active");
            Travel.Loop();
        }

        else if(ShouldEscape || Inventory.isFull())
        {
            Logger.log(
                    "SlayerCycle: onLoop: Inventory is fully or we should escape: " + ShouldEscape +
                    " " + Inventory.isFull());
            var banktask = BankItemsTask.FullDepositInventory();
            banktask.onComplete.Subscribe(this, () -> ReturnedToBank = true);
            GetScript().addNodes(banktask);
        }
        else if(Slaughter.isTargetInSight())
        {
            Logger.log("SlayerCycle: onLoop: slayer task loop");
            Slaughter.Loop();
        }
        else
        {
            Logger.log("SlayerCycle: onLoop: no target in sight, traveling to area");
            Travel = ToSlaughterArea();
        }

        return super.onLoop(Script);
    }

    @Override
    public boolean onRestart(IScript Script)
    {
        _start(Script);
        return super.onRestart(Script);
    }

    TravelTask ToSlaughterArea()
    {
        var travel = new TravelTask("Travel to Slaughter area",
                                    TargetAreas[OSRSUtilities.rand.nextInt(TargetAreas.length)].getRandomTile());
        travel.Init(GetScript());
        travel.onReachedDestination.Subscribe(this, () -> Travel = null);
        return travel;
    }

    @Override
    public boolean isCycleComplete(IScript Script)
    {
        return ReturnedToBank && isGoalMet();
    }

    @Override
    public boolean onStart(IScript Script)
    {
        _start(Script);

        return true;
    }

    private void _start(IScript Script)
    {
        Logger.log("SlayerCycle: Start");
        ReturnedToBank = false;
        Slaughter      = new SlaughterTask("Slaughter " + GetName(), TargetIDs);
        Slaughter.Init(Script);
        Slaughter.onStartTask(Script);

        Slaughter.setEatThresholdCondition(EatThreshold);
        Slaughter.setMinimumHPCondition(MinimumHealth);
        Slaughter.setCombatStyle(Style);

        Slaughter.onLowHealth.Subscribe(this, this::onLowHealth);
        Slaughter.onNoSupplies.Subscribe(this, this::onNoSupplies);
        Slaughter.onKill.Subscribe(this, this::onKill);

        Travel = ToSlaughterArea();
    }

    private Boolean onKill(NPC npc)
    {
        onKill.Fire(this, npc);
        return true;
    }

    private Boolean onLowHealth(Boolean hasSupplies)
    {
        Logger.log("SlayerCycle: onLowHealth: " + hasSupplies);
        if(EscapeOnLowHealth && !hasSupplies)
        {
            Logger.log("SlayerCycle: onLowHealth: Should escape");
            ShouldEscape = true;
        }

        return true;
    }

    private void onNoSupplies()
    {
        Logger.log("SlayerCycle: onNoSupplies:");
        if(EscapeOnLowSupplies)
        {
            Logger.log("SlayerCycle: onNoSupplies: Should escape");
            ShouldEscape = true;
        }
    }
}
