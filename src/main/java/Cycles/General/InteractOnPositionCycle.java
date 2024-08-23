package Cycles.General;

import Cycles.Tasks.SimpleTasks.Bank.BankItemsTask;
import Cycles.Tasks.SimpleTasks.ItemProcessing.InteractInventoryTask;
import Cycles.Tasks.SimpleTasks.TravelTask;
import OSRSDatabase.WoodDB;
import Utilities.Scripting.SimpleCycle;
import Utilities.Scripting.tpircSScript;
import io.vavr.Function2;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class InteractOnPositionCycle extends SimpleCycle
{
    public            Function2<Tile, Tile, Boolean> TileChecker                 = null;
    private           String                         Action                      = null;
    private           int[]                          SourceItemID;
    private           Integer                        Tool                        = null;
    private           List<Tile>                     PossibleTiles               = null;
    private           Function<Tile, List<Tile>>     GeneratePossibleTiles       = null;
    private           boolean                        WaitInteract                = false;
    private           boolean                        InOrder                     = true;
    private           boolean                        WithdrawSourceItemsFromBank = true;
    private transient InteractInventoryTask          InteractTask                = null;
    private transient BankItemsTask                  BankTask                    = null;
    private transient boolean                        InteractComplete            = false;
    private transient boolean                        Complete;
    private transient Thread                         TileListener;
    private transient Semaphore                      TileReady                   = new Semaphore(1);
    private transient Tile                           TargetTile                  = null;
    private transient Iterator<Tile>                 TileIterator;
    private transient long                           InteractTimeout;
    private transient int                            Timout                      = 10000;
    private transient TravelTask                     Traveltask                  = null;

    public InteractOnPositionCycle(String name, Tile[] tiles, int... ItemIDs)
    {
        super(name);
        SourceItemID  = ItemIDs;
        PossibleTiles = new ArrayList<>(List.of(tiles));
    }

    public InteractOnPositionCycle(String name, int Tool, Tile[] tiles, int... ItemIDs)
    {
        super(name);
        SourceItemID  = ItemIDs;
        this.Tool     = Tool;
        PossibleTiles = new ArrayList<>(List.of(tiles));
    }

    public InteractOnPositionCycle(String name, String action, Tile[] tiles, int... ItemIDs)
    {
        super(name);
        Action        = action;
        SourceItemID  = ItemIDs;
        PossibleTiles = new ArrayList<>(List.of(tiles));
    }

    public InteractOnPositionCycle(String name, Function<Tile, List<Tile>> tiles, int... ItemIDs)
    {
        super(name);
        SourceItemID          = ItemIDs;
        GeneratePossibleTiles = tiles;
    }

    public InteractOnPositionCycle(String name, int Tool, Function<Tile, List<Tile>> tileGenerator, int... ItemIDs)
    {
        super(name);
        SourceItemID          = ItemIDs;
        this.Tool             = Tool;
        GeneratePossibleTiles = tileGenerator;
    }

    public InteractOnPositionCycle(String name, String action, Function<Tile, List<Tile>> tileGenerator, int... ItemIDs)
    {
        super(name);
        Action                = action;
        SourceItemID          = ItemIDs;
        GeneratePossibleTiles = tileGenerator;
    }

    public void setInOrder(boolean inOrder)
    {
        InOrder = inOrder;
    }

    public void setWithdrawSourceItemsFromBank(boolean withdrawSourceItemsFromBank)
    {
        WithdrawSourceItemsFromBank = withdrawSourceItemsFromBank;
    }

    /**
     * will be called once there are no active tasks anymore, aka a single cycle has been completed
     *
     * @param Script
     *
     * @return true when Cycle is completed, ready for a restart
     */
    @Override
    public boolean isCycleComplete(tpircSScript Script)
    {
        return !Inventory.contains(SourceItemID);
    }

    @Override
    public boolean isCycleFinished(tpircSScript Script)
    {
        return !Bank.contains(SourceItemID) && !Inventory.contains(SourceItemID);
    }

    /**
     * @param Script
     *
     * @return if cycle has successfully started
     */
    @Override
    public boolean onStart(tpircSScript Script)
    {
        StartCycle(Script);
        return super.onStart(Script);
    }

    private void StartCycle(tpircSScript Script)
    {
        Complete         = false;
        InteractTask     = null;
        BankTask         = null;
        InteractComplete = false;
        TileIterator     = null;
        TargetTile       = null;


        boolean BankHasSource = !Inventory.contains(SourceItemID) && Bank.contains(SourceItemID);
        boolean InventoryisFullButNoSource = (Inventory.isFull() &&
                                              !Inventory.contains(SourceItemID));
        boolean ToolCheck = (Tool != null && !Inventory.contains(Tool));

        if(BankHasSource || InventoryisFullButNoSource || ToolCheck)
        {
            //            if(!OSRSUtilities.CanReachBank())
            //            {
            TravelTask Travel = new TravelTask("", BankLocation.getNearest().getTile());
            Travel.SetTaskName("IOPC Travel To Bank For ItemRequirements");
            Travel.SetTaskPriority(0);
            //Travel.CompleteCondition = OSRSUtilities::CanReachBank;
            Script.addNodes(Travel);
            //}

            BankTask = new BankItemsTask("InteractOnPositionCycle Grab items to interact with");
            Logger.log(
                    "InteractOnPositionCycle: StartCycle: Inventory has items besides necessities: " +
                    Inventory.except(t -> t == null || t.getID() == Tool ||
                                          Arrays.stream(SourceItemID).anyMatch(x -> x == t.getID()))
                             .isEmpty());
            if(!Inventory.except(t -> t == null || t.getID() == Tool ||
                                      Arrays.stream(SourceItemID).anyMatch(x -> x == t.getID()))
                         .isEmpty())
            {
                Logger.log("InteractOnPositionCycle: StartCycle: Grab items from bank, depositall");
                BankTask.AddDepositAll();
                if(Tool != null)
                {
                    BankTask.AddWithdraw(Tool, 1);
                }
                if(WithdrawSourceItemsFromBank)
                {
                    BankTask.AddWithdrawAll(SourceItemID);
                }
            }
            else
            {
                Logger.log(
                        "InteractOnPositionCycle: StartCycle: Grab items from bank, only withdraw");
                if(Tool != null && !Inventory.contains(Tool))
                {
                    BankTask.AddWithdraw(Tool, 1);
                }
                BankTask.AddWithdrawAll(SourceItemID);
            }

            Script.addNodes(BankTask);
        }

        if(TileListener != null)
        {
            TileListener.interrupt();
            TileListener = null;
        }

        if(GeneratePossibleTiles != null)
        {
            PossibleTiles = null;
        }

        TileListener = new Thread(this::TileListening);
        TileListener.start();
    }

    private void TileListening()
    {
        Tile Current = Players.getLocal().getTile();

        while(true)
        {
            Tile New = Players.getLocal().getTile();
            if(New != Current)
            {
                if(TileReady.availablePermits() == 0)
                {
                    Logger.log("InteractOnPositionCycle: TileListening: Player on new Tile");
                    TileReady.release();
                }
                Current = New;
            }
            Sleep.sleep(100);
        }
    }

    @Override
    public void onDebugPaint(Graphics graphics)
    {
        //        var PTile = Players.getLocal().getTile();
        //        if(PTile != null && PTile.getPolygon() != null)
        //        {
        //            graphics.setColor(Color.ORANGE);
        //            var close = GetSurroundingArea(PTile);
        //            if(!close.isEmpty() && close.getLast() != null &&
        //               GetSurroundingArea(PTile).getLast().getPolygon() != null)
        //            {
        //                graphics.drawPolygon(close.getLast().getPolygon());
        //            }
        //
        //            if(WoodDB.isTileSolid(PTile))
        //            {
        //                graphics.setColor(Color.GREEN);
        //            }
        //            else
        //            {
        //                graphics.setColor(Color.RED);
        //            }
        //            graphics.drawPolygon(PTile.getPolygon());
        //        }

        //        for(Tile tile : Players.getLocal().getTile().getArea(30).getTiles())
        //        {
        //            if(tile == null || tile.getPolygon() == null)
        //            {
        //                continue;
        //            }
        //            if(WoodDB.isTileSolid(tile))
        //            {
        //                graphics.setColor(Color.GREEN);
        //            }
        //            else
        //            {
        //                graphics.setColor(Color.RED);
        //            }
        //            graphics.drawPolygon(tile.getPolygon());
        //        }
    }

    /**
     * @param graphics
     */
    @Override
    public void onDebugPaint(Graphics2D graphics)
    {
        int xcord = Players.getLocal().getTile().getGridX();
        int ycord = Players.getLocal().getTile().getGridY();

        if(Players.getLocal().getTile() == null)
        {
            return;
        }

        var objs = GameObjects.getObjectsOnTile(Players.getLocal().getTile());
        //        var Collissionmap = Client.getCollisionMaps()[Client.getPlane()];
        //
        //        graphics.drawString("isSolid: " + Collissionmap.isSolid(xcord, ycord), 50, 10);
        //        graphics.drawString("isWalkable: " + Collissionmap.isWalkable(xcord, ycord, xcord, ycord),
        //                            50,
        //                            20);


        var y = 0;
        for(var obj : objs)
        {
            if(obj == null)
            {
                continue;
            }

            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font("Default", Font.BOLD, 16));
            graphics.drawString(obj.getName(), 40, 30 + y);
            graphics.setFont(new Font("Default", Font.PLAIN, 16));
            graphics.drawString("isOccludes: " + obj.isOccludes(), 50, 40 + y);
            graphics.drawString("isObstructsGround: " + obj.isObstructsGround(), 50, 60 + y);
            graphics.drawString("isImpenetrableSolid: " + obj.isImpenetrableSolid(), 50, 80 + y);
            graphics.drawString("isCastsShadows: " + obj.isCastsShadows(), 50, 100 + y);
            graphics.drawString("isBlocksProjectiles: " + obj.isBlocksProjectiles(), 50, 120 + y);
            graphics.drawString("isNonFlatShading: " + obj.isNonFlatShading(), 50, 140 + y);
            graphics.drawString("isRandomizeAnimationStart: " + obj.isRandomizeAnimationStart(),
                                50,
                                160 + y);
            graphics.drawString("isOnScreen: " + obj.isOnScreen(), 50, 180 + y);


            graphics.drawString("Flags: " + Integer.toBinaryString(obj.getFlags()), 50, 200 + y);
            y += 200;
        }
    }

    /**
     * End cycle after current cycle has finished
     *
     * @param Script
     *
     * @return if cycle has successfully ended
     */
    @Override
    public boolean onEnd(tpircSScript Script)
    {
        if(TileListener != null)
        {
            TileListener.interrupt();
            TileListener = null;
        }
        return super.onEnd(Script);
    }

    /**
     * End cycle regardless of its situation
     *
     * @param Script
     *
     * @return if cycle has successfully ended
     */
    @Override
    public boolean onEndNow(tpircSScript Script)
    {
        if(TileListener != null)
        {
            TileListener.interrupt();
            TileListener = null;
        }
        return super.onEndNow(Script);
    }

    /**
     * @param Script
     *
     * @return
     */
    @Override
    public int onLoop(tpircSScript Script)
    {
        if(PossibleTiles == null || PossibleTiles.isEmpty())
        {
            Logger.log("InteractOnPositionCycle: onLoop: Getting New Tiles");
            PossibleTiles = GetPossibleTiles(false);
        }

        if(Players.getLocal().isMoving() || Players.getLocal().isAnimating() ||
           (Traveltask != null && !Traveltask.isFinished()))
        {
            Logger.log("InteractOnPositionCycle: onLoop: Player is moving");
            return super.onLoop(Script);
        }

        var PlayerPos = Players.getLocal().getTile();
        if(!isValidTile(PlayerPos))
        {
            if(TileIterator != null && TileIterator.hasNext())
            {
                Logger.log("InteractOnPositionCycle: onLoop: Not a valid tile, tileiterator");
                var next = TileIterator.next();
                while(!isValidTile(next) && TileIterator.hasNext())
                {
                    next = TileIterator.next();
                }
                if(isValidTile(next))
                {
                    TargetTile = next;
                }
            }
            else
            {
                Logger.log("InteractOnPositionCycle: onLoop: Not a valid tile, closest valid tile");
                for(var tile : GetSurroundingArea(PlayerPos))
                {
                    if(isValidTile(tile))
                    {
                        TargetTile = tile;
                        break;
                    }
                }
            }
            if(TargetTile != null)
            {
                Traveltask = new TravelTask("Travel To FirePlace", TargetTile);
                Script.addNodes(Traveltask);
            }

            return super.onLoop(Script);
        }

        boolean TileIsReady = TileReady.tryAcquire();
        boolean NoInteract  = InteractTask == null;
        boolean timeout = (
                System.nanoTime() - InteractTimeout > TimeUnit.MILLISECONDS.toNanos(Timout) &&
                InteractComplete);
        Logger.log("InteractOnPositionCycle: onLoop: TileIsReady" + TileIsReady + " No Interact: " +
                   NoInteract + " Timeout: " + timeout);

        if(TileIsReady || NoInteract || timeout)
        {
            if(!Inventory.contains(SourceItemID))
            {
                Logger.log("InteractOnPositionCycle: onLoop: No items left, exiting");
                return 0;
            }

            if(Dialogues.inDialogue() || !WaitInteract)
            {
                Logger.log("InteractOnPositionCycle: onLoop: New Interaction");
                InteractComplete = false;
                Script.addNodes(CreateInteractTask());
                InteractTimeout = System.nanoTime();
                TargetTile      = PlayerPos;
            }
        }
        return super.onLoop(Script);
    }

    public List<Tile> GetPossibleTiles(boolean Regenerate)
    {
        List<Tile> out = PossibleTiles;
        if(PossibleTiles == null || PossibleTiles.isEmpty() || Regenerate)
        {
            if(GeneratePossibleTiles != null)
            {
                out = GeneratePossibleTiles.apply(Players.getLocal().getTile());

            }
            if(out == null || out.isEmpty())
            {
                Logger.log(
                        "InteractOnPositionCycle: getPossibleTiles: No PossibleTiles and generation function is null or returned null");
                return null;
            }
        }

        if(!InOrder)
        {
            out.sort((x, y) -> (int) ((y.walkingDistance(Players.getLocal().getTile()) -
                                       x.walkingDistance(Players.getLocal().getTile())) * 100));
        }
        PossibleTiles = out;

        TileIterator = PossibleTiles.iterator();
        return PossibleTiles;
    }

    public List<Tile> GetSurroundingArea(Tile tile)
    {
        return Arrays.stream(Players.getLocal().getTile().getArea(3).getTiles())
                     .sorted((x, y) -> (int) (y.walkingDistance(tile) - x.walkingDistance(tile)))
                     .toList();
    }

    public boolean isValidTile(Tile tile)
    {
        Logger.log("InteractOnPositionCycle: isValidTile: Tile " + tile + " " +
                   TileChecker.apply(Players.getLocal().getTile(), tile) + " " + TileChecker + " ");
        return tile != null &&
               (TileChecker == null || TileChecker.apply(Players.getLocal().getTile(), tile));
    }

    private InteractInventoryTask CreateInteractTask()
    {
        InteractTask = new InteractInventoryTask(
                "Interacting with items " + SourceItemID + " " + Action, Action, SourceItemID);
        if(Tool != null)
        {
            InteractTask.setTool(Tool);
        }
        InteractTask.AcceptCondition = () -> !BankTask.isActive();
        InteractTask.onComplete.Subscribe(this, () -> InteractComplete = true);
        return InteractTask;
    }

    /**
     * @param graphics
     */
    @Override
    public void onPaint(Graphics graphics)
    {
        //        Logger.log("InteractOnPositionCycle: OnPaint: ");
        if(PossibleTiles != null)
        {
            for(var tile : PossibleTiles)
            {
                if(tile == null || tile.getPolygon() == null)
                {
                    continue;
                }
                graphics.setColor(Color.RED);
                if(!WoodDB.isTileSolid(tile))
                {
                    graphics.setColor(Color.GREEN);
                }
                graphics.drawPolygon(tile.getPolygon());
            }
        }


        if(TargetTile != null && TargetTile.getPolygon() != null)
        {
            graphics.setColor(Color.BLUE);
            graphics.drawPolygon(TargetTile.getPolygon());
        }


        //        for(var tile : Players.getLocal().getTile().getArea(20).getTiles())
        //        {
        //            if(tile.getPolygon() == null)
        //            {
        //                continue;
        //            }
        //            if(WoodDB.isTileBurnable(tile))
        //            {
        //                graphics.setColor(Color.white);
        //                graphics.drawPolygon(tile.getPolygon());
        //            }
        //            else
        //            {
        //                graphics.setColor(Color.red);
        //                graphics.drawPolygon(tile.getPolygon());
        //            }
        //        }

    }

    /**
     * When all cycles have been completed and we want to do the cycle again, this is called
     *
     * @param Script
     */
    @Override
    public void onReset(tpircSScript Script)
    {
        StartCycle(Script);
        super.onReset(Script);
    }

    /**
     * When a cycle has been completed, this will be called
     *
     * @param Script
     */
    @Override
    public boolean onRestart(tpircSScript Script)
    {
        StartCycle(Script);
        return super.onRestart(Script);
    }
}
