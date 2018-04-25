package basetime.model

import java.util.UUID

import gremlin.scala._

@label("role")
final case class Role(
  id   : UUID,
  label: String     // admin, approver, default
)

object Role {
  val label = "role"
  val roles = List("admin", "worker", "approver")
}