package clicker.tests

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.testkit.{ImplicitSender, TestKit}
import clicker.{ClickGold, GameState, Save}
import clicker.database.DatabaseActor
import clicker.model.GameActor
import org.scalatest._
import play.api.libs.json.Json

import scala.collection.script.Update
import scala.concurrent.duration._

class TestSave extends TestKit(ActorSystem("TestSave"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "A Clicker Game" must {
    "save and load properly" in {
//    expectNoMessage(50.millis)

      val database = system.actorOf(Props(classOf[DatabaseActor], "test"))
      val gameActor = system.actorOf(Props(classOf[GameActor], "username", database))
      var gs: GameState = GameState("")
      var gold: Double = 0

      // Add gold
      for (i <- 1 to 200) {
        gameActor ! ClickGold
      }

      expectNoMessage(50.millis)

      // Update
      gameActor ! Update
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]

      assert(gold == 200)
      }
  }
}
