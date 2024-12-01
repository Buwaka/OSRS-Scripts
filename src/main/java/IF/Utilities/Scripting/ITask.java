package IF.Utilities.Scripting;

import javax.annotation.Nonnull;
import java.awt.*;

public interface ITask
{
    enum TaskType
    {
        // Simple tasks
        Travel,
        Slaughter,
        RestoreFullHealth,
        Tan,
        LootKills,
        LootLookout,
        LootArea,
        MinimumHealth,
        FleeOvercrowd,
        BankItems,
        InventoryCheck,
        BankEquipment,
        GetCombatRations,
        Combine,
        InteractTask,
        UseOnObjectTask,
        EquipmentTask,
        BossTask,
        AlchTask,
        InteractInventoryTask,
        GETask,

        //Advanced tasks
        SlaughterAndLoot,
        RestorePrayer,
        OpenBank,
        GraveStone

    }

    String GetTaskName();

    @Nonnull
    TaskType GetTaskType();

    default void onDebugPaint(Graphics graphics)   {}

    default void onDebugPaint(Graphics2D graphics) {}

    default void onPaint(Graphics graphics)        {}

    default void onPaint(Graphics2D graphics)      {}

    /**
     * @param Script
     *
     * @return true if successful, false if we need more time, keep triggering start until it is ready
     */
    default boolean onPauseTask(IScript Script) {return true;}

    /**
     * @param Script
     *
     * @return return true if successful, false if we need more time, keep triggering start until it is ready
     */
    default boolean onStartTask(IScript Script) {return true;}

    /**
     * @param Script
     *
     * @return true if successful, false if we need more time, keep triggering start until it is ready
     */
    default boolean onStopTask(IScript Script) {return true;}

    /**
     * @param Script
     *
     * @return true if successful, false if we need more time, keep triggering start until it is ready
     */
    default boolean onUnPauseTask(IScript Script) {return true;}

    void Init(IScript Script);

    boolean StartTask(IScript ifScript);

    void ReplaceTask(IScript ifScript, ITask task);

    boolean StopTask(IScript ifScript);

    void StopTaskNOW(IScript ifScript);

    int priority();

    boolean isPaused();

    boolean isActive();

    boolean accept();

    int execute();
}
