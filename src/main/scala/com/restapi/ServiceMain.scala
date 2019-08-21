package com.restapi

object ServiceMain extends  App {

  import akka.actor.ActorSystem
  import akka.http.scaladsl.Http
  import akka.stream.ActorMaterializer
  import akka.util.Timeout
  import com.routes.RestApi
  import com.typesafe.config.ConfigFactory
  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContextExecutor


  //this configs are in the application.conf file
  val config=ConfigFactory.load()

  val host=config.getString("http.host")
  val port =config.getInt("http.port")

  implicit  val timeOut:Timeout = 5 seconds

  implicit val system: ActorSystem = ActorSystem()  // ActorMaterializer requires an implicit ActorSystem
  implicit val ec: ExecutionContextExecutor = system.dispatcher  // bindingFuture.map requires an implicit ExecutionContext


  implicit val materializer: ActorMaterializer = ActorMaterializer()  // bindAndHandle requires an implicit materializer

  val api=new RestApi(system,timeOut).routes


  val bindingFuture=Http().bindAndHandle(api,host,port)



try{
  bindingFuture.map{serverBinding =>
 println( s"RestApi bound to ${serverBinding.localAddress}")

  }
}catch {
  case exception: Exception=>println(s"Failed to bind to {}:{}!, $host, $port")
    //      System shutdown
    system.terminate()
}





}
