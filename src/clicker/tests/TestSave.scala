package clicker.tests

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.testkit.{ImplicitSender, TestKit}
import clicker.{ClickGold, GameState, Save, Update}
import clicker.database.DatabaseActor
import clicker.model.GameActor
import org.scalatest._
import play.api.libs.json.Json

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
      val player1 = system.actorOf(Props(classOf[GameActor], "player1", database))
      val player2 = system.actorOf(Props(classOf[GameActor], "player2", database))
      var gs: GameState = GameState("")
      var gold: Double = 0

      // Update
      player1 ! Update
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]

      assert(gold == 0)

      // Update
      player2 ! Update
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]

      assert(gold == 0)

      // Add gold
      for (i <- 1 to 10) {
        player1 ! ClickGold
      }

      expectNoMessage(50.millis)

      // Update
      player1 ! Update
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]

      assert(gold == 10)

      // Update
      player2 ! Update
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]

      assert(gold == 0)

      expectNoMessage(100.millis)

      player1 ! Save
      player2 ! Save

      expectNoMessage(50.millis)

      val player1_2 = system.actorOf(Props(classOf[GameActor], "player1", database))

      // Update
      player1_2 ! Update
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]

      assert(gold == 10)


      val player3 = system.actorOf(Props(classOf[GameActor], "player3", database))
      // Update
      player3 ! Update
      gs = expectMsgType[GameState](1000.millis)
      gold = (Json.parse(gs.gameState) \ "gold").as[Double]

      assert(gold == 0)

    }
  }
}
