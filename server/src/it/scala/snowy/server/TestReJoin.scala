package snowy.server

import scala.concurrent.Future
import org.scalatest._
import org.scalatest.prop._
import snowy.GameClientProtocol.{Died, State}
import snowy.GameServerProtocol.{Join, ReJoin, TestDie}
import snowy.server.SnowyServerFixture.withServer

class TestReJoin extends PropSpec with PropertyChecks {
  property("ReJoin registers user with a new sled") {
    withServer { testApi =>
      import testApi.{send, skipToMessage}

      send(Join("testUser"))
      val origSled = skipToMessage { case State(mySled, _, _, _) => mySled }
      send(TestDie)
      skipToMessage { case Died => }
      send(ReJoin())
      val newSled = skipToMessage { case State(mySled, _, _, _) => mySled }
      newSled.id !== origSled.id

      Future.successful(Unit)
    }
  }
}


