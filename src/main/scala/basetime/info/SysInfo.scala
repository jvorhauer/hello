package basetime.info


final case class SysInfo(
  version: String,
  built  : String,
  env    : String,
)

object SysInfo {
  val si: SysInfo = SysInfo("1.0", "now", "dev")
}
