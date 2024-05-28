package beguine.runtime

import beguine.sorts._

import scala.Enumeration
import scala.util.Random

import scala.jdk.StreamConverters._

trait Arbitrary {
  def bitvec(b: BitVec): Int

  def bool(): Boolean

  def collection[T](s: Seq[T]): Option[T]

  def enumeration[E <: Enumeration](e: Enum[E]): e.discriminants.Value

  def numeric(n: Number): Int

  /*
  def fromIvySort[J](s: IvySort[J]): () => J =
    s match {
      case Number(lo, hi) => () => this.numeric(lo, hi)
    }
   */

  def asScala: Random // XXX: I'm not fully sure I know the semantics of this outside a RandomArbitrary.
}


class RandomArbitrary(implicit r: Random) extends Arbitrary {
  override def bitvec(b: BitVec): Int = r.between(0, 1 << b.width)

  override def bool() = r.nextBoolean

  def collection[T](s: Seq[T]) = s.size match {
    case 0 => None
    case n => Some(s(r.nextInt(n)))
  }

  // XXX: slightly dumb to fall back onto collection()
  // XXX: broken lol; how do I get this to typecheck, again?
  override def enumeration[E <: Enumeration](e: Enum[E]) = null //this.collection(e.discriminants.values.toSeq).get

  override def numeric(n: Number) = { r.between(n.lo, n.hi + 1) }

  def asScala = r
}