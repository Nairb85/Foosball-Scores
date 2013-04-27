package foosball.boot

import com.mongodb.casbah.commons.MongoDBObject
import dispatch.{thread, Http, NoLogging}
import cc.spray.{SprayCanRootService, HttpService}
import akka.actor.{Props, ActorSystem}
import akka.actor.ActorSystem
import akka.actor.Props._
import cc.spray.io.IoWorker
import cc.spray.can.server.HttpServer
import cc.spray.io.pipelines.MessageHandlerDispatch

import foosball.dao.FoosballDAO
import com.mongodb.casbah.Imports._
import foosball.FoosballEndpoint
import net.liftweb.json.DefaultFormats

/**
 * Created with IntelliJ IDEA.
 * User: aeryn
 * Date: 4/19/13
 * Time: 12:19 AM
 * To change this template use File | Settings | File Templates.
 */

object Initializer extends App {
  val SERVICE_PORT: Int = 8000

  val system = ActorSystem.create("default")

  val db = MongoConnection("localhost",27017)("foosball")

  val playerCollectionName = "players"
  val gameCollectionName = "games"
  val counterCollectionName = "idCounter"

  ///////////// INDEXES for collections go here (include all lookup fields)
  db(playerCollectionName).ensureIndex(MongoDBObject("name" -> 1), "idx_name", true)
  db(gameCollectionName).ensureIndex(MongoDBObject("winner" -> 1))
  db(gameCollectionName).ensureIndex(MongoDBObject("playerOne" -> 1))
  db(gameCollectionName).ensureIndex(MongoDBObject("playerTwo" -> 1))

  //Ensure counter collection is initialized
  db(counterCollectionName).insert(MongoDBObject("_id" -> "games", "counter" -> 0))

  val foosballDao = new FoosballDAO(db) {
  }

  val mainModule = new FoosballEndpoint {
    implicit val liftJsonFormats = DefaultFormats
    implicit def actorSystem = system

    val foosballService = foosballDao
  }

  if (db == null){
    println("DB is null in init?????")
  }

  val httpService = system.actorOf(
    props = Props(new HttpService(mainModule.restService)),
    name = "http-service"
  )
  val rootService = system.actorOf(
    props = Props(new SprayCanRootService(httpService)),
    name = "spray-root-service"
  )

  // every spray-can HttpServer (and HttpClient) needs an IoWorker for low-level network IO
  // (but several servers and/or clients can share one)
  val ioWorker = new IoWorker(system).start()

  // create and start the spray-can HttpServer, telling it that we want requests to be
  // handled by the root service actor
  val sprayCanServer = system.actorOf(
    Props(new HttpServer(ioWorker, MessageHandlerDispatch.SingletonHandler(rootService))),
    name = "http-server"
  )

  // a running HttpServer can be bound, unbound and rebound
  // initially to need to tell it where to bind to
  sprayCanServer ! HttpServer.Bind("0.0.0.0", SERVICE_PORT)

  // finally we drop the main thread but hook the shutdown of
  // our IoWorker into the shutdown of the applications ActorSystem
  system.registerOnTermination {
    ioWorker.stop()
  }
}

