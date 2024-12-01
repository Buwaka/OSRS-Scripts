package IF.Utilities.Patterns.Runnables;

import java.io.Serializable;

@FunctionalInterface
public interface Runnable6<T1, T2, T3, T4, T5, T6> extends Serializable
{
    void Run(T1 para1, T2 para2, T3 para3, T4 para4, T5 para5, T6 para6);
}
