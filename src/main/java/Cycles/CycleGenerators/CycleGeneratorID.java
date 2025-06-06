package Cycles.CycleGenerators;


import OSRSDatabase.ItemDB;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CycleGeneratorID
{
    public enum Purpose
    {
        Money,
        Experience
    }

    String Description();

    String Name();

    Purpose[] Purposes();

    ItemDB.Skill[] Skills();


}
