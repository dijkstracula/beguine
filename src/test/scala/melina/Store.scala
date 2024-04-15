package melina

import accord.api
import accord.api.{Data, DataStore, Key, Write}
import accord.api.DataStore.FetchResult
import accord.local.{Node, SafeCommandStore}
import accord.primitives.{Keys, PartialTxn, Ranges, Seekable, Seekables, SyncPoint, Timestamp, Writes}
import accord.utils.Timestamped
import accord.utils.async.{AsyncChain, AsyncChains, AsyncResult, AsyncResults}
import melina.Exceptions.NonMelinaEntity

import scala.collection.mutable


object Store {
  class Data extends accord.api.Data {
    val values: mutable.Map[Seekable, Integer] = mutable.Map.empty

    def this(k: Key, v: Option[Integer]) = {
      this()
      v match {
        case None => ()
        case Some(v) => values.addOne(k, v)
      }
    }

    override def merge(other: api.Data): api.Data = {
      other match {
         case d: Data => values.addAll(d.values)
         case d => throw NonMelinaEntity(d, this.getClass)
       }
      this
    }
  }

  class Read(keys: Keys) extends accord.api.Read {
    override def keys = keys

    override def read(key: Seekable, commandStore: SafeCommandStore, executeAt: Timestamp, store: DataStore): AsyncChain[accord.api.Data] = {
      store match {
        case ms: Store => {
          val k = key.asInstanceOf[Key]
          val v = ms.values.get(k).map(_.data)
          AsyncChains.success(new Data(k, v))
        }
        case s => throw NonMelinaEntity(s, Store.getClass)
      }
    }

    override def slice(ranges: Ranges): api.Read = new Read(keys slice ranges)

    override def merge(other: api.Read): api.Read = other match {
      case other: Read => new Read(keys `with` (other.keys))
      case other => throw new NonMelinaEntity(other, this.getClass)
    }
  }

  class Write(data: Data) extends accord.api.Write {
    override def apply(key: Seekable, safeStore: SafeCommandStore, executeAt: Timestamp, store: DataStore, txn: PartialTxn): AsyncChain[Void] = {
      store match {
        case ms: Store =>
          val ts = new Timestamped[Integer](executeAt, data.values.get(key).getOrElse(null), i => i.toString)
          ms.values.get(key) match {
            case None => ms.values.put(key, ts)
            case Some(existing) =>
              if (existing.timestamp.compareTo(ts.timestamp) < 0) {
                ms.values.put(key, ts)
              }
          }
        case other => throw NonMelinaEntity(other, this.getClass)
      }
      AsyncChains.success(null)
    }
  }

  class Update(keys: Keys) extends accord.api.Update {
    override def keys = keys

    override def apply(executeAt: Timestamp, data: api.Data): Write = {
      data match {
        case d: Data => new Write(d)
        case other => throw NonMelinaEntity(other, this.getClass)
      }
    }

    override def slice(ranges: Ranges): api.Update = new Update(keys slice ranges)

    override def merge(other: api.Update): api.Update = new Update(keys `with` other.keys().asInstanceOf[Keys])
  }

}

class Store extends DataStore {
  private val values: mutable.Map[Seekable, Timestamped[Integer]] = mutable.Map.empty

  class FixMeLaterSuccessfulFetch(ranges: Ranges) extends AsyncResults.SettableResult[Ranges] with FetchResult {
    // XXX: This is fine for the moment.  However, we are going to need
    // something like a SettableResult[V] that is driven by the model checker.
    setSuccess(ranges)

    override def abort(ranges: Ranges): Unit = ()
  }

  def fetch(node: Node, safeStore: SafeCommandStore, ranges: Ranges, syncPoint: SyncPoint[_ <: Seekables[_, _]], callback: DataStore.FetchRanges): FetchResult = {
    callback.starting(ranges).started(Timestamp.NONE)
    callback.fetched(ranges)
    new FixMeLaterSuccessfulFetch(ranges)
  }
}
