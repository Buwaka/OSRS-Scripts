package IF.Utilities;

import IF.OSRSDatabase.FoodDB;
import IF.OSRSDatabase.ItemDB;
import IF.Utilities.Combat.CombatManager;
import IF.Utilities.Patterns.GameTickDelegate;
import IF.Utilities.Requirement.IRequirement;
import IF.Utilities.Scripting.IScript;
import IF.Utilities.Scripting.Logger;
import IF.Utilities.Serializers.*;
import com.google.gson.GsonBuilder;
import io.vavr.Tuple2;
import org.dreambot.api.Client;
import org.dreambot.api.data.consumables.Food;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.bank.BankMode;
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
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.walking.path.impl.LocalPath;
import org.dreambot.api.methods.walking.pathfinding.impl.local.LocalPathFinder;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Character;
import org.dreambot.api.wrappers.interactive.*;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.awt.*;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.CRC32;


public class OSRSUtilities
{
    public static final  int                           Tick                = 600;
    public static final  int                           InventorySpace      = 28;
    static final         String                        AttackAction        = "Attack";
    static final         String[]                      ItemsToAlwaysPickUp = {
            "Clue scroll", "Coins"};
    private static final ConcurrentHashMap<Long, Long> TimeStamps          = new ConcurrentHashMap<Long, Long>();
    public static        GsonBuilder                   OSRSGsonBuilder     = new GsonBuilder().setPrettyPrinting()
                                                                                              .setLenient()
                                                                                              .disableHtmlEscaping()
                                                                                              .excludeFieldsWithModifiers(
                                                                                                      Modifier.STATIC,
                                                                                                      Modifier.TRANSIENT);
    public static        Random                        rand                = new Random();
    //    private static       HashMap<String, CacheManager> CacheManagers       = new HashMap<>();
    //        public static DB     CacheDB;

    static
    {
        OSRSGsonBuilder.registerTypeAdapter(Area.class, new AreaSerializer());
        OSRSGsonBuilder.registerTypeAdapter(Tile.class, new TileSerializer());
        OSRSGsonBuilder.registerTypeAdapter(IRequirement.class, new RequirementSerializer());
        OSRSGsonBuilder.registerTypeAdapter(SerializableSupplier.class,
                                            new SerializableSupplierSerializer<>());
        OSRSGsonBuilder.registerTypeAdapter(ItemDB.Requirement.class,
                                            new ItemDB.Requirement.RequirementDeserializer());
        OSRSGsonBuilder.registerTypeAdapter(ItemDB.Requirement.class,
                                            new ItemDB.Requirement.RequirementSerializer());
        OSRSGsonBuilder.registerTypeAdapter(SerializableRunnable.class,
                                            new SerializableRunnableSerializer());

        //Path ScriptFolder = PlayerConfig.GetScriptConfigFolder();
        //                CacheDB = DBMaker.fileDB(ScriptFolder + "\\Cache.db").closeOnJvmShutdown().fileChannelEnable().executorEnable().fileLockDisable().make();
    }

    //    public static <Key, Value> Cache<Key, Value> GetCache(String Alias, Class<Key> key, Class<Value> value)
    //    {
    //        CacheManager cacheManager;
    //        if(CacheManagers.containsKey(Alias))
    //        {
    //            cacheManager = CacheManagers.get(Alias);
    //        }
    //        else
    //        {
    //            Path ScriptFolder = PlayerConfig.GetScriptConfigFolder();
    //            File CacheFile    = new File(String.valueOf(ScriptFolder), Alias);
    //            Logger.log("OSRSUtilities: GetCache: ScriptFolder:" + CacheFile + " '" + ScriptFolder +
    //                       "'");
    //            var builder = CacheManagerBuilder.newCacheManagerBuilder()
    //                                             .with(CacheManagerBuilder.persistence(CacheFile))
    //                                             .using(new DefaultLocalPersistenceService(new DefaultPersistenceConfiguration(
    //                                                     CacheFile)));
    //            var resourcePool = ResourcePoolsBuilder.heap(256).disk(128, MemoryUnit.MB).build();
    //            var ExpiryPolicy = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofHours(6));
    //            var WriteBehindService = WriteBehindConfigurationBuilder.newBatchedWriteBehindConfiguration(
    //                    1,
    //                    TimeUnit.SECONDS,
    //                    4).build();
    //            var CacheConfiguration = CacheConfigurationBuilder.newCacheConfigurationBuilder(key,
    //                                                                                            value,
    //                                                                                            resourcePool)
    //                                                              .withExpiry(ExpiryPolicy)
    //                                                              .withService(WriteBehindService)
    //                                                              .build();
    //            cacheManager = builder.withCache(Alias, CacheConfiguration).build(true);
    //            CacheManagers.put(Alias, cacheManager);
    //            cacheManager.close();
    //        }
    //        return cacheManager.getCache(Alias, key, value);
    //    }


    public enum ScriptIntenity
    {
        Lax,
        Normal,
        Sweating,
        Bot
    }

    public static class BankEntry implements Serializable
    {
        @Serial
        private static final long serialVersionUID = -110662990986422439L;
        /**
         * -1 for whole inventory, only valid for deposit
         */
        int     ItemID;
        /**
         * INT_MAX for all instances of ID
         */
        int     Amount;
        /**
         * 0 for general dump
         */
        int     BankTab;
        boolean Noted = false;

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

        public BankEntry(Tuple2<Integer, Integer> entry)
        {
            ItemID      = entry._1;
            this.Amount = entry._2;
            BankTab     = 0;
        }

        public BankEntry(int ID, int Amount, int Tab, boolean noted)
        {
            ItemID      = ID;
            this.Amount = Amount;
            BankTab     = Tab;
            Noted       = noted;
        }

        public BankEntry(int ID, int Amount, boolean noted)
        {
            ItemID      = ID;
            this.Amount = Amount;
            BankTab     = 0;
            Noted       = noted;
        }

        public BankEntry(AbstractMap.SimpleEntry<Integer, Integer> entry, boolean noted)
        {
            ItemID      = entry.getKey();
            this.Amount = entry.getValue();
            BankTab     = 0;
            Noted       = noted;
        }

        public int GetCount() {return Amount;}

        public static ArrayList<BankEntry> CreateBankEntries(List<Tuple2<Integer, Integer>> Entries)
        {
            ArrayList<BankEntry> out = new ArrayList<>();
            for(var entry : Entries)
            {
                out.add(new BankEntry(entry));
            }
            return out;
        }

        public static ArrayList<BankEntry> CreateBankEntries(Tuple2<Integer, Integer>... Entries)
        {
            ArrayList<BankEntry> out = new ArrayList<>();
            for(var entry : Entries)
            {
                out.add(new BankEntry(entry));
            }
            return out;
        }

        @Override
        public String toString()
        {
            return "ID: " + ItemID + ", Amount: " + Amount + ", Tab: " + BankTab;
        }
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

    public static Point GetCenterPointRectangle(Rectangle rect, boolean randomize)
    {
        Point point = new Point((int) rect.getCenterX(), (int) rect.getCenterY());
        point = RandomizeClick(point);
        return point;
    }

    public static Point RandomizeClick(Point Click)
    {
        return RandomizeClick(Click, 5, 5);
    }

    public static Point RandomizeClick(Point Click, int VarianceX, int VarianceY)
    {
        Click.translate(rand.nextInt(VarianceX / 2) - (VarianceX / 2),
                        rand.nextInt(VarianceY / 2) - (VarianceY / 2));
        return Click;
    }

    public static boolean BankDepositAll(int... Except)
    {
        if(Inventory.isEmpty())
        {
            return true;
        }

        boolean result = true;
        result &= Sleep.sleepUntil(() -> Bank.open(), 60000);
        int Attempts    = 0;
        int MaxAttempts = 5;
        while(!Sleep.sleepUntil(() -> Bank.open(), 60000) && Attempts < MaxAttempts)
        {
            Attempts++;
        }
        if(Attempts > MaxAttempts)
        {
            return false;
        }

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

    public static boolean BankContainsHeals()
    {
        return true;
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
        while(Players.getLocal().getHealthPercent() < 100 && Inventory.contains(FoodID) &&
              food != null)
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

    public static boolean OpenBank()
    {
        if(Bank.isOpen())
        {
            Logger.log("OpenBank: Bank is open");
            return true;
        }

        //        if(!CanReachBank())
        //        {
        //            Logger.log("Can't reach bank");
        //            return false;
        //        }

        while(!Bank.isOpen())
        {
            Bank.open();
            Sleep.sleepUntil(() -> Bank.isOpen(), 5000);
        }

        return true;
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

    public static boolean CanReachBank(BankLocation location)
    {
        if(location == null)
        {
            return CanReachBank();
        }
        return location.canReach();
    }

    public static boolean CanReachBank()
    {
        BankLocation near = Bank.getClosestBankLocation();
        Logger.log("Nearest Bank: " + near.name() + " dist: " +
                   near.walkingDistance(Players.getLocal().getTile()));
        return near.canReach() || (near == BankLocation.GRAND_EXCHANGE &&
                                   near.walkingDistance(Players.getLocal().getTile()) < 25);
    }

    public static boolean CheckInventory(List<AbstractMap.SimpleEntry<Integer, Integer>> Requirements, boolean OnlyRequirements)
    {
        if(Requirements == null || Requirements.isEmpty())
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
            var Inv = Inventory.all(t -> Requirements.stream()
                                                     .noneMatch(x -> x.getKey() != t.getID()));
            Logger.log(Inv.size() + " unnecessary items (CheckInventory)");
            return Inv.isEmpty();
        }

        return true;
    }

    public static boolean CheckRequirements(List<Tuple2<Integer, Integer>> itemRequirements, boolean CheckBank)
    {
        for(var req : itemRequirements)
        {
            int bankCount = CheckBank ? Bank.count(req._1) : 0;
            int invCount  = Inventory.count(req._1);
            if(invCount + bankCount < req._2)
            {
                return false;
            }
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


        List<AbstractMap.SimpleEntry<Integer, Integer>> Requirements = new ArrayList<>(
                IDAmountPair.length / 2);
        for(int i = 0; i < IDAmountPair.length; i += 2)
        {
            int ID     = IDAmountPair[i];
            int amount = IDAmountPair[i + 1];
            Requirements.add(new AbstractMap.SimpleEntry<>(ID, amount));
        }

        return Requirements;
    }

    public static boolean ExamineInventoryItem(int ID, int timeout)
    {
        Inventory.interact(ID, "Examine");
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

    public static boolean ExamineInventoryItemBySlot(int slot, int timeout)
    {
        Inventory.slotInteract(slot, "Examine");
        return Sleep.sleepUntil(() -> !Mouse.isMouseDragging(), timeout);
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
            Sleep.sleepUntil(() -> (Players.getLocal().getAnimation() == -1 &&
                                    !Players.getLocal().isMoving()) || Dialogues.inDialogue(),
                             120000);

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

    public static void Wait(int MinimumMs, int VarianceMs)
    {
        Sleep.sleep(rand.nextInt(VarianceMs) + MinimumMs);
    }

    public static List<Character> GetAllCharactersInteractingWith(Character Target)
    {
        List<Character> result = new ArrayList<>();
        var             all    = NPCs.all();
        for(Character npc : all)
        {
            if(npc.isInteracting(Target))
            {
                result.add(npc);
            }
        }

        return result;
    }

    public static List<AbstractMap.SimpleEntry<Integer, Integer>> GetBestFoodChoice(int TotalHP)
    {
        if(Bank.isCached())
        {
            var CommonFood = FoodDB.GetCommonFoods(Client.isMembers());
            Logger.log(CommonFood);
            SortedMap<Integer, FoodDB.Food> Choice = new TreeMap<>();// score // ID
            for(var food : CommonFood)
            {
                var count = Bank.count(food.id);
                Choice.put(count * food.hitpoints, food);
            }
            Logger.log(Arrays.toString(Choice.entrySet().toArray()));

            var                                             reversed = Choice.reversed();
            List<AbstractMap.SimpleEntry<Integer, Integer>> out      = new ArrayList<>();
            for(var food : reversed.entrySet())
            {
                Integer count = (int) Math.min(Math.ceil(
                                                       (double) TotalHP / food.getValue().hitpoints),
                                               Bank.count(food.getValue().id));
                out.add(new AbstractMap.SimpleEntry<>(food.getValue().id, count));
                Logger.log(count * food.getValue().hitpoints + " >= " + TotalHP);
                if(count * food.getValue().hitpoints >= TotalHP)
                {
                    return out;
                }
            }
        }
        return null;
    }

    public static NPC GetClosestAttackableEnemy(String... Names)
    {

        return NPCs.closest(t -> Arrays.stream(Names)
                                       .anyMatch(x -> Objects.equals(x, t.getName())) &&
                                 t.hasAction(AttackAction) && !t.isInteractedWith() &&
                                 t.canAttack() && t.canReach());
    }

    public static int[] GetFoodIDs()
    {
        return new int[]{315};
    }

    public static List<GroundItem> GetLootItems(Area area)
    {
        var Items = GroundItems.all(t -> area.contains(t.getTile()) ||
                                         Arrays.stream(ItemsToAlwaysPickUp)
                                               .anyMatch(x -> x.contains(t.getName())));
        // Always pickup clue scrolls lol
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
                                          Arrays.stream(ItemsToAlwaysPickUp)
                                                .anyMatch(x -> x.contains(t.getName())))); // Always pickup clue scrolls lol
        Items.sort(Comparator.comparingDouble(p -> {
            double dist = p.walkingDistance(Players.getLocal().getTile());

            return Math.abs(dist);
        }));

        return Items;
    }

    public static int GetMissingHP()
    {
        return (Skills.getRealLevel(Skill.HITPOINTS) - Skills.getBoostedLevel(Skill.HITPOINTS));
    }

    public static WidgetChild GetSkillingMenu()
    {
        WidgetChild child = Widgets.get(270, 0);
        if(child != null && child.isVisible())
        {
            return child;
        }
        Logger.log("OSRSUtilities: Failed to get child item, " + child);
        return null;
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

    public static BankLocation GetValidBank()
    {
        // TODO ask pandemic if getnearest also cares about the bank being valid
        return null;
    }

    public static int HPtoPercent(int HP)
    {
        return (int) (((float) HP / (float) Skills.getRealLevel(Skill.HITPOINTS)) * 100);
    }

    // Heal once
    public static boolean Heal()
    {
        int FoodID = Food.getBestOnHand(true).getFromInventory().getID();
        Inventory.interact(FoodID);
        Sleep.sleepTicks(3);
        return Sleep.sleepUntil(() -> Players.getLocal().getAnimation() == -1, 5000);
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

    public static boolean InventoryContainsAnyFoods(boolean Member)
    {
        var foods = FoodDB.GetCommonFoods(Member);
        for(var food : foods)
        {
            if(Inventory.contains(food.id))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean InventoryContainsPrayables()
    {
        final String BuryAction    = "Bury";
        final String ScatterAction = "Scatter";
        return (Inventory.contains(t -> t.hasAction(BuryAction)) ||
                Inventory.contains(t -> t.hasAction(ScatterAction)));
    }

    public static int InventoryHPCount()
    {
        var AllFood = Inventory.all(t -> FoodDB.isFood(t.getID()));

        int TotalHP = 0;
        for(var food : AllFood)
        {
            TotalHP += FoodDB.GetFood(food.getID()).hitpoints;
        }

        return TotalHP;
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

    public static boolean IsLootLeft(Area LootArea, int... LootExcepts)
    {
        Tile[] Tiles = LootArea.getTiles();
        for(Tile tile : Tiles)
        {
            if(GroundItems.getForTile(tile)
                          .stream()
                          .anyMatch(t -> Arrays.stream(LootExcepts).anyMatch(x -> t.getID() == x)))
            {
                return true;
            }
        }
        return false;
    }

    public static Boolean IsTimeElapsed(long UID, int ms)
    {
        int ThreadID   = Thread.currentThread().hashCode();
        int FunctionID = Thread.currentThread().getStackTrace()[1].hashCode();

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

    public static boolean JumpToOtherWorld()
    {
        List<World> Wlords = Worlds.all(t -> t.isF2P() && !t.isHighRisk() && !t.isPVP() &&
                                             t.isNormal() &&
                                             !t.getDescription().contains("skill total") &&
                                             t != Worlds.getCurrent());
        Wlords.sort(Comparator.comparingInt(t -> t.getPopulation()));

        for(int i = 0; i < Wlords.size(); i++)
        {
            Logger.log("World " + i + " " + Wlords.get(i).getPopulation());
        }

        WorldHopper.openWorldHopper();
        Logger.log("Changing to world " + Wlords.get(0).getWorld());
        boolean result = WorldHopper.hopWorld(Wlords.get(0));
        Wait();
        return result;
    }

    public static void Wait()
    {
        Wait(500, 2000);
    }

    public static boolean JumpToOtherWorld(GameTickDelegate GameTick)
    {
        List<World> Wlords = Worlds.all(t -> t.isF2P() && !t.isHighRisk() && !t.isPVP() &&
                                             t.isNormal() &&
                                             !t.getDescription().contains("skill total") &&
                                             t != Worlds.getCurrent());
        Wlords.sort(Comparator.comparingInt(t -> t.getPopulation()));

        for(int i = 0; i < Wlords.size(); i++)
        {
            Logger.log("World " + i + " " + Wlords.get(i).getPopulation());
        }

        WorldHopper.openWorldHopper();
        Logger.log("Changing to world " + Wlords.get(0).getWorld());
        boolean result = WorldHopper.hopWorld(Wlords.get(0));
        GameTick.WaitTicks(2);
        return result;
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

    public static boolean PickupOnArea(Area area, int... IDs)
    {
        var Items = GroundItems.all(t -> area.contains(t.getTile()) &&
                                         (Arrays.stream(IDs).anyMatch(x -> x != t.getID()) ||
                                          Arrays.stream(ItemsToAlwaysPickUp)
                                                .anyMatch(x -> x.contains(t.getName())))); // Always pickup clue scrolls lol
        Items.sort(Comparator.comparingDouble(p -> {
            double dist = p.walkingDistance(Players.getLocal().getTile());
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
                    result &= Sleep.sleepUntil(() -> !Players.getLocal().isMoving() &&
                                                     !Item.exists(), 15000);
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

    public static boolean PickupOnTile(Tile tile, List<Integer> IDs)
    {

        var Items = GroundItems.all(t -> t.getTile() == tile && (IDs.contains(t.getID()) ||
                                                                 Arrays.stream(ItemsToAlwaysPickUp)
                                                                       .anyMatch(x -> x.contains(t.getName()))));// Always pickup clue scrolls lol
        Items.sort(Comparator.comparingDouble(p -> {
            double dist = p.walkingDistance(Players.getLocal().getTile());
            return Math.abs(dist);
        }));
        return PickupItems(Items);
    }

    public static Boolean PrayAll(int Timeout, int... IDs)
    {
        final String BonesAction   = "Bury";
        final String ScatterAction = "Scatter";
        boolean      result        = true;
        long         start         = System.nanoTime();

        Logger.log("PrayAll: Trying to Bury bones");

        while(Inventory.contains(IDs) ||
              (start - System.nanoTime()) > TimeUnit.MICROSECONDS.toNanos(Timeout))
        {
            var item = Inventory.get(IDs);
            if(item.hasAction(BonesAction))
            {
                result &= item.interact(BonesAction);
            }
            else if(item.hasAction(ScatterAction))
            {
                result &= item.interact(ScatterAction);
            }
            else
            {
                result &= item.interact();
            }

            OSRSUtilities.Wait(500, 200);

        }

        return result;
    }

    /**
     * @param Deposits
     * @param Withdraws
     * @param sleepdelay how many ms to wait inbetween each action
     *
     * @return true if successful, false if failed
     */
    public static boolean ProcessBankEntries(IScript Script, List<BankEntry> Deposits, List<BankEntry> Withdraws, int sleepdelay)
    {
        final int retries = 5;
        int       tries   = 0;
        if(OSRSUtilities.OpenBank())
        {
            ArrayList<BankEntry> _deposits =
                    Deposits == null ? new ArrayList<>() : new ArrayList<>(Deposits);
            ArrayList<BankEntry> _withdraws =
                    Withdraws == null ? new ArrayList<>() : new ArrayList<>(Withdraws);
            if(!_withdraws.isEmpty())
            {
                _withdraws.sort(Comparator.comparingInt(BankEntry::GetCount));
            }


            Logger.log("ProcessBankEntries: Deposits: " + Arrays.toString(_deposits.toArray()));
            Logger.log("ProcessBankEntries: Withdraws: " + Arrays.toString(_withdraws.toArray()));
            while((!_deposits.isEmpty() || !_withdraws.isEmpty()))
            {
                boolean success;
                // Deposits first
                if(!_deposits.isEmpty())
                {
                    var deposit = _deposits.getFirst();
                    if(!Inventory.contains(deposit.ItemID) && deposit.ItemID != -1)
                    {
                        Logger.log("ProcessBankEntries: Inventory does not contain item with ID " +
                                   deposit.ItemID + ", skipping deposit");
                        _deposits.removeFirst();
                        continue;
                    }
                    if(Bank.getCurrentTab() != deposit.BankTab)
                    {
                        Bank.openTab(deposit.BankTab);
                    }
                    if(deposit.ItemID == -1)
                    {
                        success = Bank.depositAllItems();
                    }
                    else if(deposit.Amount == Integer.MAX_VALUE)
                    {
                        success = Bank.depositAll(deposit.ItemID);
                    }
                    else
                    {
                        success = Bank.deposit(deposit.ItemID, deposit.Amount);
                    }
                    Logger.log("_deposits complete, success: " + success);
                    if(!success)
                    {
                        tries++;
                        if(tries > retries)
                        {
                            return false;
                        }
                        continue;
                    }
                    tries = 0;
                    _deposits.removeFirst();
                }
                else if(!_withdraws.isEmpty())
                {
                    var withdraw = _withdraws.getFirst();

                    if(!Bank.contains(withdraw.ItemID))
                    {
                        Logger.log("ProcessBankEntries: Bank does not contain item with ID " +
                                   withdraw.ItemID + ", skipping withdraw");
                        _withdraws.removeFirst();
                        continue;
                    }

                    if(withdraw.Noted && Bank.getWithdrawMode() != BankMode.NOTE)
                    {
                        Bank.setWithdrawMode(BankMode.NOTE);
                    }
                    else if(Bank.getWithdrawMode() != BankMode.SWAP)
                    {
                        Bank.setWithdrawMode(BankMode.SWAP);
                    }

                    if(Bank.getCurrentTab() != withdraw.BankTab)
                    {
                        Bank.openTab(withdraw.BankTab);
                    }
                    if(withdraw.Amount > InventorySpace &&
                       !(new Item(withdraw.ItemID, withdraw.Amount).isStackable()))
                    {
                        success = Bank.withdrawAll(withdraw.ItemID);
                    }
                    else
                    {
                        success = Bank.withdraw(withdraw.ItemID, withdraw.Amount);
                    }
                    Logger.log("Withdrawal complete " + success);
                    if(!success)
                    {
                        tries++;
                        if(tries > retries)
                        {
                            return false;
                        }
                        continue;
                    }
                    tries = 0;
                    _withdraws.removeFirst();
                }
                Script.onGameTicked().WaitTicks(1);
            }
            return true;
        }

        return Deposits.isEmpty() && Withdraws.isEmpty();
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
                return !Inventory.contains(ID1) || !Inventory.contains(ID2) ||
                       Dialogues.inDialogue();
            }, timeout);

            Sleep.sleep(rand.nextInt(2000) + 1000);
        }

        return true;
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

    public static boolean PickSkillingMenuItem(int index)
    {
        var child = Widgets.get(270, 13 + index);
        if(child != null && child.isVisible())
        {
            return child.interact();
        }
        Logger.log("PickSkillingMenuItem: Failed to get child item, " + child);
        return false;
    }

    public static void ResetCameraRandom(int timeout)
    {
        Camera.mouseRotateTo(Camera.getYaw() + rand.nextInt(450) + 150, 380);
        Camera.setZoom(rand.nextInt(20) + 181);
        Sleep.sleepUntil(() -> !Mouse.isMouseDragging(), timeout);
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

    //return: whether we're fighting
    public static Character Slaughter(int Timeout, String... Names)
    {
        Set<String> seen = new HashSet<>();
        int[] IDs = NPCs.all(Names)
                        .stream()
                        .filter(t -> seen.add(t.getName()))
                        .mapToInt(NPC::getID)
                        .toArray();
        return Slaughter(Timeout, IDs);
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

    private static Character PickNewTarget(int... IDs)
    {
        Player    player = Players.getLocal();
        Character Foe    = GetClosestAttackableEnemy(IDs);
        if(Foe == null)
        {
            Logger.log("No Foe found");
            return null;
        }
        Logger.log("Foe: " + Foe);
        return CombatManager.GetInstance(player).Fight(Foe);
    }

    public static NPC GetClosestAttackableEnemy(int... IDs)
    {
        //Logger.log("Searching for IDs " + Arrays.toString(IDs));
        return NPCs.closest(t -> Arrays.stream(IDs).anyMatch(x -> x == t.getID()) &&
                                 t.hasAction(AttackAction) && !t.isInteractedWith() &&
                                 t.canAttack() && t.canReach());
    }

    public static boolean SlaughterAndLoot(int Timeout, int LootTimeout, int[] LootExcepts, String... Names)
    {
        Set<Integer> seen = new HashSet<>();
        int[] IDs = NPCs.all(Names)
                        .stream()
                        .filter(t -> seen.add(t.getID()))
                        .mapToInt(NPC::getID)
                        .toArray();
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
            Logger.log("Looting Foe " + Foe);
            ExecutorService Executor = Executors.newSingleThreadExecutor();
            Future<Boolean> result = Executor.submit(() -> PickupOnAreaExcepts(GetLootArea(Foe),
                                                                               LootExcepts));
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

    public static boolean PickupOnAreaExcepts(Area area, int... ExceptIDs)
    {
        return PickupItems(GetLootItemsExclude(area, ExceptIDs));
    }

    public static List<GroundItem> GetLootItemsExclude(Area area, int... ExceptIDs)
    {
        var Items = GroundItems.all(t -> area.contains(t.getTile()) &&
                                         (Arrays.stream(ExceptIDs).anyMatch(x -> x != t.getID()) ||
                                          Arrays.stream(ItemsToAlwaysPickUp)
                                                .anyMatch(x -> x.contains(t.getName())))); // Always pickup clue scrolls lol
        Items.sort(Comparator.comparingDouble(p -> {
            double dist = p.walkingDistance(Players.getLocal().getTile());

            return Math.abs(dist);
        }));

        return Items;
    }

    public static Thread StartRandomizerThread(int minSpeed, float SpeedMultiplier, int Variance)
    {
        Logger.log("Thread started");
        Thread thr = new Thread(() -> Randomize(minSpeed, SpeedMultiplier, Variance));
        thr.start();
        return thr;
    }

    private static void Randomize(int minSpeed, float SpeedMultiplier, int Variance)
    {
        while(true)
        {
            MouseSettings.setSpeed(Math.round(
                    (rand.nextInt(Variance) + minSpeed) * SpeedMultiplier));
            Sleep.sleep(rand.nextInt(10000) + 1000);
        }
    }

    public static Thread StartRandomizerThread()
    {
        Logger.log("Thread started");
        Thread thr = new Thread(() -> Randomize(10, 1.0f, 30));
        thr.start();
        return thr;
    }

    public static void Wait(ScriptIntenity Intensity)
    {
        Wait(Intensity, 1.0f);
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
                Wait(10, 20);
            }
        }
    }

    public static void WaitForEndAnimationLoop(int WaitPerAnimation, int timeout)
    {
        Mouse.moveOutsideScreen(true);
        boolean end = false;
        while(!end)
        {
            if(Sleep.sleepUntil(() -> Players.getLocal().getAnimation() == -1, timeout))
            {
                if(!Sleep.sleepUntil(() -> Players.getLocal().getAnimation() != -1,
                                     WaitPerAnimation))
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

    public static int WaitTime(ScriptIntenity Intensity)
    {
        switch(Intensity)
        {
            case Lax ->
            {
                return WaitTime(1500, 1500);
            }
            case Normal ->
            {
                return WaitTime(1000, 1000);
            }
            case Sweating ->
            {
                return WaitTime(1000, 300);
            }
            case Bot ->
            {
                return 1;
            }
        }
        Logger.log(
                "public static int WaitTime(ScriptIntenity Intensity): Something went wrong, not supposed to reach this");
        return 500;
    }

    public static int WaitTime(int min, int max)
    {
        return rand.nextInt(max) + min;
    }

    public static void WalkTo(Tile Destination)
    {
        Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 10000);

        if(Destination.distance(Players.getLocal().getTile()) <= 1.0)
        {
            Logger.log("OSRSUtilities: Destination Reached");
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

        LocalPath<Tile> Path = LocalPathFinder.getLocalPathFinder()
                                              .calculate(Players.getLocal().getTile(), Destination);
        Logger.log("OSRSUtilities: Path: " + Path.toString());


        if(Path == null || Path.isEmpty())
        {
            Logger.log("OSRSUtilities: No Path found");
            while((Path == null || Path.isEmpty()) &&
                  Destination.distance(Players.getLocal().getTile()) > 1.0)
            {
                SimpleWalkTo_(Destination);
                Path = LocalPathFinder.getLocalPathFinder()
                                      .calculate(Players.getLocal().getTile(), Destination);
            }

        }

        Logger.log("OSRSUtilities: " + Destination.canReach());

        Tile Next = Path.next();
        while(!Path.isEmpty() && Destination.canReach())
        {
            Tile NextNext = Path.getFurthestOnMM();

            Logger.log("OSRSUtilities: " + Destination.canReach());
            Logger.log("OSRSUtilities: " + Destination.distance(Players.getLocal().getTile()));
            Logger.log("OSRSUtilities: PlayerTile: " + Players.getLocal().getTile());

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
                Logger.log("OSRSUtilities: Obstacle");
                Logger.log("OSRSUtilities:" + Path.getObstacleForTile(Next).toString());
                Path.getObstacleForTile(Next).traverse();
                Sleep.sleep(rand.nextInt(2000) + 300);
            }
            else if(Next != null && Next.canReach())
            {
                Logger.log("OSRSUtilities: Clicking");
                Logger.log("OSRSUtilities:" + Next.distance(NextNext));
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
                Logger.log("OSRSUtilities:  Last resort walking");
                Walking.walk(Destination);
                Sleep.sleep(rand.nextInt(1000) + 100);
            }
            Next = NextNext;
        }
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

}