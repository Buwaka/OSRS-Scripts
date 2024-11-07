package Scripts.Private;

import Cycles.Tasks.SimpleTasks.ItemProcessing.InteractTask;
import OSRSDatabase.WoodDB;
import Utilities.NameDictionary;
import Utilities.OSRSUtilities;
import Utilities.Scripting.Logger;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.dreambot.api.ClientSettings;
import org.dreambot.api.input.Keyboard;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.input.event.impl.keyboard.awt.Key;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.input.CameraMode;
import org.dreambot.api.methods.input.mouse.MouseSettings;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.methods.widget.helpers.ItemProcessing;
import org.dreambot.api.methods.widget.helpers.Smithing;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.PaintListener;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Entity;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.awt.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@ScriptManifest(name = "IF-TutorialIsland", description = "tut oisland", author = "Semanresu", version = 1.0, category = Category.MISC, image = "")
public class TutorialIsland extends AbstractScript implements PaintListener
{
    final static int                     TutProgressVarBit  = 281;
    static       int                     ticksSinceContinue = 0;
    static       AtomicReference<Entity> Target             = new AtomicReference<>(null);
    Thread ProgressThread = null;
    Thread ContinueThread = null;
    Random rand           = new Random();
    Tile   Choice         = null;
    Thread Randomizer;

    static class BehaviorParameters
    {
        int ThinkDelay; // ms
        int MouseSpeed;

    }

    private boolean SimpleInteract(String name, Tile FallBack)
    {
        var npc = GetClosestMatchingEntity(name);
        if(npc != null)
        {
            return SimpleInteract(npc, FallBack);
        }
        else if(FallBack != null)
        {
            Walk(FallBack);
        }
        return false;
    }

    private boolean SimpleInteract(int ID)
    {
        return SimpleInteract(ID, null);
    }

    private boolean SimpleInteract(int ID, Tile FallBack)
    {
        GameObject obj = GameObjects.closest(ID);
        return SimpleInteract(obj, FallBack);
    }

    private boolean SimpleInteract(String name)
    {
        return SimpleInteract(name, null);
    }

    private boolean SimpleInteract(Entity target, Tile FallBack)
    {
        if(target == null || target.distance(Players.getLocal().getTile()) > 15 ||
           (WoodDB.isUnderRoof(target.getTile()) &&
            !WoodDB.isUnderRoof(Players.getLocal().getTile())) || !target.isOnScreen())
        {
            Logger.log("SimpleInteract: Walking to target/fallback");
            Tile position = FallBack == null ? target.getTile() : FallBack;
            Walk(position);
        }
        else if(!target.canReach() && Equipment.isSlotEmpty(EquipmentSlot.ARROWS))
        {
            Logger.log("SimpleInteract: Backup Walking to target");
            Walking.walk(target.getTile());
        }
        else
        {
            Logger.log("SimpleInteract: Interacting with target");
            boolean result      = false;
            int     attempts    = 0;
            int     maxAttempts = 3;
            int     direction   = rand.nextInt(100) > 50 ? 1 : -1;
            while(!result)
            {
                if(attempts > maxAttempts)
                {
                    Logger.log("SimpleInteract fallback interact");
                    target.interact();
                    break;
                }

                if(Sleep.sleepUntil(() -> Mouse.move(GetRandomPointInRectangle(target.getBoundingBox())),
                                    5000,
                                    500 + rand.nextInt(700)))
                {
                    Sleep.sleep(100 + rand.nextInt(500));
                    var ents = Mouse.getEntitiesOnCursor();
                    if((ents != null && !ents.isEmpty() &&
                        ents.getFirst().hashCode() == target.hashCode()) || rand.nextInt(100) > 60)
                    {
                        Logger.log("SimpleInteract Click");
                        result = Mouse.click();
                    }
                    else
                    {
                        Logger.log("SimpleInteract rotate");
                        Camera.mouseRotateTo(
                                Camera.getYaw() + ((rand.nextInt(250) + 200) * direction), 360);
                        //Camera.mouseRotateToTile(target.getTile().getArea(3).getRandomTile());

                        //     () -> Mouse.isButtonPressed(MouseButton.LEFT_CLICK));
                        Sleep.sleepTicks(rand.nextInt(3) + 1);
                        attempts++;
                    }
                }
            }
            if(rand.nextInt(100) > 80)
            {
                Mouse.move();
            }
            return result;
        }
        return false;
    }

    private boolean TalkNoOptions(int ID)
    {
        return TalkToOptions(ID, "", null);
    }

    private boolean TalkNoOptions(String name)
    {
        return TalkNoOptions(name, "", null);
    }

    private boolean TalkNoOptions(String name, Tile Fallback)
    {
        return TalkNoOptions(name, "", Fallback);
    }

    private boolean TalkNoOptions(int ID, Tile Fallback)
    {
        return TalkToOptions(ID, "", Fallback);
    }

    private boolean TalkNoOptions(String name, String optionContaining, Tile FallBack)
    {
        var npc = GetClosestMatchingEntity(name);
        if(npc != null)
        {
            return TalkToOptions(npc, optionContaining, FallBack);
        }
        else if(FallBack != null)
        {
            Walk(FallBack);
        }
        return false;
    }

    private boolean TalkToOptions(Entity npc, String optionContaining, Tile FallBack)
    {
        if(CanContinueWithSleep() || SimpleInteract(npc, FallBack))
        {
            Logger.log("TalkToOptions: In Dialogue");
            ticksSinceContinue = 0;
            Keyboard.holdSpace(() -> CanContinueTimeout(), rand.nextInt(3000) + 5000);
            Sleep.sleepUntil(() -> CanContinueTimeout(), 4000, 100 + rand.nextInt(300));

            var option = Dialogues.getOptionIndexContaining(optionContaining);
            if(option != -1 && Dialogues.clickOption(option))
            {
                if(rand.nextInt(100) > 70)
                {
                    Mouse.move();
                }
                Logger.log("TalkToOptions: Chosen option " + optionContaining);
                return TalkToOptions(npc, "", FallBack);
            }
            return optionContaining.isBlank();
        }
        return false;
    }

    private boolean TalkToOptions(int ID, String optionContaining, Tile FallBack)
    {
        return TalkToOptions(NPCs.closest(ID), optionContaining, FallBack);
    }

    private boolean TalkToOptions(String name, String optionContaining, Tile FallBack)
    {
        var npc = GetClosestMatchingEntity(name);
        if(npc != null)
        {
            return TalkToOptions(npc, optionContaining, FallBack);
        }
        return false;
    }

    private void Walk(Tile tile)
    {
        Logger.log("Walk: Walking to tile: " + tile);

        if(tile.distance() > 15 ||
           (WoodDB.isUnderRoof(tile) && !WoodDB.isUnderRoof(Players.getLocal().getTile())))
        {
            Walking.walk(tile);
        }
        else
        {
            Walking.walkOnScreen(tile);
        }

        Sleep.sleepTick();
        if(rand.nextInt(100) > 70)
        {
            Mouse.move();
        }
        //        Sleep.sleepUntil(() -> !Players.getLocal().isMoving() || tile.distance() < 3,
        //                         5000,
        //                         100 + rand.nextInt(500));
    }

    @Override
    public void onStart()
    {
        //        var algo = new WindMouseAttempt(1000);
        //        ScriptManager.getScriptManager().addListener(algo);
        //        Mouse.setMouseAlgorithm(algo);
        Camera.setCameraMode(CameraMode.MOUSE_ONLY);

        Logger.log("onStart");
        Logger.log(PlayerSettings.getConfig(TutProgressVarBit));
        ProgressThread = new Thread(this::CheckProgress);
        ProgressThread.start();
        ContinueThread = new Thread(this::CanContinueThread);
        ContinueThread.start();
        //        Instance.getInstance().setMouseInputEnabled(true);
        //        Instance.getInstance().setKeyboardInputEnabled(true);
        MouseSettings.setSpeed(rand.nextInt(30) + 10);
        ClientSettings.clearLayoutPreferences();

        Randomizer = OSRSUtilities.StartRandomizerThread(5, rand.nextFloat(0.7f, 1.5f), 20);
    }

    @Override
    public void onStart(String... params)
    {
        //        var algo = new WindMouseAttempt(1000);
        //        ScriptManager.getScriptManager().addListener(algo);
        //        Mouse.setMouseAlgorithm(algo);
        Camera.setCameraMode(CameraMode.MOUSE_ONLY);

        Logger.log("onStart");
        Logger.log(PlayerSettings.getConfig(TutProgressVarBit));
        ProgressThread = new Thread(this::CheckProgress);
        ProgressThread.start();
        ContinueThread = new Thread(this::CanContinueThread);
        ContinueThread.start();
        //        Instance.getInstance().setMouseInputEnabled(true);
        //        Instance.getInstance().setKeyboardInputEnabled(true);
        MouseSettings.setSpeed(rand.nextInt(10) + 10);
        ClientSettings.clearLayoutPreferences();

        Randomizer = OSRSUtilities.StartRandomizerThread(5, rand.nextFloat(0.7f, 1.5f), 20);
    }

    /**
     * @return
     */
    @Override
    public int onLoop()
    {
        int TutProgress = GetTutorialProgress();

        switch(TutProgress)
        {
            case 1: // Name and Outfit
                // textbox ID: 558, 12
                if(GetNameBox() != null && GetNameBox().isVisible())
                {
                    ChooseName();
                }
                else
                {
                    ChooseAppearance();
                    WaitForProgressChange();
                }
                break;
            case 2: //TalkToGuide, NPC ID 3308
            {
                if(TalkToOptions("Gielinor Guide", "experienced", null))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 3: //Settings menu, Widget 164, 49
            {
                if(CanContinueWithSleep())
                {
                    Logger.log("30: In Dialogue");
                    TalkNoOptions("Survival expert");
                }
                else
                {
                    if(Tabs.openWithMouse(Tab.OPTIONS))
                    {
                        WaitForProgressChange();
                    }
                }
            }
            break;
            case 7: //TalkToGuide, NPC ID 3308
            {
                if(TalkToOptions("Gielinor Guide", "experienced", null))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 10: // Open door ID 9398
            {
                if(SimpleInteract(9398))
                {
                    WaitForProgressChange();
                }

            }
            break;
            case 20: //TalktoSurvival expert ID 8503
            {
                if(TalkNoOptions("Survival expert", new Tile(3103, 3097, 0)))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 30: //continue dialogue // once dialogue is finished, open inventory ID: 164, 62
            {
                if(CanContinueWithSleep())
                {
                    Logger.log("30: In Dialogue");
                    TalkNoOptions("Survival expert");
                }
                else
                {
                    Logger.log("30: Opening inventory");
                    if(Tabs.openWithMouse(Tab.INVENTORY))
                    {
                        WaitForProgressChange();
                    }
                }
            }
            break;
            case 40: // Fish, ID 3317, until we have raw shrimp ID 2514 (better to check for inventory change)
            {
                var FishSpot = NPCs.closest(3317);
                if(Sleep.sleepUntil(() -> FishSpot.interact(), 5000, 500 + rand.nextInt(1000)))
                {
                    WaitForInventoryChange();
                }
            }
            break;
            case 50: // open stats ID: 164, 60
            {
                if(Tabs.openWithMouse(Tab.SKILLS))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 60://TalktoSurvival expert ID 8503
            {
                if(TalkNoOptions("Survival expert"))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 70: // interact with tree that has "chop down" action
            {
                var Tree = InteractTask.GetTargetByActionStatic("chop down");
                if(Tree != null && Sleep.sleepUntil(() -> Tree.interact(), 300000, 3000))
                {
                    Logger.log("70: Interacted");
                    if(GetTutorialProgress() == 70)
                    {
                        WaitForProgressChange();
                    }
                }
            }
            break;
            case 80:  // use tinderbox on "logs", use tinderbox as base since ID is correct
            {
                Logger.log("80: Getting logs and tinderbox");
                var item      = Inventory.get(2511);
                var tinderbox = Inventory.get(590);
                Logger.log("80: " + item + " " + tinderbox);
                if(!WoodDB.isTileBurnable(Players.getLocal().getTile()))
                {
                    Walk(Players.getLocal().getTile().getArea(3).getRandomTile());
                }
                else if(item != null && tinderbox != null && tinderbox.useOn(item))
                {
                    Logger.log("80: both logs and tinderbox found");
                    WaitForProgressChange();
                }
                else if(tinderbox != null)
                {
                    Logger.log("80: no logs found, trying to use tinderbox on logs on ground");
                    var logs = GroundItems.closest("Logs");
                    if(logs != null)
                    {
                        Logger.log("80: tinderbox used successfully on ground logs");
                        tinderbox.useOn(logs);
                        WaitForProgressChange();
                    }
                }
                else
                {
                    Logger.log("80: No logs or tinderbox found");
                }
            }
            break;
            case 90: // use raw shrimps on fire
            {
                var shrimps = Inventory.get("Raw shrimps");
                var fire    = GameObjects.closest("Fire");
                if(shrimps != null && fire != null)
                {
                    Logger.log("90: both shrimp and fire found");
                    shrimps.useOn(fire);
                    WaitForProgressChange();
                }
            }
            break;
            case 120: // open gate 9470
            {
                if(SimpleInteract(9470))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 130: // open door 9709
            {
                var Door = GameObjects.closest(9709);
                if(Door == null || Door.distance(Players.getLocal().getTile()) > 15)
                {
                    Logger.log("130: Walking to door");
                    Tile position = new Tile(3080, 3084, 0);
                    Walking.walk(position);
                    Sleep.sleepTick();
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 10000, 1000);
                }
                else
                {
                    Logger.log("130: Interacting with door");
                    if(SimpleInteract(9709))
                    {
                        WaitForProgressChange();
                    }
                }
            }
            break;
            case 140: // talk to chef 3305
            {
                if(TalkNoOptions("Master Chef"))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 150: // use bucket of water (1929) on "Pot of flour", perhaps just listen to inventory changes
            {
                var water = Inventory.get(1929);
                var flour = Inventory.get((t) -> t.getName().contains("Flour") ||
                                                 t.getName().contains("flour"));
                if(water != null && flour != null)
                {
                    Logger.log("150: Water and flour found");
                    if(water.useOn(flour))
                    {
                        WaitForInventoryChange();
                    }
                }
            }
            break;
            case 160: // interact with range 9736
            {
                if(SimpleInteract(9736))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 170: // interact with door 9710
            case 183: //^^
            {
                if(SimpleInteract(9710))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 200: // walk to quest house and open Door 9716
            {
                var Door = GameObjects.closest(9716);
                if(Door == null || Door.distance(Players.getLocal().getTile()) > 15)
                {
                    Logger.log("200: Walking to door");
                    Tile position = new Tile(3085, 3127, 0);
                    Walking.walk(position);
                    Sleep.sleepTick();
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 10000, 1000);
                }
                else
                {
                    Logger.log("200: Door found");
                    if(SimpleInteract(9716))
                    {
                        WaitForProgressChange();
                    }
                }
            }
            break;
            case 220: // talk to Quest Guide 3312
            {
                if(Players.getLocal().getTile().getZ() == 1)
                {
                    SimpleInteract(16673);
                }
                else if(TalkNoOptions("Quest Guide", new Tile(3085, 3123, 0)))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 230: // continue dialogue, then open quest tab ID: 164, 61
            {
                if(CanContinueWithSleep())
                {
                    Logger.log("200: Continuing dialogue");
                    Dialogues.continueDialogue();
                }
                else
                {
                    Logger.log("200: opening qust tab");
                    if(Tabs.openWithMouse(Tab.QUEST))
                    {
                        WaitForProgressChange();
                    }
                }
            }
            break;
            case 240: // talk to Quest Guide 3312
            {
                if(Players.getLocal().getTile().getZ() == 1)
                {
                    SimpleInteract(16673);
                }
                else if(TalkNoOptions("Quest Guide", new Tile(3085, 3123, 0)))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 250: // interact Ladder 9726
            {
                if(Players.getLocal().getTile().getZ() == 1)
                {
                    SimpleInteract(16673);
                }
                else if(SimpleInteract(9726))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 260: // walk and talk to Mining Instructor 3311, continue dialogue
            {
                if(TalkNoOptions("Mining Instructor", new Tile(3081, 9506, 0)))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 270: //continue dialogue
            {
                if(TalkNoOptions("Mining Instructor"))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 300: // Mine Tin rocks
            {
                var PlayerRadius = Players.getLocal().getTile().getArea(10);
                var TinRocks = GameObjects.all((t) -> t.getName().toLowerCase().contains("tin") &&
                                                      PlayerRadius.contains(t.getTile()));
                if(TinRocks.isEmpty())
                {
                    Walking.walk(3078, 9505, 0);
                    break;
                }
                var TinRock = TinRocks.get(Math.max(rand.nextInt(TinRocks.size()) - 1, 0));
                if(Sleep.sleepUntil(() -> TinRock.interact(), 15000, rand.nextInt(1000) + 500))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 310: // mine Copper rocks
            {
                var PlayerRadius = Players.getLocal().getTile().getArea(10);
                var TinRocks = GameObjects.all((t) -> t.getName()
                                                       .toLowerCase()
                                                       .contains("copper") &&
                                                      PlayerRadius.contains(t.getTile()));
                if(TinRocks.isEmpty())
                {
                    Walking.walk(3078, 9505, 0);
                    break;
                }
                var TinRock = TinRocks.get(Math.max(rand.nextInt(TinRocks.size()) - 1, 0));
                if(Sleep.sleepUntil(() -> TinRock.interact(), 15000, rand.nextInt(1000) + 500))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 320: // smelt ore, interact with Furnace 10082
            {
                if(SimpleInteract("Furnace"))
                {
                    if(ItemProcessing.isOpen())
                    {
                        Keyboard.typeKey(Key.SPACE);
                    }
                    WaitForProgressChange();
                }
            }
            break;
            case 330: // talk to Mining Instructor, continue dialogue
            {
                if(TalkNoOptions("Mining Instructor", new Tile(3081, 9506, 0)))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 340: // smith dagger on Anvil
            {
                while(CanContinueWithSleep())
                {
                    Dialogues.continueDialogue();
                }
                if(SimpleInteract("Anvil"))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 350: // Choose Dagger, ID: 312, 9
            {
                if(Smithing.isOpen())
                {
                    Logger.log("350: Smithing is open");
                    var DaggerWidget = Widgets.get((t) -> t != null && t.hasAction("Smith") &&
                                                          Arrays.stream(t.getChildren())
                                                                .anyMatch((x) -> x != null &&
                                                                                 x.getText()
                                                                                  .toLowerCase()
                                                                                  .contains("dagger")));
                    if(DaggerWidget != null && DaggerWidget.isVisible())
                    {
                        Logger.log("350: widget found: " + DaggerWidget);
                        if(ClickWidget(DaggerWidget))
                        {
                            WaitForProgressChange();
                        }
                    }
                }
                else
                {
                    SimpleInteract("Anvil");
                    Sleep.sleepUntil(() -> Smithing.isOpen(), 3000, 500);
                }
            }
            break;
            case 360: // Open Gate 9718
            {
                Area area = new Area(3093, 9503, 3090, 9502);
                if(SimpleInteract(9718, area.getRandomTile()))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 370: // Talk to Combat Instructor
            {
                Area area = new Area(3103, 9507, 3107, 9508);
                if(TalkNoOptions("Combat Instructor", area.getRandomTile()))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 390: // Open Equipment tab ID 164, 56
            {
                if(CanContinueWithSleep())
                {
                    Logger.log("390: Continuing dialogue");
                    Dialogues.continueDialogue();
                }
                else
                {
                    Logger.log("390: opening Equipment tab");
                    if(Tabs.openWithMouse(Tab.EQUIPMENT))
                    {
                        WaitForProgressChange();
                    }
                }
            }
            break;
            case 400: // open stats window 387,2
            {
                if(Tabs.isOpen(Tab.EQUIPMENT))
                {
                    var widget = Widgets.get(387, 2);
                    if(widget != null && widget.isVisible())
                    {
                        if(ClickWidget(widget))
                        {
                            WaitForProgressChange(2000, 1000, 500);
                        }
                    }
                }
                else
                {
                    Tabs.openWithMouse(Tab.EQUIPMENT);
                }
            }
            break;
            case 405: // equip dagger 1205
            {
                var dagger = Inventory.get((t) -> t.getName().toLowerCase().contains("dagger"));
                if(dagger != null)
                {
                    var widget = Inventory.getWidgetForSlot(dagger.getSlot());
                    if(widget != null)
                    {
                        if(ClickWidget(widget))
                        {
                            WaitForProgressChange(2000, 1000, 500);
                        }
                    }
                }
            }
            break;
            case 410: // close equipment window with ID 84,3,11, then talk to Combat Instructor
            {
                var CloseWidget = Widgets.get(84, 3, 11);
                if(CloseWidget != null && CloseWidget.isVisible())
                {
                    CloseWidget.interact();
                    Sleep.sleepTick();
                }
                else
                {
                    if(TalkNoOptions("Combat Instructor"))
                    {
                        WaitForProgressChange();
                    }
                }
            }
            break;
            case 420: //continue dialogue, then equip sword and shield 1277, 1171
            {
                if(CanContinueWithSleep())
                {
                    Logger.log("390: Continuing dialogue");
                    Dialogues.continueDialogue();
                }
                else
                {
                    var sword  = Inventory.get(1277);
                    var shield = Inventory.get(1171);
                    if(sword != null)
                    {
                        sword.interact();
                    }
                    Sleep.sleepTicks(3);
                    if(shield != null)
                    {
                        shield.interact();
                    }
                    WaitForProgressChange();
                }
            }
            break;
            case 430: // open combat tab ID 164,52
            {
                if(Tabs.openWithMouse(Tab.COMBAT))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 440: // Open Gate 9719,9720
            {
                Area area = new Area(3108, 9521, 3111, 9516);
                var gates = GameObjects.all((t) -> area.contains(t.getTile()) &&
                                                   t.getName().toLowerCase().contains("gate"));
                var gate = gates.get(rand.nextInt(gates.size()));
                if(SimpleInteract(gate, null))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 450: // attack Giant rat, just interact
            {
                Target.set(GetClosestMatchingEntity("Giant Rat"));
                if(SimpleInteract(Target.get(), null))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 460: // wait for kill, or interact new rat if wait too long
            {
                if(SimpleInteract(Target.get(), null))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 470: // Open Gate 9719,9720, Talk to Combat Instructor, continue dialogue
            {
                Entity CI = GetClosestMatchingEntity("Combat Instructor");

                Area area = new Area(3108, 9521, 3111, 9516);
                var gates = GameObjects.all((t) -> area.contains(t.getTile()) &&
                                                   t.getName().toLowerCase().contains("gate"));
                var gate = gates.get(rand.nextInt(gates.size()));

                if(CI != null && CI.canReach())
                {
                    TalkToOptions(CI, "", null);
                }
                else if(SimpleInteract(gate, null))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 480: // equip Shortbow 841 and Bronze arrow 882, interact closest Giant rat
            {
                if(CanContinueWithSleep())
                {
                    Logger.log("480: Continuing dialogue");
                    Dialogues.continueDialogue();
                }
                else
                {
                    if(Inventory.contains(841) || Inventory.contains(882))
                    {
                        var bow   = Inventory.get(841);
                        var arrow = Inventory.get(882);
                        if(bow != null)
                        {
                            bow.interact();
                        }
                        Sleep.sleepTicks(3);
                        if(arrow != null)
                        {
                            arrow.interact();
                        }
                        WaitForInventoryChange();
                    }
                    else
                    {
                        Target.set(GetClosestMatchingEntity("Giant Rat"));
                        if(SimpleInteract(Target.get(), new Tile(3112, 9519, 0)))
                        {
                            WaitForProgressChange();
                        }
                    }
                }
            }
            break;
            case 490: // wait for kill, or interact new rat if wait too long
            {
                if(rand.nextInt(100) > 50)
                {
                    Walking.walk(3112, 9519, 0);
                    Sleep.sleepTicks(rand.nextInt(3) + 1);
                }

                if(Target.get() == null || Target.get().distance() > 8)
                {
                    Target.set(GetClosestMatchingEntity("Giant Rat"));
                }

                if(Inventory.contains(841) || Inventory.contains(882))
                {
                    var bow   = Inventory.get(841);
                    var arrow = Inventory.get(882);
                    if(bow != null)
                    {
                        bow.interact();
                    }
                    Sleep.sleepTicks(3);
                    if(arrow != null)
                    {
                        arrow.interact();
                    }
                    //WaitForInventoryChange();
                }
                else
                {
                    if(SimpleInteract(Target.get(), new Tile(3112, 9519, 0)))
                    {
                        WaitForProgressChange();
                    }
                }
            }
            break;
            case 500: // walk and interact Ladder 9727
            {
                if(SimpleInteract(9727))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 510: // walk to Bank booth 10083 and interact, chance to deposit
            {
                //SimpleInteract(10083);
                if(Bank.isOpen())
                {
                    if(rand.nextInt(100) > 50)
                    {
                        Bank.depositAllItems();
                    }
                    if(rand.nextInt(100) > 50)
                    {
                        Bank.depositAllEquipment();
                    }
                }
                else
                {
                    var area = new Area(3119, 3123, 3124, 3120);
                    SimpleInteract("Bank booth", area.getRandomTile());
                    Sleep.sleepUntil(() -> Bank.isOpen(), 10000, 1000);
                }

            }
            break;
            case 520: // interact Poll booth 26815, continue dialogue
            {
                if(Bank.isOpen())
                {
                    Bank.close();
                }

                if(TalkNoOptions("Poll booth"))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 525: // close window ID 310,2,11, then open Door 9721
            {
                var CloseWidget = Widgets.get(310, 2, 11);
                if(CloseWidget != null && CloseWidget.isVisible())
                {
                    CloseWidget.interact();
                    Sleep.sleepTick();
                }
                else
                {
                    if(SimpleInteract(9721))
                    {
                        WaitForProgressChange();
                    }
                }
            }
            break;
            case 530: // Talk with Account Guide 3310, continue dialogue
            {
                if(TalkNoOptions("Account Guide"))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 531: // open Account Management, ID 164,39
            {
                if(CanContinueWithSleep())
                {
                    Dialogues.continueDialogue();
                }
                else
                {
                    if(Tabs.openWithMouse(Tab.ACCOUNT_MANAGEMENT))
                    {
                        WaitForProgressChange();
                    }
                }
            }
            break;
            case 532: // Talk with Account Guide 3310, continue dialogue
            {
                if(TalkNoOptions("Account Guide"))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 540: // Open Door 9722
            {
                if(CanContinueWithSleep())
                {
                    Dialogues.continueDialogue();
                }
                else
                {
                    if(SimpleInteract(9722))
                    {
                        WaitForProgressChange();
                    }
                }
            }
            break;
            case 550: // Walk to and talk to Brother Brace 3319
            {
                Tile position = new Tile(3126, 3107, 0);
                if(TalkNoOptions("Brother Brace", position))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 560: // open prayer tab ID164,57
            {
                if(Tabs.openWithMouse(Tab.PRAYER))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 570: // talk to Brother Brace 3319 then continue dialogue
            {
                if(TalkNoOptions("Brother Brace"))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 580: // open friends tab, 164,40
            {
                if(Tabs.openWithMouse(Tab.FRIENDS))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 600: // talk to Brother Brace 3319 then continue dialogue
            {
                if(TalkNoOptions("Brother Brace"))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 610:    // Open Door 9723
            {
                if(SimpleInteract(9723))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 620: // walk and talk to Magic Instructor 3309
            {
                Tile position = new Tile(3140, 3090, 0);
                if(TalkNoOptions("Magic Instructor", position))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 630: // open magic tab ID164,58
            {
                if(Tabs.openWithMouse(Tab.MAGIC))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 640: //talk to Magic Instructor 3309 and continue dialogue
            {
                if(TalkNoOptions("Magic Instructor"))
                {
                    WaitForProgressChange();
                }
            }
            break;
            case 650: // open magic tab if not open, then click Wind Strike, then click Chicken
            case 660: // wait or repeat if failed
            {
                if(Tabs.isOpen(Tab.MAGIC))
                {
                    Entity chicken = GetClosestMatchingEntity("Chicken");
                    if(chicken != null)
                    {
                        Magic.castSpellOn(Normal.WIND_STRIKE, chicken);
                        WaitForProgressChange();
                    }
                }
                else
                {
                    Tabs.openWithMouse(Tab.MAGIC);
                    Sleep.sleepTicks(3);
                }
            }
            break;
            case 670: //talk to Magic Instructor 3309 and continue dialogue, choose Yes in dialogue, continue dialgoue, choose "No, I'm not planning to do that", continue dialogue, restart if misclick or not in dialogue
            {
                if(Dialogues.areOptionsAvailable() || CanContinueWithSleep() ||
                   SimpleInteract("Magic Instructor"))
                {
                    if(Dialogues.areOptionsAvailable())
                    {
                        if(Dialogues.getOptions().length == 2)
                        {
                            Dialogues.chooseFirstOptionContaining("yes");
                            Sleep.sleepTick();
                            Keyboard.holdSpace(() -> CanContinueTimeout(), 10000);
                            Sleep.sleepUntil(() -> CanContinueTimeout(), 5000, 300);
                            Dialogues.chooseFirstOptionContaining("No,");
                            Keyboard.holdSpace(() -> TutProgress == 1000, 10000);
                        }
                        if(Dialogues.areOptionsAvailable() && Dialogues.getOptions().length == 3)
                        {
                            Dialogues.chooseFirstOptionContaining("No,");
                            Keyboard.holdSpace(() -> TutProgress == 1000, 10000);
                            Sleep.sleepTicks(5);
                        }
                    }
                    else
                    {
                        while(CanContinueWithSleep())
                        {
                            Dialogues.continueDialogue();
                            Sleep.sleepTicks(rand.nextInt(4) + 1);
                        }
                    }

                }
            }
            break;
            case 1000: // done, perhaps thow a random cycle at it to give the illusion of gameplay, or make it walk to some random spot
            {
                Tile[] Choices = {
                        new Tile(2946, 3217, 0),
                        new Tile(3096, 3260, 0),
                        new Tile(3004, 3360, 0),
                        new Tile(3086, 3491, 0),
                        new Tile(3245, 3462, 0),
                        new Tile(3294, 3189, 0)};
                if(Choice == null)
                {
                    Choice = Choices[rand.nextInt(Choices.length)];
                }
                long now = System.nanoTime();
                while(Choice.distance(Players.getLocal().getTile()) > 10 &&
                      (System.nanoTime() - now) < TimeUnit.MINUTES.toNanos(10))
                {
                    Walking.walk(Choice);
                    Sleep.sleepTicks(rand.nextInt(10) + 3);
                }
                //Sleep.sleepUntil(() -> , 60000, rand.nextInt(1000) + 500);
                this.stop();
            }
            break;


            //make sure we're always within x distance to interact with something
            // make function to open inventory for when we need items
            // make sure to disable settings solvers
        }

        return OSRSUtilities.WaitTime(500, 1000);
    }

    @Override
    public void onPaint(Graphics graphics)
    {
        if(Target.get() != null)
        {
            var tilePoly = Target.get().getTile().getPolygon();
            if(tilePoly != null)
            {
                graphics.drawPolygon(tilePoly);
            }
        }
    }

    /**
     * @param graphics
     */
    @Override
    public void onPaint(Graphics2D graphics)
    {
        if(Target.get() != null)
        {
            var box = Target.get().getBoundingBox();
            if(box != null)
            {
                graphics.drawRect(box.x, box.y, box.width, box.height);
            }
        }
    }

    void CheckProgress()
    {
        int     last           = -1;
        boolean spaceIsPressed = false;
        while(true)
        {
            int TutProgress = PlayerSettings.getConfig(TutProgressVarBit);
            //            if(Keyboard.isPressed(Key.SPACE) != spaceIsPressed)
            //            {
            //                Logger.log("Space is pressed: " + Keyboard.isPressed(Key.SPACE));
            //                spaceIsPressed = Keyboard.isPressed(Key.SPACE);
            //            }
            if(TutProgress != last)
            {
                Logger.log("Progress bit: " + TutProgress);
                last = TutProgress;
            }
        }
    }

    void CanContinueThread()
    {
        while(true)
        {
            if(Dialogues.canContinue())
            {
                ticksSinceContinue = 0;
            }
            else
            {
                ticksSinceContinue++;
            }
            try
            {
                Sleep.sleepTick();
            } catch(Exception ignored)
            {

            }

        }
    }

    boolean CanContinueWithSleep()
    {
        return Sleep.sleepUntil(() -> Dialogues.canContinue(), 2000, 300 + rand.nextInt(300));
    }

    boolean ClickWidget(WidgetChild widget)
    {
        if(widget == null || widget.getRectangle() == null)
        {
            return false;
        }

        Point click  = GetRandomPointInRectangle(widget.getRectangle());
        var   result = Mouse.click(click, false);
        if(rand.nextInt(100) > 70)
        {
            Mouse.move();
        }
        return result;
    }

    Point GetRandomPointInRectangle(Rectangle rectangle)
    {
        Logger.log("GetRandomPointInRectangle: " + rectangle);
        var x = rectangle.x + rand.nextInt(rectangle.width);
        var y = rectangle.y + rand.nextInt(rectangle.height);

        return new Point(x, y);
    }

    Entity GetClosestMatchingEntity(String name)
    {
        LevenshteinDistance calc    = new LevenshteinDistance(4);
        ArrayList<Entity>   Options = new ArrayList<>();

        var npcs = NPCs.all((t) -> t != null && t.getName() != null &&
                                   !t.getName().equalsIgnoreCase("null"));
        Options.addAll(npcs);

        var objects = GameObjects.all((t) -> t != null && t.getName() != null &&
                                             !t.getName().equalsIgnoreCase("null"));
        Options.addAll(objects);

        SortedMap<Integer, Entity> Why = new TreeMap<>();
        for(var option : Options)
        {
            if(option != null)
            {
                int distance = calc.apply(option.getName(), name);
                if(distance != -1)
                {
                    //Logger.log(option.getName() + " " + distance);
                    Why.put((int) ((distance + 1) * option.distance()), option);
                }
            }
        }
        //Logger.log(Arrays.toString(npc.toArray()));
        if(!Why.isEmpty())
        {
            var npc = Why.firstEntry().getValue();
            Logger.log("SimpleInteract: NPC found: " + npc + " " + Why.firstEntry().getKey());
            Target.set(npc);
            return npc;
        }
        Logger.log("SimpleInteract: NPC not found: " + name);
        return null;
    }

    boolean CanContinueTimeout()
    {
        //Logger.log(ticksSinceContinue);
        if(ticksSinceContinue > rand.nextInt(3) + 3)
        {
            Logger.log("DialogueContinue timeout elapsed");
            return true;
        }
        return false;
    }

    void WaitForInventoryChange()
    {
        int CurrentSlots = Inventory.getEmptySlots();
        Sleep.sleepUntil(() -> {
            if(rand.nextInt(100) > 90)
            {
                Mouse.move();
            }
            return Inventory.getEmptySlots() != CurrentSlots;
        }, 5000 + rand.nextInt(1000), rand.nextInt(500) + 500);
    }

    void WaitForProgressChange()
    {
        WaitForProgressChange(3000 + rand.nextInt(3000),
                              rand.nextInt(1000) + 300,
                              rand.nextInt(300) + 100);
    }

    void WaitForProgressChange(int timeout, int pollingMax, int pollingMin)
    {
        int TutProgress = PlayerSettings.getConfig(TutProgressVarBit);
        Sleep.sleepUntil(() -> {
            if(rand.nextInt(100) > 90)
            {
                Mouse.move();
            }
            return PlayerSettings.getConfig(TutProgressVarBit) != TutProgress;
        }, timeout, rand.nextInt(pollingMax) + pollingMin);
    }

    WidgetChild GetNameBox()
    {
        return Widgets.get(558, 12);
    }

    WidgetChild GetNameConfirmBox()
    {
        return Widgets.get(558, 18);
    }

    void ChooseName()
    {
        int         MaxNameLength = 12;
        String      CurrentName   = "";
        String      ToAdd         = NameDictionary.GetRandomWord();
        WidgetChild Namebox       = null;
        WidgetChild ConfBox       = null;
        boolean     delete        = false;

        Sleep.sleepTicks(rand.nextInt(5));

        do
        {
            if(ToAdd == null)
            {
                if(MaxNameLength - CurrentName.length() > 3)
                {
                    Logger.log("Adding word");
                    ToAdd = NameDictionary.GetRandomWord(MaxNameLength - CurrentName.length());
                }
                else if(CurrentName.length() >= 12)
                {
                    Logger.log("Deleting current name");
                    CurrentName = "";
                    ToAdd       = NameDictionary.GetRandomWord();
                    delete      = true;
                }
                else
                {
                    Logger.log("Adding random number");
                    ToAdd = String.valueOf(rand.nextInt(10));
                }
                char[] CharArr = ToAdd.toCharArray();
                for(int i = 0; i < CharArr.length; i++)
                {
                    char Char = CharArr[i];
                    if(rand.nextInt(100) > 90)
                    {
                        CharArr[i] = Character.toUpperCase(Char);
                    }
                }
                ToAdd = new String(CharArr);
            }


            Namebox = GetNameBox();
            if(Namebox != null)
            {
                while(!Namebox.getText().contains("*"))
                {
                    if(Namebox.containsMouse())
                    {
                        Mouse.click(false);
                    }
                    else
                    {
                        ClickWidget(Namebox);
                    }
                    Sleep.sleepTicks(rand.nextInt(3) + 1);
                }
            }

            if(delete)
            {
                Logger.log("Deleting");
                Keyboard.holdKey(Key.BACKSPACE, () -> GetNameBox().getText().equals("*"));
                Sleep.sleepUntil(() -> GetNameBox().getText().equals("*"), 5000);
                delete = false;
            }

            Keyboard.type(ToAdd, false, true);
            CurrentName += ToAdd;
            ToAdd = null;
            Logger.log("ChoseName: CurrentName: " + CurrentName);
            Sleep.sleepTicks(rand.nextInt(3) + 3);

            ConfBox = GetNameConfirmBox();
            if(ConfBox != null)
            {
                ClickWidget(GetNameConfirmBox());
                Logger.log("Waiting for name check");
                Sleep.sleepTicks(rand.nextInt(5) + 8);
                Sleep.sleepUntil(() -> GetNameConfirmBox().getActions() != null, 5000);
            }

            ConfBox = GetNameConfirmBox();
            if(ConfBox != null && ConfBox.getActions() != null)
            {
                Logger.log(Arrays.toString(ConfBox.getActions()));
            }
        }
        while(ConfBox != null &&
              (ConfBox.getActions() == null || ConfBox.getActions().length == 0));

        int attempts    = 0;
        int maxAttempts = rand.nextInt(2) + 1;
        while(GetNameConfirmBox() != null && GetNameConfirmBox().isVisible() &&
              attempts < maxAttempts)
        {
            Logger.log("Waiting to submit");
            Mouse.click(GetRandomPointInRectangle(GetNameConfirmBox().getRectangle()), false);
            Sleep.sleepTicks(rand.nextInt(3) + 3);
            attempts++;
        }
    }

    Widget GetAppearanceWidget()
    {
        return Widgets.getWidget(679);
    }

    void ChooseGender()
    {
        int chance = 30;
        while(rand.nextInt(100) > chance)
        {
            var B = GetAppearanceWidget().getChildren()
                                         .stream()
                                         .filter((t) -> t.hasAction("B"))
                                         .findAny();
            if(B.isPresent())
            {
                ClickWidget(B.get());
            }
            else
            {
                var A = GetAppearanceWidget().getChildren()
                                             .stream()
                                             .filter((t) -> t.hasAction("A"))
                                             .findAny();
                if(A.isPresent())
                {
                    ClickWidget(A.get());
                }
            }
            chance += 20;
            Sleep.sleep(rand.nextInt(5000) + 300);
        }
    }

    WidgetChild[] GetPronouns()
    {
        var openOptions = Widgets.get(679, 72, 3);
        if(openOptions != null)
        {
            if(ClickWidget(openOptions))
            {
                var holder = Widgets.get(679, 78);
                return holder == null ? null : holder.getChildren();
            }
        }
        return null;
    }

    void ChoosePronoun()
    {
        var pronouns = GetPronouns();
        if(pronouns != null)
        {
            ClickWidget(pronouns[rand.nextInt(pronouns.length)]);
        }
    }

    void ChooseAppearance()
    {
        var MainWidget = GetAppearanceWidget();
        if(MainWidget != null && MainWidget.getChildren() != null)
        {
            var Selectables = MainWidget.getChildren()
                                        .stream()
                                        .filter((t) -> t.hasAction("Select"))
                                        .toList();
            Logger.log("ChooseAppearance: Start Selectables");
            boolean repeat;
            int     leftyrighty = rand.nextInt(2);
            do
            {
                repeat = false;
                if(rand.nextInt(100) > 0)
                {
                    ChooseGender();
                }

                int changes = 0;
                for(var widget : Selectables)
                {
                    if(rand.nextInt(100) > 70)
                    {
                        continue;
                    }

                    if(rand.nextInt(100) > 90)
                    {
                        Sleep.sleepTicks(rand.nextInt(20) + 1);
                    }

                    int pressCount = rand.nextInt(7);
                    if(changes % 2 == leftyrighty)
                    {
                        pressCount = rand.nextInt(2) + 1;
                    }

                    Point click = GetRandomPointInRectangle(widget.getRectangle());
                    for(var i = 0; i < pressCount; i++)
                    {
                        // do mouse nudge possibly
                        Mouse.click(click, false);
                        //Mouse.move();
                        if(!widget.getRectangle().contains(Mouse.getPosition()))
                        {
                            Mouse.move(GetRandomPointInRectangle(widget.getRectangle()));
                            Sleep.sleepTicks(1);
                            Mouse.click();
                        }
                        Sleep.sleep(rand.nextInt(1000) + 300);
                    }
                    changes++;
                }

                if(rand.nextInt(100) > 50)
                {
                    repeat = true;
                    ChooseGender();
                }
            }
            while(repeat);

            ChoosePronoun();
            Sleep.sleep(rand.nextInt(1000) + 300);


            Logger.log("ChooseAppearance: Start Confirm");
            var Confirm = GetAppearanceWidget().getChildren()
                                               .stream()
                                               .filter((t) -> t.hasAction("Confirm"))
                                               .toList();
            if(!Confirm.isEmpty())
            {
                Logger.log("ChooseAppearance: Confirm found");
                ClickWidget(Confirm.getFirst());
            }
        }
    }

    int GetTutorialProgress()
    {
        return PlayerSettings.getConfig(TutProgressVarBit);
    }
}
