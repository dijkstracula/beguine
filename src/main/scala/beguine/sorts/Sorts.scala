package beguine.sorts

import beguine.runtime.Arbitrary

import scala.jdk.StreamConverters.IterableHasSeqStream

// Wrappers for all Sorts defined in Porter:
// https://github.com/dijkstracula/porter/blob/main/porter/ast/sorts/__init__.py

trait IvySort[J] {
  implicit def asJ(implicit a: Arbitrary): J = arbitrary(a)

  def arbitrary(a: Arbitrary): J

  def inhabitants: Option[Iterator[J]]
  def inhabitantsAsJava = inhabitants.asJavaSeqStream
}

sealed case class Bool() extends IvySort[Boolean] {
  override def arbitrary(a: Arbitrary): Boolean = a.bool()

  override def inhabitants = Some(Iterator.from(Seq(false, true)))
}

sealed case class BitVec(width: Integer) extends IvySort[Long] {
  override def arbitrary(a: Arbitrary): Long = a.bitvec(this)

  override def inhabitants = None
}

sealed case class Enum[E <: Enumeration](discriminants: E) extends IvySort[E#Value] {
  override def arbitrary(a: Arbitrary) = a.enumeration(this)
  override def inhabitants = None // Enums are finite but I don't know how to make this typecheck yet
}

sealed case class Function(domain: List[IvySort[_]], range: IvySort[_]) extends IvySort[Nothing] {
  override def arbitrary(a: Arbitrary): Nothing = throw new Exception("Ivy is first-order, how did we get here")
  override def inhabitants = None
}

sealed case class Number(lo: Int, hi: Int) extends IvySort[Int] {
  override def arbitrary(a: Arbitrary): Int = a.numeric(this)
  override def inhabitants = Some(Range.Int.inclusive(lo, hi, 1).iterator)

  def saturate(n: Int) = Integer.min(Integer.max(n, lo), hi)
}

abstract case class Record[T <: Object]() extends IvySort[T] {
  override def inhabitants = None //throw new Exception("TODO?")
}

sealed case class Top() extends IvySort[Unit] {
  override def arbitrary(a: Arbitrary): Unit = ()
  override def inhabitants = Some(Iterator.from(Seq(())))
}

sealed case class Uninterpreted() extends IvySort[Int] {
  override def arbitrary(a: Arbitrary): Int = a.uninterpreted
  override def inhabitants = None
}

