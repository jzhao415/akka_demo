package com.akkademy

import akka.actor.{Actor, ActorSystem, Props, Status}
import akka.event.Logging

import scala.collection.mutable.HashMap

class AkkademyDb extends Actor {
  val map = new HashMap[String, Object]
  val log = Logging(context.system, this)

  override def receive = {
    case SetRequest(key, value) => {
      log.info("received SetRequest - key : {} value: {}", key, value)
      map.put(key, value)
    }
    case GetRequest(key)=>{
      log.info("received GetRequest - key : {} ", key)
      val response: Option[Object] = map.get(key)//.orElse(setIfNotExist(key))
      response match{
        case Some(x) => sender() ! x
        case None => sender() ! Status.Failure(new KeyNotFoundException(key))
      }
    }
    case RemoveRequest(key) => {
      log.info("received RemoveReqeust - key:{}", key)
      if(map.contains(key)){
        map.remove(key)
      }
    }
    case ReverseString(key)=>{
      log.info("received ReverseString - key :{}", key)
      val response: String = key.reverse.toString
      sender()! response
    }
    case _ => Status.Failure(new ClassNotFoundException)
  }

  def setIfNotExist(key: String): Option[Object] ={
    if(!map.contains(key)){
      map.put(key, new Object())
    }
    return Some(new Object())
  }

}

object Main extends App {
  val system = ActorSystem("akkademy")
  system.actorOf(Props[AkkademyDb], name = "akkademy-db")
}
