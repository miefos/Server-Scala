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

      // Buy Excavator
      gameActor ! BuyEquipment("excavator")

      // Update
      expectNoMessage(1000.millis)
      gameActor ! Update
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]
      equipm = (Json.parse(gs.gameState) \ "equipment" \ "excavator" \ "numberOwned").as[Int]

      assert((gold >= 0) && (gold <= 20))
      assert(equipm == 1)

      // Add 1002 golds
      for (i <- 1 to 167) {
        gameActor ! ClickGold
      }

      // Update ~1002+10~1012
      expectNoMessage(1000.millis)
      gameActor ! Update // + 10 from excavator // Gold ~1022
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]

      assert(gold >= 1000 && gold <= 1050) // Gold ~1022

      // Buy Excavator
      gameActor ! BuyEquipment("mine") // - 1000 // Gold ~22

      // Update // gold ~22
      expectNoMessage(1000.millis) // +10 +100
      gameActor ! Update  // ~22 + 110 ~132
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]

      assert(gold >= 22 && gold <= 250)
    }
  }


}
