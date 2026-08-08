# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Allow R8 to optimize and obfuscate release builds.
# Keeping class/source debug attributes increases APK size, so release only keeps
# metadata commonly needed by Kotlin, Compose, Media3 and reflective libraries.
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*

# Give R8 more freedom to shrink/optimize method visibility.
-allowaccessmodification

# Strip noisy logs from release builds.
-assumenosideeffects class android.util.Log {
  v(...);
  d(...);
  i(...);
}

# Also strip verbose/debug/info calls from the app logger.
-assumenosideeffects class com.kitsumed.shizucallrecorder.utils.AppLogger {
  v(...);
  d(...);
  i(...);
  java.lang.String getCallerTag(); # Needed because this is used as a parameter to stripped logger methods
}
