enablePlugins(android.AndroidApp)

name := "jenastop"

versionName := Some("2.1-SNAPSHOT")

versionCode := Some(30)

platformTarget in Android := "android-27"

minSdkVersion in Android := "15"

targetSdkVersion in Android := "27"

proguardOptions in Android ++= Seq("-dontobfuscate", "-dontoptimize")

libraryDependencies ++= Seq(
  "com.android.support" % "appcompat-v7" % "25.3.1",
  "io.spray" %%  "spray-json" % "1.3.4",
  "org.jsoup" %	"jsoup"	% "1.11.1",
  "com.github.ghik" %% "silencer-lib" % "0.5"
)

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-target:jvm-1.7", "-feature", "-deprecation", "-Xfatal-warnings")

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

addCompilerPlugin("com.github.ghik" %% "silencer-plugin" % "0.5")
