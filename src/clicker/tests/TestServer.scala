package clicker.tests

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import clicker._
import clicker.database.DatabaseActor
import clicker.server.ClickerServer
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._


class TestServer extends TestKit(ActorSystem("TestServer"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "A Clicker Server" must {
    "accept players via web sockets" in {

      // This file is the start of a test suite for the primary objective and can be used to help you write your
      // server. This test suite is not part of your submission and will not be graded, though you should use it
      // for your own testing if you are attempting the primary objective.

      val database = system.actorOf(Props(classOf[DatabaseActor], "test"))
      val server = system.actorOf(Props(classOf[ClickerServer], database, ""))

      // Wait for the server to start
      expectNoMessage(10000.millis)


      val client = system.actorOf(Props(classOf[SocketClient], this.testActor))
      expectNoMessage(2500.millis)

      client ! ClickGold
      client ! ClickGold
      client ! ClickGold
      expectNoMessage(100.millis)

      server ! SaveGames
      expectNoMessage(100.millis)

      server ! UpdateGames
      assert(expectMsgType[Result](4000.millis).passed)

//      client ! ClickGold
//      client ! ClickGold
//      client ! ClickGold
//      expectNoMessage(100.millis)
//
//      server ! SaveGames
//      expectNoMessage(100.millis)

//      server ! UpdateGames
//      assert(expectMsgType[Result](4000.millis).passed)
    }
  }
}
