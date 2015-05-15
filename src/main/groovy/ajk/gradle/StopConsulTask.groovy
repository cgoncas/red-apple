package ajk.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import static ajk.gradle.ConsulPlugin.getCYAN
import static ajk.gradle.ConsulPlugin.getNORMAL
import static ajk.gradle.ConsulPlugin.getRED
import static org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS
import static org.apache.tools.ant.taskdefs.condition.Os.isFamily

class StopConsulTask extends DefaultTask {
    @Input
    @Optional
    File consulDir

    @TaskAction
    void stopConsul() {
        if (!isFamily(FAMILY_WINDOWS)) {
            println "${CYAN}* consul:$RED for the time being this plugin is only supported on Windows$NORMAL"
            throw new UnsupportedOperationException();
        }


        f << """
wmic process where (commandline like "%%$consulDir\\consul.exe%%" and not name="wmic.exe") delete
"""

        println "${CYAN}* consul:$NORMAL stopping consul"
        [f.getAbsolutePath()].execute().waitForOrKill(10 * 1000)

        f.delete()
    }
}