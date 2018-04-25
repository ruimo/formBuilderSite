package helpers

import java.nio.file.attribute.BasicFileAttributes
import java.nio.file._

import javax.inject.{Inject, Singleton}
import play.api.Configuration

case class Release(
  path: Path,
  baseName: String,
  version: Version
)

object Release extends Ordering[Release] {
  def parse(path: Path): Release = {
    val fileName = path.getFileName.toString
    val firstHyphenLoc = fileName.indexOf('-')
    if (firstHyphenLoc == -1) throw new IllegalArgumentException("File name does not contain '-': " + path)
    val baseName = fileName.substring(0, firstHyphenLoc)
    val extLoc = fileName.lastIndexOf('.')
    if (extLoc == -1) throw new IllegalArgumentException("File name does not contain extension such as 'xxx.jar': " + path)

    val versionPart = fileName.substring(firstHyphenLoc + 1, extLoc)
    Release(path, baseName, Version.parse(versionPart))
  }

  implicit val ordering: Ordering[Release] = Ordering.by { _.version }

  def compare(a: Release, b: Release) = ordering.compare(a, b)
}

@Singleton
class ReleaseRepo @Inject()(
  conf: Configuration
) {
  val ReleaseDir = Paths.get(conf.get[String]("releaseDir"))

  def getLatestPath(moduleName: String, baseName: String, releaseDir: Path = ReleaseDir): Option[Release] = {
    var latest: Option[Release] = None

    Files.walkFileTree(releaseDir.resolve(moduleName), new SimpleFileVisitor[Path] {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        val rel = Release.parse(file)
        if (rel.baseName == baseName) {
          latest match {
            case None => latest = Some(rel)
            case Some(l) => if (rel > l) latest = Some(rel)
          }
        }

        FileVisitResult.CONTINUE
      }
    })

    latest
  }

  def getModule(moduleName: String, fileName: String, releaseDir: Path = ReleaseDir): Option[Path] = {
    val path = ReleaseDir.resolve(moduleName).resolve(fileName)
    if (Files.exists(path)) Some(path) else None
  }
}
