import akka.actor.Status.Failure
import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import akka.testkit.{TestActorRef, TestProbe}

import scala.concurrent.duration._
import akka.pattern.ask
import com.akkademy.{GetRequest, SetRequest}
import com.akkademy.askdemo.{AskDemoArticleParser, ParsingActor}
import com.akkademy.common.{HttpResponse, ParseArticle}
import org.scalatest.{BeforeAndAfterEach, FunSpecLike, Matchers}

import scala.concurrent.Await

class AskDemoArticleParserSpec extends FunSpecLike with Matchers with BeforeAndAfterEach{

  implicit val system = ActorSystem()
  implicit val timeout = Timeout(5 seconds)

  describe("ask demo"){
    val cacheActorProbe = TestProbe()
    val httpClientActorProbe = TestProbe()
    val articleParserActor = system.actorOf(Props[ParsingActor])
    val askDemoActor = TestActorRef(new AskDemoArticleParser(cacheActorProbe.ref.path.toString, httpClientActorProbe.ref.path.toString, articleParserActor.path.toString))

    it("should provide parsed article") {
      val f = askDemoActor ? ParseArticle("https://news.ycombinator.com/")

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

    it("should provide article from cache") {
      val f = askDemoActor ? ParseArticle("https://www.google.com")

      // cache gets the message and return the content directly
      cacheActorProbe.expectMsgType[GetRequest]
      cacheActorProbe.reply(de.l3s.boilerpipe.extractors.ArticleExtractor.INSTANCE.getText(Articles.article1))

      val cachedArticle = Await.result(f, 10 seconds)
      cachedArticle.toString should include("I’ve been writing a lot in emacs lately")
      cachedArticle.toString should not include("<body>")
    }
  }

}
