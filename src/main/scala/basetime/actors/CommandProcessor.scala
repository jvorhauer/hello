package basetime.actors

import akka.actor.{ ActorLogging, ActorRef, Props }
import akka.persistence.PersistentActor
import basetime.Main
import basetime.model.Command


class CommandProcessor extends PersistentActor with ActorLogging {

  override def persistenceId =  "command"

  val consumerActor: ActorRef = Main.consumerActor


  override def receiveRecover = {
    case c: Command   => process(c, recovering = true)
  }

  override def receiveCommand = {
    case c: Command => persist(c) { pc => process(pc) }
  }


  private def process(command: Command, recovering: Boolean = false): Unit = {
    log.debug(s"process: $command ${if (recovering) " (recovering)"}")
    command.topic match {
      case "consumer" => consumerActor ! command

      case _ => log.info(s"process: unhandled topic of $command")
    }
  }
}


object CommandProcessor {
  val props = Props[CommandProcessor]
}