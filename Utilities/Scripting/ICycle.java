package Utilities.Scripting;

public interface ICycle
{
    enum CycleType
    {
        byCount,
        byGoal,
        Endless,
        NaturalEnd
    }

    /**
     * @return The amount of time this cycle has been performed
     */
    int GetCycleCount();

    void SetCycleType(CycleType Type);

    CycleType GetCycleType();

    /**
     * @return Cycle is completely done and should/will be terminated
     */
    boolean isFinished();

    boolean isStarted();

    /**
     * will be called once there are no active tasks anymore, aka a single cycle has been completed
     *
     * @return Cycle completed, ready for a restart
     */
    boolean isCycleComplete(tpircSScript Script);

    /**
     * @return Whether the goal of this cycle has been met, based on CycleType
     */
    boolean isGoalMet();

    /**
     * @param Script
     *
     * @return if cycle has successfully started
     */
    boolean onStart(tpircSScript Script);

    /**
     * End cycle after current cycle has finished
     *
     * @return if cycle has successfully ended
     */
    default boolean onEnd(tpircSScript Script) {return true;}

    /**
     * End cycle regardless of its situation
     *
     * @return if cycle has successfully ended
     */
    default boolean onEndNow(tpircSScript Script) {return true;}

    /**
     * When a cycle has been completed, this will be called
     */
    default boolean onRestart(tpircSScript Script) {return true;}

    /**
     * When all cycles have been completed and we want to do the cycle again, this is called
     */
    default void onReset(tpircSScript Script) {}

    default int onLoop(tpircSScript Script) {return 1;}

}
