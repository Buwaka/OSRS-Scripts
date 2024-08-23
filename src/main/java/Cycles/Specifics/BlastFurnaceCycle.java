package Cycles.Specifics;

import Cycles.Tasks.SimpleTasks.Bank.BankItemsTask;
import OSRSDatabase.ItemDB;
import Utilities.Scripting.SimpleCycle;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;

import java.util.HashMap;

public class BlastFurnaceCycle extends SimpleCycle
{
    final int CoalBagID    = 12019;
    final int IceGlovesID  = 1580;
    final int BeltID       = 9100;
    final int DispenserID  = 9202;
    final int CofferID     = 29330;
    final int MeltingPotID = 9098;
    BFState CurrentState = BFState.Bank;

    HashMap<Integer, Integer> OreStatus;


    enum BFState
    {
        Belt,
        Bank,
        Coffer
    }


    public BlastFurnaceCycle(String name)
    {
        super(name);
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
        return super.isCycleComplete(Script);
    }

    /**
     * @param Script
     *
     * @return true when Cycle is completely done and should/will be terminated, typically the same as isCycleComplete
     */
    @Override
    public boolean isCycleFinished(tpircSScript Script)
    {
        return super.isCycleFinished(Script);
    }

    /**
     * @param Script
     *
     * @return if cycle has successfully started
     */
    @Override
    public boolean onStart(tpircSScript Script)
    {
        BankItemsTask PreRequisites = new BankItemsTask("Pres");
        PreRequisites.AddDepositAll();
        PreRequisites.AddWithdraw(IceGlovesID, 1);
        PreRequisites.AddWithdraw(CoalBagID, 1);

        Script.addNodes(PreRequisites);

        Script.onInventory.Subscribe(this, this::onInventory);


        return super.onStart(Script);
    }

    private Boolean onInventory(tpircSScript.ItemAction action, Item item, Item item1)
    {
        return false;
    }

    /**
     * @param Script
     *
     * @return
     */
    @Override
    public int onLoop(tpircSScript Script)
    {

        switch(CurrentState)
        {
            case Belt ->
            {
                var Belt = GetBelt();

                if(Sleep.sleepUntil(() -> Belt.interact(), 10000))
                {

                }

            }
            case Bank -> {}
            case Coffer -> {}
        }


        return super.onLoop(Script);
    }

    GameObject GetBelt()
    {
        return GameObjects.closest(BeltID);
    }

    GameObject GetDispenser()
    {
        return GameObjects.closest(DispenserID);
    }

    GameObject GetCoffer()
    {
        return GameObjects.closest(CofferID);
    }

    void ReadMeltingPotContents()
    {
        var       MeltingPot  = GameObjects.closest(MeltingPotID);
        int       Attempts    = 0;
        final int MaxAttempts = 5;

        while(MeltingPot != null && !MeltingPot.interact() && Attempts < MaxAttempts)
        {
            Attempts++;
            GetScript().onGameTick.WaitTicks(3);
        }

        if(Dialogues.inDialogue())
        {
            for(int i = 0; i < 2; i++)
            {
                var state = Dialogues.getNPCDialogue();

                var lines = state.split("\n");
                for(var line : lines)
                {
                    var split = line.split(":");
                    if(split.length != 2)
                    {
                        ReadMeltingPotContents();
                        return;
                    }
                    var ore   = split[0];
                    var count = split[1];

                    var item = ItemDB.GetClosestMatch(ore, true);
                }
            }


        }
    }
}
