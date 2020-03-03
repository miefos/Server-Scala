package clicker.server

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import clicker.database.DatabaseActor
import clicker.{SaveGames, UpdateGames}

/** *
  * @param database      Reference to the database actor
  * @param configuration Custom configuration of the game (Used in Bonus Objective. Pass empty string before bonus)
  */
class ClickerServer(val database: ActorRef, configuration: String) extends Actor {

  override def receive: Receive = ???



  // Comment in server.stop() to stop your web socket server when the actor system shuts down. This will free
  // the port and allow to to test again immediately. Note that this doesn't work if you stop your server through
  // IntelliJ. If you use IntelliJ's stop button you will have to wait for the port to be freed before restarting
  // your server. By using the TestServer test suite and this method to stop the server you can avoid having to
  // wait before restarting while testing
  override def postStop(): Unit = {
    println("stopping server")
//    server.stop()
  }

}


object ClickerServer {

  def main(args: Array[String]): Unit = {
    val actorSystem = ActorSystem()

    import actorSystem.dispatcher
    import scala.concurrent.duration._

    val db = actorSystem.actorOf(Props(classOf[DatabaseActor], "test"))
    val server = actorSystem.actorOf(Props(classOf[ClickerServer], db, ""))

    actorSystem.scheduler.schedule(0.milliseconds, 100.milliseconds, server, UpdateGames)
    actorSystem.scheduler.schedule(0.milliseconds, 1000.milliseconds, server, SaveGames)
  }

}
