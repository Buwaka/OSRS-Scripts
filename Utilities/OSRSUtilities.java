package Utilities;

import Database.OSRSDataBase;
import Utilities.Combat.CombatManager;
import org.dreambot.api.Client;
import org.dreambot.api.data.consumables.Food;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.input.Camera;
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
import org.dreambot.api.methods.world.World;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Character;
import org.dreambot.api.wrappers.interactive.*;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.CRC32;


public class OSRSUtilities
{

    static final String AttackAction = "Attack";

    static final         String[]                      ItemsToAlwaysPickUp = {"Clue scroll", "Coins"};
    private static final ConcurrentHashMap<Long, Long> TimeStamps          = new ConcurrentHashMap<Long, Long>();

    // TODO make function to equip best combat gear, perhaps even splitting it up into combat/magic/ranged
    static Random rand = new Random();

    /**
     * @param Deposits
     * @param Withdraws
     * @param sleepdelay how many ms to wait inbetween each action
     *
     * @return true if successful, false if failed
     */
    public static boolean ProcessBankEntries(List<BankEntry> Deposits, List<BankEntry> Withdraws, int sleepdelay)
    {
        if(OSRSUtilities.OpenBank())
        {
            boolean              result   = true;
            ArrayList<BankEntry> Deposit  = Deposits == null ? new ArrayList<>() : new ArrayList<>(Deposits);
            ArrayList<BankEntry> Withdraw = Withdraws == null ? new ArrayList<>() : new ArrayList<>(Withdraws);
            while((!Deposit.isEmpty() || !Withdraw.isEmpty()) && result)
            {
                // Deposits first
                if(!Deposit.isEmpty())
                {
                    var deposit = Deposit.getFirst();
                    if(Bank.getCurrentTab() != deposit.BankTab)
                    {
                        Bank.openTab(deposit.BankTab);
                    }
                    if(deposit.ItemID == -1)
                    {
                        result &= Bank.depositAllItems();
                    }
                    else if(deposit.Amount == -1)
                    {
                        result &= Bank.depositAll(deposit.ItemID);
                    }
                    else
                    {
                        result &= Bank.deposit(deposit.ItemID, deposit.Amount);
                    }
                    Logger.log("Deposit complete: " + result);
                    Deposit.removeFirst();
                }
                else if(!Withdraw.isEmpty())
                {
                    var withdraw = Withdraw.getFirst();
                    if(Bank.getCurrentTab() != withdraw.BankTab)
                    {
                        result &= Bank.openTab(withdraw.BankTab);
                    }
                    if(withdraw.Amount == -1)
                    {
                        result &= Bank.withdrawAll(withdraw.ItemID);
                    }
                    else
                    {
                        result &= Bank.withdraw(withdraw.ItemID, withdraw.Amount);
                    }
                    Logger.log("Withdrawal complete" + result);
                    Withdraw.removeFirst();
                }

                Sleep.sleep(sleepdelay);
            }
            return result;
        }

        return Deposits.isEmpty() && Withdraws.isEmpty();
    }

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
        while(!Bank.isOpen())
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
            Logger.log("Except " + Arrays.toString(Except));
            var items = Inventory.all(t -> Arrays.stream(Except).anyMatch(x -> {
                Logger.log(x + " != " + t.getID());
                return x != t.getID();
            }));
            for(var item : items)
            {
                Logger.log("Item ID: " + item.getID());
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
            // try to get widget to close it manually
            var CloseBankWidget = Widgets.get(12, 2, 11);
            if(CloseBankWidget != null && CloseBankWidget.isVisible())
            {
                while(CloseBankWidget.isVisible())
                {
                    Point click = GetCenterPointRectangle(CloseBankWidget.getRectangle(), true);
                    Mouse.click(click);
                    Sleep.sleepUntil(() -> !CloseBankWidget.isVisible(), 10000);
                }
                return true;
            }
            else
            {
                return Bank.close();
            }
        }
        return true;
    }

    public static boolean InventoryContainsAny(int... IDs)
    {
        for(var id : IDs)
        {
            if(Inventory.contains(id))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean InventoryContainsAnyFoods(boolean f2p)
    {
        var foods = OSRSDataBase.GetCommonFoods(f2p);
        for(var food : foods)
        {
            if(Inventory.contains(food.id))
            {
                return true;
            }
        }
        return false;
    }

    public static List<Character> GetAllCharactersInteractingWith(Character Target)
    {
        List<Character> result = new ArrayList<>();
        var all =  NPCs.all();
        for(Character npc : all)
        {
            if(npc.isInteracting(Target))
            {
                result.add(npc);
            }
        }

        return result;
    }

    public static Point GetCenterPointRectangle(Rectangle rect, boolean randomize)
    {
        Point point = new Point((int) rect.getCenterX(), (int) rect.getCenterY());
        point = RandomizeClick(point);
        return point;
    }

    public static void Mine(Tile ToMine)
    {
        GameObject[] Objs = GameObjects.getObjectsOnTile(ToMine);
        var          Obj  = Arrays.stream(Objs).filter(t -> t.hasAction("Mine")).findFirst();

        if(Obj.isPresent())
        {
            Point Center = Obj.get().getCenterPoint();
            Center.translate(rand.nextInt(5) - 3, rand.nextInt(5) - 3);
            Mouse.click(Center);
            Sleep.sleep(3000);
            Sleep.sleepUntil(() -> Players.getLocal().getAnimation() == -1, 50000);

            while(Dialogues.inDialogue())
            {
                Dialogues.clickContinue();
                Sleep.sleep(rand.nextInt(3000) + 500);
            }
        }
    }

    public static boolean ClickCombine(int ID1, int ID2)
    {
        var   Box1   = Inventory.itemBounds(Inventory.get(ID1));
        var   Box2   = Inventory.itemBounds(Inventory.get(ID2));
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

        while(!Inventory.isFull())
        {
            ClosestSpot.interact(Action);
            Wait(2000, 3000);
            Mouse.moveOutsideScreen();
            Sleep.sleepUntil(() -> (Players.getLocal().getAnimation() == -1 && !Players.getLocal().isMoving()) ||
                                   Dialogues.inDialogue(), 120000);

            while(Dialogues.inDialogue())
            {
                Dialogues.continueDialogue();
                Wait(300, 500);
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

        var Items = GroundItems.all(t -> t.getTile() == tile && (IDs.contains(t.getID()) ||
                                                                 Arrays.stream(ItemsToAlwaysPickUp).anyMatch(x -> x.contains(
                                                                         t.getName()))));// Always pickup clue scrolls lol
        Items.sort(Comparator.comparingDouble(p -> {
            double dist = p.walkingDistance(Players.getLocal().getTile());
            return Math.abs(dist);
        }));
        return PickupItems(Items);
    }

    public static boolean PickupOnArea(Area area, int... IDs)
    {
        var Items = GroundItems.all(t -> area.contains(t.getTile()) &&
                                         (Arrays.stream(IDs).anyMatch(x -> x != t.getID()) ||
                                          Arrays.stream(ItemsToAlwaysPickUp).anyMatch(x -> x.contains(t.getName())))); // Always pickup clue scrolls lol
        Items.sort(Comparator.comparingDouble(p -> {
            double dist = p.walkingDistance(Players.getLocal().getTile());
            return Math.abs(dist);
        }));

        return PickupItems(Items);
    }

    public static List<GroundItem> GetLootItems(Area area)
    {
        var Items = GroundItems.all(t -> area.contains(t.getTile()) ||
                                         Arrays.stream(ItemsToAlwaysPickUp).anyMatch(x -> x.contains(t.getName())));
        ; // Always pickup clue scrolls lol
        Items.sort(Comparator.comparingDouble(p -> {
            double dist = p.walkingDistance(Players.getLocal().getTile());

            return Math.abs(dist);
        }));

        return Items;
    }

    public static List<GroundItem> GetLootItemsInclude(Area area, int... IncludeIDs)
    {
        var Items = GroundItems.all(t -> area.contains(t.getTile()) &&
                                         (Arrays.stream(IncludeIDs).anyMatch(x -> x == t.getID()) ||
                                          Arrays.stream(ItemsToAlwaysPickUp).anyMatch(x -> x.contains(t.getName())))); // Always pickup clue scrolls lol
        Items.sort(Comparator.comparingDouble(p -> {
            double dist = p.walkingDistance(Players.getLocal().getTile());

            return Math.abs(dist);
        }));

        return Items;
    }

    public static List<GroundItem> GetLootItemsExclude(Area area, int... ExceptIDs)
    {
        var Items = GroundItems.all(t -> area.contains(t.getTile()) &&
                                         (Arrays.stream(ExceptIDs).anyMatch(x -> x != t.getID()) ||
                                          Arrays.stream(ItemsToAlwaysPickUp).anyMatch(x -> x.contains(t.getName())))); // Always pickup clue scrolls lol
        Items.sort(Comparator.comparingDouble(p -> {
            double dist = p.walkingDistance(Players.getLocal().getTile());

            return Math.abs(dist);
        }));

        return Items;
    }

    public static boolean PickupOnAreaExcepts(Area area, int... ExceptIDs)
    {
        return PickupItems(GetLootItemsExclude(area, ExceptIDs));
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
                    Logger.log("Failed to interact with item: " + Item);
                }
            }
            else
            {
                Logger.log("Failed to pick up item: " + Item);
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
        Food food = Food.getBestOnHand(true);
        if(food == null)
        {
            return false;
        }

        int FoodID = food.getFromInventory().getID();
        while(Players.getLocal().getHealthPercent() < 100 && Inventory.contains(FoodID) && food != null)
        {
            Inventory.interact(FoodID);
            Sleep.sleepTicks(3);
            Sleep.sleepUntil(() -> Players.getLocal().getAnimation() == -1, 5000);
            food = Food.getBestOnHand(true);
            if(food != null)
            {
                FoodID = food.getFromInventory().getID();
            }
        }

        return Players.getLocal().getHealthPercent() >= 100;
    }

    // Heal once
    public static boolean Heal()
    {
        int FoodID = Food.getBestOnHand(true).getFromInventory().getID();
        Inventory.interact(FoodID);
        Sleep.sleepTicks(3);
        return Sleep.sleepUntil(() -> Players.getLocal().getAnimation() == -1, 5000);
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

    private static void SimpleWalkTo_(Tile Destination)
    {
        Walking.walk(Destination);
        Wait();
        Sleep.sleepUntil(() -> {
            Tile ShortDestination = Client.getDestination();
            if(ShortDestination != null)
            {
                return ShortDestination.distance(Players.getLocal().getTile()) < 8;
            }
            return !Players.getLocal().isMoving();
        }, 10000);
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

        if(Destination.distance(Players.getLocal().getTile()) <= 1.0)
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

        LocalPath<Tile> Path = LocalPathFinder.getLocalPathFinder().calculate(Players.getLocal().getTile(),
                                                                              Destination);
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
            Tile NextNext = Path.getFurthestOnMM();

            Logger.log(Destination.canReach());
            Logger.log(Destination.distance(Players.getLocal().getTile()));
            Logger.log("PlayerTile: " + Players.getLocal().getTile());

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
            else if(Next != null && Next.canReach())
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
                    if(Center.x == 0 && Center.y == 0)
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

    public static void ResetCameraRandom(int timeout)
    {
        Camera.mouseRotateTo(Camera.getYaw() + rand.nextInt(450) + 150, 380);
        Camera.setZoom(rand.nextInt(20) + 181);
        Sleep.sleepUntil(() -> !Mouse.isMouseDragging(), timeout);
    }

    public static WidgetChild GetSkillingMenu()
    {
        WidgetChild child = Widgets.get(270, 0);
        if(child != null && child.isVisible())
        {
            return child;
        }
        Logger.log("Failed to get child item, " + child);
        return null;
    }

    public static boolean PickSkillingMenuItem(int index)
    {
        var child = Widgets.get(270, 13 + index);
        if(child != null && child.isVisible())
        {
            return child.interact();
        }
        Logger.log("Failed to get child item, " + child);
        return false;
    }

    public static boolean ProcessItems(int index, int ID1, int ID2, int timeout)
    {
        while(Inventory.contains(ID1) && Inventory.contains(ID2))
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

            if(!PickSkillingMenuItem(index))
            {
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

    public static List<AbstractMap.SimpleEntry<Integer, Integer>> CreateItemRequirements(int... IDAmountPair)
    {
        if(IDAmountPair.length == 0)
        {
            Logger.log("ID Amount pair is empty");
            return null;
        }
        if(IDAmountPair.length % 2 > 0)
        {
            Logger.log("Uneven ID Amount pair, exiting");
            return null;
        }


        List<AbstractMap.SimpleEntry<Integer, Integer>> Requirements = new ArrayList<>(IDAmountPair.length / 2);
        for(int i = 0; i < IDAmountPair.length; i += 2)
        {
            int ID     = IDAmountPair[i];
            int amount = IDAmountPair[i + 1];
            Requirements.add(new AbstractMap.SimpleEntry<>(ID, amount));
        }

        return Requirements;
    }

    public static NPC GetClosestAttackableEnemy(int... IDs)
    {
        //Logger.log("Searching for IDs " + Arrays.toString(IDs));
        return NPCs.closest(t -> Arrays.stream(IDs).anyMatch(x -> x == t.getID()) && t.hasAction(AttackAction) &&
                                 !t.isInteractedWith() && t.canAttack() && t.canReach());
    }

    public static NPC GetClosestAttackableEnemy(String... Names)
    {

        return NPCs.closest(t -> Arrays.stream(Names).anyMatch(x -> Objects.equals(x, t.getName())) &&
                                 t.hasAction(AttackAction) && !t.isInteractedWith() && t.canAttack() && t.canReach());
    }

    public static Area GetLootArea(Entity Foe)
    {
        double x = Foe.getModel().calculateModelArea().getBounds2D().getX();
        double y = Foe.getModel().calculateModelArea().getBounds2D().getY();

        Tile FoeTile = Foe.getTile();

        Tile TopRight   = FoeTile.translate((int) Math.ceil(x), (int) Math.ceil(y));
        Tile BottomLeft = FoeTile.translate((int) -Math.floor(x), (int) -Math.floor(y));

        Logger.log("FoeTile: " + FoeTile + " GridX: " + x + " GridY: " + y);
        Logger.log("LootArea: " + TopRight + " " + BottomLeft);
        Logger.log("Calculated Area: " + Foe.getModel().calculateModelArea().toString());

        return FoeTile.getArea(3);
    }

    private static Character PickNewTarget(int... IDs)
    {
        Player    player = Players.getLocal();
        Character Foe    = GetClosestAttackableEnemy(IDs);
        if(Foe == null)
        {
            Logger.log("No Foe found");
            return null;
        }
        Logger.log("Foe: " + Foe.toString());
        return CombatManager.GetInstance(player).Fight(Foe);
    }

    //return: whether we're fighting
    public static Character Slaughter(int Timeout, int... IDs)
    {
        Player    player = Players.getLocal();
        Character Foe    = player.getCharacterInteractingWithMe();
        if(!CombatManager.GetInstance(player).FightInteractingCharacter(Timeout))
        {
            Logger.log("Not interacting with anyone, looking for new target");
            Foe = PickNewTarget(IDs);
        }

        if(Foe == null)
        {
            return null;
        }

        Character finalFoe = Foe;
        if(rand.nextInt(10) < 5)
        {
            Wait();
            Mouse.moveOutsideScreen();
        }

        return Foe;
    }

    //return: whether we're fighting
    public static Character Slaughter(int Timeout, String... Names)
    {
        Set<String> seen = new HashSet<>();
        int[]       IDs  = NPCs.all(Names).stream().filter(t -> seen.add(t.getName())).mapToInt(NPC::getID).toArray();
        return Slaughter(Timeout, IDs);
    }

    public static boolean IsLootLeft(Area LootArea, int... LootExcepts)
    {
        Tile[] Tiles = LootArea.getTiles();
        for(Tile tile : Tiles)
        {
            if(GroundItems.getForTile(tile).stream().anyMatch(t -> Arrays.stream(LootExcepts).anyMatch(x -> t.getID() ==
                                                                                                            x)))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean SlaughterAndLoot(int Timeout, int LootTimeout, int[] LootExcepts, String... Names)
    {
        Set<Integer> seen = new HashSet<>();
        int[]        IDs  = NPCs.all(Names).stream().filter(t -> seen.add(t.getID())).mapToInt(NPC::getID).toArray();
        return SlaughterAndLoot(Timeout, LootTimeout, LootExcepts, IDs);
    }

    public static boolean SlaughterAndLoot(int Timeout, int LootTimeout, int[] LootExcepts, int... IDs)
    {
        Character Foe = Slaughter(Timeout, IDs);
        if(Foe != null)
        {
            if(!Sleep.sleepUntil(() -> !Foe.exists(), LootTimeout))
            {
                Logger.log("Failed to loot");
                return false;
            }
            // loot
            Logger.log("Looting Foe " + Foe.toString());
            ExecutorService Executor = Executors.newSingleThreadExecutor();
            Future<Boolean> result   = Executor.submit(() -> PickupOnAreaExcepts(GetLootArea(Foe), LootExcepts));
            try
            {
                return result.get(LootTimeout, TimeUnit.MILLISECONDS);
            } catch(Exception e)
            {
                Logger.log("Exception: " + e);
                return false;
            }
        }

        return false;
    }

    public static boolean CheckInventory(List<AbstractMap.SimpleEntry<Integer, Integer>> Requirements, boolean OnlyRequirements)
    {
        if(Requirements == null)
        {
            return true;
        }

        for(var item : Requirements)
        {
            int ID    = item.getKey();
            int Count = item.getValue();
            if(Inventory.count(ID) < Count)
            {
                return false;
            }
        }

        if(OnlyRequirements)
        {
            var Inv = Inventory.all(t -> Requirements.stream().noneMatch(x -> x.getKey() != t.getID()));
            Logger.log(Inv.size() + " unnecessary items (CheckInventory)");
            return Inv.isEmpty();
        }

        return true;
    }

    public static List<AbstractMap.SimpleEntry<Integer, Integer>> GetBestFoodChoice(int TotalHP)
    {
        if(!CanReachBank())
        {
            Logger.log("Can't reach bank to check possible food items");
            return null;
        }

        if(OpenBank())
        {
            var                                   CommonFood = OSRSDataBase.GetCommonFoods(!Client.isMembers());
            SortedMap<Integer, OSRSDataBase.Food> Choice     = new TreeMap<>();// score // ID
            for(var food : CommonFood)
            {
                var count = Bank.count(food.id);

                Choice.put(count * food.hitpoints, food);
            }
            var                                             reversed = Choice.reversed();
            List<AbstractMap.SimpleEntry<Integer, Integer>> out      = new ArrayList<>();
            for(var food : reversed.entrySet())
            {
                Integer count = (int) Math.min(Math.ceil((double) TotalHP / food.getValue().hitpoints),
                                               Bank.count(food.getValue().id));
                out.add(new AbstractMap.SimpleEntry<>(food.getValue().id, count));

                if(count * food.getValue().hitpoints > TotalHP)
                {
                    return out;
                }
            }
        }
        return null;
    }

    public static Boolean PrayAll(int Timeout, int... IDs)
    {
        final String BonesAction = "Bury";
        boolean      result      = true;
        long         start       = System.nanoTime();

        while(Inventory.contains(IDs) || (start - System.nanoTime()) > TimeUnit.MICROSECONDS.toNanos(Timeout))
        {
            result &= Inventory.get(t -> Arrays.stream(IDs).anyMatch(x -> x == t.getID())).interact(BonesAction);
            OSRSUtilities.Wait(500, 200);

        }

        return result;
    }

    public static boolean Bake(int... IngredientIDs)
    {
        final String CookAction = "Cook";
        int          i          = 0;
        while(Inventory.contains(IngredientIDs) && i < IngredientIDs.length)
        {
            GameObject Fire = GameObjects.closest(t -> t.hasAction(CookAction));

            if(Fire == null || !Fire.canReach())
            {
                Logger.log("No Cook object found found or can't reach it");
                return false;
            }

            if(Fire.isOnScreen())
            {
                if(Inventory.contains(IngredientIDs[i]))
                {
                    Inventory.use(IngredientIDs[i]);
                }

                Point pt = Fire.getClickablePoint();
                OSRSUtilities.RandomizeClick(pt, 2, 2);
                Mouse.click(pt);
                OSRSUtilities.Wait();
                if(GetSkillingMenu() == null)
                {
                    ResetCameraRandom(5000);
                }
                else
                {
                    OSRSUtilities.PickSkillingMenuItem(1);
                    OSRSUtilities.Wait();
                    OSRSUtilities.WaitForEndAnimationLoop(1500, 10000);
                }
            }
            else
            {
                OSRSUtilities.SimpleWalkTo(Fire.getTile());
                OSRSUtilities.Wait();
                Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 20000);
            }

            if(!Inventory.contains(Inventory.contains(IngredientIDs[i])))
            {
                i++;
            }
        }

        return true;
    }

    public static boolean JumpToOtherWorld()
    {
        List<World> Wlords = Worlds.all(t -> t.isF2P() && !t.isHighRisk() && !t.isPVP() && t.isNormal() &&
                                             !t.getDescription().contains("skill total"));
        Wlords.sort(Comparator.comparingInt(t -> t.getPopulation()));

        for(int i = 0; i < Wlords.size(); i++)
        {
            Logger.log("World " + i + " " + Wlords.get(i).getPopulation());
        }

        WorldHopper.openWorldHopper();
        Wait();
        Mouse.moveOutsideScreen();
        Wait(5000, 10000);
        Logger.log("Changing to world " + Wlords.get(0).getWorld());
        return WorldHopper.hopWorld(Wlords.get(0));
    }

    public static boolean IsAreaBusy(int max, boolean OnlyActive)
    {
        List<Player> players = null;
        if(OnlyActive)
        {
            players = Players.all(t -> t.isInCombat() || t.isInteractedWith());
        }
        else
        {
            players = Players.all();
        }
        return players.size() > max;
    }

    public static boolean ExamineInventoryItem(int ID, int timeout)
    {
        Inventory.interact(ID, "Examine");
        return Sleep.sleepUntil(() -> !Mouse.isMouseDragging(), timeout);
    }

    public static boolean ExamineInventoryItemBySlot(int slot, int timeout)
    {
        Inventory.slotInteract(slot, "Examine");
        return Sleep.sleepUntil(() -> !Mouse.isMouseDragging(), timeout);
    }

    public static void ExamineRandomInventoryItem()
    {
        if(Inventory.isEmpty())
        {
            return;
        }

        int Count = Inventory.all().size();
        int Slot  = Inventory.all().get(rand.nextInt(Count - 1)).getSlot();
        ExamineInventoryItemBySlot(Slot, 3000);
    }

    public static boolean CanReachBank()
    {
        BankLocation near = BankLocation.getNearest();
        return near.canReach();
    }

    public static boolean CanReachBank(BankLocation location)
    {
        if(location == null)
        {
            return CanReachBank();
        }
        return location.canReach();
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
        while(true)
        {
            MouseSettings.setSpeed(Math.round((rand.nextInt(Variance) + minSpeed) * SpeedMultiplier));
            Sleep.sleep(rand.nextInt(10000) + 1000);
        }
    }

    public static Thread StartRandomizerThread(int minSpeed, float SpeedMultiplier, int Variance)
    {
        Logger.log("Thread started");
        Thread thr = new Thread(() -> Randomize(minSpeed, SpeedMultiplier, Variance));
        thr.start();
        return thr;
    }

    public static Thread StartRandomizerThread()
    {
        Logger.log("Thread started");
        Thread thr = new Thread(() -> Randomize(10, 1.0f, 30));
        thr.start();
        return thr;
    }

    public static void Wait(int MinimumMs, int VarianceMs)
    {
        Sleep.sleep(rand.nextInt(VarianceMs) + MinimumMs);
    }

    public static void Wait()
    {
        Wait(500, 2000);
    }

    public static void Wait(ScriptIntenity Intensity, float Multiplier)
    {
        switch(Intensity)
        {
            case Lax ->
            {
                Wait(2000, 10000);
            }
            case Normal ->
            {
                Wait(1000, 5000);
            }
            case Sweating ->
            {
                Wait(300, 1000);
            }
            case Bot ->
            {
                Wait(100, 500);
            }
        }
    }

    public static int WaitTime(ScriptIntenity Intensity)
    {
        switch(Intensity)
        {
            case Lax ->
            {
                return rand.nextInt(1500) + 1500;
            }
            case Normal ->
            {
                return rand.nextInt(1000) + 1000;
            }
            case Sweating ->
            {
                return rand.nextInt(1000) + 300;
            }
            case Bot ->
            {
                return rand.nextInt(500) + 100;
            }
        }
        Logger.log(
                "public static int WaitTime(ScriptIntenity Intensity): Something went wrong, not supposed to reach this");
        return 500;
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
                else if(Dialogues.inDialogue())
                {
                    while(Dialogues.inDialogue())
                    {
                        Dialogues.continueDialogue();
                        Wait(300, 500);
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

    public static Boolean IsTimeElapsed(long UID, int ms)
    {
        int ThreadID   = Thread.currentThread().hashCode();
        int FunctionID = Thread.currentThread().getStackTrace()[1].hashCode();
        ;

        CRC32 CRC = new CRC32();
        CRC.update(ThreadID);
        CRC.update(FunctionID);
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(UID);
        CRC.update(buffer.position(0));
        long hash = CRC.getValue();
        if(TimeStamps.containsKey(hash))
        {
            long TimeStamp = TimeStamps.get(hash);

            if(System.nanoTime() - TimeStamp > TimeUnit.MICROSECONDS.toNanos(ms))
            {
                return true;
            }
        }
        TimeStamps.put(hash, System.nanoTime());
        return false;
    }

    // TODO apply these presets to the randomizers and click operations
    public enum ScriptIntenity
    {
        Lax,
        Normal,
        Sweating,
        Bot
    }

    public static class BankEntry
    {
        /**
         * -1 for whole inventory, only valid for deposit
         */
        int ItemID;
        /**
         * -1 for all instances of ID
         */
        int Amount;
        /**
         * 0 for general dump
         */
        int BankTab;

        public BankEntry(int ID, int Amount, int Tab)
        {
            ItemID      = ID;
            this.Amount = Amount;
            BankTab     = Tab;
        }

        public BankEntry(int ID, int Amount)
        {
            ItemID      = ID;
            this.Amount = Amount;
            BankTab     = 0;
        }

        public BankEntry(AbstractMap.SimpleEntry<Integer, Integer> entry)
        {
            ItemID      = entry.getKey();
            this.Amount = entry.getValue();
            BankTab     = 0;
        }

        @Override
        public String toString()
        {
            return "ID: " + ItemID + ", Amount: " + Amount + ", Tab: " + BankTab;
        }
    }

}