package controllers

import java.util.UUID

import actors.UserAgentStateActor
import akka.stream.{Materializer, OverflowStrategy}
import javax.inject.Inject
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.{HandlerResult, LogoutEvent, Silhouette}
import org.webjars.play.WebJarsUtil
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, AnyContent, AnyContentAsEmpty, ControllerComponents}
import utils.auth.DefaultEnv
import play.api.mvc._
import utils.BetterActorFlow
import akka.actor.ActorSystem
import application.services.UICmdService

import scala.concurrent.{ExecutionContext, Future}

/**
 * The basic application controller.
 *
 * @param components  The Play controller components.
 * @param silhouette  The Silhouette stack.
 * @param webJarsUtil The webjar util.
 * @param assets      The Play assets finder.
 */
class ApplicationController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  uiCmdService: UICmdService,
)(
  implicit
  webJarsUtil: WebJarsUtil,
  assets: AssetsFinder,
  system: ActorSystem,
  mat: Materializer,
  ec: ExecutionContext
) extends AbstractController(components) with I18nSupport {

  /**
   * Handles the index action.
   *
   * @return The result to display.
   */
  def index = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.home(request.identity)))
  }

  /**
   * Handles the Sign Out action.
   *
   * @return The result to display.
   */
  def signOut = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    val result = Redirect(routes.ApplicationController.index())
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, result)
  }

  def socket: WebSocket = WebSocket.acceptOrResult[String, String] { request =>
    implicit val req = Request(request, AnyContentAsEmpty)
    silhouette.SecuredRequestHandler { securedRequest =>
      Future.successful(HandlerResult(Ok, Some(securedRequest.identity)))
    }.map {
      case HandlerResult(r, Some(user)) => {
        lazy val actorName = s"wsactor-${user.userID.toString}-${UUID.randomUUID().toString}"
        Right(BetterActorFlow.actorRef(UserAgentStateActor.props(user, uiCmdService), 16, OverflowStrategy.dropNew, Some(actorName)))
      }
      case HandlerResult(r, None) => Left(r)
    }
  }
}
