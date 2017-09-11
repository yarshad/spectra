package controllers

import java.time.LocalDateTime
import javax.inject._

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout
import play.api._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.duration._
/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  import java.time.format.DateTimeFormatter

  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  def date = LocalDateTime.now.toString

  val source = Source.tick(1.second, 1 .seconds, date)

//  val x = (1 to 10).map(i => i.toString)
//  val source = Source.cycle{
//    () => x.iterator
//  }

  def wsx = WebSocket.accept[String, String] { request =>

    println("Request rec:")
    // Log events to the console
//    val in = Sink.foreach[String](println)
    val in = Sink.ignore
    val result  = Flow.fromSinkAndSource(in, source)
    result
   }

  def ws = WebSocket.acceptOrResult[Any, String] { _ =>
        val flow = Flow.fromSinkAndSource(Sink.ignore, source)
        Future.successful(Right(flow))
  }

  def ws1 = WebSocket.accept[String, String]{ request =>

    println(request.host)
        val result = Flow[String].map { msg =>
          println(msg)
          "I received your message: " + msg
        }
     result
  }




}
