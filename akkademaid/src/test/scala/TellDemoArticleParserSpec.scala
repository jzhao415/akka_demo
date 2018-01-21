import akka.actor.Status.Failure
import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestProbe}
import akka.util.Timeout
import akka.pattern.ask
import com.akkademy.{GetRequest, SetRequest}
import com.akkademy.askdemo.ParsingActor
import com.akkademy.common.{HttpResponse, ParseArticle}
import com.akkademy.telldemo.TellDemoArticleParser

import scala.concurrent.duration._
import org.scalatest.{BeforeAndAfterEach, FunSpecLike, Matchers}

import scala.concurrent.Await

class TellDemoArticleParserSpec  extends FunSpecLike with Matchers with BeforeAndAfterEach {
  implicit val system = ActorSystem()
  implicit val timeout = Timeout(5 seconds)

  describe("tell demo"){
    val cacheActorProbe = TestProbe()
    val httpClientActorProbe = TestProbe()
    val articleParserActor = system.actorOf(Props[ParsingActor])
    val tellDemoActor = TestActorRef(new TellDemoArticleParser(cacheActorProbe.ref.path.toString, httpClientActorProbe.ref.path.toString, articleParserActor.path.toString))

    it ("should provide parsed article"){
      val f = tellDemoActor ? ParseArticle("https://www.google.com")

      //cache gets the message first.
      cacheActorProbe.expectMsgType[GetRequest]
      cacheActorProbe.reply(Failure(new Exception("no cache")))

      //if cache fails, https client gets request
      httpClientActorProbe.expectMsgType[String]
      httpClientActorProbe.reply(HttpResponse(Articles.article1))

      cacheActorProbe.expectMsgType[SetRequest]

      val parsedArticle = Await.result(f, 10 seconds)

      parsedArticle.toString should include("I’ve been writing a lot in emacs lately")
      parsedArticle.toString should not include("<body>")
    }
    it("should provide cached article"){
      val f = tellDemoActor ? ParseArticle("https://www.google.com")

      // cache gets the message and return the content directly
      cacheActorProbe.expectMsgType[GetRequest]
      cacheActorProbe.reply(de.l3s.boilerpipe.extractors.ArticleExtractor.INSTANCE.getText(Articles.article1))

      val cachedArticle = Await.result(f, 10 seconds)
      cachedArticle.toString should include("I’ve been writing a lot in emacs lately")
      cachedArticle.toString should not include("<body>")
    }

    it("should timeout"){
      val f = tellDemoActor ? ParseArticle("https://www.google.com")

      // cache gets the message and return the content directly
      cacheActorProbe.expectMsgType[GetRequest]
      cacheActorProbe.reply("timeout")

      intercept[Exception]{ //intercept the timeout Exception
        Await.result(f, 10 seconds)
      }
    }
  }
}
