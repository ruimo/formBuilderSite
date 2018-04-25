package controllers

import javax.inject._

import play.api._
import play.api.mvc._
import java.nio.file.{Files, Path, Paths}

import akka.stream.scaladsl.FileIO
import helpers.{Release, ReleaseRepo}
import play.api.http.HttpEntity
import play.api.libs.json.Json

@Singleton
class HomeController @Inject()(
  cc: ControllerComponents,
  releaseRepo: ReleaseRepo
) extends AbstractController(cc) {
  def getLatest(moduleName: String, baseName: String) = Action { implicit request: Request[AnyContent] =>
    import helpers.VersionJson._

    releaseRepo.getLatestPath(moduleName, baseName) match {
      case None => NotFound("No module for " + baseName)
      case Some(release) =>
        Ok(
          Json.obj(
            "fileName" -> release.path.getFileName.toString,
            "baseName" -> release.baseName,
            "version" -> release.version
          )
        )
    }
  }

  def getModule(moduleName: String, fileName: String) = Action { implicit request: Request[AnyContent] =>
    releaseRepo.getModule(moduleName, fileName) match {
      case None => NotFound("No file for '" + moduleName + "'/'" + fileName + ")")
      case Some(path) =>
        Result(
          header = ResponseHeader(
            200,
            Map(CONTENT_DISPOSITION -> ("attachment; filename=" + fileName))
          ),
          body = HttpEntity.Streamed(FileIO.fromPath(path), Some(Files.size(path)), Some("application/octet-stream"))
        )
    }
  }

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }
}
