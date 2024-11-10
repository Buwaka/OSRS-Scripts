package Utilities.Patterns.Runables;

import java.io.Serializable;

@FunctionalInterface
public interface Runable1<T1> extends Serializable
{
    void Run(T1 para1);
}
