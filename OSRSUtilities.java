import org.dreambot.api.Client;
import org.dreambot.api.data.consumables.Food;
import org.dreambot.api.input.Keyboard;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.input.event.impl.keyboard.awt.Key;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.input.mouse.MouseSettings;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.walking.path.impl.LocalPath;
import org.dreambot.api.methods.walking.pathfinding.impl.local.LocalPathFinder;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;

import java.awt.*;
import java.util.*;
import java.util.List;

public class OSRSUtilities {

    static final String[] ItemsToAlwaysPickUp = {"Clue scroll", "Coins"};

    static Random rand = new Random();
    public static boolean OpenBank()
    {
        if(Bank.isOpen())
        {
            return true;
        }

        if(!CanReachBank())
        {
            return false;
        }

        var Banks = BankLocation.getSortedValidLocations(Players.getLocal().getTile());
        while (!Bank.isOpen())
        {
            Bank.open(Banks.get(0));
            Sleep.sleepUntil(() -> Bank.isOpen(), 5000);
        }

        return true;
    }

    public static boolean BankDepositAll(int... Except)
    {
        if(Inventory.isEmpty())
        {
            return true;
        }

        boolean result = true;
        result &= OpenBank();
        if(Except.length > 0)
        {
            var items = Inventory.all(t -> Arrays.stream(Except).anyMatch(x -> x != t.getID()));
            for(var item : items)
            {
                result &= Bank.depositAll(item);
            }
            return result;
        }
        else
        {
            return Bank.depositAllItems();
        }
    }

    public static boolean BankWithdraw(AbstractMap.SimpleEntry<Integer, Integer>... WithdrawIDs)
    {
        boolean result = true;
        if(WithdrawIDs.length > 0)
        {
            result &= OpenBank();
            for(var item : Arrays.stream(WithdrawIDs).toList())
            {
                result &= Bank.withdraw(item.getKey(), item.getValue());
            }
        }
        return result;
    }

    public static boolean BankWithdrawAll(int WithdrawID)
    {
        return OpenBank() && Bank.withdrawAll(WithdrawID);
    }

    public static boolean BankClose()
    {
        if(Bank.isOpen())
        {
            return Bank.close();
        }
        return true;
    }



    public static void Mine(Tile ToMine)
    {
        GameObject[] Objs = GameObjects.getObjectsOnTile(ToMine);
        var Obj = Arrays.stream(Objs).filter(t -> t.hasAction("Mine")).findFirst();

        if(Obj.isPresent())
        {
            Point Center = Obj.get().getCenterPoint();
            Center.translate(rand.nextInt(5) - 3, rand.nextInt(5) - 3);
            Mouse.click(Center);
            Sleep.sleep(3000);
            Sleep.sleepUntil(() -> Players.getLocal().getAnimation() == -1, 50000);

            while (Dialogues.inDialogue())
            {
                Dialogues.clickContinue();
                Sleep.sleep(rand.nextInt(3000) + 500);
            }
        }
    }

    public static boolean ClickCombine(int ID1, int ID2)
    {
        var Box1 = Inventory.itemBounds(Inventory.get(ID1));
        var Box2 = Inventory.itemBounds(Inventory.get(ID2));
        Point Click1 = new Point((int) Box1.getCenterX(), (int) Box1.getCenterY());
        Click1.translate(rand.nextInt(8) - 4, rand.nextInt(8) - 4);
        Point Click2 = new Point((int) Box2.getCenterX(), (int) Box2.getCenterY());
        Click2.translate(rand.nextInt(2) - 1, rand.nextInt(2) - 1);

        if(Box1.isEmpty() || Box2.isEmpty())
        {
            Logger.log("Couldn't find points to click, box1: " + Box1 + " box2: " + Box2);
            return false;
        }

        while(!Inventory.isItemSelected())
        {
            Mouse.click(Click1);
            Sleep.sleep(100);
            Click1 = new Point((int) Box1.getCenterX(), (int) Box1.getCenterY());
            Click1.translate(rand.nextInt(6) - 3, rand.nextInt(6) - 3);
        }

        return Mouse.click(Click2);
    }

    public static boolean Fish(String Action, int ID)
    {
        NPC ClosestSpot = NPCs.closest(t -> t.getID() == ID && t.hasAction(Action));

        if(ClosestSpot == null)
        {
            Logger.log("No Spot with ID " + ID + " or action " + Action + " nearby");
            return false;
        }

        while (!Inventory.isFull())
        {
            ClosestSpot.interact(Action);
            Wait();
            Mouse.moveOutsideScreen();
            Sleep.sleepUntil(() -> (Players.getLocal().getAnimation() == -1 && !Players.getLocal().isMoving()) || Dialogues.inDialogue(), 120000);

            while(Dialogues.inDialogue())
            {
                Dialogues.continueDialogue();
                Wait(300,500);
            }

            if(!ClosestSpot.exists())
            {
                ClosestSpot = null;
                ClosestSpot = NPCs.closest(t -> t.getID() == ID && t.hasAction(Action));
                if(ClosestSpot == null)
                {
                    return false;
                }
            }
        }

    return true;
    }

    public static boolean PickupOnTile(Tile tile, List<Integer> IDs)
    {

        var Items = GroundItems.all(t -> t.getTile() == tile && (IDs.contains(t.getID())
                || Arrays.stream(ItemsToAlwaysPickUp).anyMatch(x -> x.contains(t.getName()))));// Always pickup clue scrolls lol
        Items.sort(Comparator.comparingDouble(p -> {
            double dist = ((GroundItem)p).walkingDistance(Players.getLocal().getTile());
            return Math.abs(dist);
        }));
        return PickupItems(Items);
    }

    public static boolean PickupOnArea(Area area, List<Integer> IDs)
    {
        var Items = GroundItems.all(t ->
                        area.contains(t.getTile())
                        && (IDs.contains(t.getID())
                        || Arrays.stream(ItemsToAlwaysPickUp).anyMatch(x -> x.contains(t.getName())))); // Always pickup clue scrolls lol
        Items.sort(Comparator.comparingDouble(p -> {
            double dist = ((GroundItem)p).walkingDistance(Players.getLocal().getTile());
            return Math.abs(dist);
        }));

        return PickupItems(Items);
    }

    public static boolean PickupItems(List<GroundItem> Items)
    {
        boolean result = true;
        for(var Item : Items)
        {
            Logger.log(Item);
            if(Item.exists() && Item.canReach())
            {
                boolean tempresult = Item.interact();
                if(tempresult)
                {
                    result &= Sleep.sleepUntil(() -> !Players.getLocal().isMoving() && !Item.exists(), 15000);
                    Sleep.sleepTicks(1);
                }
                else
                {
                    return tempresult;
                }
            }
            else
            {
                Logger.log("Failed to pick up item: " + Item.toString());
            }

            if(Inventory.isFull())
            {
                Logger.log("Inventory is full");
                return false;
            }
        }

        return result;
    }

    public static int[] GetFoodIDs()
    {
        return new int[]{315};
    }

    public static int GetBestHealChoice()
    {
        return 315;
    }

    //Heal up completely
    public static boolean Healup()
    {
        int FoodID = Food.getBestOnHand(true).getFromInventory().getID();
        while(Players.getLocal().getHealthPercent() < 100 && Inventory.contains(FoodID))
        {
            Inventory.interact(FoodID);
            Sleep.sleepTicks(3);
            Sleep.sleepUntil(() -> Players.getLocal().getAnimation() == -1, 5000);
            FoodID = Food.getBestOnHand(true).getFromInventory().getID();
        }

        if(Players.getLocal().getHealthPercent() < 100)
        {
            return false;
        }
        return true;
    }

    public static boolean BankContainsHeals()
    {
        return true;
    }

    public static boolean BankHeal()
    {
        if(OpenBank())
        {
            int FoodID = GetBestHealChoice();
            Bank.withdrawAll(FoodID);
            while(!Healup() && BankContainsHeals())
            {
                FoodID = GetBestHealChoice();
                Bank.withdrawAll(FoodID);
            }
            return Players.getLocal().getHealthPercent() >= 100;
        }
        return false;
    }


//    public static void ShiftCameraToDirection(Direction direction)
//    {
//        switch (direction)
//        {
//            case NULL -> {
//            }
//            case NORTH -> { 0
//            }
//            case EAST -> { 1536
//            }
//            case SOUTH -> { 1024
//            }
//            case WEST -> { 512
//            }
//        }
//    }

    private static void SimpleWalkTo_(Tile Destination)
    {
        Walking.walk(Destination);
        Wait();
        Sleep.sleepUntil(() ->
        {
            Tile ShortDestination = Client.getDestination();
            if(ShortDestination != null )
            {
                return ShortDestination.distance(Players.getLocal().getTile()) < 8;
            }
            if(!Players.getLocal().isMoving())
            {
                return true;
            }
            return false;
        }, 10000);
    }

    public static void SimpleWalkTo(Tile Destination)
    {
        boolean isMoving = true;
        while(Destination.distance(Players.getLocal().getTile()) > 1.0 && isMoving)
        {
            SimpleWalkTo_(Destination);

            while(Dialogues.inDialogue())
            {
                Dialogues.continueDialogue();
                Wait();
            }

            isMoving = Players.getLocal().isMoving();
        }
    }

    public static void WalkTo(Tile Destination)
    {
        Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 10000);

        if (Destination.distance(Players.getLocal().getTile()) <= 1.0)
        {
            Logger.log("Destination Reached");
            return;
        }

        if(Destination.canReach() && Map.isTileOnScreen(Destination))
        {
            Point Click = Map.tileToScreen(Destination);
            OSRSUtilities.RandomizeClick(Click);
            if(!Mouse.click(Click))
            {
                SimpleWalkTo_(Destination);
                return;
            }
        }

        LocalPath<Tile> Path = LocalPathFinder.getLocalPathFinder().calculate(Players.getLocal().getTile(), Destination);
        Logger.log("Path: " + Path.toString());


        if(Path == null || Path.isEmpty())
        {
            Logger.log("No Path found");
            while((Path == null || Path.isEmpty()) && Destination.distance(Players.getLocal().getTile()) > 1.0)
            {
                SimpleWalkTo_(Destination);
                Path = LocalPathFinder.getLocalPathFinder().calculate(Players.getLocal().getTile(), Destination);
            }

        }

        Logger.log(Destination.canReach());

        Tile Next = Path.next();
        while(!Path.isEmpty() && Destination.canReach())
        {
            Tile NextNext =  Path.getFurthestOnMM();

            Logger.log(Destination.canReach());
            Logger.log(Destination.distance(Players.getLocal().getTile()));
            Logger.log("PlayerTile: "+ Players.getLocal().getTile());

            if(Next == Players.getLocal().getTile())
            {
                Next = NextNext;
                continue;
            }

            if(Destination.distance(Players.getLocal().getTile()) <= 1.0)
            {
                break;
            }



            if(Next != null && Path.isObstacleTile(Next))
            {
                Logger.log("Obstacle");
                Logger.log(Path.getObstacleForTile(Next).toString());
                Path.getObstacleForTile(Next).traverse();
                Sleep.sleep(rand.nextInt(2000) + 300);
            }
            else if (Next != null && Next.canReach())
            {
                Logger.log("Clicking");
                Logger.log(Next.distance(NextNext));
                if(Map.isTileOnScreen(Next) && Math.abs(Next.distance(NextNext)) > 1.0)
                {

                    Point Center = Map.tileToScreen(Next);
                    Center.translate(rand.nextInt(5) - 3, rand.nextInt(5) - 3);
                    if(!Mouse.click(Center))
                    {
                        Center = Map.tileToScreen(Next);
                        Mouse.click(Center);
                    }
                }
                else
                {
                    Point Center = Map.tileToMiniMap(Next);
                    if (Center.x == 0 && Center.y == 0)
                    {
                        // shouldn't happen
                        return;
                    }
                    else
                    {
                        Center.translate(rand.nextInt(5) - 3, rand.nextInt(5) - 3);
                        Walking.clickTileOnMinimap(Next);
                    }
                }

                Sleep.sleep(rand.nextInt(2000) + 500);
                Tile finalNext = Next;
                Sleep.sleepUntil(() -> Players.getLocal().getTile().distance(finalNext) < 4, 10000);
            }
            else
            {
                Logger.log("Last resort walking");
                Walking.walk(Destination);
                Sleep.sleep(rand.nextInt(1000) + 100);
            }
            Next = NextNext;
        }
    }

    public static boolean PickSkillingMenuItem(int index)
    {
        var child =  Widgets.get(270,13 + index);
        if(child != null && child.isVisible())
        {
            return child.interact();
        }
        Logger.log("Failed to get child item, " + child);
        return false;
    }

    public static boolean ProcessItems(int index, int ID1, int ID2, int timeout)
    {
        while (Inventory.contains(ID1) && Inventory.contains(ID2))
        {
            while(Dialogues.inDialogue())
            {
                Dialogues.continueDialogue();
                Wait(1000, 300);
            }

            if(!ClickCombine(ID1, ID2))
            {
                Logger.log("Failed to click combine");
                return false;
            }
            Sleep.sleepTicks(3);
            Wait(100, 500);

            if(!PickSkillingMenuItem(index)){
                Logger.log("Failed to pick menu item");
                return false;
            }

            Sleep.sleep(rand.nextInt(2000) + 1000);

            Mouse.moveOutsideScreen();

            Sleep.sleepUntil(() -> {
                return !Inventory.contains(ID1) || !Inventory.contains(ID2) || Dialogues.inDialogue();
            }, timeout);

            Sleep.sleep(rand.nextInt(2000) + 1000);
        }

        return true;
    }



    public static boolean BankingDropPickupMany(Optional<Integer> KeepID, AbstractMap.SimpleEntry<Integer, Integer>... WithdrawIDs)
    {
        boolean result = true;

        result &= OpenBank();

        if(KeepID.isPresent())
        {
            result &= BankDepositAll(KeepID.get());
        }

        if(WithdrawIDs.length > 0)
        {
            result &= BankWithdraw(WithdrawIDs);
        }

        return result;
    }

    public static boolean CanReachBank()
    {
        BankLocation near = BankLocation.getNearest();
        if(!near.canReach())
        {
            return false;
        }
        return true;
    }

    public static Point RandomizeClick(Point Click, int VarianceX, int VarianceY)
    {
        Click.translate(rand.nextInt(VarianceX / 2) - (VarianceX / 2), rand.nextInt(VarianceY / 2) - (VarianceY / 2));
        return Click;
    }

    public static Point RandomizeClick(Point Click)
    {
        return RandomizeClick(Click, 5, 5);
    }

    private static void Randomize(int minSpeed, float SpeedMultiplier, int Variance)
    {
        Logger.log("Randomize");
        while(true)
        {
            MouseSettings.setSpeed(Math.round((rand.nextInt(Variance) + minSpeed) * SpeedMultiplier));
            Sleep.sleep(rand.nextInt(20000) + 1000);
        }
    }

    public static Thread StartRandomizerThread(int minSpeed, float SpeedMultiplier, int Variance)
    {
        Logger.log("Thread started");
        Thread thr = new Thread(() -> Randomize(minSpeed,SpeedMultiplier,Variance));
        thr.start();
        return  thr;
    }

    public static Thread StartRandomizerThread()
    {
        Logger.log("Thread started");
        Thread thr = new Thread(() -> Randomize(3,1.0f,5));
        thr.start();
        return  thr;
    }

    public static void Wait(int MinimumMs, int VarianceMs)
    {
        Sleep.sleep(rand.nextInt(VarianceMs) + MinimumMs);
    }
    public static void Wait()
    {
        Wait(500, 2000);
    }

    public static void WaitForEndAnimationLoop(int WaitPerAnimation, int timeout)
    {
        Mouse.moveOutsideScreen(true);
        boolean end = false;
        while(!end)
        {
            if(Sleep.sleepUntil(() -> Players.getLocal().getAnimation() == -1, timeout))
            {
                if(!Sleep.sleepUntil(() -> Players.getLocal().getAnimation() != -1, WaitPerAnimation))
                {
                    end = true;
                }
                else if (Dialogues.inDialogue())
                {
                    while(Dialogues.inDialogue())
                    {
                        Dialogues.continueDialogue();
                        Wait(300,500);
                    }
                }
            }
            else
            {
                //timeout
                end = true;
            }
        }
    }
}

