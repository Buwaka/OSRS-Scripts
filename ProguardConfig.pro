-keepclassmembers  enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers  class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keepclassmembers  class **$* {
  public <fields>;
}
-keepclassmembers  public enum ** { *; }
-keepclassmembers  enum ** implements java.io.Serializable { public <fields>; <init>(...);}
-keep @Utilities.Scripting.ExternalLambdaUsage class ** {@Utilities.Scripting.ExternalLambdaUsage *;}
-keep @org.dreambot.api.script.ScriptManifest class ** {@Override public *;}