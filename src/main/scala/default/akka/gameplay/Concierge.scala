/*
 * Copyright 2018 gameplay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package default.akka.gameplay

import akka.actor.{ Actor, ActorRef, Props }

object Concierge {

  case class Login(id: String)
  case class Logout(id: String)
  case class Broadcast(content: String)

  val name = "concierge"

  def props = Props[Concierge]

}

class Concierge extends Actor {
  import Concierge._

  def receive = {
    case Login(id) =>
      val channel = context.actorOf(ClientChannel.props(id, self), id)
      sender ! channel
    case Logout(id) =>
      context.child(id).foreach(context stop)
    case Broadcast(content) =>
      val senderName = sender.path.name
      val msg        = ClientChannel.EchoMessage(senderName, content)
      context.children.filterNot(_.path.name == sender.path.name).foreach(_ ! msg)
  }

}

object ClientChannel {

  case class ClientMessage(content: String)
  case class EchoMessage(id: String, content: String)

  def props(login: String, broadcast: ActorRef) = Props(new ClientChannel(login, broadcast))

}

class ClientChannel(login: String, broadcast: ActorRef) extends Actor {
  import ClientChannel._

  def receive = {
    case ClientMessage(content)   => broadcast ! Concierge.Broadcast(content)
    case EchoMessage(id, content) => println(s"$login / $id -> $content")
  }

}
