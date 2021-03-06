package com.example

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import spray.routing.SimpleRoutingApp
import scala.concurrent.duration._

object Boot extends App with SimpleRoutingApp {



  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")

  // create and start our service actor
  val service = system.actorOf(Props[MyServiceActor], "demo-service")

  implicit val timeout = Timeout(5.seconds)
  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)

  startServer(interface = "127.0.0.1", port = 8090) {
    get {
      path("users") {
        traceName("GetAllUsers") {
          complete {
            Tracer.setCurrentContext(Kamon.tracer.newContext("other-context"))
            "All Users"
          }
        }
      } ~
        path("users" / IntNumber) { userID =>
          traceName("GetUserDetails") {
            complete {
              "Data about a specific user"
            }
          }
        }
    }
  }
}
