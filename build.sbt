import android.Keys._

android.Plugin.androidBuild

name := "jenastop"

versionName := Some("1.3-SNAPSHOT")

versionCode := Some(9)

platformTarget in Android := "android-23"

minSdkVersion in Android := "15"

targetSdkVersion in Android := "23"

proguardOptions in Android ++= Seq("-dontobfuscate", "-dontoptimize")

libraryDependencies ++= Seq(
  "com.android.support" % "appcompat-v7" % "23.1.0",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.5"
)

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-target:jvm-1.7", "-feature")

scalariformSettings
