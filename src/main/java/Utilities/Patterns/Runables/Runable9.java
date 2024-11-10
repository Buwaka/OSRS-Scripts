package Utilities.Patterns.Runables;

import java.io.Serializable;

@FunctionalInterface
public interface Runable9<T1, T2, T3, T4, T5, T6, T7, T8, T9> extends Serializable
{
    void Run(T1 para1, T2 para2, T3 para3, T4 para4, T5 para5, T6 para6, T7 para7, T8 para8, T9 para9);
}
