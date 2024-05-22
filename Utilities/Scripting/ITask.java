package Utilities.Scripting;

import javax.annotation.Nonnull;

public interface ITask
{
    String GetTaskName();

    @Nonnull
    TaskType GetTaskType();

    /**
     * @param Script
     *
     * @return return true if successful, false if we need more time, keep triggering start until it is ready
     */
    default boolean onStartTask(tpircSScript Script) {return true;}

    /**
     * @param Script
     *
     * @return true if successful, false if we need more time, keep triggering start until it is ready
     */
    default boolean onPauseTask(tpircSScript Script) {return true;}

    /**
     * @param Script
     *
     * @return true if successful, false if we need more time, keep triggering start until it is ready
     */
    default boolean onUnPauseTask(tpircSScript Script) {return true;}

    /**
     * @param Script
     *
     * @return true if successful, false if we need more time, keep triggering start until it is ready
     */
    default boolean onStopTask(tpircSScript Script) {return true;}

    enum TaskType
    {
        // Simple tasks
        Travel,
        Slaughter,
        RestoreFullHealth,
        Pray,
        LootKills,
        LootLookout,
        LootArea,
        MinimumHealth,
        FleeOvercrowd,
        BankItems,
        BankEquipment,
        GetCombatRations,
        RestorePrayer,
        Combine,
        OpenBank,
        MineTask,
        UseOnObjectTask,
        EquipmentTask,

        //Advanced tasks
        SlaughterAndLoot
    }
}
