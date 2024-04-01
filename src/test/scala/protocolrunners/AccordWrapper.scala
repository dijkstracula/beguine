package protocolrunners

import accordconsensus.Cluster
import beguine.runtime.RandomArbitrary
import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec

import scala.util.Random

class AccordWrapper extends AnyFunSpec with BeforeAndAfter {
  given r: Random(42)
  given a: RandomArbitrary()

  describe("The Accord wrapper"):
    val w = Cluster(a)
}
