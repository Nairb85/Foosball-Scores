package foosball

import cc.spray.{RequestContext, Directives}
import akka.dispatch.Future
import cc.spray.http._
import cc.spray.http.StatusCodes._
import cc.spray.typeconversion.LiftJsonSupport
import net.liftweb.json.JsonParser._
import foosball.services.FoosballService
import cc.spray.directives.{IntNumber, PathElement}
import net.liftweb.json.DefaultFormats
import cc.spray.http.MediaTypes._
import foosball.model._
import java.util.Date
import foosball.model.Player
import foosball.model.PostGameRequest
import foosball.model.PostPlayerRequest
import foosball.model.Game

/**
 * Created with IntelliJ IDEA.
 * User: aeryn
 * Date: 4/19/13
 * Time: 12:06 AM
 * To change this template use File | Settings | File Templates.
 */

case class PlayerResponse(players: List[Player])
case class GameResponse(games: List[Game])

trait FoosballEndpoint extends Directives with LiftJsonSupport {
  implicit val formats = DefaultFormats

  def JsonContent(content: String) = HttpContent(ContentType(`application/json`), content)

  val foosballService: FoosballService

  val getAllPlayersRoute = path("") & get & parameters('sort.as[String] ?, 'order.as[String] ?)
  val postPlayerRoute = path("") & post & content(as[PostPlayerRequest])
  val getPlayerRoute = path(PathElement) & get
  val deletePlayerRoute = path(PathElement) & delete

  val getAllGamesRoute = get & path("")
  val getGameRoute = get & path(IntNumber)
  val postGameRoute = post & path("") & content(as[PostGameRequest])
  val editGameRoute = put & path(IntNumber) & content(as[EditGameRequest])
  val deleteGameRoute = path(IntNumber) & delete
  val postRandomGame = post & path("random") & content(as[PostRandomGameRequest])

  val facebookCallbackRoute = path("campaigns/fbcallback") & parameters('campaign.as[Long], 'app.as[String] ? )
  val getByFacebookPageId = path("campaigns") & parameters('facebookPageId.as[Long], 'app.as[String] ?)

  val restService = {
    pathPrefix("players") {
      getPlayerRoute {
        playerName =>
          ctx =>
            ctx.complete(StatusCodes.OK, PlayerResponse(List(foosballService.getPlayer(playerName))))
      } ~
      postPlayerRoute {
        postPlayerRequest =>
          ctx =>
            ctx.complete(StatusCodes.Created, PlayerResponse(foosballService.postPlayer(postPlayerRequest)))
      } ~
      getAllPlayersRoute {
        (sort, order) =>
          ctx =>
            val response = foosballService.getAllPlayers(sort, order)
            ctx.complete(StatusCodes.OK, PlayerResponse(response))
      } ~
      deletePlayerRoute {
        playerName =>
          ctx =>
            ctx.complete(StatusCodes.OK, PlayerResponse(foosballService.deletePlayer(playerName)))
      }
    } ~
    pathPrefix("games") {
      postGameRoute {
        postGameRequest =>
          ctx =>
            ctx.complete(StatusCodes.Created, GameResponse(foosballService.postGame(postGameRequest)))
      } ~
      getAllGamesRoute {
        ctx =>
          ctx.complete(StatusCodes.OK, GameResponse(foosballService.getAllGames()))
      } ~
      getGameRoute {
        gameId =>
          ctx =>
            ctx.complete(StatusCodes.OK, GameResponse(List(foosballService.getGame(gameId))))
      } ~
      editGameRoute {
        (gameId, editGameRequest) =>
          ctx =>
            ctx.complete(StatusCodes.OK, GameResponse(List(foosballService.editGame(gameId, editGameRequest))))
      } ~
      deleteGameRoute {
        gameId =>
          ctx =>
            ctx.complete(StatusCodes.OK, GameResponse(foosballService.deleteGame(gameId)))
      } ~
      postRandomGame {
        postRandomGameRequest =>
          ctx =>
            ctx.complete(StatusCodes.Created, GameResponse(foosballService.postRandomGame(postRandomGameRequest)))
      }
    }
  }
}
