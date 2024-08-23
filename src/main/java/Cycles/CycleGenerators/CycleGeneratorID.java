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
    String Name();
    String Description();
    ItemDB.Skill[] Skills();
    Purpose[] Purposes();

    public enum Purpose
    {
        Money,
        Experience
    }


}
