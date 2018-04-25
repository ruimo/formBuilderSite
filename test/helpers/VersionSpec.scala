package helpers

import org.specs2.mutable.Specification

class VersionSpec extends Specification {
  "Version" should {
    "Version no hold value." in {
      VersionNo(123).value === 123
    }

    "Version can detect error." in {
      VersionNo(VersionNo.MaxValue + 1) must throwA[IllegalArgumentException]
    }

    "Can parse major version" in {
      Version.parse("1") === VersionMajor(VersionNo(1), false)
      Version.parse("1-SNAPSHOT") === VersionMajor(VersionNo(1), true)
    }

    "Can parse major/minor version" in {
      Version.parse("1.2") === VersionMajorMinor(VersionNo(1), VersionNo(2), false)
      Version.parse("1.2-SNAPSHOT") === VersionMajorMinor(VersionNo(1), VersionNo(2), true)
    }

    "Can parse major/minor/patch version" in {
      Version.parse("1.2.3") === VersionMajorMinorPatch(VersionNo(1), VersionNo(2), VersionNo(3), false)
      Version.parse("1.2.3-SNAPSHOT") === VersionMajorMinorPatch(VersionNo(1), VersionNo(2), VersionNo(3), true)
    }
  }
}
