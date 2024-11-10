package Utilities.Patterns.Runables;

import java.io.Serializable;

@FunctionalInterface
public interface Runable2<T1, T2> extends Serializable
{
    void Run(T1 para1, T2 para2);
}
