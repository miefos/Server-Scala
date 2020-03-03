package clicker.tests

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
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
    }
  }


}
