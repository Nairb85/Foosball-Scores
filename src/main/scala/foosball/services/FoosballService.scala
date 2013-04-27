package foosball.services

import foosball.model._
import foosball.model.Player
import foosball.model.PostGameRequest
import foosball.model.Game
import foosball.model.PostPlayerRequest

/**
 * Created with IntelliJ IDEA.
 * User: aeryn
 * Date: 4/19/13
 * Time: 12:53 AM
 * To change this template use File | Settings | File Templates.
 */

trait FoosballService {
  def getPlayer(playerName: String): Player
  def getAllPlayers(sort: Option[String], order: Option[String]): List[Player]
  def postPlayer(postPlayerRequest: PostPlayerRequest): List[Player]
  def deletePlayer(playerName: String): List[Player]

  def getGame(_id: Int): Game
  def getAllGames(): List[Game]
  def postGame(postGameRequest: PostGameRequest): List[Game]
  def editGame(_id: Int, editGameRequest: EditGameRequest): Game
  def deleteGame(_id: Int): List[Game]
  def postRandomGame(postRandomGameRequest: PostRandomGameRequest): List[Game]
}
