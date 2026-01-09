/**
 * Publishing configuration for AutoLocalize library modules.
 * 
 * Usage:
 * 1. Add this to root build.gradle.kts: apply(from = "publishing.gradle.kts")
 * 2. Configure gradle.properties with version, group, and credentials
 * 3. Run: ./gradlew publishToMavenLocal (for testing)
 * 4. Run: ./gradlew publishAllPublicationsToMavenCentralRepository (for Maven Central)
 */

import java.util.Properties

// Load properties
val properties = Properties()
val propertiesFile = rootProject.file("gradle.properties")
if (propertiesFile.exists()) {
    propertiesFile.inputStream().use { properties.load(it) }
}

val VERSION_NAME: String by properties
val GROUP: String by properties

// Configure publishing for each library module
subprojects {
    val projectName = name
    
    // Only configure publishing for library modules (not sample-app or app)
    if (projectName.startsWith("autolocalize") && projectName != "autolocalize-core") {
        apply(plugin = "maven-publish")
        apply(plugin = "signing")
        
        // Configure Android library publishing
        if (plugins.hasPlugin("com.android.library")) {
            afterEvaluate {
                val android = extensions.getByName("android") as com.android.build.gradle.LibraryExtension
                
                val sourcesJar = tasks.create("sourcesJar", Jar::class) {
                    archiveClassifier.set("sources")
                    from(android.sourceSets.getByName("main").java.srcDirs)
                }
                
                val javadocJar = tasks.create("javadocJar", Jar::class) {
                    archiveClassifier.set("javadoc")
                    dependsOn("dokkaJavadoc")
                    from(tasks.named("dokkaJavadoc"))
                }
                
                publishing {
                    publications {
                        create<MavenPublication>("release") {
                            groupId = GROUP
                            artifactId = projectName
                            version = VERSION_NAME
                            
                            from(components["release"])
                            
                            artifact(sourcesJar)
                            // artifact(javadocJar) // Uncomment if you add Dokka for documentation
                            
                            pom {
                                name.set(properties.getProperty("POM_NAME", "AutoLocalize"))
                                description.set(
                                    properties.getProperty(
                                        "POM_DESCRIPTION",
                                        "Android localization library with resource-based and runtime translation"
                                    )
                                )
                                url.set(properties.getProperty("POM_URL", "https://github.com/YOUR_USERNAME/autolocalize"))
                                
                                licenses {
                                    license {
                                        name.set(properties.getProperty("POM_LICENCE_NAME", "The Apache Software License, Version 2.0"))
                                        url.set(properties.getProperty("POM_LICENCE_URL", "https://www.apache.org/licenses/LICENSE-2.0.txt"))
                                        distribution.set(properties.getProperty("POM_LICENCE_DIST", "repo"))
                                    }
                                }
                                
                                developers {
                                    developer {
                                        id.set(properties.getProperty("POM_DEVELOPER_ID", "yourusername"))
                                        name.set(properties.getProperty("POM_DEVELOPER_NAME", "Your Name"))
                                        email.set(properties.getProperty("POM_DEVELOPER_EMAIL", "your.email@example.com"))
                                    }
                                }
                                
                                scm {
                                    url.set(properties.getProperty("POM_SCM_URL", "https://github.com/YOUR_USERNAME/autolocalize"))
                                    connection.set(properties.getProperty("POM_SCM_CONNECTION", "scm:git:git://github.com/YOUR_USERNAME/autolocalize.git"))
                                    developerConnection.set(properties.getProperty("POM_SCM_DEV_CONNECTION", "scm:git:ssh://github.com:YOUR_USERNAME/autolocalize.git"))
                                }
                            }
                        }
                    }
                    
                    repositories {
                        maven {
                            name = "MavenCentral"
                            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                            credentials {
                                username = properties.getProperty("SONATYPE_USERNAME") ?: System.getenv("SONATYPE_USERNAME") ?: ""
                                password = properties.getProperty("SONATYPE_PASSWORD") ?: System.getenv("SONATYPE_PASSWORD") ?: ""
                            }
                        }
                    }
                }
                
                signing {
                    val signingKeyId = properties.getProperty("SIGNING_KEY_ID") ?: System.getenv("SIGNING_KEY_ID") ?: ""
                    val signingPassword = properties.getProperty("SIGNING_PASSWORD") ?: System.getenv("SIGNING_PASSWORD") ?: ""
                    val signingSecretKeyRingFile = properties.getProperty("SIGNING_SECRET_KEY_RING_FILE") ?: System.getenv("SIGNING_SECRET_KEY_RING_FILE") ?: ""
                    
                    if (signingKeyId.isNotEmpty() && signingPassword.isNotEmpty()) {
                        useInMemoryPgpKeys(signingKeyId, signingSecretKeyRingFile, signingPassword)
                        sign(publishing.publications["release"])
                    }
                }
            }
        }
    }
    
    // Configure JVM library publishing (for autolocalize-core)
    if (projectName == "autolocalize-core") {
        apply(plugin = "maven-publish")
        apply(plugin = "signing")
        
        afterEvaluate {
            val sourcesJar = tasks.create("sourcesJar", Jar::class) {
                archiveClassifier.set("sources")
                from(sourceSets.main.get().allSource)
            }
            
            val javadocJar = tasks.create("javadocJar", Jar::class) {
                archiveClassifier.set("javadoc")
                from(tasks.named("dokkaJavadoc"))
            }
            
            publishing {
                publications {
                    create<MavenPublication>("maven") {
                        groupId = GROUP
                        artifactId = projectName
                        version = VERSION_NAME
                        
                        from(components["java"])
                        artifact(sourcesJar)
                        // artifact(javadocJar) // Uncomment if you add Dokka
                        
                        pom {
                            name.set(properties.getProperty("POM_NAME", "AutoLocalize Core"))
                            description.set("Core library for AutoLocalize - pure Kotlin translation interfaces and utilities")
                            url.set(properties.getProperty("POM_URL", "https://github.com/YOUR_USERNAME/autolocalize"))
                            
                            licenses {
                                license {
                                    name.set(properties.getProperty("POM_LICENCE_NAME", "The Apache Software License, Version 2.0"))
                                    url.set(properties.getProperty("POM_LICENCE_URL", "https://www.apache.org/licenses/LICENSE-2.0.txt"))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

