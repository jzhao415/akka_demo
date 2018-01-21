import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import akka.pattern.ask
import com.Pingpong.ScalaPongActor
import org.scalatest.{FunSpecLike, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ScalaAskExamplesTest extends FunSpecLike with Matchers {
 val system = ActorSystem()
  implicit val timeout = Timeout(5 seconds)
  val pongActor = system.actorOf(Props(classOf[ScalaPongActor]))

  def askPong(message: String) : Future[String] = {
    (pongActor ? message).mapTo[String]
  }

  describe("Pong actor"){
    it("should respond with Pong") {
      val future = pongActor ? "Ping"
      val result = Await.result(future.mapTo[String], 1 second)
      assert(result == "Pong")
    }
    it("should fail on unknown message") {
      val future = pongActor ? "unknown"
      intercept[Exception]{
        Await.result(future.mapTo[String], 1 second)
      }
    }
  }

  describe("FutureExample"){
    import scala.concurrent.ExecutionContext.Implicits.global
    it("should print to console"){
      (pongActor ? "Ping").onSuccess({
        case x: String => println("replied with:" + x )
      })
      Thread.sleep(100)
    }
    it("should print on console too"){
      val f: Future[String] = askPong("Ping").flatMap(x=>{
        askPong(x)
      })

      f.recover{
        case t: Exception => println("default")
      }
      Thread.sleep(100)
    }
    it("recovering from failure"){
      val f = askPong("cause error").recoverWith({
        case t: Exception => askPong("Ping")
      })
      f.onComplete(x=> println(x))
    }
    it("chaining operation with recover in the end"){
      val f = askPong("Ping").
        flatMap(x=> askPong("Ping"+x)).
        recover({case e:Exception => "There was an error"})
    }
    it("combining multiple futures"){
      val f1 = askPong("Ping")
      val f2 = askPong("Pong")
      val futureAddition : Future[String] = {
        for{
          res1 <- f1
          res2 <- f2
        } yield res1 + res2
      }
      futureAddition.onSuccess({
        case x => println(x)
      })
    }
  }
}
