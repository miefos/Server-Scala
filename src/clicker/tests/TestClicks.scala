package clicker.tests

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import clicker._
import clicker.database.DatabaseActor
import clicker.model.GameActor
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import play.api.libs.json.{JsValue, Json}

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
      var gs: GameState = GameState("")
      var gold: Double = 0

      gameActor ! ClickGold
      gameActor ! ClickGold

      // Wait for 50ms to ensure ClickGold messages resolve before moving on
      expectNoMessage(50.millis)

      // Send Update message and expect a GameState message in response
      // Wait up to 100ms for the response
      gameActor ! Update
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]

      assert(gold == 2)

      // Try to buy shovel
      // 11 times clickGold
      for (i <- 1 to 11) {
        gameActor ! ClickGold
      }

      // Check if current gold is 13
      gameActor ! Update
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]

      assert(gold == 13)

      // Buy shovel and check if money was taken
      gameActor ! BuyEquipment("shovel")

      gameActor ! Update
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]

      assert(gold == 3)

      // Buy shovel and check if money was not taken (not enough gold coins)
      gameActor ! BuyEquipment("shovel")

      gameActor ! Update
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]

      assert(gold == 3)

      // Get gold 5(1 + 1*1) = 10
      for (i <- 1 to 5) {
        gameActor ! ClickGold
      }

      // Buy second (!) shovel with increased price and check if money was taken
      gameActor ! Update
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]

      assert(gold == 13)

      gameActor ! BuyEquipment("shovel")

      gameActor ! Update
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]

      assert(gold == 2.5)
    }
  }

}
