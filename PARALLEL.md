Types of parallelism:
- Task Parallelism
- Functional Parallelism
- Loop Parallelism

# 1 Task-level Parallelism

## 1.1 Task Creation and Termination (Async, Finish)

Lecture Summary: In this lecture, we learned the concepts of task creation and task termination in parallel programs, using array-sum as an illustrative example. We learned the async notation for task creation: “async ⟨stmt1⟩”, causes the parent task (i.e., the task executing the async statement) to create a new child task to execute the body of the async, ⟨stmt1⟩, asynchronously (i.e., before, after, or in parallel) with the remainder of the parent task. We also learned the finish notation for task termination: “finish ⟨stmt2⟩” causes the parent task to execute ⟨stmt2⟩, and then wait until ⟨stmt2⟩ and all async tasks created within ⟨stmt2⟩ have completed. Async and finish constructs may be arbitrarily nested.

The example studied in the lecture can be abstracted by the following pseudocode:

```
finish {
  async S1; // asynchronously compute sum of the lower half of the array
  S2;       // compute sum of the upper half of the array in parallel with S1
}
S3; // combine the two partial sums after both S1 and S2 have finished
```

While async and finish notations are useful algorithmic/pseudocode notations, we also provide you access to a high-level open-source Java-8 library called PCDP (for Parallel, Concurrent, and Distributed Programming), for which the source code is available at https://github.com/habanero-rice/pcdp. PCDP contains APIs (application programming interfaces) that directly support async and finish constructs so that you can use them in real code as well. In the next lecture, you will learn how to to implement the async and finish functionality using Java’s standard Fork/Join (FJ) framework.

Optional Reading:
1. Wikipedia article on [Asynchronous method invocation](https://en.wikipedia.org/wiki/Asynchronous_method_invocation)

In multithreaded computer programming, **asynchronous method invocation** (**AMI**), also known as asynchronous method calls or the asynchronous pattern is a design pattern in which the call site is not blocked while waiting for the called code to finish. Instead, the calling thread is notified when the reply arrives. Polling for a reply is an undesired option.
AMI is a design pattern for asynchronous invocation of potentially long-running methods of an object. It is equivalent to the IOU pattern described in 1996 by Allan Vermeulen.

In most programming languages a called method is executed synchronously, i.e. in the thread of execution from which it is invoked. If the method takes a long time to complete, e.g. because it is loading data over the internet, the calling thread is blocked until the method has finished. When this is not desired, it is possible to start a "worker thread" and invoke the method from there. In most programming environments this requires many lines of code, especially if care is taken to avoid the overhead that may be caused by creating many threads. AMI solves this problem in that it augments a potentially long-running ("synchronous") object method with an "asynchronous" variant that returns immediately, along with additional methods that make it easy to receive notification of completion, or to wait for completion at a later time.

One common use of AMI is in the active object design pattern. Alternatives are synchronous method invocation and future objects.[4] An example for an application that may make use of AMI is a web browser that needs to display a web page even before all images are loaded.

2. Wikipedia article on [Active object](https://en.wikipedia.org/wiki/Active_object)

The **active object** design pattern decouples method execution from method invocation for objects that each reside in their own thread of control.[1] The goal is to introduce concurrency, by using asynchronous method invocation and a scheduler for handling requests.

## 1.2 Creating Tasks in Java's Fork/Join Framework

Lecture Summary: In this lecture, we learned how to implement the async and finish functionality using Java’s standard Fork/Join (FJ) framework. In this framework, a task can be specified in the compute() method of a user-defined class that extends the standard RecursiveAction class in the FJ framework. In our Array Sum example, we created class ASum with fields A for the input array, LO and HI for the subrange for which the sum is to be computed, and SUM for the result for that subrange. For an instance of this user-defined class (e.g., L in the lecture), we learned that the method call, L.fork(), creates a new task that executes L’s compute() method. This implements the functionality of the async construct that we learned earlier. The call to L.join() then waits until the computation created by L.fork() has completed. Note that join() is a lower-level primitive than finish because join() waits for a specific task, whereas finish implicitly waits for all tasks created in its scope. To implement the finish construct using join() operations, you have to be sure to call join() on every task created in the finish scope.

A sketch of the Java code for the ASum class is as follows:

```
private static class ASum extends RecursiveAction {
  int[] A; // input array
  int LO, HI; // subrange
  int SUM; // return value
  . . .
  @Override
  protected void compute() {
    SUM = 0;
    for (int i = LO; i <= HI; i++) SUM += A[i];
  } // compute()
}
```

FJ tasks are executed in a ForkJoinPool, which is a pool of Java threads. This pool supports the invokeAll() method that combines both the fork and join operations by executing a set of tasks in parallel, and waiting for their completion. For example, invokeAll(left,right) implicitly performs fork() operations on left and right, followed by join() operations on both objects.

Optional Reading:
1. [Tutorial on Java’s Fork/Join framework](https://docs.oracle.com/javase/tutorial/essential/concurrency/forkjoin.html)
2. [Documentation on Java’s RecursiveAction class](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/RecursiveAction.html)

## 1.3 Computation Graphs, Work, Span, Ideal Parallelism

Lecture Summary: In this lecture, we learned about Computation Graphs (CGs), which model the execution of a parallel program as a partially ordered set. Specifically, a CG consists of:

A set of vertices or nodes, in which each node represents a step consisting of an arbitrary sequential computation.
A set of directed edges that represent ordering constraints among steps.
For fork–join programs, it is useful to partition the edges into three cases:

1. Continue edges that capture sequencing of steps within a task.

2. Fork edges that connect a fork operation to the first step of child tasks.

3. Join edges that connect the last step of a task to all join operations on that task.

CGs can be used to define data races, an important class of bugs in parallel programs. We say that a data race occurs on location L in a computation graph, G, if there exist steps S1 and S2 in G such that there is no path of directed edges from S1 to S2 or from S2 to S1 in G, and both S1 and S2 read or write L (with at least one of the accesses being a write, since two parallel reads do not pose a problem).

CGs can also be used to reason about the ideal parallelism of a parallel program as follows:

Define WORK(G) to be the sum of the execution times of all nodes in CG G,
Define SPAN(G) to be the length of a longest path in G, when adding up the execution times of all nodes in the path. The longest paths are known as critical paths, so SPAN also represents the critical path length (CPL) of G.
Given the above definitions of WORK and SPAN, we define the ideal parallelism of Computation Graph G as the ratio, WORK(G)/SPAN(G). The ideal parallelism is an upper limit on the speedup factor that can be obtained from parallel execution of nodes in computation graph G.

Optional Reading:

1. Wikipedia article on [Analysis of parallel algorithms](https://en.wikipedia.org/wiki/Analysis_of_parallel_algorithms)

## 1.4 Multiprocessor Scheduling, Parallel Speedup

Lecture Summary: In this lecture, we studied the possible executions of a Computation Graph (CG) on an idealized parallel machine with P processors. It is idealized because all processors are assumed to be identical, and the execution time of a node is assumed to be fixed, regardless of which processor it executes on. A legal schedule is one that obeys the dependence constraints in the CG, such that for every directed edge (A, B), the schedule guarantees that step B is only scheduled after step A completes. Unless other specified, we will restrict our attention in this course to schedules that have no unforced idleness, i.e., schedules in which a processor is not permitted to be idle if a CG node is available to be scheduled on it. Such schedules are also referred to as "greedy" schedules.

We defined TP as the execution time of a CG on P processors, and observed that T∞ ≤ TP ≤ T1. We also saw examples for which there could be different values of TP for different schedules.

We then defined the parallel speedup for a given schedule of a CG on P processors as Speedup(P) = T1/TP, and observed that Speedup(P) must be ≤ the number of processors P , and also ≤ the ideal parallelism, WORK/SPAN.

## 1.5 Amdahl’s Law

Lecture Summary: In this lecture, we studied a simple observation made by Gene Amdahl in 1967: if q ≤ 1 is the fraction of WORK in a parallel program that must be executed sequentially, then the best speedup that can be obtained for that program for any number of processors, P , is Speedup(P) ≤ 1/q.

This observation follows directly from a lower bound on parallel execution time that you are familiar with, namely TP ≥ SPAN(G). If fraction q of WORK(G) is sequential, it must be the case that SPAN(G) ≥ q × WORK(G). Therefore, Speedup(P) = T1/TP must be ≤ WORK(G)/(q × WORK(G)) = 1/q since T1 = WORK(G) for greedy schedulers.

Amdahl’s Law reminds us to watch out for sequential bottlenecks both when designing parallel algorithms and when implementing programs on real machines. As an example, if q = 10%, then Amdahl's Law reminds us that the best possible speedup must be ≤ 10 (which equals 1/q ), regardless of the number of processors available.

Optional Reading:

1. Wikipedia article on [Amdahl’s law](https://en.wikipedia.org/wiki/Amdahl).

# 2 Functional Parallelism

## 2.1 Future Tasks

Lecture Summary: In this lecture, we learned how to extend the concept of asynchronous tasks to future tasks and future objects (also known as promise objects). Future tasks are tasks with return values, and a future object is a “handle” for accessing a task’s return value. There are two key operations that can be performed on a future object, A:

Assignment — A can be assigned a reference to a future object returned by a task of the form, future { ⟨ task-with-return-value ⟩ } (using pseudocode notation). The content of the future object is constrained to be single assignment (similar to a final variable in Java), and cannot be modified after the future task has returned.
Blocking read — the operation, A.get(), waits until the task associated with future object A has completed, and then propagates the task’s return value as the value returned by A.get(). Any statement, S, executed after A.get() can be assured that the task associated with future object A must have completed before S starts execution.
These operations are carefully defined to avoid the possibility of a race condition on a task’s return value, which is why futures are well suited for functional parallelism. In fact, one of the earliest use of futures for parallel computing was in an extension to Lisp known as MultiLisp.

Optional Reading:
1. Wikipedia article on [Futures and promises](https://en.wikipedia.org/wiki/Futures_and_promises).

## 2.2 Creating Future Tasks in Java’s Fork/Join Framework

Lecture Summary: In this lecture, we learned how to express future tasks in Java’s Fork/Join (FJ) framework. Some key differences between future tasks and regular tasks in the FJ framework are as follows:

1. A future task extends the RecursiveTask class in the FJ framework, instead of RecursiveAction as in regular tasks.
2. The compute() method of a future task must have a non-void return type, whereas it has a void return type for regular tasks.
3. A method call like left.join() waits for the task referred to by object left in both cases, but also provides the task’s return value in the case of future tasks.

Optional Reading:
1. [Documentation on Java’s RecursiveTask class](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/RecursiveTask.html)

## 2.3 Memoization

Lecture Summary: In this lecture, we learned the basic idea of “memoization”, which is to remember results of function calls f (x) as follows:

Create a data structure that stores the set {(x1, y1 = f (x1)), (x2, y2 = f (x2)), . . .} for each call f (xi) that returns yi.
Perform look ups in that data structure when processing calls of the form f (x') when x' equals one of the xi inputs for which f (xi) has already been computed.
Memoization can be especially helpful for algorithms based on dynamic programming. In the lecture, we used Pascal’s triangle as an illustrative example to motivate memoization.

The memoization pattern lends itself easily to parallelization using futures by modifying the memoized data structure to store {(x1, y1 = future(f (x1))), (x2, y2 = future(f (x2))), . . .}. The lookup operation can then be replaced by a get() operation on the future value, if a future has already been created for the result of a given input.

Optional Reading:
1. Wikipedia article on [Memoization](https://en.wikipedia.org/wiki/Memoization).

## 2.4 Java Streams

Lecture Summary: In this lecture we learned about Java streams, and how they provide a functional approach to operating on collections of data. For example, the statement, “students.stream().forEach(s → System.out.println(s));”, is a succinct way of specifying an action to be performed on each element s in the collection, students. An aggregate data query or data transformation can be specified by building a stream pipeline consisting of a source (typically by invoking the .stream() method on a data collection , a sequence of intermediate operations such as map() and filter(), and an optional terminal operation such as forEach() or average(). As an example, the following pipeline can be used to compute the average age of all active students using Java streams:

```
students.stream()
    .filter(s -> s.getStatus() == Student.ACTIVE)
    .map(a -> a.getAge())
    .average();
```

From the viewpoint of this course, an important benefit of using Java streams when possible is that the pipeline can be made to execute in parallel by designating the source to be a parallel stream, i.e., by simply replacing students.stream() in the above code by students.parallelStream() or Stream.of(students).parallel(). This form of functional parallelism is a major convenience for the programmer, since they do not need to worry about explicitly allocating intermediate collections (e.g., a collection of all active students), or about ensuring that parallel accesses to data collections are properly synchronized.

Optional Reading:
1. Article on “[Processing Data with Java SE 8 Streams](http://www.oracle.com/technetwork/articles/java/ma14-java-se-8-streams-2177646.html)”
2. Tutorial on specifying [Aggregate Operations using Java streams](https://docs.oracle.com/javase/tutorial/collections/streams/)
3. Documentation on [java.util.stream.Collectors class](http://docs.oracle.com/javase/8/docs/api/java/util/stream/Collectors.html) for performing reductions on streams

## 2.5 Determinism and Data Races

Lecture Summary: In this lecture, we studied the relationship between determinism and data races in parallel programs. A parallel program is said to be functionally deterministic if it always computes the same answer when given the same input, and structurally deterministic if it always computes the same computation graph, when given the same input. The presence of data races often leads to functional and/or structural nondeterminism because a parallel program with data races may exhibit different behaviors for the same input, depending on the relative scheduling and timing of memory accesses involved in a data race. In general, the absence of data races is not sufficient to guarantee determinism. However, all the parallel constructs introduced in this course (“Parallelism”) were carefully selected to ensure the following Determinism Property:

If a parallel program is written using the constructs introduced in this course and is guaranteed to never exhibit a data race, then it must be both functionally and structurally deterministic.

Note that the determinism property states that all data-race-free parallel programs written using the constructs introduced in this course are guaranteed to be deterministic, but it does not imply that a program with a data race must be functionally/structurally non-deterministic. Furthermore, there may be cases of “benign” nondeterminism for programs with data races in which different executions with the same input may generate different outputs, but all the outputs may be acceptable in the context of the application, e.g., different locations for a search pattern in a target string.

Optional Reading:

1. Wikipedia article on [Race condition](https://en.wikipedia.org/wiki/Race_condition)
