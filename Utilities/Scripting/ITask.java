package Utilities.Scripting;

import javax.annotation.Nonnull;

public interface ITask
{
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
        Combine,
        MineTask,
        UseOnObjectTask,
        EquipmentTask,
        BossTask,
        AlchTask,

        //Advanced tasks
        SlaughterAndLoot,
        RestorePrayer,
        OpenBank

    }

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
}
