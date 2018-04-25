package basetime.actors

import akka.actor.{ ActorLogging, Props }
import akka.persistence.{ PersistentActor, RecoveryCompleted }
import basetime.model.transfer.Worker
import basetime.model.{ Command, Person }
import io.circe.generic.auto._
import io.circe.parser.decode


final class WorkerActor extends PersistentActor with ActorLogging {

  override def persistenceId = "worker"

  override def receiveRecover = {
    case Command("worker", "create", data) => upsert(data)
    case RecoveryCompleted                 => log.info("receiveRecover: recovery completed")
    case _ => log.info("receiveRecover: ToDo")
  }

  override def receiveCommand = {
    case c: Command if c.topic == "worker" => persist(c) {
      case Command("worker", "create", data) => sender ! upsert(data)
      case _ => log.info("receiveCommand: ToDo")
    }
  }

  def handle(data: String, f: Worker => Option[Person]): Option[Person] = {
    log.info(s"worker handle: $data")
    decode[Worker](data) match {
      case Left(e) =>
        log.error(s"$data is not a Worker: $e")
        None
      case Right(w) => f(w)
    }
  }

  def upsert(data: String): Option[Person] = handle(data, Worker.save)
}

object WorkerActor {
  val props = Props[WorkerActor]
}
