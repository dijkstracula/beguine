package protocolrunners

import beguine.runtime.{Arbitrary, Protocol, RandomArbitrary}
import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec

import scala.util.Random


class HelloTest extends AnyFunSpec with BeforeAndAfter {
  implicit val r: Random = new Random(42)
  implicit val a: Arbitrary = new RandomArbitrary()

  class Hello extends Protocol(a) {
    var hello_id: Int = 42

    def doit() = out(hello_id)

    def out(ivy_val: Int) = debug("out: " + ivy_val)

    exported[Unit]("doit", doit)
  }

  describe("The hello protocol") {
    it ("Should correctly dispatch to the one available exported action") {
      val h = new Hello()
      for (_ <- 1 to 10) {
        val res = h()
        assert(res.isRight)
      }

      assert(h.getHistory.size == 10)
    }
  }
}