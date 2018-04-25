package basetime.actors

import akka.actor.{ ActorLogging, Props }
import akka.persistence.PersistentActor
import basetime.Main
import basetime.model.{ Command, Consumer }
import gremlin.scala._
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser.decode


final class ConsumerActor extends PersistentActor with ActorLogging {

  type Handler = String => Option[Consumer]

  override val persistenceId = "consumer"

  private val mappings: Map[String, Handler] = Map(
    "create" -> create,
    "update" -> update,
    "delete" -> delete
  )

  override def receiveRecover = {
    case Command("consumer", method, data) => mappings.find(p => p._1 == method).map(e => e._2(data))
  }

  override def receiveCommand = {
    case c: Command if c.topic == "consumer" => persist(c) {
      case Command("consumer", method, data) =>
        log.info(s"receiveCommand: consumer $method $data")
        sender ! mappings.find(_._1 == method).flatMap(_._2(data))

      case _ => log.error("receiveCommand: some fuckup...")
    }

    case x          => log.error(s"received: x: $x ???")
  }

  def create(data: String): Option[Consumer] = {
    decode[Consumer](data) match {
      case Left(e)   =>
        log.error(s"$data is not a Consumer: $e")
        None
      case Right(c) =>
        log.info(s"create: $c")
        if (Consumer.findAsV(c.id).isDefined) {
          log.error(s"create: ${c.id} already exists")
          None
        } else {
          val v = Main.graph + c
          Some(v.toCC[Consumer])
        }
    }
  }

  def update(data: String): Option[Consumer] = {
    decode[Consumer](data) match {
      case Left(e)  =>
        log.error(s"$data is not a Consumer: $e")
        None
      case Right(c) =>
        log.info(s"update: $c")
        val found = Consumer.findAsV(c.id)
        if (found.isEmpty) {
          log.error(s"update: ${c.id} not found")
          None
        } else {
          val updated = found.map(v => v.updateAs[Consumer](_.copy(name = c.name)))
          updated.map(v => v.toCC[Consumer])
        }
    }
  }

  def delete(data: String): Option[Consumer] = {
    decode[Consumer](data) match {
      case Left(e) =>
        log.error(s"$data is not a Consumer: $e")
        None
      case Right(c) =>
        log.info(s"delete: $c")
        Consumer.delete(c.id)
        Some(c)
    }
  }

  def update2(data: String): Option[Consumer] = {
    def bbb(c: Consumer): Consumer = {
      log.info(s"update2: $c")

      // TODO: fill in the actual logic

      c
    }
    handle(data, bbb)
  }

  private def handle[T](data: String, f: T => Consumer)(implicit decoder: Decoder[T]): Option[Consumer] = {
    decode[T](data) match {
      case Left(e) =>
        log.error(s"$data is not a Consumer: $e")
        None
      case Right(c) =>
        Some(f(c))
    }
  }
}


object ConsumerActor {
  val props = Props[ConsumerActor]
}
