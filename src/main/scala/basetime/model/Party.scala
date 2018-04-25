package basetime.model

import java.util.UUID

import gremlin.scala._


trait Party

@label("person")
final case class Person(
  email: String,
  name : String,
) extends Party

/*
 * Worker is a DTO for a Person, but with the link to a Producer;
 * the producerId will be used to look up the Producer and, if found,
 * to link the Person to said Producer
 * The Person creeated as a consequence of the Command containing this Worker
 * will have an Edge to the Producer and a Role of "worker".
 */
final case class Worker(
  email: String,
  name: String,
  producerId: UUID,
)

/*
 * Approver is a DTO for a Person with a link to a Consumer;
 */
final case class Approver(
  email: String,
  name: String,
  consumerId: UUID
)
