package IF.Logic.Cycles.Minigame;

import IF.OSRSDatabase.HerbDB;
import IF.OSRSDatabase.OSRSPrices;
import IF.Utilities.Scripting.IScript;
import IF.Utilities.Scripting.Logger;
import IF.Utilities.Scripting.SimpleCycle;
import io.vavr.Tuple2;
import org.dreambot.api.data.GameState;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.GraphicsObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.jetbrains.annotations.Range;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MasteringMixologyCycle extends SimpleCycle
{
    static final         int  RefinerID             = 54904;
    static final         int  HopperID              = 54903;
    static final         int  MixingVesselID        = 55395;
    static final         int  ConveyorBeltID        = 54917;
    static final         int  CrystallizerID        = 55391;
    static final         int  MixerID               = 55390;
    static final         int  BoilerID              = 55389;
    static final         int  MatureDigweedObjectID = 55396; // TODO
    static final         int  MatureDigweedItemID   = 30031; // TODO
    static final         int  MoxLeverID            = 54868;
    static final         int  AgaLeverID            = 54867;
    static final         int  LyeLeverID            = 54869;
    static final         int  MoxPasteID            = 30005;
    static final         int  AgaPasteID            = 30007;
    static final         int  LyePasteID            = 30009;
    static final         int  MoxDepositVarbit      = 11431;
    static final         int  AgaDepositVarbit      = 11432;
    static final         int  LyeDepositVarbit      = 11433;
    static final         int  LeftMixerVarbit       = 11326;
    static final         int  MiddleMixerVarbit     = 11325;
    static final         int  RightMixerVarbit      = 11324;
    static final         int  BoilerProgressVarbit  = 11327;
    static final         int  MixerProgressVarbit   = 11329;
    static final         int  CrystalProgressVarbit = 11328;
    static final         int  ProgressFinalState    = 16;
    static final         Tile StartTile             = new Tile(1394, 9321, 0);
    static final         Tile PotionSelectTile      = new Tile(1394, 9324, 0);
    static final         int  PotionSlots           = 3;
    static final         int  Potion1Varbit         = 11315;
    static final         int  Potion1MethodVarbit   = 11316;
    static final         int  Potion2Varbit         = 11317;
    static final         int  Potion2MethodVarbit   = 11318;
    static final         int  Potion3Varbit         = 11319;
    static final         int  Potion3MethodVarbit   = 11320;
    static final         int  GetMinimumDeposit     = 100;
    static final         int  MixalotID1            = 30020;
    static final         int  MixalotID2            = 30030;
    @Serial
    private static final long serialVersionUID      = 5504318900349378426L;
    int ToCreateIndex  = 0;
    int ToProcessIndex = 0;

    boolean isReloading = false;
    boolean isActive    = false;
    boolean NoMoreHerbs = false;

    List<HerbDB.HerbData> ToIgnore = new ArrayList<>();


    // mixer order right to left
    //11326 left mixer
    //11325 middle mixer
    //11324 right mixer
    // 1 = M
    // 2 = A
    // 3 = L


    //11315 Potion ID 1?
    //11316 PotionMethod ID ?
    //11317 Potion ID 2?
    //11318 PotionMethod ID ?
    //11319 Potion ID 3?
    //11320 PotionMethod ID ?

    // Potion IDs
    // 1 = MMM
    // 2 = MMA
    // 3 = MML?
    // 4 = AAA
    // 5 = AAM
    // 6 = AAL
    // 7 = LLL
    // 8 = LLM?
    // 9 = LLA
    // 10 = ALM


    //Potion Mix IDs
    // 3 = crystallize
    // 2 = boil
    // 1 = mix

    // Processing states
    // 11341 Boiler Busy state? 0 or 10?
    // 11327 Boiler progress state 0 = done, 1-16 busy
    // 11340 Mixer Busy state? 0 or 5
    // 11329 Mixer Progress state 0 = done, 1-16 busy
    // 11342 Crystal busy state? 0 or 7
    // 11328 Crystal progress state?  0 = done, 1-16 busy


    enum Ingredient
    {
        Empty(0),
        Mox(1),
        Aga(2),
        Lye(3);
        final int value;

        Ingredient(int v)
        {
            value = v;
        }

        public int GetPotionValue()
        {
            switch(this)
            {
                case Mox:
                    return 1;
                case Aga:
                    return 10;
                case Lye:
                    return 100;
            }
            return 0;
        }

        public static Ingredient FromInt(int i)
        {
            switch(i)
            {
                case 0:
                    return Empty;
                case 1:
                    return Mox;
                case 2:
                    return Aga;
                case 3:
                    return Lye;
            }
            return Empty;
        }
    }

    enum Mixer
    {
        Left,
        Middle,
        Right
    }

    enum Potion
    {
        MMM(1, 3),
        MMA(2, 12),
        MML(3, 102),
        AAA(4, 30),
        AAM(5, 21),
        AAL(6, 120),
        LLL(7, 300),
        LLM(8, 201),
        LLA(9, 210),
        ALM(10, 111);

        public final int ID;
        public final int Recipe;

        Potion(int id, int recipe)
        {
            ID     = id;
            Recipe = recipe;
        }

        public static Potion FromID(int ID)
        {
            return Arrays.stream(Potion.values())
                         .filter((t) -> t.ID == ID)
                         .findFirst()
                         .orElse(null);
        }

        public static int GetAgaCount(int recipe)
        {
            return recipe % 100 / 10;
        }

        public static int GetLyeCount(int recipe)
        {
            return recipe / 100;
        }

        public static int GetMoxCount(int recipe)
        {
            return recipe % 10;
        }
    }

    public MasteringMixologyCycle(String name)
    {
        super(name, null);
    }

    @Override
    public boolean isCycleComplete(IScript Script)
    {
        return NoMoreHerbs;
    }

    @Override
    public boolean onStart(IScript Script)
    {
        Script.onGameStateChange().Subscribe(this, (A, B) -> {
            if(B == GameState.LOGGED_IN)
            {
                ResetMMStates();
            }
        });
        return super.onStart(Script);
    }

    private boolean ResetMMStates()
    {
        Logger.log("MasterMixology: ResetMMStates: Resetting state");
        ToCreateIndex  = 0;
        ToProcessIndex = 0;
        isActive       = false;
        isReloading    = false;
        if(!isInventoryEmpty())
        {
            int count;
            do
            {
                count = Inventory.fullSlotCount();
                HandInPotions();
                Sleep.sleepTick();
            }
            while(count != Inventory.fullSlotCount());

            do
            {
                DropAtBank();
            }
            while(!isInventoryEmpty() && GetScript().GetCurrentGameState() == GameState.LOGGED_IN);
        }
        return true;
    }

    boolean isInventoryEmpty()
    {
        return Inventory.all((t) -> t != null && t.getID() != MoxPasteID &&
                                    t.getID() != AgaPasteID && t.getID() != LyePasteID &&
                                    t.getID() != MatureDigweedItemID).isEmpty();
    }

    boolean HandInPotions()
    {
        var Conveyor = GameObjects.closest(ConveyorBeltID);
        if(Conveyor != null)
        {
            if(Conveyor.interact())
            {
                // wait for inventory change
                return GetScript().onInventory().WaitForChange(5000);
            }
            Logger.log("MasterMixology: HandInPotions: Failed to interact with Conveyor");
        }
        else
        {
            Logger.log("MasterMixology: HandInPotions: Can't find Conveyor");
        }
        return false;
    }

    private void DropAtBank()
    {
        if(Sleep.sleepUntil(() -> Bank.open() && Bank.isOpen(), 100000, 300))
        {
            Bank.depositAllItems();
        }
    }

    @Override
    public int onLoop(IScript Script)
    {
        var digweed = GameObjects.closest(MatureDigweedObjectID);
        if(digweed != null && digweed.hasAction("Collect"))
        {
            Logger.log("MasterMixology: onLoop: Digweed found");
            digweed.interact();
            return 10;
        }

        if(Inventory.contains(MatureDigweedItemID) &&
           (Inventory.contains(MixalotID1) || Inventory.contains(MixalotID2)))
        {
            Logger.log("MasterMixology: onLoop: has digweed and Mixalot, combining");
            var weed = Inventory.get(MatureDigweedItemID);
            var mix1 = Inventory.get(MixalotID1);
            var mix2 = Inventory.get(MixalotID2);
            if(weed != null)
            {
                if(mix2 != null)
                {
                    Logger.log("MasterMixology: onLoop: digweed combined 2");
                    weed.useOn(mix2);
                }
                else if(mix1 != null)
                {
                    Logger.log("MasterMixology: onLoop: digweed combined 2");
                    weed.useOn(mix1);
                }
            }
        }

        if((isReloading || !isSufficientDeposit()) && !isActive)
        {
            var PotentialPots = Inventory.except((t) -> t != null &&
                                                        t.getID() == MatureDigweedItemID);
            if(Inventory.contains(MatureDigweedItemID) && !PotentialPots.isEmpty())
            {
                var weed = Inventory.get(MatureDigweedItemID);
                var pot  = PotentialPots.getFirst();
                if(weed != null)
                {
                    weed.useOn(pot);
                }
            }

            Logger.log("MasterMixology: onLoop: Reloading Paste deposit " + isSufficientDeposit() +
                       isReloading);
            isReloading = !ReloadDeposit(2000, 2000, 2000);
            return super.onLoop(Script);
        }

        if(isProcessing())
        {
            Logger.log("MasterMixology: onLoop: Busy Processing");
            SpeedUpProcessing();
            return super.onLoop(Script);
        }

        if(ToCreateIndex < PotionSlots)
        {
            isActive = true;
            Potion potion = GetPotion(ToCreateIndex + 1);
            Logger.log("MasterMixology: onLoop: Creating new potion with recipe " + potion);
            if(GetUnpreparedPotion(potion))
            {
                ToCreateIndex++;
                Logger.log("MasterMixology: onLoop: Unfinished Potion created, Current index " +
                           ToCreateIndex);
            }
            return super.onLoop(Script);
        }

        if(ToProcessIndex < PotionSlots)
        {
            int method = GetPotionMethod(ToProcessIndex + 1);
            Logger.log("MasterMixology: onLoop: Processing new potion with method " + method);
            if(ProcessPotion(method))
            {
                ToProcessIndex++;
                Logger.log("MasterMixology: onLoop: Potion Processed, Current index " +
                           ToProcessIndex);
            }
            return super.onLoop(Script);
        }

        if(HandInPotions())
        {
            Logger.log("MasterMixology: onLoop: Successfully Handed in potion");
            ResetMMStates();
        }
        return super.onLoop(Script);
    }

    Ingredient GetMixerValue(Mixer mixer)
    {
        switch(mixer)
        {
            case Left:
                return Ingredient.FromInt(PlayerSettings.getBitValue(LeftMixerVarbit));
            case Middle:
                return Ingredient.FromInt(PlayerSettings.getBitValue(MiddleMixerVarbit));
            case Right:
                return Ingredient.FromInt(PlayerSettings.getBitValue(RightMixerVarbit));
        }
        return Ingredient.Empty;
    }

    int GetCurrentRecipe()
    {
        var Left   = GetMixerValue(Mixer.Left);
        var Middle = GetMixerValue(Mixer.Middle);
        var Right  = GetMixerValue(Mixer.Right);

        return Left.GetPotionValue() + Middle.GetPotionValue() + Right.GetPotionValue();
    }

    int GetMoxDeposit()
    {
        var count = PlayerSettings.getBitValue(MoxDepositVarbit);
        Logger.log("MasterMixology: GetMoxDeposit: count " + count);
        return count;
    }

    int GetAgaDeposit()
    {
        var count = PlayerSettings.getBitValue(AgaDepositVarbit);
        Logger.log("MasterMixology: GetAgaDeposit: count " + count);
        return count;
    }

    int GetLyeDeposit()
    {
        var count = PlayerSettings.getBitValue(LyeDepositVarbit);
        Logger.log("MasterMixology: GetLyeDeposit: count " + count);
        return count;
    }

    Potion GetPotion(@Range(from = 1, to = 3) int slot)
    {
        Potion pot;
        switch(slot)
        {
            case 1:
                pot = Potion.FromID(PlayerSettings.getBitValue(Potion1Varbit));
                if(pot == null)
                {
                    Logger.log("MasterMixology: GetPotionRecipe: Potion recipe not found, " +
                               PlayerSettings.getBitValue(Potion1Varbit));
                }
                return pot;
            case 2:
                pot = Potion.FromID(PlayerSettings.getBitValue(Potion2Varbit));
                if(pot == null)
                {
                    Logger.log("MasterMixology: GetPotionRecipe: Potion recipe not found, " +
                               PlayerSettings.getBitValue(Potion2Varbit));
                }
                return pot;
            case 3:
                pot = Potion.FromID(PlayerSettings.getBitValue(Potion3Varbit));
                if(pot == null)
                {
                    Logger.log("MasterMixology: GetPotionRecipe: Potion recipe not found, " +
                               PlayerSettings.getBitValue(Potion3Varbit));
                }
                return pot;
        }
        Logger.log("MasterMixology: GetPotionRecipe: Not a valid slot, " + slot);
        return null;
    }

    int GetPotionMethod(@Range(from = 1, to = 3) int slot)
    {
        switch(slot)
        {
            case 1:
                return PlayerSettings.getBitValue(Potion1MethodVarbit);
            case 2:
                return PlayerSettings.getBitValue(Potion2MethodVarbit);
            case 3:
                return PlayerSettings.getBitValue(Potion3MethodVarbit);
        }
        Logger.log("MasterMixology: GetPotionMethod: Not a valid slot, " + slot);
        return 0;
    }

    boolean isSufficientDeposit()
    {
        boolean MoxCheck = GetAgaDeposit() > GetMinimumDeposit;
        boolean AgaCheck = GetMoxDeposit() > GetMinimumDeposit;
        boolean LyeCheck = GetLyeDeposit() > GetMinimumDeposit;
        Logger.log("MasterMixology: isSufficientDeposit: checks " + MoxCheck + AgaCheck + LyeCheck);
        return MoxCheck && AgaCheck && LyeCheck;
    }

    boolean ReloadDeposit(int Mox, int Aga, int Lye)
    {
        boolean MoxCheck = Inventory.count(MoxPasteID) + GetMoxDeposit() < Mox;
        boolean AgaCheck = Inventory.count(AgaPasteID) + GetAgaDeposit() < Aga;
        boolean LyeCheck = Inventory.count(LyePasteID) + GetLyeDeposit() < Lye;

        Logger.log("MasterMixology: ReloadDeposit: Checks: Mox " + MoxCheck + " Aga " + AgaCheck +
                   " Lye " + LyeCheck);
        if(MoxCheck || AgaCheck || LyeCheck)
        {
            if(!Bank.isOpen())
            {
                Bank.open();
                return false;
            }

            int BankMox = Bank.count(MoxPasteID);
            int BankAga = Bank.count(AgaPasteID);
            int BankLye = Bank.count(LyePasteID);

            if(BankMox + GetMoxDeposit() < Mox)
            {
                CreatePaste(HerbDB.PasteType.Mox);
                return false;
            }

            if(BankAga + GetAgaDeposit() < Aga)
            {
                CreatePaste(HerbDB.PasteType.Aga);
                return false;
            }

            if(BankLye + GetLyeDeposit() < Lye)
            {
                CreatePaste(HerbDB.PasteType.Lye);
                return false;
            }

            if(MoxCheck)
            {
                Logger.log("MasterMixology: ReloadDeposit: Withdrawing Mox");
                Bank.withdraw(MoxPasteID, Mox);
            }
            if(AgaCheck)
            {
                Logger.log("MasterMixology: ReloadDeposit: Withdrawing Aga");
                Bank.withdraw(AgaPasteID, Aga);
            }
            if(LyeCheck)
            {
                Logger.log("MasterMixology: ReloadDeposit: Withdrawing Lye");
                Bank.withdraw(LyePasteID, Lye);
            }
        }

        if(Inventory.count(MoxPasteID) + GetMoxDeposit() >= Mox &&
           Inventory.count(AgaPasteID) + GetAgaDeposit() >= Aga &&
           Inventory.count(LyePasteID) + GetLyeDeposit() >= Lye)
        {
            var Hopper = GameObjects.closest(HopperID);
            if(Hopper != null)
            {
                if(Hopper.interact())
                {
                    Sleep.sleepUntil(() -> Inventory.isEmpty(), 5000);
                }
            }
            else
            {
                Logger.log("MasterMixology: ReloadDeposit: Failed to find Hopper");
            }
        }
        return isSufficientDeposit();
    }

    HerbDB.HerbData GetCheapestAvailablePaste(HerbDB.PasteType Type)
    {
        var Herbs = HerbDB.getInstance().FilterHerbs(Bank.all(), false);

        Tuple2<Float, HerbDB.HerbData> cheapest = null;
        for(var herb : Herbs)
        {
            if(herb != null && ToIgnore.stream().anyMatch((t) -> t.id == herb.getID()))
            {
                continue;
            }

            var data = HerbDB.getInstance().GetHerbData(herb.getID());
            if(data != null && data.paste_type == Type)
            {
                int   Price         = OSRSPrices.GetLatestPrice(data.id);
                float PricePerPaste = (float) Price / (float) data.paste_count;
                if(cheapest == null || PricePerPaste < cheapest._1)
                {
                    cheapest = new Tuple2<>(PricePerPaste, data);
                }
            }
        }

        return cheapest == null ? null : cheapest._2;
    }

    boolean CreatePaste(HerbDB.PasteType Type)
    {
        if(!Bank.isOpen())
        {
            Logger.log("MasterMixology: CreatePaste: Opening bank");
            Bank.open();
            return false;
        }

        if(!Inventory.isEmpty())
        {
            Logger.log("MasterMixology: CreatePaste: Depositing Inventory");
            Bank.depositAllItems();
            return false;
        }

        var herb = GetCheapestAvailablePaste(Type);
        if(herb != null)
        {
            if(Bank.withdrawAll(herb.id))
            {
                var Refiner = GameObjects.closest(RefinerID);
                if(Refiner != null)
                {
                    if(Refiner.interact())
                    {
                        return Sleep.sleepUntil(() -> !Inventory.contains(herb.id), 40000);
                    }
                    Logger.log("MasterMixology: CreatePaste: Failed to interact with Refiner");
                }
                else
                {
                    Logger.log("MasterMixology: CreatePaste: Failed to find Refiner");
                }
            }
            else
            {
                Logger.log("MasterMixology: CreatePaste: Failed to withdraw " + herb);
            }
        }
        else
        {
            Logger.log("MasterMixology: CreatePaste: No herb found to make " + Type.name() +
                       " paste with, quiting");
            NoMoreHerbs = true;
        }

        return false;
    }

    void SpeedUpProcessing()
    {
        int MixerProgress   = PlayerSettings.getBitValue(MixerProgressVarbit);
        int BoilerProgress  = PlayerSettings.getBitValue(BoilerProgressVarbit);
        int CrystalProgress = PlayerSettings.getBitValue(CrystalProgressVarbit);

        if(!Players.getLocal().isAnimating())
        {
            Logger.log(
                    "MasterMixology: CreatePaste: Player is not longer animating, interacting with the processor");
            if(MixerProgress > 0)
            {
                ProcessPotion(1);
            }
            else if(BoilerProgress > 0)
            {
                ProcessPotion(2);
            }
            else if(CrystalProgress > 0)
            {
                ProcessPotion(3);
            }
            return;
        }

        if(MixerProgress > 0 && MixerProgress < 12)
        {
            var Mixer = GameObjects.closest(MixerID);
            Mouse.move(Mixer);
            if(Sleep.sleepUntil(() -> GraphicsObjects.closest(2954) != null, 10000))
            {
                Mixer = GameObjects.closest(MixerID);
                if(Mixer != null)
                {
                    Mixer.interact();
                }
            }
        }


        else if(BoilerProgress > 0)
        {
            int current = BoilerProgress;
            while(PlayerSettings.getBitValue(BoilerProgressVarbit) < 14)
            {
                BoilerProgress = PlayerSettings.getBitValue(BoilerProgressVarbit);
                if(BoilerProgress == 0 || !Players.getLocal().isAnimating())
                {
                    break;
                }
                var Boiler = GameObjects.closest(BoilerID);
                if(Boiler != null && current != BoilerProgress)
                {
                    Boiler.interact();
                    current = BoilerProgress;
                }
            }
        }

        else if(CrystalProgress > 0 && CrystalProgress < 4)
        {
            var Crystallizer = GameObjects.closest(CrystallizerID);
            Mouse.move(Crystallizer);
            if(Sleep.sleepUntil(() -> PlayerSettings.getBitValue(CrystalProgressVarbit) == 4,
                                10000))
            {
                Crystallizer = GameObjects.closest(CrystallizerID);
                if(Crystallizer != null)
                {
                    Crystallizer.interact();
                }
            }
        }
    }

    boolean isProcessing()
    {
        int MixerProgress   = PlayerSettings.getBitValue(MixerProgressVarbit);
        int BoilerProgress  = PlayerSettings.getBitValue(BoilerProgressVarbit);
        int CrystalProgress = PlayerSettings.getBitValue(CrystalProgressVarbit);

        // TODO do fancy speedup thing

        return BoilerProgress + MixerProgress + CrystalProgress > 0;
    }

    boolean GetUnpreparedPotion(Potion potion)
    {
        if(PotionSelectTile.distance() > 5)
        {
            Logger.log("MasterMixology: GetUnpreparedPotion: Too far from station, walking");
            Walking.walk(PotionSelectTile);
            return false;
        }

        int DesiredRecipe = potion.Recipe;
        int CurrentRecipe = GetCurrentRecipe();
        Logger.log(
                "MasterMixology: GetUnpreparedPotion: " + CurrentRecipe + " <=> " + DesiredRecipe);

        if(DesiredRecipe == CurrentRecipe)
        {
            Logger.log("MasterMixology: GetUnpreparedPotion: We have desired recipe, getting vessel");
            var Vessel   = GameObjects.closest(MixingVesselID);
            int InvCount = Inventory.fullSlotCount();
            if(Vessel != null && Vessel.interact())
            {
                Logger.log(
                        "MasterMixology: GetUnpreparedPotion: Interacted with Vessel, waiting for potion in inventory");
                return Sleep.sleepUntil(() -> GetScript().onInventory().WaitForChange(8000), 8000);
                //return Sleep.sleepUntil(() -> InvCount !=  Inventory.fullSlotCount(),2000);
            }
            return false;
        }


        if(Potion.GetMoxCount(DesiredRecipe) > Potion.GetMoxCount(CurrentRecipe))
        {
            Logger.log("MasterMixology: GetUnpreparedPotion: Not enough Mox");
            var Lever = GameObjects.closest(MoxLeverID);
            if(Lever != null && Lever.interact())
            {
                Logger.log("MasterMixology: GetUnpreparedPotion: Successfully added more mox");
            }
        }

        else if(Potion.GetAgaCount(DesiredRecipe) > Potion.GetAgaCount(CurrentRecipe))
        {
            Logger.log("MasterMixology: GetUnpreparedPotion: Not enough Aga");
            var Lever = GameObjects.closest(AgaLeverID);
            if(Lever != null && Lever.interact())
            {
                Logger.log("MasterMixology: GetUnpreparedPotion: Successfully added more Aga");
            }
        }

        else if(Potion.GetLyeCount(DesiredRecipe) > Potion.GetLyeCount(CurrentRecipe))
        {
            Logger.log("MasterMixology: GetUnpreparedPotion: Not enough Lye");
            var Lever = GameObjects.closest(LyeLeverID);
            if(Lever != null && Lever.interact())
            {
                Logger.log("MasterMixology: GetUnpreparedPotion: Successfully added more Lye");
            }
        }
        return false;
    }

    boolean ProcessPotion(@Range(from = 1, to = 3) int ProcessID)
    {
        Logger.log("MasterMixology: ProcessPotion: Processing ID " + ProcessID);
        var Boiler       = GameObjects.closest(BoilerID);
        var Crystallizer = GameObjects.closest(CrystallizerID);
        var Mixer        = GameObjects.closest(MixerID);

        Logger.log("MasterMixology: ProcessPotion: Boiler " + Boiler);
        Logger.log("MasterMixology: ProcessPotion: Crystallizer " + Crystallizer);
        Logger.log("MasterMixology: ProcessPotion: Mixer " + Mixer);

        switch(ProcessID)
        {
            case 1:
                if(Mixer != null)
                {
                    Mixer.interact();
                }
                break;
            case 2:
                if(Boiler != null)
                {
                    Boiler.interact();
                }
                break;
            case 3:
                if(Crystallizer != null)
                {
                    Crystallizer.interact();
                }
                break;
            default:
                return false;
        }

        return Sleep.sleepUntil(() -> isProcessing(), 5000);
    }
}
