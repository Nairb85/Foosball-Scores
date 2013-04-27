package foosball.model

import java.util.Date

/**
 * Created with IntelliJ IDEA.
 * User: aeryn
 * Date: 4/19/13
 * Time: 4:06 AM
 * To change this template use File | Settings | File Templates.
 */
case class PostGameRequest(playerOne: String,
                           playerTwo: String,
                           playerOneScore: Int,
                           playerTwoScore: Int,
                           gamePlayedDate: Option[Date])
