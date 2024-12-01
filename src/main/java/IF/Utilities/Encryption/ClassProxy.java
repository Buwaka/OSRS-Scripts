package IF.Utilities.Encryption;

import IF.Utilities.Scripting.Logger;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;

public class ClassProxy
{


    @RuntimeType
    public static Object intercept(@This Object self, @Origin Method method, @AllArguments Object[] args, @SuperMethod(nullIfImpossible = true) Method superMethod, @Empty Object defaultValue) throws
            Throwable
    {
        Object injected = self.getClass().getDeclaredField(EncryptedClassLoader.field).get(self);
        //        System.out.print(injected + "\n");
        //        System.out.print(method + "\n");
        //        System.out.print(args + "\n");
        //        System.out.print(superMethod + "\n");
        //        System.out.print(defaultValue + "\n");
        if(injected == null)
        {
            Logger.error("ClassProxy: Failed to get injected instance");
            return null;
            //            throw new IllegalStateException("ClassProxy: Failed to get injected instance");
        }

        Method injectedMethod = injected.getClass()
                                        .getMethod(method.getName(), method.getParameterTypes());
        return injectedMethod.invoke(injected, args);
    }


    public static void ConstructorIntercept(@Argument(0) Object argument, @This Object target) throws
            NoSuchFieldException,
            IllegalAccessException
    {

        Logger.log("ConstructorIntercept: " + argument + " " + target);

        target.getClass().getField(EncryptedClassLoader.field).set(target, argument);
    }


}
