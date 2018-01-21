package com.Pingpong
import akka.actor.{Actor, Props, Status}


class ScalaPongActor extends Actor{
  override def receive: Receive = {
    case "Ping" => sender() ! "Pong"
    case "Pong" => sender() ! "Ping"
    case _ => sender() ! Status.Failure(new Exception("Unknown message"))
  }
}

object ScalaPongActor {
  def props(response: String) : Props = {
    Props(classOf[ScalaPongActor], response)
  }
}

