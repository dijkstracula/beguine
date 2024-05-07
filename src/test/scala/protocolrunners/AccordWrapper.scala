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


  def deliverScatter(w: Cluster): Unit = {
    for (i <- 1 to 3) {
      deliverOne(w, new Node.Id(i))
    }
  }

  def deliverGather(w: Cluster, id: Node.Id): Unit = {
    for (_ <- 1 to 3) {
      deliverOne(w, id)
    }
  }

  def deliverOne(w: Cluster, id: Node.Id): Unit = {
    w.getNetwork().deliver(id)
  }

  describe("The Accord wrapper") {
    val w = new Cluster(a)
    val id = new Id(1)
    val res = w.write(id, 42, 99)

    // Pre-accept
    deliverScatter(w)
    deliverGather(w, id)

    // Commit
    deliverScatter(w)

    // ReadOK
    deliverOne(w, id)

    // Apply
    deliverScatter(w)
    deliverGather(w, id)

    // InformOfPersistence
    deliverScatter(w)
    deliverGather(w, id)

    // InformOfPersistence
    AsyncChains.getUninterruptibly(res)
  }
}
