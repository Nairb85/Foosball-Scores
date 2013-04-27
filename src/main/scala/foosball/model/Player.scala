package foosball.model

import org.bson.types.ObjectId

/**
 * Created with IntelliJ IDEA.
 * User: aeryn
 * Date: 4/18/13
 * Time: 11:44 PM
 * To change this template use File | Settings | File Templates.
 */
case class Player(_id: ObjectId, name: String, wins: Int = 0, losses: Int = 0, winPercent: Option[Int])
