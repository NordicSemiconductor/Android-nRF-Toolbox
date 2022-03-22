
# Simple XML
-keep public class org.simpleframework.** { *; }
-keep class org.simpleframework.xml.** { *; }
-keep class org.simpleframework.xml.core.** { *; }
-keep class org.simpleframework.xml.util.** { *; }

-keepattributes ElementList, Root, InnerClasses, LineNumberTable

-keepclasseswithmembers class * {
    @org.simpleframework.xml.* <fields>;
}
