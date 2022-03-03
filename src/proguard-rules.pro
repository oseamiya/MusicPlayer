# Add any ProGuard configurations specific to this
# extension here.

-keep public class com.oseamiya.themusicplayer.TheMusicPlayer {
    public *;
 }
-keeppackagenames gnu.kawa**, gnu.expr**

-optimizationpasses 4
-allowaccessmodification
-mergeinterfacesaggressively

-repackageclasses 'com/oseamiya/themusicplayer/repack'
-flattenpackagehierarchy
-dontpreverify
