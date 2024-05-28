package beguine

import beguine.sorts.IvySort

import scala.collection.Iterator
import scala.collection.mutable.{HashMap, Map}


object Maps {
  abstract class AbstractMap[K, V] extends Map[K, V] {
    private var default: Option[() => V] = None

    private[beguine] val backingMap = HashMap[K, V]()
    override def apply(key: K): V = {
      backingMap.getOrElse(key, default.getOrElse(throw new Exception("Producer for defaults not set")).apply())
    }

    override def get(key: K): Option[V] = Some(this (key))

    override def addOne(elem: (K, V)): this.type = {
      backingMap.addOne(elem)
      this
    }

    override def subtractOne(elem: K): this.type = {
      backingMap.subtractOne(elem)
      this
    }

    def initWithDefault(d: () => V) = {
      default = Some(d)
      backingMap.clear()
      this
    }
  }

  class Map1[A, Z](as: IvySort[A]) extends AbstractMap[A, Z] {
    override def iterator = Maps.iterator(as)(this)
  }


  class Map2[A, B, Z](as: IvySort[A], bs: IvySort[B])
                     extends AbstractMap[(A, B), Z] {
    override def iterator = Maps.iterator(as, bs)(this)
  }

  class Map3[A, B, C, Z](as: IvySort[A], bs: IvySort[B], cs: IvySort[C])
                     extends AbstractMap[(A, B, C), Z] {
    override def iterator = Maps.iterator(as, bs, cs)(this)
  }

  private def iterator[A, Z](as: IvySort[A])
                            (m: Maps.Map1[A,Z]): Iterator[(A, Z)] = for {
    a <- as.inhabitants.getOrElse(m.backingMap.map(_._1).iterator)

  } yield {
    (a, m(a))
  }

  private def iterator[A, B, Z](as: IvySort[A], bs: IvySort[B])
                               (m: Maps.Map2[A, B, Z]): Iterator[((A, B), Z)] = for {
    a <- as.inhabitants.getOrElse(m.backingMap.map(_._1._1).iterator)
    b <- bs.inhabitants.getOrElse(m.backingMap.map(_._1._2).iterator)
  } yield {
    ((a, b), m((a, b)))
  }

  private def iterator[A, B, C, Z](as: IvySort[A], bs: IvySort[B], cs: IvySort[C])
                                  (m: Maps.Map3[A, B, C, Z]): Iterator[((A, B, C), Z)] = for {
    a <- as.inhabitants.getOrElse(m.backingMap.map(_._1._1).iterator)
    b <- bs.inhabitants.getOrElse(m.backingMap.map(_._1._2).iterator)
    c <- cs.inhabitants.getOrElse(m.backingMap.map(_._1._3).iterator)
  } yield {
    val z = m((a,b,c))
    ((a, b, c), z)
  }
}
