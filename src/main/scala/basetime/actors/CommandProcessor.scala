package basetime.actors

import akka.actor.{ ActorLogging, ActorRef, Props }
import akka.pattern.{ PipeToSupport, ask }
import akka.persistence.{ PersistentActor, RecoveryCompleted }
import akka.util.Timeout
import basetime.model.Command

import scala.concurrent.duration._

final class CommandProcessor extends PersistentActor with ActorLogging with PipeToSupport {

  implicit val timeout     : Timeout           = Timeout(5.seconds)

  private val consumerActor = context.actorOf(ConsumerActor.props, "consumer")
  private val producerActor = context.actorOf(ProducerActor.props, "producer")
  private val workerActor   = context.actorOf(WorkerActor.props,   "worker")

  override def persistenceId = "command"

  override def receiveRecover = {
    case c: Command => log.info(s"command: $c")
    case RecoveryCompleted => log.info("recovery completed")
  }

  override def receiveCommand = {
    case c: Command => persist(c) { pc => process(pc, sender) }
  }

  def process(c: Command, sender: ActorRef) = {
    c.topic match {
      case "consumer" => (consumerActor ? c) pipeTo sender
    }
  }
}

object CommandProcessor {
  val props = Props[CommandProcessor]
}
