package com.akkademy
import akka.actor.{Actor, ActorIdentity, ActorRef, ActorSystem, Identify, Terminated}
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.duration._
class SClient(remoteAddress: String) extends Actor {
  private implicit val timeout = Timeout(2 seconds)
  private implicit val system = ActorSystem("LocalSystem")

  val identifyId = 1
  context.actorSelection("/user/akkademy-db") ! Identify(identifyId)

  override def receive = {
    case ActorIdentity('identifyId', Some(ref)) =>
      context.watch(ref)
      context.become(active(ref))
    case ActorIdentity('identifyId, None) => context.stop(self)
  }

  def active(another: ActorRef): Actor.Receive = {
    case Terminated('another') => context.stop(self)
  }
  private val remoteDb = system.actorSelection(s"akka.tcp://akkademy@127.0.0.1:2552/user/akkademy-db");
  def set(key: String, value: Object) = {
    remoteDb ? SetRequest(key, value)
  }

  def get(key: String) = {
    remoteDb ? GetRequest(key)
  }

  def reverseStringService(key: String) = {
    remoteDb ? ReverseString(key)
  }
}
