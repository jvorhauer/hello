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
import basetime.actors.ConsumerActor
import basetime.info.SysInfo
import basetime.model._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import gremlin.scala._
import io.circe.generic.auto._
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.io.StdIn


object Main {

  implicit val system      : ActorSystem       = ActorSystem("basetime")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout     : Timeout           = Timeout(5.seconds)

  implicit val graph       : ScalaGraph        = TinkerFactory.createModern().asScala

  val consumerActor    = system.actorOf(ConsumerActor.props)


  def main(args: Array[String]): Unit = {
    implicit val execCtx: ExecutionContextExecutor = system.dispatcher

    val route: Route =
      path("hi") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>BaseTime API</h1>"))
        }
      } ~
      path("command" / "consumer") {
        post {
          entity(as[Command]) { command =>
            system.log.info(s"/command/consumer: $command")
            val foc = (consumerActor ? command).mapTo[Option[Consumer]]
            complete(foc)
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
        path("list" / "consumers") {
          complete(Consumer.list)
        } ~
        pathPrefix("find" / "consumer" / Segment) { seg =>
          val uuid = UUID.fromString(seg)
          Consumer.find(uuid) match {
            case Some(c) => complete(c)
            case None    => complete(StatusCodes.NotFound)
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8888)

    println("Main online at http://localhost:8888/api")
    println("Press Enter to stop...")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }
}
