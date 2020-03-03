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
    "earn the correct idle income" in {
      val database = system.actorOf(Props(classOf[DatabaseActor], "test"))
      val gameActor = system.actorOf(Props(classOf[GameActor], "username", database))
      var gs: GameState = GameState("")
      var gold: Double = 0
      var equipm: Int = 0

      // Add 50 golds
      for (i <- 1 to 200) {
        gameActor ! ClickGold
      }

      expectNoMessage(50.millis)

      // Update
      gameActor ! Update
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]

      assert(gold == 200)

      // Buy Excavator
      gameActor ! BuyEquipment("excavator")

      // Update
      gameActor ! Update
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]
      equipm = (Json.parse(gs.gameState) \ "equipment" \ "excavator" \ "numberOwned").as[Int]

      assert(gold == 10)
      assert(equipm == 1)

      // Add 1002 golds
      for (i <- 1 to 167) {
        gameActor ! ClickGold
      }

      // Update
      gameActor ! Update // + 10 from excavator
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]

      assert(gold == 1022)

      // Buy Excavator
      gameActor ! BuyEquipment("mine") // - 1000

      // Update // gold 22
      gameActor ! Update // +10 +100
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]

      assert(gold == 132)

      // Add 1002 golds
      for (i <- 1 to 167) {
        gameActor ! ClickGold
      }

      // Update // gold 1134
      gameActor ! Update // +10 +100
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]

      assert(gold == 1244)

      // Buy Excavator
      gameActor ! BuyEquipment("mine") // - 1100

      // Update // gold 144
      gameActor ! Update // +10 +200
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]
      equipm = (Json.parse(gs.gameState) \ "equipment" \ "mine" \ "numberOwned").as[Int]

      assert(gold == 354)
      assert(equipm == 2)


    }
  }


}
