package com.akkademy.askdemo

import akka.actor.{Actor, ActorSystem, Props}
import akka.util.Timeout
import com.akkademy.{GetRequest, SetRequest}
import akka.pattern.ask
import com.akkademy.common.{ArticleBody, HttpResponse, ParseArticle, ParseHtmlArticle}

import scala.concurrent.Future

class AskDemoArticleParser (cacheActorPath: String, httpClientActorPath: String, articleParserActorPath: String)(implicit val timeout: Timeout) extends Actor
{
  val cacheActor = context.actorSelection(cacheActorPath)
  val httpClientActor = context.actorSelection(httpClientActorPath)
  val articleParserActor = context.actorSelection(articleParserActorPath)

  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {
    case ParseArticle(uri) => {
      val senderRef = sender()
      val cacheResult  = cacheActor ? GetRequest(uri)
      val result = cacheResult.recoverWith { // if request fails, then ask the articleParseActor
        case _: Exception => {
          val fRawResult = httpClientActor ? uri

          fRawResult flatMap {
            case HttpResponse(rawArticle) => articleParserActor ? ParseHtmlArticle(uri, rawArticle)
            case x => Future.failed(new Exception("unknown response"))
          }
        }
      }

      result onComplete { //could use Pipe?
        case scala.util.Success(x : String) =>
          println("cached result! hooray!!")
          senderRef ! x
        case scala.util.Success(x: ArticleBody) =>
          cacheActor ! SetRequest(uri, x.body)
          senderRef ! x
        case scala.util.Failure(t) =>
          senderRef ! akka.actor.Status.Failure(t)
        case x =>
          println("unknown message! " + x)
      }
    }
  }
}

object Main extends App {
  val system = ActorSystem("akkaArticleParser")
  system.actorOf(Props[AskDemoArticleParser] ,name = "akkaArticleParser")
}