package snowy.server

import java.io.File
import kamon.Kamon
import socketserve.WebServer.socketApplication

object ServerMain {

  case class CmdlineArgs(conf: Option[File] = None)

  val cmdLineParser = new scopt.OptionParser[CmdlineArgs]("scopt") {
    head("snowy", "0.1")

    opt[File]("conf").valueName("<file>").
      validate { file =>
        if (file.canRead()) Right(()) else Left(s"${file.toString} not readable")
      }.
      action((file, config) => config.copy(conf = Some(file))).
      text("config file to parse")

    help("help").text("prints this usage text")
  }

  def main(args: Array[String]): Unit = {
    cmdLineParser.parse(args, CmdlineArgs()).foreach { config =>
      run(config)
    }
  }

  def run(cmdLine: CmdlineArgs): Unit = {
    val config = ConfigUtil.configFromFilesAndResources(cmdLine.conf)
    ConfigUtil.writeConfig(config)

    KamonConfig.setConfig(config)
    Kamon.start()
    socketApplication(new GameControl(_))
  }
}
