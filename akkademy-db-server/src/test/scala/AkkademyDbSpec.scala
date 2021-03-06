import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestProbe}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import com.akkademy._
import com.akkademy.messages._
import org.scalatest.{BeforeAndAfterEach, FunSpecLike, Matchers}

import scala.concurrent.Await

class AkkademyDbSpec extends FunSpecLike with Matchers with BeforeAndAfterEach{

  implicit val system = ActorSystem()
  implicit val timeout = Timeout(5 seconds)
  val actorRef = TestActorRef(new AkkademyDb)
  val testProbe = TestProbe()

  describe("akkademyDb") {
    describe ("given SetRequest"){
      it("should place key/value into map"){
        actorRef ! SetRequest("he","is learning akka, and will succeed", testProbe.ref)
        actorRef ! SetRequest("she","is working hard as well", testProbe.ref)
        val akkademyDb = actorRef.underlyingActor
        akkademyDb.map.get("he") should equal(Some("is learning akka, and will succeed"))
        akkademyDb.map.get("she") should equal(Some("is working hard as well"))
      }
      it("test getRequest class"){
        actorRef ! SetRequest("he","is learning akka, and will succeed",  testProbe.ref)
        actorRef ! SetRequest("she","is working hard as well", testProbe.ref)
        actorRef ! GetRequest("he")
      }
      it("should reverse string"){
        val future = actorRef ? ReverseString("incredible")
        val result = Await.result(future.mapTo[String], 1 second)
        assert(result == "elbidercni")
      }
      it("should fail when send unknown object"){
        val future = actorRef ? "failure case"
        intercept[Exception]{
          Await.result(future.mapTo[String], 2 second)
        }
      }
      it("should remove message"){
        actorRef ! SetRequest("he","is learning akka, and will succeed")
        actorRef ! RemoveRequest("he")
        val future = actorRef ? GetRequest("he")
        intercept[Exception]{
          Await.result(future.mapTo[String], 2 second)
        }
      }
      it("should return Connected msg when ping") {
        val f = actorRef ? new Connected
        val result = Await.result(f, 2 second)
        assert(result == Connected())
      }
    }
  }
}
