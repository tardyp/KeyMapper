package io.github.sds100.keymapper.system.root

import io.github.sds100.keymapper.system.Shell
import java.io.IOException
import java.io.InputStream

/**
 * Created by sds100 on 01/10/2018.
 */
object RootUtils {

    @Throws(IOException::class)
    fun getSuProcess(): Process {
        return ProcessBuilder("su").start()
    }

    /**
     * @return whether the command was executed successfully
     */
    fun executeRootCommand(vararg command: String, waitFor: Boolean = false): Boolean {
        return Shell.run("su", "-c", *command, waitFor = waitFor)
    }

    /**
     * Remember to close it after using it.
     */
    @Throws(IOException::class)
    fun getRootCommandOutput(command: String): InputStream {
        return Shell.getShellCommandStdOut("su", "-c", command)
    }
}