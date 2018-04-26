package basetime.actors

import akka.actor.{ ActorLogging, Props }
import akka.pattern.{ PipeToSupport, ask }
import akka.persistence.{ PersistentActor, RecoveryCompleted }
import akka.util.Timeout
import basetime.model.transfer.Worker
import basetime.model.{ Command, Consumer, Person, Producer }

import scala.concurrent.duration._


final class CommandProcessor extends PersistentActor with ActorLogging with PipeToSupport {

  implicit val timeout: Timeout = Timeout(5.seconds)
  import context.dispatcher


  private val consumerActor = context.actorOf(ConsumerActor.props, "consumer")
  private val producerActor = context.actorOf(ProducerActor.props, "producer")
  private val workerActor   = context.actorOf(WorkerActor.props, "worker")

  override def persistenceId = "command"

  override def receiveRecover = {
    case c: Command =>
      log.info(s"receiveRecover: $c")
      c.topic match {
        case "consumer" => Consumer.process(c)
        case "producer" => Producer.process(c)
        case "worker"   => Worker.process(c)
        case _          => log.error(s"receiveRecover: unknown topic in $c")
      }

    case RecoveryCompleted => log.info("recovery completed")
  }

  override def receiveCommand = {
    case c: Command => persist(c) { pc =>
      pc.topic match {
        case "consumer" => pipe((consumerActor ? pc).mapTo[Option[Consumer]]) to sender
        case "producer" => pipe((producerActor ? pc).mapTo[Option[Producer]]) to sender
        case "worker"   => pipe((workerActor ? pc).mapTo[Option[Person]]) to sender
        case _ => log.error(s"receiveCommand: unknown topic in $pc")
      }
    }

    case other => log.error(s"receiveCommand: $other")
  }
}


object CommandProcessor {
  val props = Props[CommandProcessor]
}
