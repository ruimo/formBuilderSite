package helpers

import com.ruimo.scoins.Version
import play.api.libs.json._

object VersionJson {
  implicit object versionFormat extends Format[Version] {
    override def reads(jv: JsValue): JsResult[Version] = JsSuccess(Version.parse(jv.as[String]))
    override def writes(v: Version): JsValue = JsString(v.toString)
  }
}
