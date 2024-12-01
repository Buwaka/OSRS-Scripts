package IF.Logic.Generators;


import IF.Utilities.Scripting.Logger;
import IF.Utilities.Scripting.SimpleCycle;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;

public class CycleLibrary
{
    // Json Paths
//    protected static IScript OwnerScript = null;

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
                        Logger.log(
                                "CycleLibrary: GetCycleGenerators: Failed to invoke Cycle generator " +
                                annotation);
                    }
                    return null;
                });
            }

        }

        return out;
    }

//    static public void init(IScript owner)
//    {
//        Logger.log("CycleLibrary loaded by: " + CycleLibrary.class.getClassLoader());
//        Logger.log("IScript loaded by: " + owner.getClass().getClassLoader()); // Assuming 'owner' is an instance of your script
//        Logger.log("IFScript loaded by: " + IFScript.class.getClassLoader()); // If accessibl
//        OwnerScript = owner;
//    }

}
