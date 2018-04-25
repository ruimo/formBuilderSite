package helpers

import scala.concurrent.duration._
import java.nio.file.{Files, Paths}

import play.api.Configuration
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import org.specs2.mutable.Specification
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.test.WithServer

import scala.concurrent.Await

class ReleaseSpec extends Specification with InjectorSupport {
  val baseDir = Files.createTempDirectory(null)
  val testPort = 3000

  "Release" should {
    "Can parse file name" in {
      Release.parse(Paths.get("/tmp/foobar-1.2.zip")) == Release(
        Paths.get("/tmp/foobar-1.2.zip"),
        "foobar",
        Version.parse("1.2")
      )
    }

    "Can fetch latest version" in {
      val baseDir = Files.createTempDirectory(null)

      val config: Config = ConfigFactory.parseString(
        s"""
releaseDir = ${baseDir.toAbsolutePath.toString}
"""
      )
      val conf: Configuration = new Configuration(config)
      val dir = baseDir.resolve("mymodule")
      Files.createDirectory(dir)

      Files.createFile(dir.resolve("base-1.2.zip"))
      Files.createFile(dir.resolve("base-1.0.zip"))
      Files.createFile(dir.resolve("base-1.10.zip"))

      Files.createFile(dir.resolve("foo-2.5.zip"))
      Files.createFile(dir.resolve("foo-3.11.zip"))
      Files.createFile(dir.resolve("foo-4.2.zip"))

      (new ReleaseRepo(conf)).getLatestPath("mymodule", "base", baseDir) === Some(
        Release(
          dir.resolve("base-1.10.zip"),
          "base",
          VersionMajorMinor(VersionNo(1), VersionNo(10), false)
        )
      )

      (new ReleaseRepo(conf)).getLatestPath("mymodule", "foo", baseDir) === Some(
        Release(
          dir.resolve("foo-4.2.zip"),
          "foo",
          VersionMajorMinor(VersionNo(4), VersionNo(2), false)
        )
      )
    }

    "Can download module" in new WithServer(app = GuiceApplicationBuilder().configure(Map("releaseDir" -> baseDir.toAbsolutePath.toString)).build(), port = testPort) {
      val dir = baseDir.resolve("mymodule")
      Files.createDirectories(dir)
      Files.write(dir.resolve("myfile"), Array[Byte](0, 1, 2, 3, 4))

      val ws = app.injector.instanceOf[WSClient]

      val response = Await.result(
        ws.url(
          s"http://localhost:$testPort" + controllers.routes.HomeController.getModule("mymodule", "myfile").url
        ).withHttpHeaders(
        ).get(),
        60.seconds
      )

      response.status === 200
      response.bodyAsBytes.toArray === Array[Byte](0, 1, 2, 3, 4)
    }
  }
}
