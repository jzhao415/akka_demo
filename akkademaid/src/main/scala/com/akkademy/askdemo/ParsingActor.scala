package com.akkademy.askdemo

import akka.actor.Actor
import com.akkademy.common.{ArticleBody, ParseHtmlArticle}

class ParsingActor extends Actor{
  override def receive: Receive = {
    case ParseHtmlArticle(key, html) =>
      sender() ! ArticleBody(key, de.l3s.boilerpipe.extractors.ArticleExtractor.INSTANCE.getText(html))
    case x =>
      println("unknown message " + x.getClass)
  }
}