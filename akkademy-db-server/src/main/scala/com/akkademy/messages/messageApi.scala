package com.akkademy.messages

import akka.actor.ActorRef

trait Request
case class SetRequest (key: String, value: Object, sender: ActorRef = ActorRef.noSender) extends Request
case class GetRequest (key: String, sender: ActorRef = ActorRef.noSender) extends Request
case class ReverseString (key: String)
case class RemoveRequest (key: String)
case class Ping()
case class Connected()
case class Disconnected()
case class KeyNotFoundException(key: String) extends Exception
