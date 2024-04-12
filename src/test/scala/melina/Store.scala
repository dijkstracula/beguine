package melina

import accord.api
import accord.api.{Data, DataStore, Key}
import accord.api.DataStore.FetchResult
import accord.local.{Node, SafeCommandStore}
import accord.primitives.{Keys, Ranges, Seekable, Seekables, SyncPoint, Timestamp}
import accord.utils.Timestamped
import accord.utils.async.{AsyncChain, AsyncChains, AsyncResult, AsyncResults}
import melina.Exceptions.NonMelinaEntity

import scala.collection.mutable


object Store {
  class Data extends accord.api.Data {
    private val data: mutable.Map[Key, Timestamped[Integer]] = mutable.Map.empty

    def this(k: Key, v: Option[Timestamped[Integer]]) = {
      this()
      v match {
        case None => ()
        case Some(v) => data.addOne(k, v)
      }
    }

    override def merge(other: api.Data): api.Data = {
      other match {
         case d: Data => data.addAll(d.data)
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
          val v = ms.values.get(k)
          AsyncChains.success(new Data(k, v))
        }
        case s => throw NonMelinaEntity(s, Store.getClass)
      }
    }

    override def slice(ranges: Ranges): api.Read = new Read(keys.slice(ranges))

    override def merge(other: api.Read): api.Read = other match {
      case other: Read => new Read(keys.`with`(other.keys))
      case other => throw new NonMelinaEntity(other, this.getClass)
    }
  }
}

class Store extends DataStore {
  private val values: mutable.Map[Key, Timestamped[Integer]] = mutable.Map.empty

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
