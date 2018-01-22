package com.akkademy

import akka.actor.{ActorSystem, Props, Status}
import akka.testkit.{TestActorRef, TestProbe}
import akka.util.Timeout
import com.akkademy.messages.SetRequest
import com.typesafe.config.ConfigFactory
import org.scalatest.{FunSpecLike, Matchers}

import scala.concurrent.duration._
import scala.language.postfixOps

class HotswapClientActorSpec extends FunSpecLike with Matchers {
  implicit val system = ActorSystem("test-system", ConfigFactory.defaultReference())
  implicit val timeout = Timeout(5 seconds)

  describe("HotswapClientActor") {
    it("should set a value"){
      val dbRef = TestActorRef[AkkademyDb]
      val db = dbRef.underlyingActor
      val probe = TestProbe()
      val clientRef = TestActorRef(Props.create(classOf[HotswapClientActor], dbRef.path.toString))

      clientRef ! new SetRequest("testkey", "testvalue", probe.ref)
      probe.expectMsg(Status.Success)
      db.map.get("testkey") should equal(Some("testvalue"))
    }
   /* it("should reverse a string"){
      val future = client.reverseStringService("incredible")
      val result = Await.result(future, 1 seconds)
      result should equal("elbidercni")
    }
    it("should received Connected after Ping"){
      val future = client.ping()
      val result = Await.result(future, 2 seconds)
      assert(result == Connected)
    }*/
  }

}
