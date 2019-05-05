package application.services

import akka.actor.ActorSystem
import javax.inject.Inject
import play.api.{ Configuration, Logger }
import play.api.libs.json._

class UICmdService @Inject() (
  conf: Configuration,
  actorSystem: ActorSystem) {

  def sendToClients(userid: Long, payload: JsValue) = {
    //Logger.info(s"Sent userid:${userid} ${payload.toString()}")
    actorSystem.actorSelection(s"user/wsactor-${userid}-*") ! payload
  }

  def sendToAll(payload: String) = {
    // Logger.debug(s"Sent to all: ${payload.toString()}")
    val actors = actorSystem.actorSelection(s"user/wsactor-*")
    List(actors).foreach({ _ ! payload })
  }
}