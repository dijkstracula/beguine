package melina

import accord.api
import accord.api.{Data, DataStore, Key, Result, Write}
import accord.api.DataStore.FetchResult
import accord.local.Node.Id
import accord.local.{Node, SafeCommandStore}
import accord.primitives.{Keys, PartialTxn, Ranges, Seekable, Seekables, SyncPoint, Timestamp, TxnId, Writes}
import accord.utils.Timestamped
import accord.utils.async.{AsyncChain, AsyncChains, AsyncResult, AsyncResults}
import melina.Exceptions.NonMelinaEntity

import scala.collection.mutable
import scala.jdk.CollectionConverters
import scala.jdk.CollectionConverters.SetHasAsJava

object Store {
  class Data extends accord.api.Data {
    val values: mutable.Map[Seekable, Integer] = mutable.Map.empty

    def this(k: Key, v: Integer) = {
      this()
      values.addOne(k, v)
    }

    def this(k: Key, v: Option[Integer]) = {
      this()
      v match {
        case None => ()
        case Some(null) => ()
        case Some(v) => values.addOne(k, v)
      }
    }

    def this(data: Iterable[(Seekable, Integer)]) = {
      this()
      values.addAll(data)
    }

    override def merge(other: api.Data): api.Data = {
      other match {
         case d: Data => new Data(values ++ d.values)
         case d => throw NonMelinaEntity(d, this.getClass)
       }
    }

    def slice(ranges: Ranges) = {
      def data = values.filter(kv => ranges.contains(kv._1.asKey()))
      new Data(data)
    }
  }

  class Read(val keys: Keys) extends accord.api.Read {
    override def read(key: Seekable, commandStore: SafeCommandStore, executeAt: Timestamp, store: DataStore): AsyncChain[accord.api.Data] = {
      store match {
        case ms: Store => {
          val k = key.asInstanceOf[Key]
          val v = ms.values.get(k).map(_.data)
          AsyncChains.success(new Data(k, v))
        }
        case s => throw NonMelinaEntity(s)
      }
    }

    override def slice(ranges: Ranges): api.Read = new Read(keys slice ranges)

    override def merge(other: api.Read): api.Read = other match {
      case other: Read => new Read(keys `with` other.keys)
      case other => throw NonMelinaEntity(other)
    }
  }

  class Write(val data: Data) extends accord.api.Write {
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
        case other => throw NonMelinaEntity(other)
      }
      AsyncChains.success(null)
    }
  }

  class Update(val data: Data) extends accord.api.Update {
    override def keys = {
      val jset = new java.util.TreeSet[Key]()
      jset.addAll(data.values.keySet.map(_.asKey()).asJava)
      new Keys(jset)
    }

    override def apply(executeAt: Timestamp, d: api.Data): Write = {
      new Write(d.merge(data).asInstanceOf[Data])
    }

    override def slice(ranges: Ranges): api.Update = new Update(data slice ranges)

    override def merge(other: api.Update): api.Update = {
      val u = other.asInstanceOf[Update]
      val merged = u.data.merge(data).asInstanceOf[Data]
      new Update(merged)
    }
  }

  case class Result(client: Id, results: Map[Seekable, Integer]) extends api.Result

  class Query(client: Id) extends api.Query {
    override def compute(txnId: TxnId, executeAt: Timestamp, keys: Seekables[_, _], data: api.Data, read: api.Read, update: api.Update): Result = {
      val md = data.asInstanceOf[Data]
      Result(client, md.values.toMap)
    }
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
