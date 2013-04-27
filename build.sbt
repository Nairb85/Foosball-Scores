import AssemblyKeys._

name := "foosball"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.9.1"

seq(assemblySettings: _*)

excludedFiles in assembly := { (bases: Seq[File]) =>
  bases flatMap { base =>
    val miConf = (base / "META-INF" * "*").get collect {
      case f if f.getName.toLowerCase.contains(".rsa") => f
      case f if f.getName.toLowerCase.contains(".dsa") => f
      case f if f.getName.toLowerCase.contains(".sf") => f
      case f if f.getName.toLowerCase == "manifest.mf" => f
    }
    val licConf = (base / "LICENSE").get
    miConf ++ licConf
  }}

seq(Revolver.settings: _*)

// These settings are for sbt-revolver when you use the command "re-start". To debug test cases, you still
// need to use sbt debug script that you usually use. This section will only invoke the debugger after your
// service has started successfully.

//javaOptions in Revolver.reStart += "-Xdebug"

//javaOptions in Revolver.reStart += "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

//javaOptions in Revolver.reStart += "-Dconfig.resource=application.dev.conf"

//javaOptions in Revolver.reStart += "-Dakka.log-config-on-start=on"

ivyXML :=
 	        <dependencies>
 	        	<exclude org="org.slf4j" module="slf4j-simple"/>
	 	        <exclude org="commons-logging" module="commons-logging"/>
	 	        <exclude org="com.mongodb.casbah" module="casbah-core_2.9.1"/>
	 	        <exclude org="com.mongodb.casbah" module="casbah-commons_2.9.1"/>
	 	        <exclude org="net.liftweb" module="lift-json_2.9.1"/>
	 	        <exclude org="commons-beanutils" module="commons-beanutils"/>
 	        </dependencies>

libraryDependencies ++= Seq(
  //SPRAY
  "cc.spray" % "spray-base" % "1.0-M2.1" % "compile" withSources(),
  "cc.spray" % "spray-server" % "1.0-M2.1" % "compile" withSources(),
  "cc.spray" % "spray-can" % "1.0-M2.1" % "compile" withSources(),
  "cc.spray" % "spray-io" % "1.0-M2.1" % "compile" withSources(),
  //AKKA
  "com.typesafe.akka" % "akka-actor" % "2.0.2",
  "com.typesafe.akka" % "akka-slf4j" % "2.0.2",
  //LIFT-JSON
  "net.liftweb" % "lift-json-ext_2.9.0-1" % "2.4-M2",
  "net.liftweb" % "lift-json_2.9.0-1" % "2.4-M2",
  //CASBAH
  "com.mongodb.casbah" % "casbah_2.9.0-1" % "2.1.5.0",
  "com.novus" %% "salat-core" % "0.0.8",
  //HTTP
  "net.databinder" % "dispatch-http_2.9.0-1" % "0.8.4",
  //Validations
  "commons-validator" % "commons-validator" % "1.3.1"
)

resolvers ++= Seq(
  "TypeSafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS" at "http://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Web plugin repo" at "http://siasia.github.com/maven2",
  "repo.novus rels" at "http://repo.novus.com/releases/",
  "repo.novus snaps" at "http://repo.novus.com/snapshots/",
  "Twitter4j repo" at "http://twitter4j.org/maven2",
  "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository",
  "Spray Repo" at "http://repo.spray.cc/",
  "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"
)

testOptions in Test += Tests.Setup( loader => {
    System.setProperty("config.resource", "application.test.conf")
} )

testOptions := Seq(Tests.Filter(s =>
  Seq("Spec", "Suite", "Unit", "all").exists(s.endsWith(_))))
