package melina

import accord.api.{Agent, DataStore, ProgressLog}
import accord.impl.InMemoryCommandStore
import accord.local.CommandStore.EpochUpdateHolder
import accord.local.{NodeTimeService, PreLoadContext, SafeCommandStore}
import accord.utils.async.AsyncChain

import java.util.concurrent.Callable
import java.util.function
import java.util.function.Consumer

// Not unlike InMemoryCommandStore.SingleThread but everything is actually
// run synchronously on the caller's thread.
class CommandStore(id: Int,
                   time: NodeTimeService,
                   agent: Agent,
                   store: DataStore,
                   progressLogFactory: ProgressLog.Factory,
                   epochUpdateHolder: EpochUpdateHolder)
  extends InMemoryCommandStore(id, time, agent, store, progressLogFactory, epochUpdateHolder) {
  override def inStore(): Boolean = true

  override def execute(context: PreLoadContext, consumer: Consumer[_ >: SafeCommandStore]): AsyncChain[Void] = ???

  override def submit[T](context: PreLoadContext, apply: function.Function[_ >: SafeCommandStore, T]): AsyncChain[T] = ???

  override def shutdown(): Unit = ???

  override def submit[T](task: Callable[T]): AsyncChain[T] = ???
}
