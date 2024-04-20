package Utilities.Scripting;

public interface ICycle
{
    /**
     * @return The amount of time this cycle has been performed
     */
    int GetCycleCount();

    void SetCycleType(CycleType Type);

    CycleType GetCycleType();

    default boolean GoalIsMet() {return false;}

    /**
     * @param Script
     * @param CycleCount Amount of cycles to perform
     *
     * @return if cycle has successfully started
     */
    default boolean onStart(tpircSScript Script, int CycleCount) {return true;}

    /**
     * End cycle after current cycle has finished
     *
     * @return if cycle has successfully ended
     */
// Cycle until goal has been reached
//boolean Start(Requirement)
    default boolean onEnd(tpircSScript Script) {return true;}

    /**
     * End cycle regardless of its situation
     *
     * @return if cycle has successfully ended
     */
    default boolean onEndNow(tpircSScript Script) {return true;}

    default boolean onRestart(tpircSScript Script) {return true;}

    default int onLoop(tpircSScript Script)        {return 0;}

    enum CycleType
    {
        byCount,
        byGoal,
        Endless,
        Null
    }

}
