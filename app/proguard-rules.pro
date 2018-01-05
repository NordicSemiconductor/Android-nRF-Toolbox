-libraryjars ../app/libs
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-dontshrink
-verbose

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keepattributes *Annotation*
-keepattributes Signature

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older platform version.
-dontwarn android.support.**

# android-support-design
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep,includedescriptorclasses class android.support.transition.** { *; }
-keep public class android.support.design.R$* { *; }

# android-support-v4
-dontwarn android.support.v4.**
-dontnote android.support.v4.**
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v4.app.** { *; }
-keep class android.support.v4.** { *; }

# android-support-v7
-dontwarn android.support.v7.**
-dontnote android.support.v7.**
-keep interface android.support.v7.internal.** { *; }
-keep class android.support.v7.internal.** { *; }
-keep class android.support.v7.** { *; }

-keep class com.google.android.gms.**
-dontwarn com.google.android.gms.**

# Java
-keep class java.** { *; }
-dontnote java.**
-dontwarn java.**

-keep class javax.** { *; }
-dontnote javax.**
-dontwarn javax.**

-keep class sun.misc.Unsafe { *; }
-dontnote sun.misc.Unsafe

-keep class javax.xml.stream.XMLOutputFactory { *; }

# (the rt.jar has them)
-dontwarn com.bea.xml.stream.XMLWriterBase
-dontwarn javax.xml.stream.events.**

# Simple XML
-keep public class org.simpleframework.** { *; }
-keep class org.simpleframework.xml.** { *; }
-keep class org.simpleframework.xml.core.** { *; }
-keep class org.simpleframework.xml.util.** { *; }

-keepattributes ElementList, Root, InnerClasses, LineNumberTable

-keepclasseswithmembers class * {
    @org.simpleframework.xml.* <fields>;
}

# Chart Engine
-keep class org.achartengine.** { *; }
-dontnote org.achartengine.**

# HTTP (might require legacyLibraries) ?
-dontnote org.apache.http.params.**
-dontnote org.apache.http.conn.scheme.**
-dontnote org.apache.http.conn.**
-dontnote android.net.http.**

# DFU Library
-keep class no.nordicsemi.android.dfu.** { *; }

