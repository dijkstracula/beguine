package melina

import accord.api.{Agent, DataStore, ProgressLog}
import accord.impl.InMemoryCommandStore
import accord.local.CommandStore.{EpochUpdateHolder, register}
import accord.local.{CommandStores, NodeTimeService, PreLoadContext, SafeCommandStore, ShardDistributor}
import accord.utils.RandomSource
import accord.utils.async.{AsyncChain, AsyncChains}

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{Callable, Executor, Executors, FutureTask}
import java.util.function
import java.util.function.Consumer
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.jdk.FunctionConverters._

// Not unlike InMemoryCommandStore.Synchronized but we are sure to, like
// with InMemoryCommandStore.SingleThreaded, execute within a CommandStore's registered scope.
class CommandStore(id: Int,
                   time: NodeTimeService,
                   agent: Agent,
                   store: DataStore,
                   progressLogFactory: ProgressLog.Factory,
                   epochUpdateHolder: EpochUpdateHolder)
  extends InMemoryCommandStore(id, time, agent, store, progressLogFactory, epochUpdateHolder) {

  private val t: AtomicReference[Thread] = new AtomicReference[Thread](null)
  private val exec: Executor = Executors.newSingleThreadExecutor(r => {
    val thread = new Thread(r)
    t.set(thread)
    thread.setName(String.format("%s[%d][%d]", this.getClass.getSimpleName, id, time.now()))
    thread
  })
  exec.execute(() => register(this))

  def submitAndBlock[T](ctx: PreLoadContext, f: SafeCommandStore => T): AsyncChain[T] = {

    if (inStore()) {
      val doit = new Callable[T] {
        override def call() = {
          executeInContext(CommandStore.this, ctx, f.asJava)
        }
      }
      AsyncChains.ofCallable(exec, doit)
    } else {
      val doit = new Callable[AsyncChain[T]] {
        override def call() = {
          try {
            val t = executeInContext(CommandStore.this, ctx, f.asJava)
            AsyncChains.success(t)
          } catch {
            case e: Throwable => AsyncChains.failure[T](e)
          }
        }
      }
      val ft = new FutureTask[AsyncChain[T]](doit)
      exec.execute(ft)
      ft.get()
    }
  }

  override def inStore(): Boolean = t.get() == Thread.currentThread()

  override def execute(context: PreLoadContext, consumer: Consumer[_ >: SafeCommandStore]): AsyncChain[Void] =
    submitAndBlock(context, t => { consumer.accept(t); return null; })

  override def submit[T](context: PreLoadContext, apply: function.Function[_ >: SafeCommandStore, T]): AsyncChain[T] =
    submitAndBlock(context, apply.asScala)

  override def submit[T](task: Callable[T]): AsyncChain[T] = AsyncChains.ofCallable(exec, task)

  override def shutdown(): Unit = ()

}

object CommandStore {
  class Factory(time: NodeTimeService,
                agent: Agent,
                store: DataStore,
                random: RandomSource,
                shard: ShardDistributor,
                logFactory: ProgressLog.Factory)
  extends CommandStores(
    time,
    agent,
    store,
    random,
    shard,
    logFactory,
    new CommandStore(_, _, _, _, _, _)) {}
}