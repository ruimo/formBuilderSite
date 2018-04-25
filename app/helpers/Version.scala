package helpers

import scala.annotation.tailrec

class VersionNo private (
  val value: Int
) extends AnyVal

object VersionNo {
  val MaxValue = 1000

  def apply(value: Int): VersionNo =
    if (value >= MaxValue) throw new IllegalArgumentException("value should be less than " + MaxValue)
    else new VersionNo(value)
}

sealed trait Version {
  val isSnapshot: Boolean
  val majorVersion: VersionNo
  val minorVersion: Option[VersionNo]
  val patchVersion: Option[VersionNo]
  def asSnapshot: Version
}

case class VersionMajor(majorVersion: VersionNo, isSnapshot: Boolean) extends Version {
  val minorVersion = None
  val patchVersion = None
  def asSnapshot = copy(isSnapshot = true)
}

case class VersionMajorMinor(majorVersion: VersionNo, minor: VersionNo, isSnapshot: Boolean) extends Version {
  val minorVersion = Some(minor)
  val patchVersion = None
  def asSnapshot = copy(isSnapshot = true)
}

case class VersionMajorMinorPatch(majorVersion: VersionNo, minor: VersionNo, patch: VersionNo, isSnapshot: Boolean) extends Version {
  val minorVersion = Some(minor)
  val patchVersion = Some(patch)
  def asSnapshot = copy(isSnapshot = true)
}

object Version extends Ordering[Version] {
  import VersionNo.MaxValue

  implicit val ordering: Ordering[Version] = Ordering.by { v =>
    v.majorVersion.value * MaxValue * MaxValue +
    v.minorVersion.map(_.value).getOrElse(0) * MaxValue +
    v.patchVersion.map(_.value).getOrElse(0)
  }

  // Can perse the following form (n, m, p is digit).
  // n
  // n-SNAPSHOT
  // n.m
  // n.m-SNAPSHOT
  // n.m.p
  // n.m.p-SNAPSHOT
  def parse(s: String): Version = {
    def parseVersion(ver: String): Version = {
      val part = ver.split("\\.")
      part.length match {
        case 1 => VersionMajor(VersionNo(part(0).toInt), false)
        case 2 => VersionMajorMinor(VersionNo(part(0).toInt), VersionNo(part(1).toInt), false)
        case 3 => VersionMajorMinorPatch(VersionNo(part(0).toInt), VersionNo(part(1).toInt), VersionNo(part(2).toInt), false)
        case _ => throw new IllegalArgumentException("Cannot parse as verion '" + ver + "'")
      }
    }

    if (s.endsWith("-SNAPSHOT"))
      parseVersion(s.substring(0, s.length - 9)).asSnapshot
    else
      parseVersion(s)
  }

  def compare(x: Version, y: Version): Int = ordering.compare(x, y)
}
