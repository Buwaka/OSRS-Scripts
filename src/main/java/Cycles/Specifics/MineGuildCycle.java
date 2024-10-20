package Cycles.Specifics;

import Utilities.OSRSUtilities;
import Utilities.Scripting.IFScript;
import Utilities.Scripting.SimpleCycle;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;

import java.util.Arrays;
import java.util.Scanner;

public class MineGuildCycle extends SimpleCycle
{
    static final int     MineHopperID       = 26674;
    static final int     PayDirtID          = 12011;
    static final int     BrokenStrutID      = 26670;
    static final int     SackID             = 26688;
    static final int     CrateID            = 357;
    static final int     HammerID           = 2347;
    static final int     CoalBagID          = 12019;
    static final int     GemBagID           = 12020;
    static final int     CoalID             = 453;
    final        String  BagEmptyAction     = "Empty";
    final        Tile    SackTile           = new Tile(3750, 5659, 0);
    final        Tile    HopperTile         = new Tile(3749, 5672, 0);
    final        Tile    MineBackupTile     = new Tile(3737, 5652, 0);
    final        Tile    CrateTile          = new Tile(3752, 5674, 0);
    final        String  OreVeinName        = "Ore vein";
    final        int[]   SackSpaceWidgetIDs = {382, 6};
    final        int[]   SackOreWidgetIDs   = {382, 5};
    final        int     MaxHopperAttempts  = 3;
    transient    MGState CurrentState       = MGState.Mining;
    transient    boolean Complete           = false;
    int HopperAttempts = 0;

    enum MGState
    {
        Mining,
        Hopper,
        Repair,
        Deposit
    }

    public MineGuildCycle(String name)
    {
        super(name);
    }

    /**
     * will be called once there are no active tasks anymore, aka a single cycle has been completed
     *
     * @param Script
     *
     * @return Cycle completed, ready for a restart
     */
    @Override
    public boolean isCycleComplete(IFScript Script)
    {
        return Complete;
    }

    /**
     * @param Script
     *
     * @return if cycle has successfully started
     */
    @Override
    public boolean onStart(IFScript Script)
    {
        Init(Script);

        return super.onStart(Script);
    }

    private void Init(IFScript Script)
    {
        Logger.log("MineGuildCycle: Init: ");
        //TODO make sure we have a pickaxe

        if(GetSackSpace() <= OSRSUtilities.InventorySpace)
        {
            CurrentState = MGState.Deposit;
        }
        else
        {
            CurrentState = MGState.Mining;
        }

        if(!Inventory.isEmpty())
        {
            DropInventoryAtBank(Script);
        }

        Complete = false;
    }

    public int GetSackSpace()
    {
        var SackSpaceWidget = Widgets.get(SackSpaceWidgetIDs);
        if(SackSpaceWidget == null)
        {
            return 108;
        }
        Logger.log("MineGuildCycle: GetSackSpace: " + SackSpaceWidget.getText());
        Scanner scan = new Scanner(SackSpaceWidget.getText()).useDelimiter("[^0-9]+");
        return scan.nextInt();
    }

    private void DropInventoryAtBank(IFScript Script)
    {
        while(!Bank.isOpen())
        {
            Bank.open();
            Sleep.sleepTick();
        }

        if(Inventory.contains(GemBagID))
        {
            Inventory.get(GemBagID).interact(BagEmptyAction);
            Script.onGameTick.WaitRandomTicks(2);
        }

        if(Inventory.contains(CoalBagID))
        {
            Inventory.get(CoalBagID).interact(BagEmptyAction);
            Script.onGameTick.WaitRandomTicks(2);
        }

        Bank.depositAllItems();
        Script.onGameTick.WaitRandomTicks(3);

        if(Bank.contains(GemBagID) && CurrentState != MGState.Deposit)
        {
            Bank.withdraw(GemBagID);
            Script.onGameTick.WaitRandomTicks(2);
        }

        if(Bank.contains(CoalBagID) && CurrentState == MGState.Deposit)
        {
            Bank.withdraw(CoalBagID);
            Script.onGameTick.WaitRandomTicks(2);
        }

        Bank.close();
    }

    /**
     * @param Script
     *
     * @return
     */
    @Override
    public int onLoop(IFScript Script)
    {
        Logger.log("MineGuildCycle: onLoop: State: " + CurrentState.name());
        switch(CurrentState)
        {
            case Mining ->
            {
                while(!Inventory.isFull())
                {
                    var target = GameObjects.closest(t -> t.getName().equals(OreVeinName) &&
                                                          t.canReach() &&
                                                          t.distance(Players.getLocal().getTile()) <
                                                          10);
                    if(target == null)
                    {
                        Walking.walk(MineBackupTile);
                        Script.onGameTick.WaitRandomTicks(6);
                        return super.onLoop(Script);
                    }
                    while(target.exists())
                    {
                        if(!Players.getLocal().isAnimating() && !Players.getLocal().isMoving())
                        {
                            target.interact();
                        }
                        else
                        {
                            Script.onGameTick.WaitTicks(3);
                        }
                    }

                    if(!Inventory.all(t -> t.getName().toLowerCase().contains("uncut")).isEmpty() &&
                       Inventory.contains(GemBagID))
                    {
                        Inventory.get(GemBagID).interact();
                    }
                }
                CurrentState = MGState.Hopper;
            }
            case Hopper ->
            {
                var Hopper = GameObjects.closest(t -> t.canReach() &&
                                                      t.distance(Players.getLocal().getTile()) <
                                                      10 && t.getID() == MineHopperID);
                if(Inventory.contains(PayDirtID))
                {
                    Logger.log("MineGuildCycle: OnLoop: InDialogue + Dialogue " +
                               Dialogues.inDialogue() + Dialogues.getNPCDialogue());
                    if(Dialogues.inDialogue() && Hopper != null &&
                       Hopper.distance(Players.getLocal()) < 4)
                    { // Dialogues.getNPCDialogue().toLowerCase().contains("you can put more in once") bad, language specific
                        if(HopperAttempts > MaxHopperAttempts)
                        {
                            HopperAttempts = 0;
                            CurrentState   = MGState.Repair;
                            return super.onLoop(Script);
                        }
                        HopperAttempts++;
                    }
                    else if(Hopper != null)
                    {
                        if(!Players.getLocal().isMoving())
                        {
                            Logger.log("MineGuildCycle: SackSpace: " + GetSackSpace());
                            while(!Hopper.canReach())
                            {
                                Walking.walk(HopperTile);
                                Sleep.sleepTick();
                                Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 10000);
                            }

                            while(Inventory.contains(PayDirtID) && GetSackSpace() > 0)
                            {
                                if(!Hopper.exists())
                                {
                                    Hopper = GameObjects.closest(t -> t.canReach() &&
                                                                      t.distance(Players.getLocal()
                                                                                        .getTile()) <
                                                                      10 &&
                                                                      t.getID() == MineHopperID);
                                    if(Hopper == null)
                                    {
                                        Logger.log("MineGuildCycle: onLoop: Hopper is null");
                                        return super.onLoop(Script);
                                    }
                                }
                                Hopper.interact();
                                Script.onGameTick.WaitRandomTicks(6);
                            }
                        }
                        else
                        {
                            return super.onLoop(Script);
                        }
                    }
                    else
                    {
                        Walking.walk(HopperTile);
                        Script.onGameTick.WaitRandomTicks(6);
                    }
                }


                if(GetSackSpace() > OSRSUtilities.InventorySpace * 2)
                {
                    CurrentState = MGState.Mining;
                }
                else if(!Inventory.contains(PayDirtID) || GetSackSpace() < 4)
                {
                    if(GameObjects.all(BrokenStrutID).isEmpty())
                    {
                        CurrentState = MGState.Deposit;
                    }
                    else
                    {
                        CurrentState = MGState.Repair;
                    }
                }
                if(!Inventory.all(t -> t.getID() != PayDirtID && t.getID() != GemBagID &&
                                       t.getID() != CoalBagID).isEmpty())
                {
                    DropInventoryAtBank(Script);
                }
            }
            case Repair ->
            {
                if(Inventory.isFull())
                {
                    if(Inventory.onlyContains(PayDirtID))
                    {
                        Inventory.drop(PayDirtID);
                    }
                    else if(Inventory.contains(PayDirtID) && GetSackSpace() > 0)
                    {
                        CurrentState = MGState.Hopper;
                        return super.onLoop(Script);
                    }
                    else
                    {
                        CurrentState = MGState.Deposit;
                        return super.onLoop(Script);
                    }
                }

                var Crate = Arrays.stream(GameObjects.getObjectsOnTile(CrateTile))
                                  .filter(t -> t.getID() == CrateID)
                                  .findFirst();
                if(!Inventory.contains(HammerID))
                {
                    if(Crate.isPresent())
                    {
                        Crate.get().interact();
                        Sleep.sleepTick();
                        Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 10000);
                    }
                    else
                    {
                        Walking.walk(CrateTile);
                        Sleep.sleepTick();
                        Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 10000);
                        return super.onLoop(Script);
                    }
                }

                var BrokenStrut = GameObjects.closest(BrokenStrutID);
                if(Inventory.contains(HammerID) && BrokenStrut != null)
                {
                    BrokenStrut.interact();
                    Script.onGameTick.WaitRandomTicks(6);
                }
                else if(BrokenStrut == null)
                {
                    CurrentState = MGState.Deposit;
                }
            }
            case Deposit ->
            {
                Logger.log("MineGuildCycle: SackOres: " + GetSackOres());
                if(GetSackOres() == 0)
                {
                    Complete = true;
                    return super.onLoop(Script);
                }

                if(Inventory.contains(PayDirtID) && GetSackSpace() > 4)
                {
                    CurrentState = MGState.Hopper;
                    return super.onLoop(Script);
                }

                var Sack = GameObjects.closest(SackID);
                if(Sack != null)
                {
                    while(!Inventory.isFull())
                    {
                        if(!Sack.exists() || GetSackOres() == 0)
                        {
                            return super.onLoop(Script);
                        }
                        Sack.interact();
                        Script.onGameTick.WaitRandomTicks(5);
                        if(Inventory.contains(CoalBagID) && Inventory.contains(CoalID))
                        {
                            Inventory.get(CoalBagID).interact();
                            Script.onGameTick.WaitRandomTicks(2);
                        }
                    }
                    DropInventoryAtBank(Script);
                }
                else
                {
                    Walking.walk(SackTile);
                    Sleep.sleepTick();
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 10000);
                }
            }
        }
        return super.onLoop(Script);
    }

    public int GetSackOres()
    {
        var SackOreWidget = Widgets.get(SackOreWidgetIDs);
        if(SackOreWidget == null)
        {
            return 0;
        }
        Logger.log("MineGuildCycle: GetSackOres: " + SackOreWidget.getText());
        Scanner scan = new Scanner(SackOreWidget.getText()).useDelimiter("[^0-9]+");
        return scan.nextInt();
    }

    /**
     * When all cycles have been completed and we want to do the cycle again, this is called
     *
     * @param Script
     */
    @Override
    public void onReset(IFScript Script)
    {
        Init(Script);
        super.onReset(Script);
    }

    /**
     * When a cycle has been completed, this will be called
     *
     * @param Script
     */
    @Override
    public boolean onRestart(IFScript Script)
    {
        Init(Script);
        return super.onRestart(Script);
    }
}
