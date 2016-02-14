import android.Keys._

android.Plugin.androidBuild

name := "jenastop"

versionName := Some("2.0-SNAPSHOT")

versionCode := Some(26)

platformTarget in Android := "android-23"

minSdkVersion in Android := "15"

targetSdkVersion in Android := "23"

proguardOptions in Android ++= Seq("-dontobfuscate", "-dontoptimize")

libraryDependencies ++= Seq(
  "com.android.support" % "appcompat-v7" % "23.1.1",
  "io.spray" %%  "spray-json" % "1.3.2",
  "org.jsoup" %	"jsoup"	% "1.8.3"
)

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-target:jvm-1.7", "-feature")

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

scalariformSettings
