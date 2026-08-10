# DevInfo R8 configuration.
#
# Room, Hilt and Glance ship their own consumer rules, so most of what is
# needed arrives automatically. What does NOT is kotlinx-serialization, whose
# generated serializers are only reached reflectively — without these rules the
# report export throws SerializationException in a release build while working
# perfectly in debug.

# ---- kotlinx.serialization -------------------------------------------------
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

# Keep the generated Companion.serializer() for every @Serializable class.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    *** Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static ** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# The report model and the typed navigation routes are both serialized.
-keep,includedescriptorclasses class com.anhprgm.deviceinfo.data.export.**$$serializer { *; }
-keepclassmembers class com.anhprgm.deviceinfo.data.export.** {
    *** Companion;
}
-keep,includedescriptorclasses class com.anhprgm.deviceinfo.navigation.**$$serializer { *; }
-keepclassmembers class com.anhprgm.deviceinfo.navigation.** {
    *** Companion;
}

# ---- Navigation typed routes -----------------------------------------------
# Route classes are resolved by name when restoring a back stack after process
# death, which is exactly the case the AppDetail fix depends on.
-keep class com.anhprgm.deviceinfo.navigation.** { *; }

# ---- Room ------------------------------------------------------------------
-keep class com.anhprgm.deviceinfo.data.db.entity.** { *; }

# ---- Glance / widgets ------------------------------------------------------
# The receiver and tile service are instantiated by the framework by name.
-keep class com.anhprgm.deviceinfo.widget.** { *; }
-keep class com.anhprgm.deviceinfo.background.** { *; }

# ---- Keep line numbers for readable crash reports --------------------------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
