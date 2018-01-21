package com.akkademy.telldemo

import java.util.concurrent.TimeoutException

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import com.akkademy.{GetRequest, SetRequest}
import com.akkademy.common.{ArticleBody, HttpResponse, ParseArticle, ParseHtmlArticle}

class TellDemoArticleParser (cacheActorPath: String, httpClientActorPath: String, articleParserActorPath: String)(implicit val timeout: Timeout) extends Actor
{
  val cacheActor = context.actorSelection(cacheActorPath)
  val httpClientActor = context.actorSelection(httpClientActorPath)
  val articleParserActor = context.actorSelection(articleParserActorPath)

  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {
    case ParseArticle(uri) =>
      val extraActor = buildExtraActor(sender(), uri)
      cacheActor tell (GetRequest(uri), extraActor)
      httpClientActor tell ("test", extraActor)

      context.system.scheduler.scheduleOnce(timeout.duration, extraActor, "timeout")
  }

  private def buildExtraActor(senderRef: ActorRef, uri : String): ActorRef = {
    return context.actorOf(Props(new Actor{
      override def receive = {
        case "timeout" =>
          senderRef ! Failure(new TimeoutException("timeout"))
          context.stop(self)
        case HttpResponse(body) =>
          articleParserActor ! ParseHtmlArticle(uri, body)
        case body: String =>
          senderRef ! body
          context.stop(self)
        case ArticleBody(uri, body) =>
          cacheActor ! SetRequest(uri, body)
          senderRef ! body
          context.stop(self)
        case t =>
          println("ignoring msg: " + t.getClass)
      }
    }))
  }
}
