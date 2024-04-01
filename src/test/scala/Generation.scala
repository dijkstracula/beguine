import beguine.runtime.RandomArbitrary
import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec

import scala.util.Random

class GenerationTest extends AnyFunSpec with BeforeAndAfter {
  describe("Random arbitrary values") {
    implicit val rand: Random = new Random(42)
    val r = new RandomArbitrary()
    it("Can be generated") {
      for (i <- 0 to 1000) {
        val b = r.bool
        val i = r.numeric(-100, 100)
        val n = r.numeric(0, 1 << 16)
        assert(n >= 0)
        val bv = r.bitvec(8)
        assert(bv >= 0 && bv < 256)
      }
    }
  }
}