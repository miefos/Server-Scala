package clicker.model

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import clicker.{ClickGold, Update}
import clicker.database.DatabaseActor
import play.api.libs.json.{JsNumber, JsObject, JsString, JsValue, Json}

object TestGame {
  def main(args: Array[String]): Unit = {
    val system: ActorSystem = ActorSystem("ValueActorSystem")
    val database: ActorRef = system.actorOf(Props(classOf[DatabaseActor], "test")) // Sets initial value
    val actor: ActorRef = system.actorOf(Props(classOf[GameActor], "testUser", database)) // Sets initial value

    actor ! ClickGold
    actor ! Update

    val returnObj: JsValue = JsObject(
      Seq(
        "username" -> JsString("username"),
        "gold" -> JsNumber(55),
        "lastUpdateTime" -> JsNumber(23),
        "equipment" -> JsNumber(231)
      ))
    println("ending...")

    val returnString: String = Json.stringify(returnObj)
    println(returnString)


  }
}
