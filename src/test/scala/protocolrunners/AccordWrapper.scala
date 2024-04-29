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
    w.write(new Id(1), 42, 99)

    // Pre-accept send
    w.getNetwork().deliver(new Node.Id(1))
    w.getNetwork().deliver(new Node.Id(2))
    w.getNetwork().deliver(new Node.Id(3))

    // Pre-accept response
    w.getNetwork().deliver(new Node.Id(1))
    w.getNetwork().deliver(new Node.Id(1))
    w.getNetwork().deliver(new Node.Id(1))
    //AsyncChains.getUninterruptibly(w.write(new Id(0), 42, 99))
  }
}
