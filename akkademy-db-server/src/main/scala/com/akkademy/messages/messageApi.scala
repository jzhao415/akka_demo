package com.akkademy

case class SetRequest (key: String, value: Object)
case class GetRequest (key: String)
case class ReverseString (key: String)
case class RemoveRequest (key: String)
case class KeyNotFoundException(key: String) extends Exception