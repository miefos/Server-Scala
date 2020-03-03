package clicker.tests

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import clicker._
import clicker.database.DatabaseActor
import clicker.model.GameActor
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class TestClicks extends TestKit(ActorSystem("TestClicks"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "A Clicker Game" must {
    "react to user clicks with shovels appropriately" in {

      val database = system.actorOf(Props(classOf[DatabaseActor], "test"))
      val gameActor = system.actorOf(Props(classOf[GameActor], "username", database))

      gameActor ! ClickGold
      gameActor ! ClickGold

      // Wait for 50ms to ensure ClickGold messages resolve before moving on
      expectNoMessage(50.millis)

      // Send Update message and expect a GameState message in response
      // Wait up to 100ms for the response
      gameActor ! Update
      val gs: GameState = expectMsgType[GameState](100.millis)

      val gameStateJSON: String = gs.gameState


      // Parse gameState and use assert to test each value
      // TODO

    }
  }

}
