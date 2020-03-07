package clicker.tests

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import clicker._
import clicker.database.DatabaseActor
import clicker.model.GameActor
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration._

class TestIdle extends TestKit(ActorSystem("TestIdle"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "A Clicker Game" must {
    "earn the correct idle income -- excavator" in {
      val database = system.actorOf(Props(classOf[DatabaseActor], "test"))
      val gameActor = system.actorOf(Props(classOf[GameActor], "username", database))
      var gs: GameState = GameState("")
      var gold: Double = 0
      var equipm: Int = 0

      // Add gold
      for (i <- 1 to 420) {
        gameActor ! ClickGold
      }

      expectNoMessage(50.millis)

      // Buy Excavator
      gameActor ! BuyEquipment("excavator")
      gameActor ! BuyEquipment("excavator")

      // Update
      expectNoMessage(1200.millis)
      gameActor ! Update
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]
      equipm = (Json.parse(gs.gameState) \ "equipment" \ "excavator" \ "numberOwned").as[Int]

      assert(gold == 20)
      assert(equipm == 2)

    }
    "earn the correct idle income -- mine" in {
      val database = system.actorOf(Props(classOf[DatabaseActor], "test"))
      val gameActor = system.actorOf(Props(classOf[GameActor], "username", database))
      var gs: GameState = GameState("")
      var gold: Double = 0
      var equipm: Int = 0

      // Add gold
      for (i <- 1 to 10) {
        gameActor ! ClickGold
      }
      gameActor ! BuyEquipment("shovel") // Left gold is 0 // GPC = 2 // GPS = 0
      for (i <- 1 to 6) {
        gameActor ! ClickGold
      }
      gameActor ! BuyEquipment("shovel") // Left gold is 0.5 // GPC = 3 // GPS = 0
      for (i <- 1 to 4) {
        gameActor ! ClickGold
      }
      gameActor ! BuyEquipment("shovel") // Left gold is ~1.5 // GPC = 4 // GPS = 0
      for (i <- 1 to 250) { // +250*4 = +1000
        gameActor ! ClickGold
      }

      // Buy Excavator
      gameActor ! BuyEquipment("mine")

      // Update
      expectNoMessage(1100.millis)
      gameActor ! Update
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]
      equipm = (Json.parse(gs.gameState) \ "equipment" \ "mine" \ "numberOwned").as[Int]

      assert(gold > 100 && gold < 103)
      assert(equipm == 1)

    }
  }


}
