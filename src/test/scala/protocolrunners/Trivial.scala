package protocolrunners

import beguine.runtime.{Arbitrary, Protocol, RandomArbitrary}
import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec

import scala.util.Random


class TrivialProtocolTests extends AnyFunSpec with BeforeAndAfter {
  implicit val r: Random = new Random(42)
  implicit val a: Arbitrary = new RandomArbitrary()

  class EmptyProtocol extends Protocol(a) {
    // Can't do much with this: no actions, so every attempt to choose
    // a random action should fail.
  }

  describe("an empty protocol") {
    val proto = new EmptyProtocol()
    it("Should never be able to generate an Action to invoke") {
      assert(proto().isLeft)
    }
  }
}