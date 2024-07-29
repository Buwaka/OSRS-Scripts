package OSRSDatabase;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import io.vavr.Tuple2;
import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.SceneObject;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class WoodDB extends OSRSDataBase
{
    final private static String                               WoodDBPath = "woodDB.json";
    private static       ConcurrentHashMap<Integer, WoodData> WoodDBMap  = null;

    public enum WoodType
    {
        @SerializedName("Logs") Logs("Logs"),
        @SerializedName("Oak logs") Oak("Oak logs"),
        @SerializedName("Willow logs") Willow("Willow logs"),
        @SerializedName("Maple logs") Maple("Maple logs"),
        @SerializedName("Yew logs") Yew("Yew logs"),
        @SerializedName("Magic logs") Magic("Magic logs"),
        @SerializedName("Redwood logs") Redwood("Redwood logs"),
        @SerializedName("Teak logs") Teak("Teak logs"),
        @SerializedName("Mahogany logs") Mahogany("Mahogany logs");

        public final String value;

        WoodType(String type)
        {
            this.value = type;
        }
    }

    public static class WoodData
    {
        public           int      id;
        public           String   name;
        public           boolean  member;
        public           int      level;
        public           float    exp;
        public           int      noted_id;
        public           String[] trees;
        public           boolean  pyre;
        public @Nullable Integer  pyre_dose;
        public @Nullable Integer  pyre_id;
        public           boolean  burnable;
        public @Nullable WoodType type_enum;

        public String toString()
        {
            return _toString(this, this.getClass());
        }
    }

    public final class CollisionDataFlag
    {
        /**
         * Directional movement blocking flags.
         */
        public static final int BLOCK_MOVEMENT_NORTH_WEST = 0x1;
        public static final int BLOCK_MOVEMENT_NORTH      = 0x2;
        public static final int BLOCK_MOVEMENT_NORTH_EAST = 0x4;
        public static final int BLOCK_MOVEMENT_EAST       = 0x8;
        public static final int BLOCK_MOVEMENT_SOUTH_EAST = 0x10;
        public static final int BLOCK_MOVEMENT_SOUTH      = 0x20;
        public static final int BLOCK_MOVEMENT_SOUTH_WEST = 0x40;
        public static final int BLOCK_MOVEMENT_WEST       = 0x80;

        /**
         * Movement blocking type flags.
         */
        public static final int BLOCK_MOVEMENT_OBJECT           = 0x100;
        public static final int BLOCK_MOVEMENT_FLOOR_DECORATION = 0x40000;
        public static final int BLOCK_MOVEMENT_FLOOR            = 0x200000; // Eg. water
        public static final int BLOCK_MOVEMENT_FULL             =
                BLOCK_MOVEMENT_OBJECT | BLOCK_MOVEMENT_FLOOR_DECORATION | BLOCK_MOVEMENT_FLOOR;

        /**
         * Directional line of sight blocking flags.
         */
        public static final int BLOCK_LINE_OF_SIGHT_NORTH = BLOCK_MOVEMENT_NORTH << 9; // 0x400
        public static final int BLOCK_LINE_OF_SIGHT_EAST  = BLOCK_MOVEMENT_EAST << 9; // 0x1000
        public static final int BLOCK_LINE_OF_SIGHT_SOUTH = BLOCK_MOVEMENT_SOUTH << 9; // 0x4000
        public static final int BLOCK_LINE_OF_SIGHT_WEST  = BLOCK_MOVEMENT_WEST << 9; // 0x10000
        public static final int BLOCK_LINE_OF_SIGHT_FULL  = 0x20000;
    }

    public static List<WoodData> GetBurnableLogs(int FireMakingLevel, boolean isMember)
    {
        List<WoodData> out = new ArrayList<>();

        for(var wood : WoodDBMap.values())
        {
            if(GetFireMakingLevel(wood.id) <= FireMakingLevel && wood.burnable &&
               (!wood.member || isMember))
            {
                out.add(wood);
            }
        }
        return out;
    }

    public static Tuple2<WoodType, WoodData> GetBestFireMakingLog(int FireMakingLevel, boolean isMember)
    {
        if(WoodDBMap == null)
        {
            ReadWoodDB();
        }

        WoodData out = GetWoodData("Logs");

        for(var wood : WoodDBMap.values())
        {
            if(wood.level <= FireMakingLevel && wood.exp > out.exp && wood.burnable &&
               wood.type_enum != null && (!wood.member || isMember))
            {
                out = wood;
            }
        }
        return new Tuple2<>(out.type_enum, out);
    }

    public static WoodData GetBestWoodCuttingLog(int WoodCuttingLevel)
    {
        if(WoodDBMap == null)
        {
            ReadWoodDB();
        }

        WoodData out = GetWoodData("Logs");

        for(var wood : WoodDBMap.values())
        {
            if(wood.level < WoodCuttingLevel && wood.exp > out.exp)
            {
                out = wood;
            }
        }
        return out;
    }

    public static int GetBurnableLogsInventory(int FireMakingLevel)
    {
        int count = 0;
        for(var item : Inventory.all())
        {
            if(item == null || !item.isValid())
            {
                continue;
            }
            var wood = GetWoodData(item.getID());
            if(wood != null && wood.burnable && FireMakingLevel >= GetFireMakingLevel(wood.id))
            {
                count++;
            }
        }
        return count;
    }

    public static int GetFireMakingLevel(int LogID)
    {
        return GetWoodCuttingLevel(LogID);
    }

    public static Tile[] GetFireMakingPositions(int FireMakingLevel, Area StartingPosition)
    {
        return GetFireMakingPositions(FireMakingLevel, StartingPosition.getCenter());
    }

    public static Tile[] GetFireMakingPositions(int FireMakingLevel, Tile StartingTile)
    {
        final int PerfectWidth       = Math.max(GetBurnableLogsInventory(FireMakingLevel), 3);
        final int MaxWalkingDistance = 20 + PerfectWidth;
        var       Options            = MostConsecutiveTiles(StartingTile);
        var       inOrder            = Options.reversed();
        var       last               = inOrder.pollFirstEntry();

        TreeMap<Double, Tile[]> ViableOptions = new TreeMap<>();
        while(last != null && last.getKey() >= PerfectWidth)
        {
            if(last.getValue() != null && last.getValue().length != 0)
            {
                double WalkingDistance = last.getValue()[0].walkingDistance(StartingTile);
                if(WalkingDistance > MaxWalkingDistance)
                {
                    Logger.log("GetFireMakingPositions: Length = " + last.getKey() + " " +
                               Arrays.toString(last.getValue()));
                    ViableOptions.put(WalkingDistance, last.getValue());
                }
                else
                {
                    Options.remove(last.getKey());
                }
            }

            last = inOrder.pollFirstEntry();
        }

        Logger.log(inOrder.entrySet().toString());
        Logger.log(ViableOptions.entrySet().toString());

        if(!ViableOptions.isEmpty())
        {
            Logger.log("GetFireMakingPositions: distance = " + ViableOptions.firstEntry().getKey() +
                       " " + Arrays.toString(ViableOptions.firstEntry().getValue()));
            return ViableOptions.firstEntry().getValue();
        }
        else
        {
            var closestTiles = Options.values().stream().sorted((x, y) -> {
                int a = y.length == 0
                        ? Integer.MAX_VALUE
                        : Arrays.stream(y).max((t, p) -> p.getY() - t.getY()).get().getY();
                int b = x.length == 0
                        ? Integer.MAX_VALUE
                        : Arrays.stream(x).max((t, p) -> p.getY() - t.getY()).get().getY();

                return b - a;
            });
            List<Tile> out   = new ArrayList<>();
            int        count = 0;
            var        iter  = closestTiles.iterator();
            Tile[]     tile;
            while(count < PerfectWidth && iter.hasNext())
            {
                tile = iter.next();
                if(tile == null || tile.length == 0)
                {
                    continue;
                }
                out.addAll(List.of(tile));
                count += tile.length;
                Logger.log("GetFireMakingPositions: y distance = " + tile[0].getY() + " " +
                           Arrays.toString(tile));
            }
            return out.toArray(new Tile[0]);
        }
    }

    public static WoodData GetMoneyEfficientPyreLog(boolean ignoreLogPrice, boolean IgnoreLowVolume)
    {
        if(WoodDBMap == null)
        {
            ReadWoodDB();
        }


        final int SacredOilID        = 3430;
        final int SacredOilPrice     = OSRSPrices.GetAveragePrice(SacredOilID);
        final int SacredOilDosePrice = SacredOilPrice / 4;


        List<Tuple2<Integer, WoodData>> woods = new ArrayList<>();
        for(var wood : WoodDBMap.values())
        {

            if(wood.pyre)
            {
                Logger.log("IsLowVolume: " + OSRSPrices.isLowVolume(wood.pyre_id));
                if(!IgnoreLowVolume || !OSRSPrices.isLowVolume(wood.pyre_id))
                {
                    int LogPrice  = ignoreLogPrice ? 0 : OSRSPrices.GetAveragePrice(wood.id);
                    int OilPrice  = SacredOilDosePrice * wood.pyre_dose;
                    int PyreValue = OSRSPrices.GetAveragePrice(wood.pyre_id);
                    int profit    = PyreValue - (LogPrice + OilPrice);
                    Logger.log(wood.name + ": " + profit + " = " + PyreValue + " - (" + LogPrice +
                               " + " + OilPrice + ")");
                    woods.add(new Tuple2<>(profit, wood));
                }
            }
        }
        woods.sort((t1, t2) -> t2._1.compareTo(t1._1));
        return woods.getFirst()._2;
    }

    public static int GetPyreLevel(int LogID)
    {
        WoodData data = GetWoodData(LogID);

        if(data != null)
        {
            return data.level + 5;
        }
        return 101;
    }

    public static int GetWoodCuttingLevel(int LogID)
    {
        WoodData data = GetWoodData(LogID);

        if(data != null)
        {
            return data.level;
        }
        return 101;
    }

    public static WoodData GetWoodData(String Name)
    {
        if(WoodDBMap == null)
        {
            ReadWoodDB();
        }

        return WoodDBMap.search(1, (key, val) -> val.name.equalsIgnoreCase(Name) ? val : null);
    }

    public static WoodData GetWoodData(int ID)
    {
        if(WoodDBMap == null)
        {
            ReadWoodDB();
        }

        return WoodDBMap.get(ID);
    }

    public static WoodData GetWoodData(WoodType type)
    {
        return GetWoodData(type.value);
    }

    public static TreeMap<Integer, Tile[]> MostConsecutiveTiles(Tile tile)
    {
        final int SearchHeight = 20;
        final int SearchWidth  = 35;
        int       y            = tile.getY();
        Logger.log("WoodDB: MostConsecutiveTiles: y = " + y + ", x = " + tile.getX());
        TreeMap<Integer, Tile[]> out = new TreeMap<>();
        for(int i = y - SearchHeight; i < y + SearchHeight; i++)
        {
            int        CurrentMax   = 0;
            int        count        = 0;
            int        TileY        = i;
            List<Tile> MaxTiles     = new ArrayList<>();
            List<Tile> CurrentTiles = new ArrayList<>();
            for(int j = -SearchWidth; j < SearchWidth; j++)
            {
                int  TileX   = tile.getX() + j;
                Tile Current = new Tile(TileX, TileY);
                if(isTileBurnableAndWithinReach(Current))
                {
                    count++;
                    CurrentTiles.add(Current);
                }
                else
                {
                    if(count > CurrentMax)
                    {
                        CurrentMax = count;
                        MaxTiles   = CurrentTiles;
                    }
                    CurrentTiles = new ArrayList<>();
                    count        = 0;
                }
            }
            out.put(CurrentMax, MaxTiles.reversed().toArray(new Tile[0]));
        }
        return out;
    }

    public static boolean isTileBurnable(Tile tile)
    {
        if(tile == null) {return false;}

        var objs = GameObjects.getObjectsOnTile(tile);
        boolean nonematch = Arrays.stream(objs)
                                  .noneMatch((t) -> t.getClass()
                                                     .isAssignableFrom(SceneObject.class));


        boolean isUnderRoof = isUnderRoof(tile);
        boolean isSolid     = isTileSolid(tile);


        //Logger.log("isTileBurnable: " + isUnderRoof + isSolid + objs.length + nonematch);

        return !isUnderRoof && !isSolid && nonematch;
    }

    public static boolean isTileBurnableAndWithinReach(Tile tile)
    {
        return tile != null && tile.canReach() && isTileBurnable(tile) && tile.walkingDistance(
                Players.getLocal().getTile()) < 100;
    }

    public static boolean isTileSolid(Tile tile)
    {
        var Collissionmap = Client.getCollisionMaps();

        int x = tile.getGridX();
        int y = tile.getGridY();
        int z = Client.getPlane();
        //Logger.log("isTileSolid: x " + x + " y " + y + " z " + z + Collissionmap.length);

        return Collissionmap[Client.getPlane()].isSolid(x, y);
    }

    public static boolean isUnderRoof(Tile t)
    {
        final int  TILE_FLAG_UNDER_ROOF = 4;
        final int  TILE_SETTINGS_MAX_Z  = 4;
        byte[][][] tileSettings         = Client.getTileSettings();


        // Very unsure about this!
        int x = t.getGridX();
        int y = t.getGridY();
        int z = Client.getPlane();

        if(x < 0 || x >= tileSettings[0].length || y < 0 || y >= tileSettings[0][0].length ||
           z < 0 || z > tileSettings.length)
        {
            // Tile is outside of scope
            return false;
        }

        if((tileSettings[z][x][y] & TILE_FLAG_UNDER_ROOF) == TILE_FLAG_UNDER_ROOF)
        {
            return true;
        }

        return false;
    }

    private static void ReadWoodDB()
    {
        WoodDBMap = new ConcurrentHashMap<>();

        try
        {
            InputStreamReader File   = new InputStreamReader(GetInputStream(WoodDBPath));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = new Gson();
            Reader.setLenient(true);

            Reader.beginObject();

            while(Reader.hasNext())
            {
                int      ID  = Integer.parseInt(Reader.nextName());
                WoodData Obj = gson.fromJson(Reader, WoodData.class);
                WoodDBMap.put(ID, Obj);
            }

            Reader.endObject();
            Reader.close();

        } catch(Exception e)
        {
            Logger.log("Error reading HerbDB, Exception: " + e);
            throw new RuntimeException(e);
        }
    }
}
