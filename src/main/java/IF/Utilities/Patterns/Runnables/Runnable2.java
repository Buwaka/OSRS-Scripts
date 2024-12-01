package IF.Utilities.Patterns.Runnables;

import java.io.Serializable;

@FunctionalInterface
public interface Runnable2<T1, T2> extends Serializable
{
    void Run(T1 para1, T2 para2);
}
