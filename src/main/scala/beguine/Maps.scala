package beguine

import beguine.sorts.IvySort

import scala.collection.Iterator
import scala.collection.mutable.{HashMap, Map}


object Maps {
  abstract class AbstractMap[K, V](default: => V) extends Map[K, V] {
    private val backingMap = HashMap[K, V]().withDefault(_ => default)
    override def apply(key: K): V = backingMap(key)

    override def get(key: K): Option[V] = Some(this (key))

    override def addOne(elem: (K, V)): AbstractMap.this.type =
      backingMap.addOne(elem);
      this

    override def subtractOne(elem: K): AbstractMap.this.type =
      backingMap.subtractOne(elem);
      this
  }

  class Map1[A, Z](as: IvySort[A])(default: => Z) extends AbstractMap[A, Z](default) {
    override def iterator = Maps.iterator(as)(this)
  }


  class Map2[A, B, Z](as: IvySort[A], bs: IvySort[B])
                     (default: => Z) extends AbstractMap[(A, B), Z](default) {
    override def iterator = Maps.iterator(as, bs)(this)
  }

  class Map3[A, B, C, Z](as: IvySort[A], bs: IvySort[B], cs: IvySort[C])
                     (default: => Z) extends AbstractMap[(A, B, C), Z](default) {
    override def iterator = Maps.iterator(as, bs, cs)(this)
  }

  private def iterator[A, Z](as: IvySort[A])
                            (m: Map[A,Z]): Iterator[(A, Z)] = for {
    a <- as.inhabitants
  } yield {
    (a, m(a))
  }

  private def iterator[A, B, Z](as: IvySort[A], bs: IvySort[B])
                               (m: Map[(A, B), Z]): Iterator[((A, B), Z)] = for {
    a <- as.inhabitants
    b <- bs.inhabitants
  } yield {
    ((a, b), m(a, b))
  }

  private def iterator[A, B, C, Z](as: IvySort[A], bs: IvySort[B], cs: IvySort[C])
                                  (m: Map[(A, B, C), Z]): Iterator[((A, B, C), Z)] = for {
    a <- as.inhabitants
    b <- bs.inhabitants
    c <- cs.inhabitants
  } yield {
    ((a, b, c), m(a, b, c))
  }
}
