package beguine.runtime

import scala.Enumeration
import scala.util.Random

trait Arbitrary:
  def bitvec(width: Int): Int
  def bool: Boolean
  def collection[T](s: Seq[T]): Option[T]
  def enumeration(e: Enumeration): e.Value
  def numeric(lo: Option[Int], hi: Option[Int]): Int


class RandomArbitrary(using r: Random) extends Arbitrary:
  override def bitvec(width: Int): Int = r.between(0, 1 << width)

  override def bool: Boolean = r.nextBoolean

  def collection[T](s: Seq[T]) = s.size match {
    case 0 => None
    case n => Some(s(r.nextInt(n)))
  }

  // XXX: this is wrong, lol
  override def enumeration(e: Enumeration): e.Value = e.values.iterator.next()

  override def numeric(lo: Option[Int], hi: Option[Int]): Int = {
    val min = lo.getOrElse(Int.MinValue)
    val max = hi.getOrElse(Int.MaxValue)
    r.between(min, max)
  }