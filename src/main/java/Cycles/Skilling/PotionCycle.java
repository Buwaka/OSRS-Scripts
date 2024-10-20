package Cycles.Skilling;

import OSRSDatabase.PotionDB;
import Utilities.GrandExchange.Orders.GEOrder;
import Utilities.GrandExchange.Orders.MarketBuyOrder;
import Utilities.Scripting.SimpleCycle;
import org.dreambot.api.methods.container.impl.bank.Bank;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

public class PotionCycle extends SimpleCycle
{
    @Serial
    private static final long serialVersionUID = 9219489276057601669L;

    private PotionDB.PotionStep PotionPlan;
    private int                 Amount;
    private boolean             SellProduct                 = true;
    private boolean             DecantProduct               = true;
    private boolean             WearNecklaceOfAlchemy       = true;
    private int                 AmuletOfChemistryID         = 21163;
    private int                 UpgradedAmuletOfChemistryID = -1;


    public PotionCycle(String name, PotionDB.PotionStep steps, int amount)
    {
        super(name);
        PotionPlan = steps;
        Amount     = amount;
    }

    public List<GEOrder> GetShoppingList()
    {
        List<GEOrder> orders      = new ArrayList<>();
        var           ingredients = PotionDB.GetAllIngredients(PotionPlan);

        for(var ingredient : ingredients)
        {
            int BankCount = Bank.count(ingredient._1);

            if(BankCount < ingredient._2)
            {
                orders.add(new MarketBuyOrder(ingredient._1, ingredient._2 - BankCount));
            }
        }

        //if(Bank.contains(Amulet of Chemistry * count))
        return orders;
    }

    public void setDecantProduct(boolean decantProduct)
    {
        DecantProduct = decantProduct;
    }

    public void setSellProduct(boolean sellProduct)
    {
        SellProduct = sellProduct;
    }

    public void setWearNecklaceOfAlchemy(boolean wearNecklaceOfAlchemy)
    {
        WearNecklaceOfAlchemy = wearNecklaceOfAlchemy;
    }
}
