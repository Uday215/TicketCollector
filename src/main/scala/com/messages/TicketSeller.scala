package com.messages

object TicketSeller {

  import akka.actor.Props
  import akka.util.Timeout
  import com.actors.{ Admin, TicketSeller }
  def props(implicit timeout: Timeout) = Props(new Admin())

  def apply(event: String) = Props(new com.actors.TicketSeller(event))

  case class Add(tickets: Vector[Ticket]) // message to add tickets to the TicketSeller
  case class Buy(tickets: Int) // message to buy tickets from the TicketSeller
  case class Ticket(id: Int) // A ticket
  case class Tickets(
    event: String,
    entries: Vector[Ticket] = Vector.empty[Ticket]) // a list of tickets for an event
  case object GetEvent // a message containing the remaining tickets for an event
  case object Cancel // a message to cancel the event

}
