package basetime.model

import java.util.UUID


trait Party


/*
 * Approver is a DTO for a Person with a link to a Consumer;
 */
final case class Approver(
  email: String,
  name: String,
  consumerId: UUID
)
