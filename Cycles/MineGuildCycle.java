package Cycles;

import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleCycle;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
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
    final int    MineHopperID       = 26674;
    final int    PayDirtID          = 12011;
    final int    BrokenStrutID      = 26670;
    final int    SackID             = 26688;
    final int    CrateID            = 357;
    final int    HammerID           = 2347;
    final Tile   SackTile           = new Tile(3750, 5659, 0);
    final Tile   HopperTile         = new Tile(3749, 5672, 0);
    final Tile   MineBackupTile     = new Tile(3737, 5652, 0);
    final Tile   CrateTile          = new Tile(3752, 5674, 0);
    final String OreVeinName        = "Ore vein";
    final int[]  SackSpaceWidgetIDs = {382, 6};
    final int[]  SackOreWidgetIDs   = {382, 5};
    MGState CurrentState = MGState.Mining;
    boolean Complete     = false;

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
     * @param Script
     *
     * @return if cycle has successfully started
     */
    @Override
    public boolean onStart(tpircSScript Script)
    {
        Init(Script);

        return super.onStart(Script);
    }

    private void Init(tpircSScript Script)
    {
        Logger.log("MineGuildCycle: Init: ");
        //TODO make sure we have a pickaxe
        if(!Inventory.isEmpty())
        {
            DropInventoryAtBank(Script);
        }

        if(GetSackSpace() <= OSRSUtilities.InventorySpace)
        {
            CurrentState = MGState.Deposit;
        }
        else
        {
            CurrentState = MGState.Mining;
        }
        Complete = false;
    }

    private static void DropInventoryAtBank(tpircSScript Script)
    {
        while(!Bank.isOpen())
        {
            Bank.open();
            Sleep.sleepTick();
        }
        Bank.depositAllItems();
        Script.onGameTick.WaitRandomTicks(6);
        Bank.close();
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

    /**
     * @return Whether the goal of this cycle has been met, based on CycleType
     */
    @Override
    public boolean isGoalMet()
    {
        return Complete;
    }

    /**
     * will be called once there are no active tasks anymore, aka a single cycle has been completed
     *
     * @param Script
     *
     * @return Cycle completed, ready for a restart
     */
    @Override
    public boolean isCycleComplete(tpircSScript Script)
    {
        return Complete;
    }

    /**
     * When a cycle has been completed, this will be called
     *
     * @param Script
     */
    @Override
    public boolean onRestart(tpircSScript Script)
    {
        Init(Script);
        return super.onRestart(Script);
    }

    /**
     * When all cycles have been completed and we want to do the cycle again, this is called
     *
     * @param Script
     */
    @Override
    public void onReset(tpircSScript Script)
    {
        Init(Script);
        super.onReset(Script);
    }

    /**
     * @param Script
     *
     * @return
     */
    @Override
    public int onLoop(tpircSScript Script)
    {
        Logger.log("MineGuildCycle: onLoop: State: " + CurrentState.name());
        switch(CurrentState)
        {
            case Mining ->
            {
                while(!Inventory.isFull())
                {
                    var target = GameObjects.closest(t -> t.getName().equals(OreVeinName) && t.canReach() &&
                                                          t.distance(Players.getLocal().getTile()) < 10);
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
                }
                CurrentState = MGState.Hopper;
            }
            case Hopper ->
            {
                var Hopper = GameObjects.closest(t -> t.canReach() && t.distance(Players.getLocal().getTile()) < 10 &&
                                                      t.getID() == MineHopperID);
                if(Inventory.contains(PayDirtID))
                {
                    if(Hopper != null)
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
                                    Hopper = GameObjects.closest(t -> t.canReach() && t.distance(Players.getLocal().getTile()) < 10 &&
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
                if(!Inventory.all(t -> t.getID() != PayDirtID).isEmpty())
                {
                    DropInventoryAtBank(Script);
                }
            }
            case Repair ->
            {
                if(Inventory.isFull())
                {
                    if(Inventory.contains(PayDirtID) && GetSackSpace() > 0)
                    {
                        CurrentState = MGState.Hopper;
                    }
                    else
                    {
                        if(Inventory.onlyContains(PayDirtID))
                        {
                            Inventory.drop(PayDirtID);
                        }
                        else
                        {
                            CurrentState = MGState.Deposit;
                        }
                    }
                }

                var Crate = Arrays.stream(GameObjects.getObjectsOnTile(CrateTile)).filter(t -> t.getID() ==
                                                                                               CrateID).findFirst();
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
                        Script.onGameTick.WaitRandomTicks(6);
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
}
