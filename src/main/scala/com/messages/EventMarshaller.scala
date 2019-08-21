package com.messages

import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport

// message containing the initial number of tickets for the event
case class EventDescription(tickets: Int) {
  require(tickets > 0)
}

// message containing the required number of tickets
case class TicketRequest(tickets: Int) {
  require(tickets > 0)
}

// message containing an error
case class Error(message: String)



trait EventMarshaller extends  PlayJsonSupport{

  import play.api.libs.json.{Json, OFormat}
  implicit val eventDescriptionFormat: OFormat[EventDescription] = Json.format[EventDescription]
  implicit val ticketRequests: OFormat[TicketRequest] = Json.format[TicketRequest]
  implicit val errorFormat: OFormat[Error] = Json.format[Error]
  implicit val eventFormat: OFormat[Messages.Event] = Json.format[Messages.Event]
  implicit val eventsFormat: OFormat[Messages.Events] = Json.format[Messages.Events]
  implicit val ticketFormat: OFormat[TicketSeller.Ticket] = Json.format[TicketSeller.Ticket]
  implicit val ticketsFormat: OFormat[TicketSeller.Tickets] = Json.format[TicketSeller.Tickets]

}

object EventMarshaller extends EventMarshaller