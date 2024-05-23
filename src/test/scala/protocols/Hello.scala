package protocols

import beguine.runtime.{Arbitrary, Protocol, RandomArbitrary}
import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec
import org.slf4j.Marker
import org.slf4j.helpers.BasicMarker

import scala.util.Random


class HelloTest extends AnyFunSpec with BeforeAndAfter {
  implicit val r: Random = new Random(42)
  implicit val a: Arbitrary = new RandomArbitrary()

  class Hello extends Protocol(a) {
    val nat = beguine.sorts.Number(0, Int.MaxValue)


    var greeter__hello_id: Int = 0

    exported[Unit]("ext:greeter.doit", ext__greeter__doit);


    greeter__hello_id = 42;


    def ext__greeter__doit(): Unit = {
      greeter__out(greeter__hello_id);

    }

    def greeter__out(val_ident: Int): Unit = {
      imp__greeter__out(val_ident);

    }

    def imp__greeter__out(val_ident: Int): Unit = {
      debug("greeter__out", val_ident)
    }
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