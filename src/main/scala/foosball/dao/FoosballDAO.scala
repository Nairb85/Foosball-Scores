package foosball.dao

import java.util.Date
import net.liftweb.json.JsonParser._
import java.lang.Exception
import net.liftweb.json.DefaultFormats

import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._

import foosball.services.FoosballService
import foosball.model._
import foosball.model.PostGameRequest
import foosball.model.PostPlayerRequest
import foosball.model.Player
import foosball.model.Game
import scala.util.Random

/**
 * Created with IntelliJ IDEA.
 * User: aeryn
 * Date: 4/19/13
 * Time: 12:17 AM
 * To change this template use File | Settings | File Templates.
 */
abstract class FoosballDAO(db: MongoDB) extends FoosballService {
  implicit val formats = DefaultFormats

  val playersCollection = db("players")
  val gamesCollection = db("games")
  val countersCollection = db("idCounter")

  val DEFAULT_OFFSET: Int = 0
  val DEFAULT_MAX_SIZE: Int = 100

  val WINS = "wins"
  val LOSSES = "losses"

  val ASCENDING = "asc"
  val DESCENDING = "desc"

  val randomGenerator = new Random()

  //service functions
  def getPlayer(playerName: String): Player = {
    val dbo = playersCollection.findOne(MongoDBObject("name" -> playerName))
    dbo.map(f => {
      try {
        grater[Player].asObject(f)
      } catch {
        case e => println("Error grating player object %s" format(f))
        throw e
      }
    }).get
  }

  def getAllPlayers(sort: Option[String] = None, order: Option[String] = None): List[Player] = {
    val sortValue = sort.getOrElse(WINS)
    val sortOrder = if (order.getOrElse(DESCENDING) == "asc") 1 else -1
    val dbo = playersCollection.find().sort(MongoDBObject(sortValue -> sortOrder)).limit(DEFAULT_MAX_SIZE)
    dbo.toList.map(f => {
      try {
        grater[Player].asObject(f)
      } catch {
        case e => println("Error grating player object %s" format(f))
                  throw e
      }
    })
  }

  def postPlayer(postPlayerRequest: PostPlayerRequest): List[Player] = {
    playersCollection.insert(MongoDBObject("name" -> postPlayerRequest.name, "wins" -> 0, "losses" -> 0))

    getAllPlayers()
  }

  def deletePlayer(playerName: String): List[Player] = {
    playersCollection.remove(MongoDBObject("name" -> playerName))

    getAllPlayers()
  }

  def getGame(_id: Int): Game = {
    val dbo = gamesCollection.findOne(MongoDBObject("_id" -> _id))

    dbo.map(f => {
      try {
        grater[Game].asObject(f)
      } catch {
        case e => println("Error grating game object %s" format(f))
        throw e
      }
    }).get
  }

  def getAllGames(): List[Game] = {
    val dbo = gamesCollection.find().limit(DEFAULT_MAX_SIZE)
    dbo.toList.map(f => {
      try {
        grater[Game].asObject(f)
      } catch {
        case e => println("Error grating game object %s" format(f))
        throw e
      }
    })
  }

  def postGame(postGameRequest: PostGameRequest): List[Game] = {
    val nextId = getNextId("games")

    val winner = if (postGameRequest.playerOneScore > postGameRequest.playerTwoScore) {
      incrementPlayerWinsLosses(postGameRequest.playerOne, WINS)
      incrementPlayerWinsLosses(postGameRequest.playerTwo, LOSSES)
      postGameRequest.playerOne
    } else {
      incrementPlayerWinsLosses(postGameRequest.playerTwo, WINS)
      incrementPlayerWinsLosses(postGameRequest.playerOne, LOSSES)
      postGameRequest.playerTwo
    }
    gamesCollection.insert(MongoDBObject("_id" -> nextId, "playerOne" -> postGameRequest.playerOne, "playerTwo" -> postGameRequest.playerTwo,
                                         "playerOneScore" -> postGameRequest.playerOneScore, "playerTwoScore" -> postGameRequest.playerTwoScore,
                                         "gamePlayedDate" -> postGameRequest.gamePlayedDate.getOrElse(new Date), "winner" -> winner))

    getAllGames()
  }

  def editGame(_id: Int, editGameRequest: EditGameRequest): Game = {
    val dbo = gamesCollection.findOne(MongoDBObject("_id" -> _id))

    dbo.map(f => {
      try {
        val game = grater[Game].asObject(f)

        val newWinner = if (editGameRequest.playerOneScore > editGameRequest.playerTwoScore) game.playerOne else game.playerTwo
        val set = $set("playerOneScore" -> editGameRequest.playerOneScore, "playerTwoScore" -> editGameRequest.playerTwoScore, "winner" -> newWinner)

        val newGame = gamesCollection.findAndModify(query = MongoDBObject("_id" -> _id), fields = null, sort = null,
          remove = false, update = set, returnNew = true, upsert = false)

        if (newWinner != game.winner) {
          decrementPlayerWinsLosses(game.winner, WINS)
          decrementPlayerWinsLosses(newWinner, LOSSES)
          incrementPlayerWinsLosses(newWinner, WINS)
          incrementPlayerWinsLosses(game.winner, LOSSES)
        }

        grater[Game].asObject(newGame.get)
      } catch {
        case e => println("Error grating game object %s" format(f))
        throw e
      }
    }).get
  }

  def deleteGame(_id: Int): List[Game] = {
    val dbo = gamesCollection.findOne(MongoDBObject("_id" -> _id))

    dbo.map(f => {
      try {
        val game = grater[Game].asObject(f)
        if (game.playerOne == game.winner) {
          decrementPlayerWinsLosses(game.playerOne, WINS)
          decrementPlayerWinsLosses(game.playerTwo, LOSSES)
        } else {
          decrementPlayerWinsLosses(game.playerTwo, WINS)
          decrementPlayerWinsLosses(game.playerOne, LOSSES)
        }
      } catch {
        case e => println("Error grating game object %s" format(f))
        throw e
      }
    })

    gamesCollection.remove(MongoDBObject("_id" -> _id))

    getAllGames()
  }

  def postRandomGame(postRandomGameRequest: PostRandomGameRequest): List[Game] = {
    val (winner, playerOneScore, playerTwoScore) = if (randomGenerator.nextBoolean) {
      (postRandomGameRequest.playerOne, 5, randomGenerator.nextInt(5))
    } else {
      (postRandomGameRequest.playerTwo, randomGenerator.nextInt(5), 5)
    }

    val nextId = getNextId("games")
    val game = Game(nextId, new Date, postRandomGameRequest.playerOne, postRandomGameRequest.playerTwo, playerOneScore, playerTwoScore, winner)
    val gameDbo = grater[Game].asDBObject(game)

    gamesCollection += gameDbo

    if (postRandomGameRequest.playerOne == winner) {
      incrementPlayerWinsLosses(postRandomGameRequest.playerOne, WINS)
      incrementPlayerWinsLosses(postRandomGameRequest.playerTwo, LOSSES)
    } else {
      incrementPlayerWinsLosses(postRandomGameRequest.playerTwo, WINS)
      incrementPlayerWinsLosses(postRandomGameRequest.playerOne, LOSSES)
    }

    getAllGames()
  }

  /**
   * Get next seq. value for a given counter
   */
  private def getNextId(collectionName: String): Int  = {
    val inc = $inc("counter" -> 1)
    val counter = countersCollection.findAndModify(query = MongoDBObject("_id" -> collectionName), fields = null, sort = null,
      remove = false, update = inc, returnNew = true, upsert = true)
    counter.get.getAs[Int]("counter").get
  }

  private def incrementPlayerWinsLosses(playerName: String, field: String) {
    val inc = $inc(field -> 1)
    playersCollection.findAndModify(query = MongoDBObject("name" -> playerName), fields = null, sort = null,
      remove = false, update = inc, returnNew = true, upsert = true)
  }

  private def decrementPlayerWinsLosses(playerName: String, field: String) {
    val inc = $inc(field -> -1)
    playersCollection.findAndModify(query = MongoDBObject("name" -> playerName), fields = null, sort = null,
      remove = false, update = inc, returnNew = true, upsert = true)
  }
}


