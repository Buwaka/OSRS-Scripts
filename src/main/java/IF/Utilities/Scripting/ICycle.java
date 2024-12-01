package IF.Utilities.Scripting;

import java.awt.*;

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

    CycleType GetCycleType();

    void SetCycleType(CycleType Type);

    /**
     * will be called once there are no active tasks anymore, aka a single cycle has been completed
     *
     * @return true when Cycle is completed, ready for a restart
     */
    boolean isCycleComplete(IScript Script);

    /**
     * @return true when Cycle is completely done and should/will be terminated, typically the same as isCycleComplete
     */
    boolean isCycleFinished(IScript Script);

    boolean isStarted();

    default void onDebugPaint(Graphics graphics)   {}

    default void onDebugPaint(Graphics2D graphics) {}

    /**
     * End cycle after current cycle has finished
     *
     * @return if cycle has successfully ended
     */
    default boolean onEnd(IScript Script) {return true;}

    /**
     * End cycle regardless of its situation
     *
     * @return if cycle has successfully ended
     */
    default boolean onEndNow(IScript Script) {return true;}

    default int onLoop(IScript Script)       {return 1;}

    default void onPaint(Graphics graphics)   {}

    default void onPaint(Graphics2D graphics) {}

    /**
     * When all cycles have been completed and we want to do the cycle again, this is called
     */
    default void onReset(IScript Script) {}

    /**
     * When a cycle has been completed, this will be called
     */
    default boolean onRestart(IScript Script) {return true;}

    /**
     * @param Script
     *
     * @return if cycle has successfully started
     */
    boolean onStart(IScript Script);

}
