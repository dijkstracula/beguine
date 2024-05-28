package protocols

import beguine.runtime.{Arbitrary, Protocol, RandomArbitrary}
import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec

import scala.util.Random


class TrivialProtocolTests extends AnyFunSpec with BeforeAndAfter {
  implicit val r: Random = new Random(42)
  implicit val a: Arbitrary = new RandomArbitrary()


  describe("an empty protocol") {
    class EmptyProtocol extends Protocol(a) {
      // Can't do much with this: no actions, so every attempt to choose
      // a random action should fail.
    }

    val proto = new EmptyProtocol()
    it("Should never be able to generate an Action to invoke") {
      assert(proto().isLeft)
    }
  }

  describe("a livelockable protocol") {
    class PorterIsolate(a: Arbitrary) extends Protocol(a) {
      val nat = beguine.sorts.Number(0, Int.MaxValue)

      exported("ext:p.doit", ext__p__doit, () => 42)

      def ext__p__doit(val_ident: Int) : Unit = {
        this.assumeThat(val_ident == 42)
      }
    }
  }
}