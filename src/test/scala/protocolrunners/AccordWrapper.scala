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


  def doWrite(c: Cluster, id: Node.Id, k: Int, v: Int) = {
    val res = c.write(id, 42, 99)

    // Pre-accept
    deliverScatter(c)
    deliverGather(c, id)

    // Commit
    deliverScatter(c)

    // ReadOK
    deliverOne(c, id)

    // Apply
    deliverScatter(c)
    deliverGather(c, id)

    // InformOfPersistence
    deliverScatter(c)
    deliverGather(c, id)

    res
  }

  def doRead(c: Cluster, id: Node.Id, k: Int) = {
    val res = c.read(id, k)

    // Pre-accept
    deliverScatter(c)
    deliverGather(c, id)

    // Commit
    deliverScatter(c)

    // ReadOK
    deliverOne(c, id)

    // Apply
    deliverScatter(c)
    deliverGather(c, id)

    // InformOfPersistence
    deliverScatter(c)
    deliverGather(c, id)

    res
  }

  describe("The Accord wrapper") {
    it("can issue a read") {
      val w = new Cluster(a)
      val res = AsyncChains.getUninterruptibly(doRead(w, new Id(1), 42))
      res
    }

    it("can issue a write") {
      val w = new Cluster(a)
      val res = AsyncChains.getUninterruptibly(doWrite(w, new Id(1), 42, 99))
      res
    }

    it("Can read a write") {
      val c = new Cluster(a)
      AsyncChains.getUninterruptibly(doWrite(c, new Id(1), 42, 99))
      val res = AsyncChains.getUninterruptibly(doRead(c, new Id(1), 42))
      res
    }
  }
}
