package IF.Utilities.Encryption;

import IF.Utilities.Scripting.Annotations.DontEncrypt;
import IF.Utilities.Scripting.Annotations.EncyptedInjection;
import io.vavr.Tuple3;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.*;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class JarEncrypter
{
    static final   String               INJECTED_FIELD_NAME = "WhyAreYouReadingThis";
    static         Map<Integer, byte[]> Classes             = new HashMap<>();
    public static  String               EncryptedDB         = "Juicy.Sex";
    private static String               Key                 = "DvHC.?*!(#h.[f=K!x+CKFXSP68+)=]N";
    private static File                 DBClientJar         = new File(
            System.getProperty("user.dir") + "/client.jar");
    private static File                 EncryptedDBFile     = new File(
            System.getProperty("user.dir") +
            "/target/cYOnCgVU3JUNVvwnhF+gbYluOXvZiY44CyVGyuLP38Y=");

    static ClassFileLocator DynamicLocator = null;

    static
    {


    }

    static void InitLocator(JarFile Data)
    {
        try
        {
            ClassFileLocator jarLocator    = new ClassFileLocator.ForJarFile(Data);
            ClassFileLocator ClientLocator = ClassFileLocator.ForJarFile.of(DBClientJar);
            ClassFileLocator bootloader    = ClassFileLocator.ForClassLoader.ofBootLoader();
            ;
            DynamicLocator = new ClassFileLocator.Compound(jarLocator, ClientLocator, bootloader);
        } catch(Exception e)
        {
            System.out.print(e);
        }
    }


    public static void main(String[] args) throws Exception
    {
        //        String pass           = args[0];
        //                        String jarFilePath    = args[1].replace("\\","/");
        //                        String jarOutFilePath = jarFilePath.replace(".jar", "_encrypted.jar");


        System.out.print(Arrays.toString(args));

        //        String jarFilePath = args[0];
        String jarFilePath    = "C:\\Users\\SammyLaptop\\Documents\\Git\\OSRS-Scripts\\target\\OSRS-Scripts-0.1-hahahahah shaded.jar";
        String jarOutFilePath = "C:\\Users\\SammyLaptop\\Documents\\Git\\OSRS-Scripts\\target\\OSRS-Scripts-0.1-hahahahah shaded_encrypted.jar";

        JarFile jarFile = new JarFile(jarFilePath);
        InitLocator(jarFile);
        JarOutputStream       jos     = new JarOutputStream(new FileOutputStream(jarOutFilePath));
        Enumeration<JarEntry> entries = jarFile.entries();

        List<Tuple3<String, String, byte[]>> Delegated = new ArrayList<>();

        while(entries.hasMoreElements())
        {
            JarEntry entry = entries.nextElement();
            //                System.out.print(entry.getName() + "\n");

            try(InputStream is = jarFile.getInputStream(entry))
            {
                byte[] Bytecode = is.readAllBytes();
                if((entry.getName().endsWith(".class"))) // is in the root folder
                {
                    String className = EncryptedClassLoader.getClassName(entry);

                    if(!hasClassAnnotation(className, DontEncrypt.class, false) &&
                       hasClassAnnotation(className, EncyptedInjection.class, true))
                    {
                        System.out.print("Class " + className + " has @EncyptedInjection\n");

                        var    loader    = new JarEncrypter();
                        String ClassName = EncryptedClassLoader.getClassName(entry);
                        System.out.println(ClassName);

                        //                            var klass = loader.defineClass(ClassName,
                        //                                                           classBytes,
                        //                                                           0,
                        //                                                           classBytes.length);
                        //
                        //
                        //                            var proxy = MakeProxy(klass, locator);
                        var proxy = createProxyWithoutLoading(ClassName, Bytecode);

                        if(proxy != null)
                        {
                            var encrypted = Encryption.encrypt(Bytecode, Key.getBytes());
                            Classes.put(ClassName.hashCode(), encrypted);
                            var Jentry = new JarEntry(entry.getName());
                            Jentry.setTime(System.nanoTime());
                            jos.putNextEntry(Jentry);
                            jos.write(proxy.getBytes());
                            jos.closeEntry();
                        }
                        else
                        {
                            Delegated.add(new Tuple3<>(ClassName, entry.getName(), Bytecode));
                        }

                        continue;
                    }
                }
                jos.putNextEntry(entry);
                jos.write(Bytecode);
                jos.closeEntry();
            }
        }
        while(!Delegated.isEmpty())
        {
            for(var index : Delegated)
            {
                var name      = index._1;
                var entryName = index._2;
                var data      = index._3;
                var proxy     = createProxyWithoutLoading(name, data);

                if(proxy != null)
                {
                    var encrypted = Encryption.encrypt(data, Key.getBytes());
                    Classes.put(name.hashCode(), encrypted);
                    var Jentry = new JarEntry(entryName);
                    Jentry.setTime(System.nanoTime());
                    jos.putNextEntry(Jentry);
                    jos.write(proxy.getBytes());
                    jos.closeEntry();
                    Delegated.remove(index);
                }
            }

        }

        if(!Classes.isEmpty())
        {
            FileOutputStream out = new FileOutputStream(EncryptedDBFile);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);

            objectOutputStream.writeObject(Classes);

            objectOutputStream.close();
            out.close();
        }
        jos.close();

    }

    private static boolean hasClassAnnotation(String classname, Class<? extends Annotation> annotationClass, boolean CheckChildren)
    {
        TypePool typePool = new TypePool.Default(new TypePool.CacheProvider.Simple(),
                                                 DynamicLocator,
                                                 TypePool.Default.ReaderMode.EXTENDED);
        TypeDescription typeDesc = typePool.describe(classname).resolve();
        try
        {
            for(var annotation : typeDesc.getInheritedAnnotations())
            {
                if(annotation.getAnnotationType()
                             .getCanonicalName()
                             .contentEquals(annotationClass.getName()))
                {
                    return true;
                }
            }
        } catch(Exception e)
        {
            System.out.println(
                    "Supposedly can safely ignore a lot of these, just check that these aren't classes we're using: " +
                    e);
        }

        return false;
    }

    static DynamicType.Unloaded<?> createProxyWithoutLoading(String className, byte[] classBytes)
    {
        try
        {
            TypePool typePool = new TypePool.Default(new TypePool.CacheProvider.Simple(),
                                                     DynamicLocator,
                                                     TypePool.Default.ReaderMode.EXTENDED);

            TypeDescription typeDesc = typePool.describe(className).resolve();


            var ClassInProgress = new ByteBuddy().rebase(typeDesc,
                                                         DynamicLocator) // Define the proxy
                                                 .name(className)
                                                 //                                  .visit(AnnotationRemover.of(EncyptedInjection.class))
                                                 //                                  .visit(AnnotationRemover.of(DontEncrypt.class))
                                                 .defineField(INJECTED_FIELD_NAME,
                                                              Object.class,
                                                              Visibility.PUBLIC)
                                                 .method(ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class)))
                                                 .intercept(MethodDelegation.to(ClassProxy.class));
            //            if(typeDesc.getSuperClass() != null)
            //            {
            //                var constructorInterceptor = MethodCall.invoke(typeDesc.getSuperClass().getDeclaredMethods()
            //                                                                   .filter(ElementMatchers.isConstructor())
            //                                                                   .getFirst())  // Select appropriate super constructor
            //                                                       .onSuper()        // Target the superclass
            //                                                       .withAllArguments();  // Pass all arguments to the super constructor
            //                ClassInProgress =  ClassInProgress.constructor(ElementMatchers.any())    // Intercept all constructors
            //                                                       .intercept(constructorInterceptor);
            //            }
            //            else
            //            {
            //                ClassInProgress = ClassInProgress.constructor(ElementMatchers.any())
            //                                                 .intercept(StubMethod.INSTANCE);
            //            }

            return ClassInProgress.make();

        } catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }


    static DynamicType.Unloaded<?> MakeProxy(Class klass, ClassFileLocator data)
    {
        return new ByteBuddy().redefine(klass, data)
                              .name(klass.getName())
                              .visit(AnnotationRemover.of(EncyptedInjection.class))
                              .defineField(INJECTED_FIELD_NAME, Object.class, Visibility.PUBLIC)
                              .method(ElementMatchers.any())
                              .intercept(MethodDelegation.to(ClassProxy.class))
                              .make();
    }

    public static class AnnotationRemover implements AsmVisitorWrapper
    {

        private final Class<? extends Annotation> annotationToRemove;

        public static AsmVisitorWrapper of(Class<? extends Annotation> annotation)
        {
            return new AnnotationRemover(annotation);
        }

        private AnnotationRemover(Class<? extends Annotation> annotationToRemove)
        {
            this.annotationToRemove = annotationToRemove;
        }

        @Override
        public int mergeWriter(int flags)
        {
            return flags;
        }

        @Override
        public int mergeReader(int flags)
        {
            return flags;
        }

        @Override
        public ClassVisitor wrap(TypeDescription instrumentedType, ClassVisitor classVisitor, Implementation.Context implementationContext, TypePool typePool, FieldList<FieldDescription.InDefinedShape> fields, MethodList<?> methods, int writerFlags, int readerFlags)
        {
            return new ClassVisitor(Opcodes.ASM9, classVisitor)
            { // Use appropriate ASM version, like Opcodes.ASM9
                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible)
                {
                    if(descriptor.equals(Type.getDescriptor(annotationToRemove)))
                    {
                        return null; // Remove annotation
                    }
                    return super.visitAnnotation(descriptor, visible);
                }
            };
        }
    }


}
