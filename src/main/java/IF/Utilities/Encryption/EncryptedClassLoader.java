package IF.Utilities.Encryption;

import IF.Utilities.Scripting.Logger;
import net.bytebuddy.implementation.bytecode.Throw;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;

public class EncryptedClassLoader extends ClassLoader
{

    static final   String                   field  = "WhyAreYouReadingThis";
    private        HashMap<Integer, byte[]> Map    = null;
    private static ClassLoader              parent = null;

    private EncryptedClassLoader()
    {
        super(parent);
    }

    static public void Init(ClassLoader parent)
    {
        EncryptedClassLoader.parent = parent;
    }

    private <T> T _GetInstance(Class<T> klass)
    {
        if(ReadMap())
        {
            int    id        = klass.getName().hashCode();
            byte[] encrypted = Map.get(id);

            if(encrypted == null)
            {
                Logger.error("id not found");
                return null;
            }

            var newClass = DecryptClass(klass.getName(), encrypted);

            if(newClass == null)
            {
                Logger.error("class not found");
                return null;
            }

            Constructor<?> constructor = null;
            try
            {
                constructor = newClass.getDeclaredConstructor();
            } catch(Throwable e)
            {
                Logger.error("No Constructor found " + e);
                return null;
            }

            Object Dynamicinstance = null;
            T      StaticInstance  = null;
            try
            {
                Logger.log("Constructors: " + Arrays.toString(klass.getConstructors()));
//                Logger.log("Declared Constructors: " + Arrays.toString(klass.getDeclaredConstructors()));

                Dynamicinstance = constructor.newInstance();
                Logger.log("Dynamic instance: " + Dynamicinstance);
                StaticInstance  = klass.getConstructor().newInstance();
                Logger.log("Static instance: " + StaticInstance);
                injectInstance(StaticInstance, Dynamicinstance);
            } catch(Throwable e)
            {
                Logger.error("Failed to create instance " + e + " " + e.getCause());
                Logger.error(Arrays.toString(e.getStackTrace()));
                return null;
            }
            Logger.log("Instance: " + StaticInstance + " " + Dynamicinstance);
            return StaticInstance;
        }
        Logger.log("Failed to read map");
        return null;
    }

    public static <T> CompletableFuture<T> GetInstance(Class<T> klass)
    {
        //#if DynamicClassLoader == true
        //        try
        //        {
        //            var constructor = klass.getDeclaredConstructor();
        //            return constructor.newInstance();
        //        } catch(Exception e)
        //        {
        //            Logger.error("Failed to get instance " + klass + ", " + e);
        //        }
        //#endif
        var loader = new EncryptedClassLoader();
        return CompletableFuture.supplyAsync(() -> loader._GetInstance(klass));
    }

    private Class<?> DecryptClass(String name, byte[] data)
    {
        byte[] decrypted = null;
        byte[] key       = Authenticator.GetInstance().GetScriptKey();
        try
        {
            decrypted = Encryption.decrypt(data, key);
        } catch(Exception e)
        {
            Logger.error("Failed to Decrypt " + Arrays.toString(e.getStackTrace()));

            return null;
        }

        return defineClass(name, decrypted, 0, decrypted.length);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
    {

        Logger.log("Loading Class: " + name);
        if(Map == null)
        {
            if(!ReadMap())
            {
                Logger.log("Loading Class: Failed to load map, fallback on default loader");
//                return getClass().getClassLoader().loadClass(name);
                return super.loadClass(name, resolve);
            }
        }

        var klass = Map.get(name.hashCode());
        if(klass != null)
        {
            var DecryptedClass = DecryptClass(name, klass);
            if(DecryptedClass != null)
            {
                Logger.log("Loading Class: Class found");
                return DecryptedClass;
            }
        }
        Logger.log("Loading Class: Class not found, fallback on default");
//        return getClass().getClassLoader().loadClass(name);
        return super.loadClass(name, resolve);
    }

    private static File[] getResourceFolderFiles(String folder)
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL         url    = loader.getResource(folder);
        String      path   = url.getPath();
        return new File(path).listFiles();
    }

    private boolean ReadMap()
    {
        if(Map != null)
        {
            return true;
        }
        var map = Authenticator.GetInstance().GetScriptKeyMap();
        if(map != null)
        {
            Map = (HashMap<Integer, byte[]>) map._2;
            return true;
        }
        Logger.error("Failed to get map");
        return false;
    }

    public static String getClassName(JarEntry entry)
    {
        if(entry != null && entry.getName().endsWith(".class"))
        {
            String name = entry.getRealName();
            if(name != null)
            {
                var lastDotIndex = name.lastIndexOf('.');
                var extension    = name.substring(lastDotIndex);
                return name.substring(0, lastDotIndex).replace('/', '.');
            }
            return null;
        }
        else
        {
            return null; // Or handle the case where it's not a class file
        }
    }

    private static File GetCurrentJar()
    {
        try
        {
            return new File(EncryptedClassLoader.class.getProtectionDomain()
                                                      .getCodeSource()
                                                      .getLocation()
                                                      .toURI());
        } catch(Exception e)
        {
            Logger.error("GetCurrentJar: Failed to get current jar, " + e);
        }

        return null;
    }

    private static boolean isClassInsideJar(Class<?> targetClass)
    {
        String classPath = targetClass.getResource(targetClass.getSimpleName() + ".class")
                                      .toString();
        return classPath.startsWith("jar:");
    }

    void injectInstance(Object proxy, Object instance) throws
            IllegalAccessException,
            NoSuchFieldException
    {
        proxy.getClass().getDeclaredField(field).set(proxy, instance);
    }
}
