package actors

import akka.actor.Status.Status
import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.routing.Broadcast
import application.services.UICmdService
import com.google.inject.Inject
import models.User
import play.api.Logger
import play.api.libs.json.{ JsValue, Json }

import scala.concurrent.ExecutionContext

object UserAgentStateActor {
  def props(user: User, uiss: UICmdService)(out: ActorRef) = Props(new UserAgentStateActor(user, uiss, out))
  var listActors = scala.collection.mutable.ArrayBuffer.empty[ActorRef]
}

class UserAgentStateActor @Inject() (user: User, uiss: UICmdService, out: ActorRef) extends Actor with ActorLogging {

  override def preStart(): Unit = {
    UserAgentStateActor.listActors += out
    log.debug(s"Actor starting: ${self.path.name} ${user.userID}")
  }
  override def postStop() = {
    UserAgentStateActor.listActors -= out
    log.debug(s"Actor disconnected")
  }
  def receive = {
    // Strings are from the UI, and should be well-formed json
    case msg: String => {
      UserAgentStateActor.listActors.map(actor =>
        actor ! msg)
      log.debug(s"UserAgentStateActor: ${msg}")
    }
    // JsValues are messages from the server that should be .toStringed() to the UI
    case json: JsValue => {
      uiss.sendToAll(json.toString())
    }
    case status: Status => {
      Console.print("Hello")
      out ! "Hello"
    }
  }
}