package clicker.tests

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import clicker.GameState
import clicker.database.DatabaseActor
import clicker.model.GameActor
import org.scalatest._

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
        expectNoMessage(50.millis)

        val actorSystem = ActorSystem()
        var gs: GameState = GameState("")

        val db = actorSystem.actorOf(Props(classOf[DatabaseActor], "test"))
        val player1 = actorSystem.actorOf(Props(classOf[GameActor], "user1", db))
        val player2 = actorSystem.actorOf(Props(classOf[GameActor], "user2", db))
        val player3 = actorSystem.actorOf(Props(classOf[GameActor], "user3", db))

        val player3_2 = actorSystem.actorOf(Props(classOf[GameActor], "user3", db))
      }
  }
}
