resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

resolvers += "spray repo" at "http://repo.spray.cc"

resolvers += Resolver.url("artifactory", url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.8.3")

addSbtPlugin("cc.spray" % "sbt-revolver" % "0.6.1")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.4.0")