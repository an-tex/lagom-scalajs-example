package org.mliarakos.example.impl

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import org.mliarakos.example.api.{ExampleService, NonPositiveIntegerException, Pong}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

class ExampleServiceImpl extends ExampleService {

  override def greeting = ServerServiceCall { _ =>
    Future.successful(s"Welcome!")
  }

  override def hello(name: String) = ServerServiceCall { _ =>
    Future.successful(s"Hello $name!")
  }

  override def random(count: Int) = ServerServiceCall { _ =>
    if (count < 1) {
      Future.failed(NonPositiveIntegerException(count))
    } else {
      val numbers = Seq.fill(count)(Random.nextInt(10) + 1)
      Future.successful(numbers)
    }
  }

  override def ping = ServerServiceCall { request =>
    val message = s"Hello ${request.name}!"
    Future.successful(Pong(message))
  }

  override def tick(interval: Int) = ServerServiceCall { message =>
    if (interval < 1) {
      Future.failed(NonPositiveIntegerException(interval))
    } else {
      val source = Source.tick(Duration.Zero, interval.milliseconds, message).mapMaterializedValue(_ => NotUsed)
      Future.successful(source)
    }
  }

  override def echo = ServerServiceCall { source =>
    Future.successful(source)
  }

  override def binary = ServerServiceCall { _ =>
    val bytes = Array.ofDim[Byte](16)
    def nextByteString: ByteString = {
      Random.nextBytes(bytes)
      ByteString.apply(bytes)
    }

    val source = Source
      .tick(Duration.Zero, 1.second, NotUsed)
      .map(_ => nextByteString)
      .mapMaterializedValue(_ => NotUsed)
    Future.successful(source)
  }

  override def fast = ServiceCall { _ =>
    // delay:
    // 0-10: all elements lost
    // 10-15: some (from the beginning) are lost
    // 15+: all are received
    //Thread.sleep(10)
    Future.successful(
      Source(1 to 1024)
      // rather use the Thread.sleep above as this is leads to an actual delay of 100ms+ (guess due to akka scheduler precision)
      //.initialDelay(1.millis)
    )
  }
}
