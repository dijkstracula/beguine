package protocolrunners

import beguine.ConjectureFailure
import beguine.runtime.{Arbitrary, RandomArbitrary}
import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec
import protocols.NonNegCounter

import scala.util.Random

class Counter extends AnyFunSpec with BeforeAndAfter {
  given r: Random(42)


  describe("The non-negative counter test"):
    val p = NonNegCounter(RandomArbitrary())
    it("should eventually fail"):
      assertThrows[ConjectureFailure] {
        for (_ <- 1 to 100) do
          p() match {
            case Left(e) => throw e
            case Right(()) => ()
          }
      }

}
