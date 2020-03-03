package clicker.model

import akka.actor.{Actor, ActorRef}

class GameActor(username: String, database: ActorRef) extends Actor {

  override def receive: Receive = ???

}
