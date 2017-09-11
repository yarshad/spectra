package models

import javax.inject.Inject

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import org.reactivestreams.Publisher
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.akkastream.{AkkaStreamCursor, State, cursorProducer}
import reactivemongo.api.ReadPreference
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONObjectID}
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

case class Todo(_id: Option[BSONObjectID], title: String, completed: Option[Boolean])

object JsonFormats{
  import play.api.libs.json._

  implicit val todoFormat: OFormat[Todo] = Json.format[Todo]

}

class TodoRepository @Inject()(implicit ec: ExecutionContext, reactiveMongoApi: ReactiveMongoApi){

  implicit val system = akka.actor.ActorSystem("reactivemongo-akkastream")
  implicit val materializer = akka.stream.ActorMaterializer.create(system)

  import JsonFormats._

  def todosCollection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection("todos"))

  def getAll: Future[Seq[Todo]] = {
    val query = Json.obj()
    todosCollection.flatMap( _.find(query)
      .cursor[Todo](ReadPreference.primary)
      .collect[Seq]()
    )
  }

//  def foo (collection: BSONCollection): (Source[Todo, Future[State]]) = {
////  def foo(collection: BSONCollection): (Source[Int, Future[State]], Publisher[Int]) = {
//    val cursor: AkkaStreamCursor[Todo] =
//      collection.find(BSONDocument.empty/* findAll */).
//        sort(BSONDocument("id" -> 1)).cursor[Todo]()
//
//    val src: Source[Todo, Future[State]] = cursor.documentSource()
////    val pub: Publisher[Int] = cursor.documentPublisher()
////    src -> pub
//
//    src
//  }

//  def processPerson1(implicit m: Materializer): Future[Seq[BSONDocument]] = {
//
//    val collection = todosCollection
//    val query = BSONDocument("_id" -> id)
//    val sourceOfPeople: Source[BSONDocument, Future[State]] =
//      collection.find(query).cursor[BSONDocument].documentSource()
//
//    sourceOfPeople.runWith(Sink.seq[BSONDocument])
//  }


  def getTodo(id: BSONObjectID): Future[Option[Todo]] = {
    val query = BSONDocument("_id" -> id)
    todosCollection.flatMap(_.find(query).one[Todo])
  }

  def addTodo(todo: Todo): Future[WriteResult] = {
    todosCollection.flatMap(_.insert(todo))
  }

  def updateTodo(id: BSONObjectID, todo: Todo): Future[Option[Todo]] = {

    val selector = BSONDocument("_id" -> id)
    val updateModifier = BSONDocument(
      "$set" -> BSONDocument(
        "title" -> todo.title,
        "completed" -> todo.completed)
    )

    todosCollection.flatMap(
      _.findAndUpdate(selector, updateModifier, fetchNewObject = true).map(_.result[Todo])
    )
  }

  def deleteTodo(id: BSONObjectID): Future[Option[Todo]] = {
    val selector = BSONDocument("_id" -> id)
    todosCollection.flatMap(_.findAndRemove(selector).map(_.result[Todo]))
  }

}
