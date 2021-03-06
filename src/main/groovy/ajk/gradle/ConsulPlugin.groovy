package ajk.gradle

import ajk.gradle.check.DeregisterCheckExtension
import ajk.gradle.service.DeregisterServiceExtension
import ajk.gradle.service.ListServicesExtension
import ajk.gradle.service.ListServicesTask
import ajk.gradle.service.RegisterServiceExtension
import ajk.gradle.start.StartConsulExtension
import ajk.gradle.start.StartConsulTask
import ajk.gradle.stop.StopConsulExtension
import ajk.gradle.stop.StopConsulTask
import org.gradle.BuildAdapter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle

class ConsulPlugin implements Plugin<Project> {
    static final String DEFAULT_VERSION = "0.6.4"
    static final int DEFAULT_HTTP_PORT = 8500
    static final int DEFAULT_DNS_PORT = 8600

    static final String ESC = "${(char) 27}"
    static final String CYAN = "${ESC}[36m"
    static final String GREEN = "${ESC}[32m"
    static final String YELLOW = "${ESC}[33m"
    static final String MAGENTA = "${ESC}[35m"
    static final String RED = "${ESC}[31m"
    static final String NORMAL = "${ESC}[0m"

    private Project project

    @Override
    void apply(Project project) {
        this.project = project

        StartConsulTask startConsul = project.task(type: StartConsulTask, 'startConsul')
        StopConsulTask stopConsul = project.task(type: StopConsulTask, 'stopConsul')
        ListServicesTask listConsulServices = project.task(type: ListServicesTask, 'listConsulServices')

        def consulExtension = project.extensions.create('consul', ConsulExtension)
        consulExtension.with {
            version = DEFAULT_VERSION
            httpPort = DEFAULT_HTTP_PORT
            dnsPort = DEFAULT_DNS_PORT
            consulDir = new File("$project.rootProject.projectDir/gradle/tools/consul")
            dataDir = new File("$project.buildDir/consul/data")
            configDir = new File("$project.buildDir/consul/consul.d")
        }

        def projectAdapter = [
                startConsul      : startConsul,
                stopConsul       : stopConsul,
                listConsulServices: listConsulServices,
                projectsEvaluated: { Gradle gradle ->
                    startConsul.with {
                        version = consulExtension.version
                        httpPort = consulExtension.httpPort
                        dnsPort = consulExtension.dnsPort
                        consulDir = consulExtension.consulDir
                        dataDir = consulExtension.dataDir
                        configDir = consulExtension.configDir
                    }

                    stopConsul.with {
                        consulDir = consulExtension.consulDir
                    }

                    listConsulServices.with {
                        httpPort = consulExtension.httpPort
                    }
                }
        ] as BuildAdapter

        project.gradle.addBuildListener(projectAdapter)

        project.extensions.create('startConsul', StartConsulExtension, project, consulExtension)
        project.extensions.create('stopConsul', StopConsulExtension, project, consulExtension)
        project.extensions.create('registerConsulService', RegisterServiceExtension, consulExtension)
        project.extensions.create('deregisterConsulService', DeregisterServiceExtension, consulExtension)
        project.extensions.create('deregisterConsulCheck', DeregisterCheckExtension, consulExtension)
        project.extensions.create('listConsulServices', ListServicesExtension, consulExtension)
    }
}