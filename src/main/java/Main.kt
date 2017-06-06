import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

/**
 * Created by kasra on 5/25/17.
 */

fun main(args: Array<String>) {
  val shell = Shell()
  shell.loop()
}

class Shell(workingDir: File = Paths.get("/home/kasra").toFile()) {

  var workingDirectory: File = workingDir
  var error: String = ""
  var status: Int = 0

  fun loop() {
    do {
      print("$ ")
      val line: String? = readLine()
      if (null != line) {
        execute(line)
      } else {
        status = 1
      }
    } while (0 == status)
  }

  /**
   * Executes a command with the shell. Checks for builtins.
   *
   * @return a nullable pair, the first being the stdout and the second being the stderr.
   *         The result is null when there was an error attempting to execute the command.
   */
  fun execute(command: String): Pair<String, String>? {
    when (command.split("\\s+".toRegex())[0]) {
      "cd" -> return cd(command.args()[1])
      "lsa" -> return lsa(command)
      else -> return runCommand(command)
    }
  }

  /**
   * Change directory to named subdirectory.
   */
  fun cd(arg: String): Pair<String, String>? {
    when(arg) {

      // parent directory
      ".." ->
        if (workingDirectory.parentFile.exists()) {
          // only doesn't exist if directory is root
          return Pair("", "file does not have parent")

        } else {
          workingDirectory = workingDirectory.parentFile
          return Pair("", "")
        }

      // current directory
      "." -> return Pair("", "")
    }

    //TODO("show current directory in bash prompt")


    val subdirectory = workingDirectory.listFiles()
        .filter { f -> f.isDirectory && f.name == arg }
        .firstOrNull()
    if (null == subdirectory) {
      return Pair("", "subdirectory '$arg' does not exist")
    } else {
      workingDirectory = subdirectory
      return Pair("", "")
    }
  }

  /**
   *
   */
  fun lsa(command: String): Pair<String, String>? {
    if (1 == command.args().size) {
      val result =  workingDirectory.listFiles()
          .map { f -> if (f.isDirectory) f.name else f.name + "/" }
          .joinToString(separator = "\n")
      return Pair(result, "")
    } else if (2 == command.args().size) {
      val subdirectory = workingDirectory.listFiles()
          .filter { f -> f.isDirectory && f.name == command.args()[1] }
          .firstOrNull()
      TODO("implement subdirectory listing")
    }
    TODO("see previous todo ^^")
  }

  fun runCommand(command: String, wd: File = workingDirectory): Pair<String, String>? {
    try {
      val parts = command.split("\\s".toRegex())
      val proc = ProcessBuilder(*parts.toTypedArray())
          .directory(wd)
          .redirectOutput(ProcessBuilder.Redirect.PIPE)
          .redirectError(ProcessBuilder.Redirect.PIPE)
          .start()

      proc.waitFor(60, TimeUnit.MINUTES)

      val inputReader = proc.inputStream.bufferedReader()
      val errorReader = proc.errorStream.bufferedReader()

      //TODO test to see if error ever actually has stuff in it.
      //TODO maybe use java 9's better processes

      val input = inputReader.readText(); inputReader.close()
      val error = errorReader.readText(); errorReader.close()

      return Pair(input, error)

    } catch(e: IOException) {
      error = e.message ?: ""
      status = -1
      return null
    }
  }

  fun String.args(): List<String> {
    return this.split("\\s+".toRegex())
  }
}