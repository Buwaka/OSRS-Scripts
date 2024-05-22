package Utilities.Scripting;

import java.util.function.Supplier;

public interface ICycle
{
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
     * will onlu be called once there are no active tasks anymore, so implement this as an extra check
     *
     * @return Cycle completed, ready for a restart
     */
    default boolean isCycleComplete(tpircSScript Script) {return true;}

    ;

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

    default boolean onRestart(tpircSScript Script) {return true;}

    default int onLoop(tpircSScript Script)        {return 0;}

    enum CycleType
    {
        byCount,
        byGoal,
        Endless,
        Null;

        /**
         * returns true when goal is met
         */
        public Supplier<Boolean> Goal  = null;
        public Integer           Count = null;
    }

}
