package basetime

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import basetime.actors.{ ConsumerActor, ProducerActor, WorkerActor }
import basetime.info.SysInfo
import basetime.model._
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import gremlin.scala._
import io.circe.generic.auto._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.io.StdIn
import scala.util.Try


object Main {

  implicit val system      : ActorSystem       = ActorSystem("basetime")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout     : Timeout           = Timeout(5.seconds)
  private  val config      : Config            = system.settings.config

  private val consumerActor = system.actorOf(ConsumerActor.props, "consumer")
  private val producerActor = system.actorOf(ProducerActor.props, "producer")
  private val workerActor   = system.actorOf(WorkerActor.props,   "worker")


  def main(args: Array[String]): Unit = {
    implicit val execCtx: ExecutionContextExecutor = system.dispatcher

    val route: Route =
      pathSingleSlash {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>BaseTime API</h1>"))
        }
      } ~
      path("command") {
        post {
          entity(as[Command]) { command =>
            system.log.info(s"/command: $command")
            command.topic match {
              case "consumer" => complete((consumerActor ? command).mapTo[Option[Consumer]])
              case "producer" => complete((producerActor ? command).mapTo[Option[Producer]])
              case "worker"   => complete((workerActor   ? command).mapTo[Option[Person]])
              case _          => complete(StatusCodes.NotAcceptable)
            }
          }
        }
      } ~
      get {
        path ("info") {
          complete(SysInfo.si)
        } ~
        path ("id") {
          complete(Id(UUID.randomUUID().toString))
        } ~
        path("dump") {
          complete({
            Repository.dump()
            StatusCodes.OK
          })
        } ~
        pathPrefix("list") {
          path("consumers") {
            complete(Consumer.list)
          } ~
          path("producers") {
            complete(Producer.list)
          } ~
          path("persons") {
            complete(Person.list)
          }
        } ~
        pathPrefix("find" / "consumer" / Segment) { seg =>
          val uuid = UUID.fromString(seg)
          Consumer.find(uuid) match {
            case Some(c) => complete(c.toCC[Consumer])
            case None    => complete(StatusCodes.NotFound)
          }
        } ~
        pathPrefix("find" / "producer" / Segment) { seg =>
          val uuid = UUID.fromString(seg)
          Producer.find(uuid) match {
            case Some(p) => complete(p.toCC[Producer])
            case None    => complete(StatusCodes.NotFound)
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", getServerPort)

    println(s"Main online at http://localhost:$getServerPort")
    println("Press Enter to stop...")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }

  def getServerPort: Int = {
    Try(config.getInt("basetime.server.port")).getOrElse(9999)
  }
}
