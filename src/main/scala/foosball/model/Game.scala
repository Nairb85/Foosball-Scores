package foosball.model

import org.bson.types.ObjectId
import java.util.Date

/**
 * Created with IntelliJ IDEA.
 * User: aeryn
 * Date: 4/18/13
 * Time: 11:46 PM
 * To change this template use File | Settings | File Templates.
 */
case class Game(_id: Int,
                gamePlayedDate: Date,
                playerOne: String,
                playerTwo: String,
                playerOneScore: Int,
                playerTwoScore: Int,
                winner: String)
