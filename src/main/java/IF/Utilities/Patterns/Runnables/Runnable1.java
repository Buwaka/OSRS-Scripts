package IF.Utilities.Patterns.Runnables;

import java.io.Serializable;

@FunctionalInterface
public interface Runnable1<T1> extends Serializable
{
    void Run(T1 para1);
}
