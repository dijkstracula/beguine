package beguine.sorts

import scala.jdk.StreamConverters.*

// Wrappers for all Sorts defined in Porter:
// https://github.com/dijkstracula/porter/blob/main/porter/ast/sorts/__init__.py

trait IvySort[J]:
  def inhabitants: Iterator[J]
  def inhabitantsAsJava = inhabitants.asJavaSeqStream

sealed case class Bool() extends IvySort[Boolean]:
  override def inhabitants: Iterator[Boolean] = Iterator.from(Seq(false, true))

sealed case class BitVec(width: Integer) extends IvySort[Long]:
  override def inhabitants: Iterator[Long] = Range.Long.inclusive(0, 1 << width, 1).iterator

sealed case class Enum[E <: Enumeration](discriminants: E) extends IvySort[Enum[E]]:
  override def inhabitants = Enum(discriminants).inhabitants

sealed case class Function(domain: List[IvySort[_]], range: IvySort[_]) extends IvySort[Nothing]:
  override def inhabitants: Iterator[Nothing] = throw Exception("Function is an infinite sort")

sealed case class Number(lo: Integer, hi: Integer) extends IvySort[Integer]:
  override def inhabitants: Iterator[Integer] = Range.Int.inclusive(lo, hi, 1).iterator.map(_.asInstanceOf[Integer])

case class Record[T <: Object]() extends IvySort[T]:
  override def inhabitants: Iterator[T] = throw Exception("TODO?")

sealed case class Top() extends IvySort[Unit]:
  override def inhabitants: Iterator[Unit] = Iterator.from(Seq(()))

sealed case class Uninterpreted() extends IvySort[Nothing]:
  override def inhabitants: Iterator[Nothing] = throw Exception("Uninterpreted is an infinite sort")

