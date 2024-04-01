package protocolrunners

import beguine.ConjectureFailure
import beguine.runtime.{Arbitrary, RandomArbitrary}
import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec
import protocols.NonNegCounter

import scala.util.Random

class Counter extends AnyFunSpec with BeforeAndAfter {
  implicit val r: Random = new Random(42)

  describe("The non-negative counter test") {
    val p = new NonNegCounter(new RandomArbitrary())
    it("should eventually fail") {
      assertThrows[ConjectureFailure] {
        for (_ <- 1 to 10) {
          p() match {
            case Left(e) => throw e
            case Right(()) => ()
          }
        }
      }
    }
  }
}
