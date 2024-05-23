package beguine.sorts

import scala.jdk.StreamConverters.IterableHasSeqStream

// Wrappers for all Sorts defined in Porter:
// https://github.com/dijkstracula/porter/blob/main/porter/ast/sorts/__init__.py

trait IvySort[J] {
  def inhabitants: Option[Iterator[J]]
  def inhabitantsAsJava = inhabitants.asJavaSeqStream
}

sealed case class Bool() extends IvySort[Boolean] {
  override def inhabitants = Some(Iterator.from(Seq(false, true)))
}

sealed case class BitVec(width: Integer) extends IvySort[Long] {
  override def inhabitants = None
}

sealed case class Enum[E <: Enumeration](discriminants: E) extends IvySort[Enum[E]] {
  override def inhabitants = None // Enums are finite but I don't know how to make this typecheck yet
}

sealed case class Function(domain: List[IvySort[_]], range: IvySort[_]) extends IvySort[Nothing] {
  override def inhabitants = None
}

sealed case class Number(lo: Int, hi: Int) extends IvySort[Int] {
  override def inhabitants = Some(Range.Int.inclusive(lo, hi, 1).iterator)

  def saturate(n: Int) = Integer.min(Integer.max(n, lo), hi)
}

case class Record[T <: Object]() extends IvySort[T] {
  override def inhabitants = throw new Exception("TODO?")
}

sealed case class Top() extends IvySort[Unit] {
  override def inhabitants = Some(Iterator.from(Seq(())))
}

sealed case class Uninterpreted() extends IvySort[Int] {
  override def inhabitants = None
}

