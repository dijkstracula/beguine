import beguine.runtime.{Arbitrary, RandomArbitrary}
import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec
import beguine.sorts

import scala.util.Random

class GenerationTest extends AnyFunSpec with BeforeAndAfter {
  describe("Arbitrary") {
    implicit val rand: Random = new Random(42)
    val r = new RandomArbitrary()
    it("generates simple sort values") {
      for (i <- 0 to 1000) {
        val b = r.bool()
        val i = r.numeric(sorts.Number(-100, 100))
        val n = r.numeric(sorts.Number(0, 4))
        val bv = r.bitvec(sorts.BitVec(8))

        assert(b || !b) // #wow #woah
        assert(i >= -100 && i <= 100)
        assert(n >= 0)
        assert(bv >= 0 && bv < 256)
      }
    }

    it("generates compound sorts") {
      class txn_t {
        var origin: Int = 0
        var id: Int = 0
      }
      object txn_t extends sorts.Record[txn_t] {
        override def arbitrary(a: Arbitrary): txn_t = {
          val ret = new txn_t()
          ret.id = a.uninterpreted
          ret.origin = a.numeric(sorts.Number(0, 3))
          ret
        }
      }

      val t = r.record(txn_t)
      assert(t.origin >= 0 && t.origin <= 3)
    }
  }
}