package beguine.runtime

import scala.Enumeration
import scala.util.Random

trait Arbitrary:
  def bitvec(width: Int): Long
  def bool: Boolean
  def collection[T](s: Seq[T]): Option[T]
  def enumeration(e: Enumeration): e.Value
  def numeric(lo: Option[Int], hi: Option[Int]): Long


class RandomArbitrary(using r: Random) extends Arbitrary:
  override def bitvec(width: Int): Long = r.between(0, 1 << width)

  override def bool: Boolean = r.nextBoolean

  def collection[T](s: Seq[T]) = s.size match {
    case 0 => None
    case n => Some(s(r.nextInt(n)))
  }

  // XXX: this is wrong, lol
  override def enumeration(e: Enumeration): e.Value = e.values.iterator.next()

  override def numeric(lo: Option[Int], hi: Option[Int]): Long = {
    val min = lo.getOrElse(Int.MinValue)
    val max = hi.getOrElse(Int.MaxValue)
    r.between(min, max)
  }