package sorts

// Wrappers for all Sorts defined in Porter:
// https://github.com/dijkstracula/porter/blob/main/porter/ast/sorts/__init__.py

sealed trait IvySort[J]
sealed case class Bool() extends IvySort[Boolean]
sealed case class BitVec(width: Int) extends IvySort[Long]
sealed case class Enum[E <: Enumeration](discriminants: E) extends IvySort[E]
sealed case class Function(domain: List[IvySort[_]], range: IvySort[_]) extends IvySort[Nothing]
sealed case class Number(lo: Option[Int], hi: Option[Int]) extends IvySort[Long]
sealed case class Uninterpreted() extends IvySort[Long]