-keep class IF.Utilities.Encryption.ClassProxy {*;}


-keepclassmembers,allowoptimization enum * implements java.io.Serializable {
    public <fields>;
    <init>(...);
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


-keepclassmembers  class * implements java.io.Serializable {
    public <fields>;
    <init>(...);
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}


-keep @IF.Utilities.Scripting.ExternalLambdaUsage class ** {@IF.Utilities.Scripting.ExternalLambdaUsage *;}
-keep class IF.Scripts.ScriptTemplate { public <methods>; }