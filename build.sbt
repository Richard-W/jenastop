android.Plugin.androidBuild

name := "jenastop"

versionName := Some("2.1-SNAPSHOT")

versionCode := Some(28)

platformTarget in Android := "android-24"

minSdkVersion in Android := "15"

targetSdkVersion in Android := "24"

proguardOptions in Android ++= Seq("-dontobfuscate", "-dontoptimize")

libraryDependencies ++= Seq(
  "com.android.support" % "appcompat-v7" % "23.2.1",
  "io.spray" %%  "spray-json" % "1.3.2",
  "org.jsoup" %	"jsoup"	% "1.9.2"
)

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-target:jvm-1.7", "-feature")

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")
