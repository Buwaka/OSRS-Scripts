package Utilities.MouseAlgorithm;

import org.dreambot.api.input.event.impl.mouse.MouseButton;
import org.dreambot.api.input.mouse.algorithm.MouseAlgorithm;
import org.dreambot.api.input.mouse.destination.AbstractMouseDestination;

public class IFMouseAlgorithm implements MouseAlgorithm
{
    /**
     * @param abstractMouseDestination
     *
     * @return
     */
    @Override
    public boolean handleMovement(AbstractMouseDestination abstractMouseDestination)
    {
        return false;
    }

    /**
     * @param mouseButton
     *
     * @return
     */
    @Override
    public boolean handleClick(MouseButton mouseButton)
    {
        return false;
    }
}
