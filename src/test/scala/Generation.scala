import beguine.runtime.RandomArbitrary
import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec

import scala.util.Random

class GenerationTest extends AnyFunSpec with BeforeAndAfter:
  given Random(42)

  describe("Random arbitrary values"):
    val r = RandomArbitrary()
    it("Can be generated"):
      for (i <- 0 to 1000) do
        val b = r.bool
        val i = r.numeric(None, None)
        val n = r.numeric(Some(0), None)
        assert(n >= 0)
        val bv = r.bitvec(8)
        assert(bv >= 0 && bv < 256)


