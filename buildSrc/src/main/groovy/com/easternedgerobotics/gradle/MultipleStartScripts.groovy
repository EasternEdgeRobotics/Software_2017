package com.easternedgerobotics.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.application.CreateStartScripts

class MulipleStartScriptsContainer {
    def mainClasses = [:]
}

class MultipleStartScripts implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create('multipleStartScripts', MulipleStartScriptsContainer)
        project.afterEvaluate {
            project.multipleStartScripts.mainClasses.each {
                def script = it.key
                def fullyQualifiedClassName = it.value
                def taskName = script + 'StartScripts'

                project.tasks.create(name: taskName, type: CreateStartScripts) {
                    defaultJvmOpts = project.applicationDefaultJvmArgs
                    mainClassName = fullyQualifiedClassName
                    applicationName = script
                    outputDir = new File(project.buildDir, 'scripts')
                    classpath = project.tasks[JavaPlugin.JAR_TASK_NAME].outputs.files + project.configurations.runtime
                }

                project.applicationDistribution.into('bin') {
                    from(project.tasks[taskName])
                    fileMode = 0755
                    duplicatesStrategy = 'exclude'
                }
            }
        }
    }
}
