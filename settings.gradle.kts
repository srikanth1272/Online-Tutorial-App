pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven { url = uri("https://storage.zego.im/maven") }
        maven(url = "https://maven.aliyun.com/repository/jcenter" )
        jcenter()
        //maven(url = "https://google.bintray.com/webrtc")
        //maven( url ="http://dl.bintray.com/devjn/JNlibs")
    }
}


rootProject.name = "MultiPurposeApp"
include(":app")
