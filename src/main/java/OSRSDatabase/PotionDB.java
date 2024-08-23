package OSRSDatabase;

import Utilities.OSRSUtilities;
import Utilities.Requirement.IRequirement;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.vavr.Function2;
import io.vavr.Tuple2;
import org.dreambot.api.utilities.Logger;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

public class PotionDB extends OSRSDataBase
{
    final private static String                       PotionDBPath = "Skilling/potionDB.json";
    private static final HashMap<Integer, PotionData> PotionDBMap  = new HashMap<>();

    public static class PotionData
    {
        public           int            id;
        public           String         name;
        public @Nullable int[]          portion_ids;
        public           int            base;
        public           int            base_count       = 1;
        public           int      ingredient;
        public @Nullable Tuple2<Integer,Integer>[] ingredient_alternatives;
        public           int      ingredient_count = 1;
        public @Nullable int[]          extra_ingredient;
        public           int            level;
        public           float          experience;
        public @Nullable IRequirement[] extra_requirement;

        public String toString()
        {
            return _toString(this, this.getClass());
        }
    }

    public static PotionData GetPotion(int ID)
    {
        if(PotionDBMap.containsKey(ID))
        {
            return PotionDBMap.get(ID);
        }

        try
        {
            InputStreamReader File   = new InputStreamReader(GetInputStream(PotionDBPath));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = OSRSUtilities.OSRSGsonBuilder.create();
            Reader.setLenient(true);

            Reader.beginObject();

            while(Reader.hasNext())
            {
                int        id  = Integer.parseInt(Reader.nextName());
                PotionData Obj = gson.fromJson(Reader, PotionData.class);
                if(id == ID || (Obj.portion_ids != null &&
                                Arrays.stream(Obj.portion_ids).anyMatch((t) -> t == ID)))
                {
                    PotionDBMap.put(id, Obj);
                    return Obj;
                }
            }
            Reader.endObject();
            Reader.close();

        } catch(Exception e)
        {
            Logger.log("Error reading PotionDB, Exception: " + e);
            return null;
        }
        return null;
    }

    private static void ReadAll()
    {
        try
        {
            InputStreamReader File   = new InputStreamReader(GetInputStream(PotionDBPath));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = OSRSUtilities.OSRSGsonBuilder.create();
            Reader.setLenient(true);

            Reader.beginObject();

            while(Reader.hasNext())
            {
                int        id  = Integer.parseInt(Reader.nextName());
                PotionData Obj = gson.fromJson(Reader, PotionData.class);
                PotionDBMap.put(id, Obj);
            }
            Reader.endObject();
            Reader.close();

        } catch(Exception e)
        {
            Logger.log("Error reading PotionDB, Exception: " + e);
            return;
        }
        return;
    }

    public static int[] GetPortions(int ID)
    {
        // basically any potion id, and the portion being the specific portion variation
        var potion = GetPotion(ID);

        if(potion != null && potion.portion_ids != null)
        {
            Logger.log("PotionDB: GetPortion: Potion with ID " + ID + " found with portions: " +
                       Arrays.toString(potion.portion_ids));
            var           portions = potion.portion_ids;
            List<Integer> out      = new ArrayList<>();
            for(var portion : portions)
            {
                out.add(portion);
            }
            return out.stream().mapToInt(Integer::intValue).toArray();
        }
        Logger.log("PotionDB: GetPortion: Potion with ID " + ID + " not found");
        return null;
    }

    public static ItemDB.ItemData GetPortion(int ID, int Portion)
    {
        // basically any potion id, and the portion being the specific portion variation
        var     portions = GetPortions(ID);
        Pattern brackets = Pattern.compile("[(](\\d*)[)]");

        if(portions != null)
        {
            for(var portion : portions)
            {
                ItemDB.ItemData item = ItemDB.GetItemData(portion);
                if(item != null)
                {
                    var match = brackets.matcher(item.name);
                    if(match.find())
                    {
                        var group = match.group(1);
                        try
                        {
                            int amount = Integer.parseInt(group);
                            if(amount == Portion)
                            {
                                return item;
                            }
                        } catch(Exception ignored)
                        {

                        }
                    }

                }
                else
                {
                    Logger.log("PotionDB: GetPortion: Failed to find item with ID " + portion);
                }

            }
        }
        Logger.log("PotionDB: GetPortion: Potion with ID " + ID +
                   " does not exist or has no portion of " + Portion);
        return null;
    }

    public static boolean isPotion(int ID)
    {
        return GetPotion(ID) != null;
    }

    public static boolean hasPortions(int ID)
    {
        var potion = GetPotion(ID);
        return potion != null && potion.portion_ids != null;
    }

    public static class PotionStep
    {
        public PotionData result;
        PotionStep left       = null;
        PotionStep right      = null;
        Integer    price      = null;
        float      experience = 0;
        boolean    purchase   = false;

        public PotionStep(PotionData result)
        {
            this.result = result;
        }

        public int GetProfit()
        {
            if(price == null)
            {
                Logger.log(
                        "PotionStep: GetProfit: price hasn't been calculated yet, " + toString());
                return 0;
            }

            return OSRSPrices.GetAveragePrice(result.id) - price;
        }

        @Override
        public String toString()
        {
            return "PotionStep{" + ", result=" + result.name + ", left=" + left + ", right=" +
                   right + ", price=" + price + ", experience=" + experience + ", purchase=" +
                   purchase + '}';
        }
    }

    /**
     * @param Potion
     *
     * @return the steps to create the potion, if the first step already has the purchase flag set, it's not profitable
     */
    public static PotionStep GetProfitablePotionSteps(PotionData Potion)
    {
        if(Potion == null)
        {
            return null;
        }

        Function2<Integer, Integer, Integer> GetPrice = (Integer ID, Integer count) -> {
            ItemDB.ItemData portion = GetPortion(ID, count);
            if(portion != null)
            {
                return OSRSPrices.GetAveragePrice(portion.id);
            }
            else
            {
                return OSRSPrices.GetAveragePrice(ID) * count;
            }
        };

        Stack<PotionStep> ToDo = new Stack<>();
        var               top  = new PotionStep(Potion);
        ToDo.push(top);

        while(!ToDo.isEmpty())
        {
            var step = ToDo.peek();

            if(step.left == null && isPotion(step.result.base))
            {
                var pot  = GetPotion(step.result.base);
                var next = new PotionStep(pot);
                step.left = next;
                ToDo.push(next);
            }
            else if(step.left == null && isPotion(step.result.base))
            {
                var pot  = GetPotion(step.result.base);
                var next = new PotionStep(pot);
                step.right = next;
                ToDo.push(next);
            }
            else
            {
                int leftPrice = step.left == null
                        ? GetPrice.apply(step.result.base, step.result.base_count)
                        : step.left.price;
                int rightPrice = step.right == null
                        ? GetPrice.apply(step.result.base,
                                         step.result.base_count)
                        : step.right.price;

                Integer buyPrice               = OSRSPrices.GetAveragePrice(step.result.id);
                int     ingredientCombinePrice = (leftPrice + rightPrice);
                Logger.log("PotionDB: GetProfitablePotionSteps: buyPrice: " + buyPrice);
                Logger.log("PotionDB: GetProfitablePotionSteps: ingredientCombinePrice: " +
                           ingredientCombinePrice);






                ///TODO ingredient_alternatives, extra_ingredients, ingredient_count and base_count









                if(buyPrice != null)
                {
                    Logger.log("PotionDB: GetProfitablePotionSteps: profit: " +
                               (buyPrice - ingredientCombinePrice));
                }

                if(buyPrice == null || buyPrice - ingredientCombinePrice > 0)
                {
                    step.price = ingredientCombinePrice;
                    step.experience += step.result.experience;

                }
                else
                {
                    step.price    = buyPrice;
                    step.left     = null;
                    step.right    = null;
                    step.purchase = true;
                }


                ToDo.pop();
            }
        }

        return top;
    }

    //    public static void main(String[] args)
    //    {
    //        var steps = GetProfitablePotionSteps(GetPotion(12907));
    //
    //        System.out.print(steps);
    //    }

    public static void main(String[] args)
    {
        ReadAll();

        Gson                         gson = OSRSUtilities.OSRSGsonBuilder.create();
        HashMap<Integer, PotionData> out  = new HashMap<>();
        for(var potion : PotionDBMap.entrySet())
        {

            out.put(potion.getKey(), potion.getValue());

        }

        System.out.print(gson.toJson(out));
    }

    //    public static void main(String[] args)
    //    {
    //        ReadPotionDB();
    //        //System.out.print(Arrays.toString(PotionDBMap.values().toArray()));
    //
    //        ConcurrentHashMap<Integer, PotionData> newMap = new ConcurrentHashMap<>();
    //
    //        for(var potion : PotionDBMap.entrySet())
    //        {
    //            ItemDB.ItemData potionItem = ItemDB.GetClosestMatch(potion.getValue().name + "(4)",
    //                                                                true);
    //
    //            ItemDB.ItemData BaseItem = ItemDB.GetClosestMatch(potion.getValue().base, true);
    //
    //            ItemDB.ItemData IngredientItem = ItemDB.GetClosestMatch(potion.getValue().ingredient,
    //                                                                    true);
    //
    //            if(potion.getValue().extra_ingredient != null)
    //            {
    //                List<Integer> out = new ArrayList<>();
    //                for(var extra : potion.getValue().extra_ingredient)
    //                {
    //                    ItemDB.ItemData extraItem = ItemDB.GetClosestMatch(extra, true);
    //                    out.add(extraItem.id);
    //                }
    //                potion.getValue().extra_ingredientID = out.stream()
    //                                                          .mapToInt(Integer::intValue)
    //                                                          .toArray();
    //            }
    //
    //            PotionData newData = new PotionData();
    //
    //            newData.name              = potion.getValue().name;
    //            newData.experience        = potion.getValue().experience;
    //            newData.level             = potion.getValue().level;
    //            newData.extra_requirement = potion.getValue().extra_requirement;
    //            ;
    //
    //            if(potionItem != null)
    //            {
    //                newData.id = potionItem.id;
    //            }
    //
    //            if(BaseItem != null)
    //            {
    //                newData.baseID = BaseItem.id;
    //            }
    //
    //            if(IngredientItem != null)
    //            {
    //                newData.ingredientID = IngredientItem.id;
    //            }
    //
    //            // portions
    //            List<ItemDB.ItemData> portions = new ArrayList<>(List.of(ItemDB.GetAllItemKeywordMatch(
    //                    potion.getValue().name,
    //                    true)));
    //
    //            if(portions.size() > 1)
    //            {
    //                List<ItemDB.ItemData> out = new ArrayList<>();
    //                System.out.print(newData.name + " " + newData.id + "\n");
    //
    //                for(var option : portions)
    //                {
    //
    //                    System.out.print(option.name + " " + option.id + "\n");
    //                    out.add(option);
    //                }
    //                System.out.print("\n");
    //                newData.portion_ids = out.stream().mapToInt((t) -> t.id).toArray();
    //            }
    //
    //            newMap.put(newData.id, newData);
    //        }
    //
    //        System.out.print(OSRSUtilities.OSRSGsonBuilder.create().toJson(newMap));
    //    }
}
