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

import akka.Done
import akka.actor.{ ActorRef, ActorSystem, Inbox }

import scala.concurrent.duration._
import scala.concurrent.{ Await, SyncVar }
import scala.util.{ Failure, Random, Success, Try }

object Main {

  def main(args: Array[String]): Unit = {

    val actorSystem = ActorSystem("gameplay")
    val concierge   = actorSystem.actorOf(Concierge.props, Concierge.name)

    val client1 = new Client("client1", concierge, Inbox.create(actorSystem))
    val client2 = new Client("client2", concierge, Inbox.create(actorSystem))
    val client3 = new Client("client3", concierge, Inbox.create(actorSystem))

    val messages = Array("hello", "blah", "do no disturb", "oaihsfaoishf", "thanks")

    val clients = client1 :: client2 :: client3 :: Nil
    clients.map(_.send(messages(Random.nextInt(messages.length)))).foreach {
      case Success(Done) => println("OK")
      case Failure(ex)   => Console.err.println(ex.getMessage)
    }

    Console.in.readLine()
    clients.foreach(_.logout())
    Console.in.readLine()
    val res = Await.result(actorSystem.terminate(), 5.seconds)
  }

  class Client(name: String, concierge: ActorRef, inbox: Inbox) {
    import scala.concurrent.duration._

    val connected: SyncVar[Boolean] = new SyncVar()

    val channel: ActorRef = {
      try {
        inbox.send(concierge, Concierge.Login(name))
        val tmpRef = inbox.receive(2.seconds).asInstanceOf[ActorRef]
        connected.put(true)
        tmpRef
      } catch {
        case e: Exception =>
          println(s"Failed to create client $name: ${e.getMessage}")
          throw new RuntimeException(e)
      }

    }

    def logout(): Unit = {
      concierge ! Concierge.Logout(name)
      connected.take()
      connected.put(false)
    }

    def send(content: String): Try[akka.Done] =
      if (connected.get(1l).exists(identity)) {
        channel ! ClientChannel.ClientMessage(content)
        Success(akka.Done)
      } else {
        Failure(new IllegalStateException("Client is not connected"))
      }

  }

}
