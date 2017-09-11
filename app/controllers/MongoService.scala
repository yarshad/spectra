package controllers


import akka.stream.{ActorMaterializer, Materializer}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult

import scala.concurrent.{ExecutionContext, Future}
import reactivemongo.api.{DefaultDB, MongoConnection, MongoConnectionOptions, MongoDriver}
import reactivemongo.bson.{BSON, BSONDocument, BSONDocumentReader, BSONDocumentWriter, Macros, document}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.akkastream.{State, cursorProducer}

import scala.util.{Failure, Success}

object MongoService {

  implicit val system = akka.actor.ActorSystem("reactivemongo-akkastream")
  implicit val materializer = akka.stream.ActorMaterializer.create(system)

  val driver1 = new reactivemongo.api.MongoDriver
  val connection3 = driver1.connection(List("localhost:27017"))

  val conOpts = MongoConnectionOptions(/* connection options */)
  val connection4 = driver1.connection(List("localhost"), options = conOpts)

  // My settings (see available connection options)
  val mongoUri = "mongodb://localhost:27017/example"

  import ExecutionContext.Implicits.global // use any appropriate context


  def dbFromConnection(connection: MongoConnection): Future[BSONCollection] =
    connection.database("example").
      map(_.collection("person"))


  val document1 = BSONDocument(
    "firstName" -> "Yaser",
    "lastName" -> "DD",
    "age" -> 33)

  def insertDoc1(coll: BSONCollection, doc: BSONDocument): Future[Unit] = {
    val writeRes: Future[WriteResult] = coll.insert(document1)

    writeRes.onComplete { // Dummy callbacks
      case Failure(e) => e.printStackTrace()
      case Success(writeResult) =>
        println(s"successfully inserted document with result: $writeResult")
    }

    writeRes.map(_ => {}) // in this example, do nothing with the success
  }


  def processPerson(collection: BSONCollection, query: BSONDocument)(implicit m: Materializer): Source[BSONDocument, Future[State]] = {
    val sourceOfPeople: Source[BSONDocument, Future[State]] =
      collection.find(query).cursor[BSONDocument].documentSource()

    sourceOfPeople
  }

  def processPerson1(collection: BSONCollection, query: BSONDocument)(implicit m: Materializer): Future[Seq[BSONDocument]] = {
    val sourceOfPeople: Source[BSONDocument, Future[State]] =
      collection.find(query).cursor[BSONDocument].documentSource()
      sourceOfPeople.runWith(Sink.seq[BSONDocument])
  }

  def run(): Future[Source[BSONDocument, Future[State]]]  ={
    println("Starting mongo")

    val query = BSONDocument.empty

  val t = dbFromConnection(connection3).map{ c =>{
      processPerson(c,query)
//      x.runWith(Sink.foreach(i => println(i.get("tile"))))
    }}

    t

//
//    dbFromConnection(connection3).map{ c =>{
//      update(c, 99)
//    }}
//
//    dbFromConnection(connection3).map{ c =>{
//      insertDoc1(c, document1)
//    }}

  }


  def personSource(): Future[Source[BSONDocument, Future[State]]]  ={
    val query = BSONDocument.empty
    val source = dbFromConnection(connection3).map{ collection =>{
      collection.find(query).cursor[BSONDocument].documentSource()
    }}
    source
  }

  case class Person(firstName: String, lastName: String, age: Int)

  def update(collection: BSONCollection, age: Int): Future[Option[Person]] = {
    import collection.BatchCommands.FindAndModifyCommand.FindAndModifyResult
    implicit val reader = Macros.reader[Person]

    val result: Future[FindAndModifyResult] = collection.findAndUpdate(
      BSONDocument("firstName" -> "Stephane"),
      BSONDocument("$set" -> BSONDocument("age" -> age)),
      fetchNewObject = true)

    result.map(_.result[Person])
  }

}
