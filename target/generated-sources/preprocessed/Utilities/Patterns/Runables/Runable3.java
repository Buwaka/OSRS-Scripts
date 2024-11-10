package Utilities.Patterns.Runables;

import java.io.Serializable;

@FunctionalInterface
public interface Runable3<T1, T2, T3> extends Serializable
{
    void Run(T1 para1, T2 para2, T3 para3);
}
