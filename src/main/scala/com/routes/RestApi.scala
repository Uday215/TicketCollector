package com.routes

import akka.actor.ActorSystem
import akka.util.Timeout

import akka.pattern.ask
import com.messages.EventMarshaller
import com.messages.EventDescription
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.messages.Messages._
import com.messages.Messages


import akka.http.scaladsl.server.Directives._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import StatusCodes._

class RestApi(system:ActorSystem,timeout: Timeout)  extends RestRoutes {

  import com.messages.TicketSeller

  implicit  val requestTimeout:Timeout=timeout

  implicit  def executionContext=system.dispatcher

  def createTicket()=system.actorOf(TicketSeller.props)

}




trait  RestRoutes extends TicketApi with EventMarshaller{




  val service="ticket"

  val version="v1"

  protected  val createEventRoute:Route ={


    pathPrefix(service/version/"events"/ Segment){ event =>

      post{
        //    POST show-tix/v1/events/event_name
        pathEndOrSingleSlash{
          entity(as[EventDescription]){
            ed =>
              onSuccess(createEvent(event, ed.tickets)) {
                case Messages.EventCreated(event) => complete(Created)
                case Messages.EventExists =>
                  import com.messages.Error
                  val err = Error(s"$event event already exists!")
                  complete(BadRequest, err)
              }

          }

        }

      }

    }

  }


  protected val  getAllEventsRoute:Route={

    pathPrefix(service/version/"events"){
      get{
        // GET show-tix/v1/events
        pathEndOrSingleSlash{
          onSuccess(getEvents()){ events=>
            complete(OK,events)

          }
        }
      }
    }
  }


  protected val  getEventRoute:Route={

    pathPrefix(service/version/"events"/Segment){ event =>
      get{
        // GET show-tix/v1/events/:event

        pathEndOrSingleSlash{
          onSuccess(getEvent(event)){
            _.fold(complete(NotFound))(e ⇒ complete(OK,e))
          }
        }

      }

    }
  }


  protected  val deleteEventRoute:Route={
    pathPrefix(service / version / "events" / Segment) { event ⇒
      delete {
        // DELETE show-tix/v1/events/:event
        pathEndOrSingleSlash {
          onSuccess(cancelEvent(event)) {
            _.fold(complete(NotFound))(e => complete(OK))
          }
        }
      }
    }

  }

  protected val purchaseEventTicketRoute:Route={

    pathPrefix(service / version / "events" / Segment / "tickets") { event ⇒
      post {
        import  com.messages.TicketRequest
        // POST show-tix/v1/events/:event/tickets
        pathEndOrSingleSlash {
          entity(as[TicketRequest]) { request ⇒
            onSuccess(requestTickets(event, request.tickets)) { tickets ⇒
              if (tickets.entries.isEmpty) complete(NotFound)
              else complete(Created, tickets)
            }
          }
        }
      }
    }
  }

  val routes: Route = createEventRoute ~ getAllEventsRoute ~ getEventRoute ~ deleteEventRoute ~ purchaseEventTicketRoute
}






trait TicketApi{

  import akka.actor.ActorRef
  import com.messages.TicketSeller

  import scala.concurrent.{ExecutionContext, Future}

  def createTicket(): ActorRef

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout


  lazy val ticketRef: ActorRef = createTicket()

  def createEvent(event: String, numberOfTickets: Int): Future[EventResponse] = {
    ticketRef.ask(CreateEvent(event, numberOfTickets))
      .mapTo[EventResponse]
  }

  def getEvents(): Future[Events] = ticketRef.ask(GetEvents).mapTo[Events]

  def getEvent(event: String): Future[Option[Event]] = ticketRef.ask(GetEvent(event)).mapTo[Option[Event]]

  def cancelEvent(event: String): Future[Option[Event]] = ticketRef.ask(CancelEvent(event)).mapTo[Option[Event]]

  def requestTickets(event: String, tickets: Int): Future[TicketSeller.Tickets] = {
    ticketRef.ask(GetTickets(event, tickets)).mapTo[TicketSeller.Tickets]
  }

}