package com.akkademy
import akka.actor.{Actor, ActorLogging, Stash}
import com.akkademy.messages.{Connected, Disconnected, Ping, Request}


class HotswapClientActor(address: String) extends Actor with ActorLogging with Stash{
  private val remoteDb = context.system.actorSelection(address)

  override def receive  = {
    case x: Request =>
      log.info("receive Request")
      remoteDb ! new Connected
      stash()
    case _: Connected =>
      log.info("receive Connected")
      unstashAll()
      context.become(active)
  }

  def active : Receive = {
    case x: Disconnected =>
      context.unbecome()
    case x: Request =>
      remoteDb forward x
  }

}
