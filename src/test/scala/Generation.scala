import beguine.runtime.RandomArbitrary
import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec
import beguine.sorts

import scala.util.Random

class GenerationTest extends AnyFunSpec with BeforeAndAfter {
  describe("Random arbitrary values") {
    implicit val rand: Random = new Random(42)
    val r = new RandomArbitrary()
    it("Can be generated") {
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
  }
}