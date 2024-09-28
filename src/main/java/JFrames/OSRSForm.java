package JFrames;

import java.awt.*;

public interface OSRSForm
{
    Container GetForm();

    default void onEnd()   {}

    default void onStart() {}

    default void tick()    {}
}
