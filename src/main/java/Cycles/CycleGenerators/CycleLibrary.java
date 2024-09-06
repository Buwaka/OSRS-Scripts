package Cycles.CycleGenerators;

import Utilities.Scripting.SimpleCycle;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.utilities.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;

public class CycleLibrary
{
    // Json Paths
    protected static tpircSScript OwnerScript = null;

    static public void init(tpircSScript owner)
    {
        OwnerScript = owner;
    }


    public static Map<String, List<Supplier<SimpleCycle[]>>> GetCycleGenerators()
    {
        Map<String, List<Supplier<SimpleCycle[]>>> out = new HashMap<>();
        Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forJavaClassPath())
                                                                            .setScanners(new MethodAnnotationsScanner()));
        Set<Method> methods = reflections.getMethodsAnnotatedWith(CycleGeneratorID.class);

        for(var method : methods)
        {
            var annotation = methods.iterator().next().getAnnotation(CycleGeneratorID.class);
            for(var skill : annotation.Skills())
            {
                out.getOrDefault(skill.name(), new ArrayList<>()).add(() -> {
                    try
                    {
                        return (SimpleCycle[]) method.invoke(null);
                    } catch(Exception e)
                    {
                        Logger.log("CycleLibrary: GetCycleGenerators: Failed to invoke Cycle generator " + annotation);
                    }
                    return null;
                });
            }

        }

        return out;
    }

}
