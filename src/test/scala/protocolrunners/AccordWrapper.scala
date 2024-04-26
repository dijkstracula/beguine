package protocolrunners

import accord.api.Key
import accord.local.Node
import accord.local.Node.Id
import accord.primitives.Routable.Domain
import accord.primitives.{Keys, Txn}
import accord.utils.async.{AsyncChain, AsyncChains}
import accordconsensus.shims.Cluster
import beguine.runtime.RandomArbitrary
import melina.Store
import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec

import scala.util.Random

class AccordWrapper extends AnyFunSpec with BeforeAndAfter {
  implicit val r: Random = new Random(42)
  implicit val a: RandomArbitrary = new RandomArbitrary()


  describe("The Accord wrapper") {
    val w = new Cluster(a)
    w.getNode(new Id(0))

    w.write(new Id(0), 42, 99)

    // Pre-accept message deliveries
    w.getNetwork().deliver(new Node.Id(0))
    w.getNetwork().deliver(new Node.Id(1))
    w.getNetwork().deliver(new Node.Id(2))
    //AsyncChains.getUninterruptibly(w.write(new Id(0), 42, 99))
  }
}
