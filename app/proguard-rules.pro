
# Simple XML
-dontwarn javax.xml.**

-keep public class org.simpleframework.**{ *; }
-keep class org.simpleframework.xml.**{ *; }
-keep class org.simpleframework.xml.core.**{ *; }
-keep class org.simpleframework.xml.util.**{ *; }

-keepattributes Signature
-keepattributes *Annotation*

# Ignore our XML Serialization classes
-keep public class your.annotated.pojo.models.*{
  public protected private *;
}

# Crashlytics
-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.