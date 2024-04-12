package protocolrunners

import accord.local.Node
import accord.primitives.Routable.Domain
import accord.primitives.Txn
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
    val n = w.getNodes().get(0)

    val txnId = n.nextTxnId(Txn.Kind.Write, Domain.Key)
    //val txn = Txn.InMemory()
  }
}
