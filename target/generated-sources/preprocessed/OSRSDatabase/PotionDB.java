package OSRSDatabase;

import Utilities.OSRSUtilities;
import Utilities.Requirement.IRequirement;
import Utilities.Scripting.Logger;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.vavr.Function2;
import io.vavr.Tuple2;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

public class PotionDB extends OSRSDataBase
{
    final static         int                          AoCID        = 21163;
    final private static String                       PotionDBPath = "Skilling/potionDB.json";
    private static final HashMap<Integer, PotionData> PotionDBMap  = new HashMap<>();

    public static class PotionData implements Serializable
    {
        public           int                        id;
        public           String                     name;
        public @Nullable int[]                      portion_ids;
        public           int                        base;
        public           int                        base_count       = 1;
        public           int                        ingredient;
        public @Nullable Tuple2<Integer, Integer>[] ingredient_alternatives;
        public           int                        ingredient_count = 1;
        public @Nullable int[]                      extra_ingredient;
        public           int                        level;
        public           float                      experience;
        public @Nullable IRequirement[]             extra_requirement;

        public String toString()
        {
            return _toString(this, this.getClass());
        }
    }

    public static class PotionStep implements Serializable
    {
        public  PotionData result;
        public  PotionStep base       = null;
        public  PotionStep ingredient = null;
        public  float      experience = 0;
        public  boolean    purchase   = false;
        public  boolean    DeGrime    = false;
        private Integer    price      = null;

        public PotionStep(PotionData result)
        {
            this.result = result;
        }

        public PotionStep(PotionStep potionPlan)
        {
            result     = potionPlan.result;
            base       = potionPlan.base;
            ingredient = potionPlan.ingredient;
            price      = potionPlan.price;
            experience = potionPlan.experience;
            purchase   = potionPlan.purchase;
        }

        public Integer GetPrice()
        {
            int extraprice = 0;
            if(result.extra_ingredient != null)
            {
                for(var extra : result.extra_ingredient)
                {
                    extraprice += OSRSPrices.GetAveragePrice(extra);
                }
            }
            return price + extraprice;
        }

        public int GetProfit()
        {
            if(price == null)
            {
                Logger.log("PotionStep: GetProfit: price hasn't been calculated yet, " + this);
                return 0;
            }

            Integer AVGprice = OSRSPrices.GetAveragePrice(result.id);
            if(AVGprice == null)
            {
                return 0;
            }

            return AVGprice - GetPrice();
        }

        //        @Override
        //        public String toString()
        //        {
        //            return "PotionStep{" + ", result=" + result.name + ", left=" + base + ", right=" +
        //                   ingredient + ", price=" + price + ", experience=" + experience + ", purchase=" +
        //                   purchase + '}';
        //        }

        private String IngredientString(int id, int count)
        {
            ItemDB.ItemData ingredient = ItemDB.GetItemData(id);
            Integer         price      = OSRSPrices.GetAveragePrice(id);
            String          priceStr   = "";
            if(price != null)
            {
                priceStr = "(" + price + " * " + count + ")";
            }
            return ingredient.name + " * " + count + priceStr;
        }

        private String StepString()
        {
            String out = result.name + "(" + GetPrice() + ")";
            if(base != null)
            {
                out += " (" + base.StepString();
            }
            else
            {
                out += " (" + IngredientString(result.base, result.base_count);
            }
            if(ingredient != null)
            {
                out += " + " + ingredient.StepString() + ") ";
            }
            else
            {
                out += " + " + IngredientString(result.ingredient, result.ingredient_count) + ") ";
            }

            if(result.extra_ingredient != null)
            {
                for(var extra : result.extra_ingredient)
                {
                    out += " + " + IngredientString(extra, 1);
                }
            }

            return out;
        }

        @Override
        public String toString()
        {
            String out = result.name + "(" + GetProfit() + " = " +
                         OSRSPrices.GetAveragePrice(result.id) + " - " + GetPrice() + ") = ";
            if(base != null && !base.purchase)
            {
                out += base.StepString();
            }
            else
            {
                out += IngredientString(result.base, result.base_count);
            }
            out += " + ";
            if(ingredient != null && !ingredient.purchase)
            {
                out += ingredient.StepString();
            }
            else
            {
                out += IngredientString(result.ingredient, result.ingredient_count);
            }
            if(result.extra_ingredient != null)
            {
                for(var extra : result.extra_ingredient)
                {
                    out += " + " + IngredientString(extra, 1);
                }
            }

            return out + "\n";
        }
    }

    public static PotionData[] GetAllPotions()
    {
        ReadAll();
        return PotionDBMap.values().toArray(new PotionData[0]);
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
    }

    public static PotionData[] GetAllPotions(boolean includeNonTradable)
    {
        ReadAll();
        if(includeNonTradable)
        {
            return PotionDBMap.values().toArray(new PotionData[0]);
        }
        else
        {
            var              potions = PotionDBMap.values();
            List<PotionData> out     = new ArrayList<>();
            for(var potion : potions)
            {
                PotionStep PotionPlan  = GetProfitablePotionSteps(potion);
                var        ingredients = GetAllIngredients(PotionPlan);
                if(!Arrays.stream(ingredients)
                          .anyMatch((t) -> !ItemDB.GetItemData(t._1).tradeable_on_ge))
                {
                    out.add(potion);
                }
            }
            return out.toArray(out.toArray(new PotionData[0]));

        }
    }

    public static Tuple2<Integer, Integer>[] GetAllIngredients(PotionStep PotionPlan)
    {
        List<Tuple2<Integer, Integer>> out   = new ArrayList<>();
        Stack<PotionStep>              steps = new Stack<>();
        steps.push(new PotionStep(PotionPlan));

        while(!steps.isEmpty())
        {
            PotionStep next = steps.pop();

            if(next.base == null)
            {
                out.add(new Tuple2<>(next.result.base, next.result.base_count));
            }
            else
            {
                var base = new PotionStep(next.base);
                base.result.base_count *= next.result.base_count;
                base.result.ingredient_count *= next.result.ingredient_count;
                steps.push(base);
            }
            if(next.ingredient == null)
            {
                out.add(new Tuple2<>(next.result.ingredient, next.result.ingredient_count));
            }
            else
            {
                var ingredient = new PotionStep(next.ingredient);
                ingredient.result.base_count *= next.result.ingredient_count;
                ingredient.result.ingredient_count *= next.result.ingredient_count;
                steps.push(ingredient);
            }
            if(next.result.extra_ingredient != null)
            {
                for(var extra : next.result.extra_ingredient)
                {
                    out.add(new Tuple2<>(extra, 1));
                }
            }
        }

        return out.toArray(new Tuple2[0]);
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
                Integer price = OSRSPrices.GetAveragePrice(portion.id);
                return price == null ? 0 : price;
            }
            else
            {
                Integer price = OSRSPrices.GetAveragePrice(ID);
                return price == null ? 0 : price * count;
            }
        };

        Stack<PotionStep> ToDo = new Stack<>();
        var               top  = new PotionStep(Potion);
        ToDo.push(top);

        while(!ToDo.isEmpty())
        {
            var step = ToDo.peek();

            if(step.base == null && isPotion(step.result.base))
            {
                var pot  = GetPotion(step.result.base);
                var next = new PotionStep(pot);
                step.base = next;
                ToDo.push(next);
            }
            else if(step.ingredient == null && isPotion(step.result.ingredient))
            {
                var pot  = GetPotion(step.result.ingredient);
                var next = new PotionStep(pot);
                step.ingredient = next;
                ToDo.push(next);
            }
            else
            {
                if(HerbDB.isHerb(step.result.base) && HerbDB.isDegrimeProfitable(step.result.base))
                {
                    step.result.base = HerbDB.GetGrimyHerb(step.result.base).grimy_id;
                }
                int leftPrice = step.base == null
                        ? GetPrice.apply(step.result.base, step.result.base_count)
                        : step.base.GetPrice();

                if(HerbDB.isHerb(step.result.ingredient) &&
                   HerbDB.isDegrimeProfitable(step.result.ingredient))
                {
                    step.result.ingredient = HerbDB.GetGrimyHerb(step.result.ingredient).grimy_id;
                }
                int rightPrice = step.ingredient == null
                        ? GetPrice.apply(step.result.ingredient,
                                         step.result.ingredient_count)
                        : step.ingredient.GetPrice();

                //ingredient alternatives check
                if(step.result.ingredient_alternatives != null)
                {
                    for(var alt : step.result.ingredient_alternatives)
                    {
                        var altprice = OSRSPrices.GetAveragePrice(alt._1);
                        if(altprice == null)
                        {
                            continue;
                        }
                        altprice = altprice * alt._2;
                        if(altprice < rightPrice)
                        {
                            rightPrice             = altprice;
                            step.result.ingredient = alt._1;
                            step.ingredient        = null;
                        }
                    }
                }


                // Extra ingredient check
                if(step.result.extra_ingredient != null)
                {
                    for(var extra : step.result.extra_ingredient)
                    {
                        rightPrice += OSRSPrices.GetAveragePrice(extra);
                    }
                }

                Integer buyPrice               = OSRSPrices.GetAveragePrice(step.result.id);
                int     ingredientCombinePrice = (leftPrice + rightPrice);
                Logger.log("PotionDB: GetProfitablePotionSteps: buyPrice: " + buyPrice);
                Logger.log("PotionDB: GetProfitablePotionSteps: ingredientCombinePrice: " +
                           ingredientCombinePrice);


                ///TODO ingredient_alternatives


                if(buyPrice != null)
                {
                    Logger.log("PotionDB: GetProfitablePotionSteps: profit: " +
                               (buyPrice - ingredientCombinePrice));
                }

                if(buyPrice == null || ingredientCombinePrice < buyPrice)
                {
                    step.price = ingredientCombinePrice;
                    step.experience += step.result.experience;

                }
                else
                {
                    step.price      = buyPrice;
                    step.base       = null;
                    step.ingredient = null;
                    step.purchase   = true;
                }

                ToDo.pop();
            }
        }

        return top;
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

    public static boolean isPotion(int ID)
    {
        return GetPotion(ID) != null;
    }

    public static PotionData[] GetAllPotionsForLV(int level)
    {
        ReadAll();

        List<PotionData> out = new ArrayList<>();

        for(var potion : PotionDBMap.entrySet())
        {
            if(potion.getValue() == null)
            {
                continue;
            }

            if(IRequirement.IsAllRequirementMet(potion.getValue().extra_requirement))
            {
                out.add(potion.getValue());
            }
        }

        return out.toArray(new PotionData[0]);
    }

    public static Integer GetPotionCountToNextPotionUnlock(PotionStep PotionPlan)
    {
        if(PotionPlan.experience == 0)
        {
            return null;
        }
        Integer Needed = GetEXPToNextPotion();
        if(Needed == null || Needed == 0)
        {
            return null;
        }
        return (int) Math.ceil(Needed / PotionPlan.experience);
    }

    public static Integer GetEXPToNextPotion()
    {
        ReadAll();
        int CurrentLevel = Skills.getRealLevel(Skill.HERBLORE);

        var next = PotionDBMap.values()
                              .stream()
                              .filter((t) -> t.level > CurrentLevel)
                              .min((x, y) -> Integer.compare(x.level, y.level));

        if(next.isEmpty())
        {
            return null;
        }
        int targetLevel = next.get().level;

        return Skills.getExperienceForLevel(targetLevel) - Skills.getExperience(Skill.HERBLORE);
    }

    public static PotionData[] GetPotionsWithIngredient(int ID)
    {
        ReadAll();

        List<PotionData> out = new ArrayList<>();
        for(var potion : PotionDBMap.values())
        {
            if(potion.ingredient == ID || potion.base == ID ||
               (potion.ingredient_alternatives != null &&
                Arrays.stream(potion.ingredient_alternatives).anyMatch((t) -> t._1 == ID)) ||
               (potion.extra_ingredient != null &&
                Arrays.stream(potion.extra_ingredient).anyMatch((t) -> t == ID)))
            {
                out.add(potion);
            }
        }
        return out.toArray(new PotionData[0]);
    }

    /**
     * Amulet of Chemistry
     */
    public static boolean IsAoCProfitable(PotionData potion)
    {
        if(potion.portion_ids == null || potion.portion_ids.length < 4)
        {
            return false;
        }

        return OSRSPrices.GetAveragePrice(AoCID) / 5 <
               (OSRSPrices.GetAveragePrice(GetPortion(potion.id, 4).id) -
                OSRSPrices.GetAveragePrice(GetPortion(potion.id, 3).id));
    }

    public static boolean hasPortions(int ID)
    {
        var potion = GetPotion(ID);
        return potion != null && potion.portion_ids != null;
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
