# ===== WAJIB KEEP (Retrofit/Gson butuh nama asli) =====

# DTO & Response - auth
-keep class com.bit.bilikdigitalkarawang.features.auth.data.source.remote.dto.** { *; }

# DTO & Response - pemilihan
-keep class com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto.** { *; }

# Gson annotation (field dengan @SerializedName)
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ===== LIBRARY SUPPORT =====
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keepclassmembers enum * { *; }
-renamesourcefileattribute SourceFile

# ===== SUPPRESS WARNINGS =====
-dontwarn org.slf4j.impl.StaticLoggerBinder