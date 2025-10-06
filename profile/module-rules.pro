# Simple XML
-dontwarn javax.xml.**

-keep public class org.simpleframework.**{ *; }
-keep class org.simpleframework.xml.**{ *; }
-keep class org.simpleframework.xml.core.**{ *; }
-keep class org.simpleframework.xml.util.**{ *; }

# Ignore our XML Serialization classes
-keep public class no.nordicsemi.android.toolbox.profile.repository.uartXml.XmlConfiguration {
  public protected private *;
}
-keep public class no.nordicsemi.android.toolbox.profile.repository.uartXml.XmlMacro {
  public protected private *;
}