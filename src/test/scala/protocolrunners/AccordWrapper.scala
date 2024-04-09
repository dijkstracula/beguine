package protocolrunners

import accordconsensus.shims.Cluster
import beguine.runtime.RandomArbitrary
import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec

import scala.util.Random

class AccordWrapper extends AnyFunSpec with BeforeAndAfter {
  implicit val r: Random = new Random(42)
  implicit val a: RandomArbitrary = new RandomArbitrary()

  describe("The Accord wrapper") {
    val w = new Cluster(a)
  }
}
