package controllers


import javax.inject.Inject

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink, Source}
import models.JsonFormats._
import models.{Todo, TodoRepository}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, WebSocket}
import reactivemongo.akkastream.State
import reactivemongo.bson.{BSONDocument, BSONObjectID, Macros}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class TodoController @Inject()(cc: ControllerComponents, todoRepo: TodoRepository) extends AbstractController(cc) {

  implicit val userFormat = Macros.handler[Todo]

  def getAllTodos = Action.async {
    todoRepo.getAll.map{ todos =>
      println(todos)
      Ok(Json.toJson(todos))
    }
  }

  def helloMongox = Action{
    MongoService.run()
    Ok("done")
  }



  def ws = WebSocket.accept[String, String] { _ =>

    val src : Future[Source[BSONDocument, Future[State]]]  = MongoService.personSource()
    val converter : Flow[BSONDocument,String, NotUsed] = Flow[BSONDocument].map(b => b.get("firstName").get.toString)
    val sink = Sink.foreach[String](println(_))

    val result = src.map(s => {
      Flow.fromSinkAndSource(sink, s.via(converter))
    })

    Await.result(result, Duration.Inf)

  }

  def ws1 = WebSocket.accept[Any, String] { request =>

    println("Testing....")

    val list = todoRepo.getAll.map{ data =>{

      val items = data.map(d => d.title)
      val source = Source.fromIterator( () => items.iterator)
      source
    }}

    val p = list.map{ l => {
      val in = Sink.ignore
      val result  = Flow.fromSinkAndSource(in, l)
      result
    }}

    val result = Await.result(p, Duration.Inf)
    result
  }


  def getTodo(todoId: BSONObjectID) = Action.async{ req =>
    todoRepo.getTodo(todoId).map{ maybeTodo =>
      maybeTodo.map{ todo =>
        Ok(Json.toJson(todo))
      }.getOrElse(NotFound)
    }
  }

  def createTodo() = Action.async(parse.json){ req =>

    println(req.body)
    req.body.validate[Todo].map{ todo =>
      todoRepo.addTodo(todo).map{ _ =>
        Created
      }
    }.getOrElse(Future.successful(BadRequest("Invalid Todo format")))
  }

  def updateTodo(todoId: BSONObjectID) = Action.async(parse.json){ req =>
    req.body.validate[Todo].map{ todo =>
      todoRepo.updateTodo(todoId, todo).map {
        case Some(todo) => Ok(Json.toJson(todo))
        case None => NotFound
      }
    }.getOrElse(Future.successful(BadRequest("Invalid Json")))
  }

  def deleteTodo(todoId: BSONObjectID) = Action.async{ req =>
    todoRepo.deleteTodo(todoId).map {
      case Some(todo) => Ok(Json.toJson(todo))
      case None => NotFound
    }
  }

}